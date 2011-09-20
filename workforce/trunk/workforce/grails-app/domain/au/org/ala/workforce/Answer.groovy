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
        answerValue(nullable:true, maxSize:10000)
    }

    static transients = ['started']

    static numberStartedForYear(int setId, String year) {
        def yearStart = year + "-01-01"
        def count = Answer.executeQuery("select count(distinct userId) from Answer where setId = :set and last_updated >= :year",
            [set: setId, year: yearStart])
        return count[0]
    }

    static boolean isStarted(int setId, String year, int userid) {
        def yearStart = year + "-01-01"
        def count = Answer.executeQuery("select count(*) from Answer where setId = :set and last_updated >= :year and userid = :user",
            [set: setId, year: yearStart, user: userid])
        return count > 0
    }

    static String lastUpdate(int setId, int userId, int year) {
        def yearStart = year + "-01-01"
        def result = Answer.executeQuery(
                 "select lastUpdated from Answer " +
                 "where setId = :set " +
                 "and userId = :user " +
                 "and last_updated >= :year " +
                 "order by last_updated desc", [set: setId, user: userId, year: yearStart])
        if (result.empty) {
            return null
        } else {
            return result[0]
        }
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

        // populate map with user's most recent answers
        result.each { it ->
            answers[it[0]] = it[1]
        }

        return answers
    }

    static Map getAllAnswers(List guids, int year, boolean getQText) {
        def from = new GregorianCalendar(year, Calendar.JANUARY, 1)
        def to = new GregorianCalendar(year+1, Calendar.JANUARY, 1)
        def query = getQText ?
            "select a.userId, a.guid, a.answerValue, q.qtext from Answer a, Question q " +
            "where a.guid in(:guids) " +
            "and a.guid = q.guid " +
            "and a.lastUpdated >= :from and a.lastUpdated < :to " +
            "order by a.userId, a.lastUpdated" :

            "select userId, guid, answerValue from Answer " +
            "where guid in(:guids) " +
            "and lastUpdated >= :from and lastUpdated < :to " +
            "order by userId, lastUpdated"

        def result = Answer.executeQuery(query, [guids: guids, from: from.time, to: to.time])

        // get most recent answer for each question for each user
        def answers = [:]
        def currentUser = result[0][0]
        answers[(currentUser)] = [:]
        result.each { it ->
            if (it[0] != currentUser) {
                currentUser = it[0]
                answers[(currentUser)] = [:]
            }
            answers[(currentUser)][it[1]] = getQText ? ['answer' : it[2], 'qtext' : it[3]] : ['answer' : it[2]]
        }

        return answers
    }

    static Map getAnswerCounts(List guids, int year, boolean getQText) {
        // get most recent answer for each question for each user
        def answers = getAllAnswers(guids, year, getQText)
        // count each answer instance
        def counts = [:]
        answers.each { it ->
            println('it.key=' + it.key)
            it.value.each { it2 ->
                println('it2.key=' + it2.key)
                if (it2.value['answer']) {
                    def answer = getQText ? it2.value['qtext'] : it2.value['answer']
                    if (counts[answer]) {
                        counts[answer]++
                    } else {
                        counts[answer] = 1
                    }
                }
             }
        }
        return counts
    }

    static List getAnswerTotalsByUser(List guids, int year, boolean getQText) {
        // get most recent answer for each question for each user
        def answers = getAllAnswers(guids, year, getQText)

        // total up values for each user
        def totals = []
        answers.each { it ->
            def total = 0
            it.value.each { it2 ->
                if (it2.value['answer']) {
                    def answer = getQText ? it2.value['qtext'] : it2.value['answer']
                    total += it2.value['answer'] as int
                }
            }
            totals << total
        }
        return totals
    }

    static Map getAnswerTotalsByGuid(List guids, int year, boolean getQText) {
        // get most recent answer for each question for each user
        def answers = getAllAnswers(guids, year, getQText)

        // total up values grouped by guid
        def totals = [:]
        answers.each { it ->
            it.value.each { guid, value ->
                if (value['answer']) {
                    def answer = getQText ? value['qtext'] : value['answer']
                    if (totals[guid]) {
                        totals[guid] += answer as int
                    } else {
                        totals[guid] = answer as int
                    }
                }
            }
        }
        return totals
    }

}
