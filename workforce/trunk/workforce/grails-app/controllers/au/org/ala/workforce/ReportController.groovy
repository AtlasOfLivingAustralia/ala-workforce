package au.org.ala.workforce

class ReportController {

    def modelLoaderService

    /**
     * Display questions that have answers.
     *
     */
    def answers = {

        def questionList = []
        def setId = params.set.toInteger() ?: 1
        def qset = QuestionSet.findBySetId(setId)
        def userid = params.id as int ?: 1
        def user = User.findByUserid(userid)

        def questions = Question.findAllByLevel2AndQset(0, setId)

        questions.each {
            questionList <<  modelLoaderService.loadQuestionWithAnswer(setId, it.level1, userid)
        }

        assert questionList

        // render the page
        [qset: qset, questions: questionList, user: user]
    }

    /**
     * Display a single question with answers.
     *
     */
    def singleQuestionAnswer = {

        def questionId = params.qid.toInteger() ?: 1;
        def setId = params.set.toInteger() ?: 1
        def qset = QuestionSet.findBySetId(setId)
        def userid = params.id as int ?: 1
        def user = User.findByUserid(userid)

        def question = modelLoaderService.loadQuestionWithAnswer(setId, questionId, userid)

        // render the page
        render(view:'answer', model:[qset: qset, question: question, user: user])
    }

}
