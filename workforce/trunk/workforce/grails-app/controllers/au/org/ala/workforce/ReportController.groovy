package au.org.ala.workforce

class ReportController {

    def modelLoaderService
    def scaffold = true

    /**
     * Display questions that have answers.
     *
     */
    def answers = {

        def questionList = []
        def questionId = 1;
        def setId = params.set.toInteger() ?: 1
        def qset = QuestionSet.findBySetId(setId)

        def questions = Question.findAllByLevel2AndQset(0, setId)

        questions.each {
            questionList <<  modelLoaderService.loadQuestionWithAnswer(setId, it.level1, 1)
        }

        assert questionList

        // render the page
        render(view:'answers', model:[qset: qset, questions: questionList])
    }

    /**
     * Display a single question with answers.
     *
     */
    def singleQuestionAnswer = {

        def questionId = params.id.toInteger() ?: 1;
        def setId = params.set.toInteger() ?: 1
        def qset = QuestionSet.findBySetId(setId)

        def question = modelLoaderService.loadQuestionWithAnswer(setId, questionId, 1)

        // render the page
        render(view:'answer', model:[qset: qset, question: question])
    }

}
