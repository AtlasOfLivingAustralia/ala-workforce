package au.org.ala.workforce

class Answer {

    // all answers are text - they can be interpreted as int, range, etc by examining the question metadata
    String answerValue

    // the question identifier is the question's assigned guid
    String guid

    // the user id is a key to the user table
    int userId

    // the timestamp reflects when the answer was stored - note that Grails handles this automagically based on the property name
    Date lastUpdated

    // all properties are required but the answer may be blank
    static constraints = {
        guid(maxSize:36)
        answerValue(nullable: true)
    }

}
