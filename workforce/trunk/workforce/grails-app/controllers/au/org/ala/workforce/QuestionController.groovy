package au.org.ala.workforce

class QuestionController {

    def dataLoaderService, modelLoaderService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    /**
     * Preloads objects for page requests.
     *
     * @param set Question set metadata is loaded if set is specified
     * @param question page number will be derived
     */
    def beforeInterceptor = {
        if (params.set?.isInteger()) {
            params.qset = QuestionSet.findBySetId(params.set.toInteger())

            if (params.question?.isInteger() && !params.page) {
                params.page = params.qset.findPageByQuestionNumber(params.question.toInteger()).pageNumber
            }
        }
    }

    def scaffold = true;

    /**
     * Load a specific question set. DEVT only.
     */
    def loadQuestionSetXML = {
        def set = params.set ?: 1
        loadXML(set)
        def list = "<ul>" + Question.list().collect {"<li>${it.level1}-${it.level2}-${it.level3} ${it.qtext}</li>"}.join("\n") + "</ul>"
        render "XML question set loaded - ${list}"
    }

    /**
     * Reload all questions sets. DEVT only.
     */
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
     * Display the specified page of questions.
     *
     * @param set the number of the question set
     * @param qset the question set object (injected by the beforeInterceptor
     * @param page the page to display
     */
    def page = {

        // grab the required page number
        def pageNumber = params.page.isInteger() ? params.page.toInteger() : 1
        assert params.qset
        assert pageNumber >= 1 && pageNumber <= params.qset.totalPages

        // get the page definition
        def pg = params.qset.getPage(pageNumber)
        assert pg

        // load the question metadata for each question on the page
        def questionList = (pg.from..pg.to).collect { modelLoaderService.loadQuestionWithAnswer(params.qset.setId, it, 1) }

        // render the page
        render(view:'questions', model:[qset: params.qset, pagination: pg, questions: questionList])
    }

    /**
     * Handle move to next page.
     */
    def next = {
        // set the direction to move
        params.where = 'next'

        // forward to page submission
        forward(action:'changePage', params:params)
    }

    /**
     * Handle move to previous page.
     */
    def previous = {
        // set the direction to move
        params.where = 'prev'

        // forward to page submission
        forward(action:'changePage', params:params)
    }

    /**
     * Processes page submission when the user leaves a page.
     *
     */
    def changePage = {
        // validate the answers
        def result = validate(params.from as int, params.to as int, params)

        // check for validation errors
        def errors = result.errors
        if (errors) {

            // redisplay the same page with errors highlighted
            errors = errors.sort {it.key}
            render(view: "questions", model: [qset: QuestionSet.findBySetId(params.set as int),
                    pagination: buildPagination(params),
                    questions: result.questionList, errors:errors])
        } else {

            // save the answers
            result.questionList.each {q1 ->
                q1.saveAllAnswers(1)
            }

            // calculate the next page to show
            int pageNumber = params.pageNumber.toInteger() + (params.where == 'next' ? 1 : -1)

            // display the next page
            chain(action: 'page', params:[set:params.set, page:pageNumber])
        }
    }

    def loadJSON(set) {
        dataLoaderService.loadQuestionSet(servletContext.getResource("metadata/question-set-${set}.json").text, set)
    }

    def loadXML(set) {
        def filename = "metadata/question-set-${set}.xml"
        def qsetFile = servletContext.getResource(filename)
        assert qsetFile : "file not found - ${filename}"
        dataLoaderService.loadQuestionSetXML(qsetFile.text)
    }

    def loadTestXML() {
        dataLoaderService.loadTestXML(servletContext.getResource('metadata/test.xml').text)
    }

    /**
     * 
     */
    def submit = {
        def result = validate(params.from as int, params.to as int, params)
        def questionList = result.questionList
        def errors = result.errors
        if (errors) {
            errors = errors.sort {it.key}
            //println "redirect to errors"
            render(view: "questions", model: [qset: QuestionSet.findBySetId(params.set as int),
                     pagination: buildPagination(params),
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

    /**
     * Mark the survey as complete.
     *
     * 1. validate entire question set and feedback errors
     * 2. mark latest answers as complete
     */
    def complete = {
        // re-create all questions and the most recent answers from the database
        
    }

    Map buildPagination(params) {
        return [from:params.from as int, to:params.to as int,
                pageNumber:params.pageNumber as int, totalPages:params.totalPages as int]
    }

    /**
     * Validates the answers in params for the range specified against the question metadata.
     *
     * @param from the first question to validate
     * @param to the last question to validate
     * @param params the answers
     * @return populated questions and any errors
     */
    def validate(int from, int to, params) {

        // load question metadata
        def questionList = (from..to).collect {
            modelLoaderService.loadQuestion(params.set as int, it)
        }

        // inject answers into questions
        injectAnswers(questionList, params)

        // validate answers against each question
        Map<String, String> errors = new HashMap<String, String>()
        questionList.each {
            //println "validating " + it.ident()
            errors += it.validate()
        }

        // return the populated question list and any errors
        return [questionList:questionList, errors:errors]
    }

    def injectAnswers(questions, answers) {
        questions.each {
            if (it.atype != AnswerType.none) {
                it.answerValueStr = answers."${it.ident()}"
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

    def cancel = {
        redirect(url: "/workforce")
    }

}
