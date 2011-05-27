package au.org.ala.workforce

class Event {

    final static String STARTED = "start"
    final static String COMPLETE = "complete"

    int userid                      // the user
    int setId                       // the question set
    String name                     // the type of event

    static constraints = {
        name(maxSize:45, inList: [STARTED, COMPLETE])
    }

}
