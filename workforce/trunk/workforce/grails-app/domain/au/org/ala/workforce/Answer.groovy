package au.org.ala.workforce

class Answer {

    // all answers are text - they can be interpreted as int, range, etc by examining the question metadata
    String answerValue

    // the question identifier is a unique integer formed from the set and levels values of the question
    int questionId

    // the user id is a key to the user table
    int userId

    // the timestamp reflects when the answer was stored - note that Grails handles this automagically based on the property name
    Date lastUpdated

    // all properties are required but the answer may be blank
    static constraints = {
        answerValue(nullable: true)
    }

    /**
     *
     * Business rule: only save the answer if the answer value has changed from the most recent answer.
     *
    def beforeInsert = { event ->
        // find the most recent answer to this question by this user
        def answers = Answer.findAllByUserIdAndQuestionId(userId, questionId, [sort:'lastUpdated',order:'desc'])
        if (answers && answers[0].answerValue == answerValue) {
            println "cancelling save"
            discard()
            return false
        }
    }
     */
}
