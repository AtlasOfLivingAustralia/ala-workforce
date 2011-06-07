package au.org.ala.workforce

class Answer {

    // all answers are text - they can be interpreted as int, range, etc by examining the question metadata
    String answerValue

    // the question identifier is the question's assigned guid
    String guid

    // the question set
    int setId
    
    // the user id is a key to the user table
    int userId

    // the timestamp reflects when the answer was stored - note that Grails handles this automagically based on the property name
    Date lastUpdated

    // all properties are required but the answer may be blank
    static constraints = {
        guid(maxSize:36)
        answerValue(nullable: true)
    }

    static transients = ['started']

    static numberStartedForYear(int setId, String year) {
        def yearStart = year + "-01-01"
        def count = Event.executeQuery("select count(distinct userId) from Answer where setId = :set and last_updated >= :year",
            [set: setId, year: yearStart])
        return count[0]
    }

    static boolean isStarted(int setId, String year, int userid) {
        def yearStart = year + "-01-01"
        def count = Event.executeQuery("select count(*) from Answer where setId = :set and last_updated >= :year and userid = :user",
            [set: setId, year: yearStart, user: userid])
        return count > 0
    }

    static Map getAnswers(int setId, int userId, int year) {
         def from = new GregorianCalendar(year, Calendar.JANUARY, 1)
         def to = new GregorianCalendar(year+1, Calendar.JANUARY, 1)
         def result = Answer.executeQuery(
                 "select guid, answerValue from Answer " +
                 "where setId = :set " +
                 "and userId = :user " +
                 "and lastUpdated >= :from and lastUpdated < :to " +
                 "order by lastUpdated", [set: setId, user: userId, from: from.time, to: to.time])
        def answers = [:]

        // populate map with most recent answers
        result.each { it ->
            answers[it[0]] = it[1]
        }

        return answers
    }
}
