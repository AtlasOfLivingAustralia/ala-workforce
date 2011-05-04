package au.org.ala.workforce

class Answer {

    // all answers are text - they can be interpreted as int, range, etc by examining the question metadata
    String answerValue

    // the question identifier is a unique integer formed from the set and levels values of the question
    int questionId

    // the user id is a key to the user table
    int userId

    // the timestamp reflects when the answer was stored - not that Grails handles this automagically based on the property name
    Date lastUpdated

    // all properties are required
    static constraints = {
    }

    
}
