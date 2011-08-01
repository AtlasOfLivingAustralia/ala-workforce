package au.org.ala.workforce

class ReportController {

    def modelLoaderService

    /**
     * Display questions that have answers.
     *
     */
    def answers = {
        cache false
        
        def questionList = []

        def setId = params.set as int ?: QuestionModel.CURRENT_PERSONAL_SURVEY
        def qset = QuestionSet.findBySetId(setId)

        def year = DateUtil.getYear(params.year)
        def users = User.getUsersWithAnswers(setId, year)

        def loggedInUserId = request.userPrincipal.attributes.userid as int
        def userId
        User user
        if (request.isUserInRole('ROLE_ABRS_ADMIN') || request.isUserInRole('ROLE_ADMIN')) {
            if (params.id) {
                userId = params.id as int
                user = users.find{it.userid == userId}
            } else {
                user = users[0]
                userId = user.userid
            }
        } else {
            userId = loggedInUserId
            user = User.findByUserid(userId)
        }

        def questions = Question.findAllByLevel2AndQset(0, setId)

        def answers = Answer.getAnswers(setId, userId, year)
        questions.each {
            questionList <<  modelLoaderService.loadQuestionWithAnswer(setId, it.level1, answers)
        }

        assert questionList

        // render the page
        [qset: qset, questions: questionList, users: users, user: user]
    }

    
    /**
     * Display a single question with answers.
     *
     */
    def singleQuestionAnswer = {

        def questionId = params.qid.toInteger() ?: 1
        def setId = params.set.toInteger() ?: QuestionModel.CURRENT_PERSONAL_SURVEY
        def qset = QuestionSet.findBySetId(setId)
        def loggedInUserId = request.userPrincipal.attributes.userid as int
        def userId = params.id as int ?: loggedInUserId
        if (userId != loggedInUserId && !(request.isUserInRole('ROLE_ABRS_ADMIN') || request.isUserInRole('ROLE_ADMIN'))) {
            userId = loggedInUserId
        }
        def user = User.findByUserid(userId)
        def year = DateUtil.getYear(params.year)
        def answers = Answer.getAnswers(setId, userId, year)
        def question = modelLoaderService.loadQuestionWithAnswer(setId, questionId, answers)

        // render the page
        render(view:'answer', model:[qset: qset, question: question, user: user])
    }

}
