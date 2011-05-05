package au.org.ala.workforce

import grails.converters.JSON

class QuestionController {

    def dataLoaderService, modelLoaderService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def scaffold = true;

    def loadQuestionSet = {
        def set = params.set ?: 1
        loadJSON(set)
        def list = "<ul>" + Question.list().collect {"<li>${it.level1}-${it.level2}-${it.level3} ${it.qtext}</li>"}.join("\n") + "</ul>"
        render list
    }

    def loadQuestionSetXML = {
        def set = params.set ?: 1
        loadXML(set)
        def list = "<ul>" + Question.list().collect {"<li>${it.level1}-${it.level2}-${it.level3} ${it.qtext}</li>"}.join("\n") + "</ul>"
        render "XML question qset loaded - ${list}"
    }

    def test = {
        loadTestXML()
        render "Done."
    }

    def reload = {
        dataLoaderService.clearQuestionSets()
        loadXML(1)
        loadXML(2)
        render "Done."
    }

    def singleQuestion = {
        def set = params.set ?: 1
        // during development reload question set each time - remove later
        if (!params.noreload) {
            loadXML(set)
            //loadJSON()
        }

        def level1 = params.id ? params.id : 1
        def model = modelLoaderService.loadQuestion(set, level1 as int)
        if (!model) {
            flash.message = "No such question"
        }
        [question: model]
    }

    /**
     * Display a set of questions.
     *
     * Optional params 'from' and 'to' control the range of questions as follows:
     *  - neither specified -> show all questions
     *  - from specified -> show a 'page full' of questions starting with from
     *  - from and to specified -> show from..to inclusive
     *  - to specified -> illegal - this won't happen from URL mapping as a single number is mapped to 'from'
     *
     * @param set the number of the question set
     * @param from the first question number to display
     * @param to the last question number to display
     */
    def questions = {
        //println ".params............."
        //params.each { println it}
        //println "> questions set=${params.set} from=${params.from} to=${params.to}"
        def set = params.set as int ?: 1
        // during development reload question set each time - remove later
        //if (!params.noreload) {
            //dataLoaderService.clearQuestionSet()
            //loadXML(set)
            //loadJSON()
        //}

        def questionList = []
        def from, to
        if (!params.from && !params.to) {
            // neither specified
            from = 1
            to = Question.findAllByLevel2(0).size()
            questionList = (from..to).collect { modelLoaderService.loadQuestion(set, it) }
        }
        else if (!params.to) {
            // show as many as will fit on a page
            from = params.from  // must be present
            int current = from as int
            boolean roomForMore = true
            while (roomForMore) {
                def modelInstance = modelLoaderService.loadQuestion(set, current++ as int)
                if (modelInstance) {
                    questionList << modelInstance
                    int pageHeight = 0
                    questionList.each {
                        println "${it.ident()} height is ${it.heightHint}"
                        pageHeight += it.heightHint
                    }
                    println "pageHeight is ${pageHeight}\n"
                    if (pageHeight > 10) {
                        roomForMore = false
                    }
                }
                else {
                    roomForMore = false
                }
            }
            to = current - 1
        }
        else {
            // both specified
            from = params.from as int
            to = params.to as int
            questionList = (from..to).collect { modelLoaderService.loadQuestion(set, it) }
        }

        [set: set, from: from, to: to, questions: questionList]
    }

    def next = {
        // just navigate for now and don't worry about validation or saving
        params.from = (params.to as int) + 1
        params.to = null
        forward(action: 'questions', params:params)
    }

    def loadJSON(set) {
        dataLoaderService.loadQuestionSet(servletContext.getResource("metadata/question-set-${set}.json").text, set)
    }

    def loadXML(set) {
        def filename = "metadata/question-set-${set}.xml"
        def qsetFile = servletContext.getResource(filename)
        assert qsetFile : "file not found - ${filename}"
        dataLoaderService.loadQuestionSetXML(qsetFile.text, set)
    }

    def loadTestXML() {
        dataLoaderService.loadTestXML(servletContext.getResource('metadata/test.xml').text)
    }

    def submit = {

        // load question metadata
        def questionList = ((params.from as int)..(params.to as int)).collect {
            modelLoaderService.loadQuestion(params.set as int, it)
        }

        params.each { println it }
        
        // inject answers into questions
        injectAnswers(questionList, params)

        // validate answers against each question
        Map<String, String> errors = new HashMap<String, String>()
        questionList.each {
            println "validating " + it.ident()
            errors += it.validate()
        }

        if (errors) {
            errors = errors.sort {it.key}
            render(view: "questions", model: [set: params.set as int, from: params.from as int, to: params.to as int,
                    questions: questionList, errors:errors])
        } else {
            // save the answers
            questionList.each {q1 ->
                q1.saveAllAnswers(1)
            }

            def roughRepresentation = ""
            questionList.each {q1 ->
                roughRepresentation += dumpQuestion(q1)
                q1.questions.each {q2 ->
                    roughRepresentation += dumpQuestion(q2)
                    q2.questions.each {q3 ->
                        roughRepresentation += dumpQuestion(q3)
                    }
                }
            }
            render "<html><body><pre>" + roughRepresentation + "</pre></body></html>"
        }
    }

    def injectAnswers(questions, answers) {
        questions.each {
            if (it.atype != AnswerType.none) {
                it.answerValueStr = answers."${it.ident()}"
                //println "${it.ident()} : ${it.answerValueStr}"
            }
            injectAnswers(it.questions,answers)
        }
    }

    def dumpQuestion(q) {
        def dump = q.toString()
        if (q.errorMessage || q.answerValueStr) {
            return "<b>${dump}</b>\n"
        } else {
            return "${dump}\n"
        }
    }

    /*def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [questionInstanceList: Question.list(params), questionInstanceTotal: Question.count()]
    }

    def create = {
        def questionInstance = new Question()
        questionInstance.properties = params
        return [questionInstance: questionInstance]
    }

    def save = {
        def questionInstance = new Question(params)
        if (questionInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'question.label', default: 'Question'), questionInstance.id])}"
            redirect(action: "show", id: questionInstance.id)
        }
        else {
            render(view: "create", model: [questionInstance: questionInstance])
        }
    }

    def show = {
        def questionInstance = Question.get(params.id)
        if (!questionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'question.label', default: 'Question'), params.id])}"
            redirect(action: "list")
        }
        else {
            [questionInstance: questionInstance]
        }
    }

    def edit = {
        def questionInstance = Question.get(params.id)
        if (!questionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'question.label', default: 'Question'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [questionInstance: questionInstance]
        }
    }

    def update = {
        def questionInstance = Question.get(params.id)
        if (questionInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (questionInstance.version > version) {
                    
                    questionInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'question.label', default: 'Question')] as Object[], "Another user has updated this Question while you were editing")
                    render(view: "edit", model: [questionInstance: questionInstance])
                    return
                }
            }
            questionInstance.properties = params
            if (!questionInstance.hasErrors() && questionInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'question.label', default: 'Question'), questionInstance.id])}"
                redirect(action: "show", id: questionInstance.id)
            }
            else {
                render(view: "edit", model: [questionInstance: questionInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'question.label', default: 'Question'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def questionInstance = Question.get(params.id)
        if (questionInstance) {
            try {
                questionInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'question.label', default: 'Question'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'question.label', default: 'Question'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'question.label', default: 'Question'), params.id])}"
            redirect(action: "list")
        }
    }*/
}
