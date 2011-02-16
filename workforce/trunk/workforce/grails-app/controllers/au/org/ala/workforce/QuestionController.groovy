package au.org.ala.workforce

class QuestionController {

    def dataLoaderService, modelLoaderService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def scaffold = true;

    def loadQuestionSet = {
        loadJSON()
        def list = "<ul>" + Question.list().collect {"<li>${it.level1}-${it.level2}-${it.level3} ${it.qtext}</li>"}.join("\n") + "</ul>"
        render list
    }

    def loadQuestionSetXML = {
        loadXML()
        def list = "<ul>" + Question.list().collect {"<li>${it.level1}-${it.level2}-${it.level3} ${it.qtext}</li>"}.join("\n") + "</ul>"
        render "XML question set loaded - ${list}"
    }

    def test = {
        loadTestXML()
        render "Done."
    }

    def singleQuestion = {
        // during development reload question set each time - remove later
        loadXML()
        //loadJSON()

        def level1 = params.id ? params.id : 1
        def model = modelLoaderService.loadQuestion(level1 as int)
        if (!model) {
            flash.message = "No such question"
        }
        [question: model]
    }

    def allQuestions = {
        // during development reload question set each time - remove later
        loadXML()
        //loadJSON()

        def tops = Question.findAllByLevel2(0)
        [questions: tops.collect {modelLoaderService.loadQuestion(it.level1)}]
    }

    def loadJSON() {
        dataLoaderService.loadQuestionSet(servletContext.getResource('metadata/question-set.json').text)
    }

    def loadXML() {
        dataLoaderService.loadQuestionSetXML(servletContext.getResource('metadata/question-set.xml').text)
    }

    def loadTestXML() {
        dataLoaderService.loadTestXML(servletContext.getResource('metadata/test.xml').text)
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
