package au.org.ala.workforce

import org.codehaus.groovy.grails.commons.ConfigurationHolder

class QuestionController {

    def dataLoaderService, modelLoaderService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    /**
     * Default action
     */
    def index = {
        log.warn("Default action invoked - " + request.requestURL)
        redirect(uri: '/')
    }

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
        if (request.getUserPrincipal()) {
            // this is called to make sure we have a full record of the user in the user store
            User.getUser(request.getUserPrincipal())

            // this checks that the user is allowed to access this survey
            if (params.qset?.requiredRole) {
                if (!request.isUserInRole(params.qset.requiredRole) &&
                    !request.isUserInRole('ROLE_ABRS_ADMIN')) {
                    println "rejecting user ${request.getUserPrincipal().name} - does not have role ${params.qset.requiredRole}"
                    //println "redirecting to ${ConfigurationHolder.config.grails.serverURL}"
                    flash.message = "You do not have the required role to access the ${params.qset.shortName}."
                    redirect(uri: '/')
                }
            }
        }
    }

    def scaffold = true;

    /**
     * Reload a specific question set.
     */
    def loadQuestionSetXML = {
        def set = params.set ?: 1
        dataLoaderService.loadQuestionSet(set)
        def list = "<ul>" + Question.list().collect {"<li>${it.level1}-${it.level2}-${it.level3} ${it.qtext}</li>"}.join("\n") + "</ul>"
        render "XML question set loaded - ${list}"
    }

    /**
     * Reload all questions sets.
     */
    def reload = {
        dataLoaderService.clearQuestionSets()
        dataLoaderService.loadQuestionSet(1)
        dataLoaderService.loadQuestionSet(2)
        render "Done."
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
        def setId = params.qset.setId
        def year = DateUtil.getYear(params.year)
        def answers = Answer.getAnswers(setId, userId(), year)
        def questionList = (pg.from..pg.to).collect { modelLoaderService.loadQuestionWithAnswer(setId, it, answers) }

        // render the page
        render(view:'questions', model:[qset: params.qset, pagination: pg, questions: questionList])
    }

    /**
     * Leave the last page of questions.
     */
    def finish = {
        // set the destination page
        params.chainTo = [action: 'showComplete', params:[set:params.set]]

        // forward to page submission
        forward(action: 'leavePage', params:params)
    }

    /**
     * Handle move to next page.
     */
    def next = {
        // set the destination page
        params.chainTo = [action: 'page', params:[set:params.set, page:params.pageNumber.toInteger() + 1]]

        // forward to page submission
        forward(action: 'leavePage', params:params)
    }

    /**
     * Handle move to previous page.
     */
    def previous = {
        // test if Next of Finish is also present - weird behaviour in IE7 on pressing Enter to go to Next page
        if (params._action_next == 'Next') {
            params.chainTo = [action: 'page', params:[set:params.set, page:params.pageNumber.toInteger() + 1]]
        } else if (params._action_finish == 'Finish') {
            params.chainTo = [action: 'showComplete', params:[set:params.set]]
        } else {
            // set the destination page
            params.chainTo = [action: 'page', params:[set:params.set, page:params.pageNumber.toInteger() - 1]]
        }

        // forward to page submission
        forward(action: 'leavePage', params:params)
    }

    def jumpPage = {
        // set the destination page
        params.chainTo = [action: 'page', params:[set:params.set, page:params._action_jumpPage.toInteger()]]

        // forward to page submission
        forward(action: 'leavePage', params:params)
    }

    /**
     * Processes page submission when the user leaves a page.
     *
     */
    def leavePage = {
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
                q1.saveAllAnswers(userId())
            }

            //println "set# = ${params.set} qset = ${params.qset}"

            // display the next page
            chain(params.chainTo)
        }
    }

    /**
     * Validate all answers and show the completion page if all good.
     */
    def showComplete = {
        /* validate all pages and display any problems */
        QuestionSet qset = params.qset
        def year = DateUtil.getYear(params.year)
        def answers = Answer.getAnswers(qset.setId, userId(), year)

        boolean noErrors = true

        // validate each - stop at first page with errors
        for (page in qset.getPaginationData()) {

            // load the question metadata for each question on the page
            def questionList = (page.from..page.to).collect { modelLoaderService.loadQuestionWithAnswer(qset.setId, it, answers) }

            // validate answers against each question on the page
            Map<String, String> errors = new HashMap<String, String>()
            questionList.each {
                errors += it.validate()
            }

            // redirect to page if any errors
            if (errors) {
                noErrors = false
                // redisplay the same page with errors highlighted
                errors = errors.sort {it.key}
                render(view: "questions", model: [qset: qset,
                        pagination: [from: page.from, to: page.to, pageNumber: page.pageNumber, totalPages: qset.totalPages],
                        questions: questionList,
                        errors:errors])
            }
        }

        if (noErrors) {
            // create completion event
            Event.complete(userId(), params.qset.setId, request.getHeader("User-Agent"), request.getRemoteAddr())
            // show home page
            redirect(uri: '/')
        }
    }

    /**
     * Manually invoke injectGuidsAndSave for the specified question set.
     */
    def injectGuids = {
        def set = params.set
        assert set
        def filename = "metadata/question-set-${set}.xml"
        def qsetFile = servletContext.getResource(filename)

        dataLoaderService.injectGuidsAndSave(set, qsetFile)

        render "Done"
    }

    // DEVT only
    def loadTestXML() {
        dataLoaderService.loadTestXML(servletContext.getResource('metadata/test.xml').text)
    }

    /**
     * DEVT only
     */
    def submit = {
        redirect(uri: '/')
    }

    /**
     * Assembles the pagination parameters into a single pagination model.
     * @param params
     * @return map of pagination parameters
     */
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

        // if navigating backwards and current page has no answers then don't validate
        if (params._action_previous == 'Prev' || (params._action_jumpPage && params._action_jumpPage < params.pageNumber)) {
            if (!areAnswers(questionList)) {
                return [questionList:questionList, errors:[:]]
            }
        }

        // validate answers against each question
        Map<String, String> errors = new HashMap<String, String>()
        questionList.each {
            errors += it.validate(params)
        }

        // return the populated question list and any errors
        return [questionList:questionList, errors:errors]
    }

    /**
     * Check for any answers in the list of questions
     */
    def areAnswers(questions) {
        def result
        for (q in questions) {
            if (q.answerValueStr != null && q.answerValueStr != "") {
                result = true
            } else if (q.questions) {
                result = areAnswers(q.questions)
            } else {
                result = false
            }
            if (result) {
                break
            }
        }
        return result
    }

    /**
     * Injects submitted answers into the appropriate question model.
     *
     * @param questions
     * @param answers
     * @return
     */
    def injectAnswers(questions, answers) {
        questions.each {
            if (it.atype != AnswerType.none) {
                it.answerValueStr = answers."${it.ident()}"
            }
            injectAnswers(it.questions,answers)
        }
    }

    // DEVT only
    def dumpQuestion(q) {
        def dump = q.toString()
        if (q.errorMessage || q.answerValueStr) {
            return "<b>${dump}</b>\n"
        } else {
            return "${dump}\n"
        }
    }

    /**
     * Cancels the questionaire. NOT CURRENTLY USED.
     */
    def cancel = {
        redirect(url: "/workforce")
    }

    /**
     * Returns the userid attribute of the current authenticated user.
     * @return userid
     */
    int userId() {
        return request.getUserPrincipal().attributes.userid as int
    }
}
