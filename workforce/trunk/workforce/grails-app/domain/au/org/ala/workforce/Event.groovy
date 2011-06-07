package au.org.ala.workforce

class Event {

    final static String STARTED = "start"
    final static String COMPLETE = "complete"

    int userid                      // the user
    int setId                       // the question set
    String name                     // the type of event
    // the timestamp reflects when the answer was stored - note that Grails handles this automagically based on the property name
    Date lastUpdated

    static constraints = {
        name(maxSize:45, inList: [STARTED, COMPLETE])
    }

    static complete(userid, set) {
        new Event(userid: userid, setId: set, name: COMPLETE).save()
    }

    static def numberCompletedForYear(int setId, String year) {
        def yearStart = year + "-01-01"
        def count = Event.executeQuery(
            "select count(distinct userid) from Event where setId = :set and last_updated >= :year and name = 'complete'",
            [set: setId, year: yearStart])
        return count[0]
    }
}
