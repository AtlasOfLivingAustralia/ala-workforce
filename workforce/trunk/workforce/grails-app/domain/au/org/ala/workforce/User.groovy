package au.org.ala.workforce

class User {

    String name
    int userid
    String firstName
    String lastName

    static mapping = {
        userid index:'userid_idx'
    }
    
    static constraints = {
        userid(unique: true)
        firstName(nullable:true)
        lastName(nullable:true)
    }

    static transients = ['user']

    /**
     * Gets the user that matches the specified name - creating the user entry if it doesn't exist.
     *
     * @param name
     * @return the user
     */
    static User getUser(userPrincipal) {
        User u = User.findByUserid(userPrincipal.attributes.userid as int)
        if (!u) {
            u = new User(userid: userPrincipal.attributes.userid as int, name: userPrincipal.name)
            u.firstName = userPrincipal.attributes.firstname ?: null
            u.lastName = userPrincipal.attributes.lastname ?: null
            u.save(flush:true)
        }
        return u
    }

    static List getUsersWithAnswers(int setId, int year) {
        def from = new GregorianCalendar(year, Calendar.JANUARY, 1)
        def to = new GregorianCalendar(year+1, Calendar.JANUARY, 1)
        def names = User.executeQuery(
                 "select distinct u.name from User as u, Answer as a " +
                 "where a.setId = :set " +
                 "and a.lastUpdated >= :from and a.lastUpdated < :to " +
                 "and u.userid = a.userId " +
                 "order by name", [set: setId, from: from.time, to: to.time])
        def users = []
        names.each {
            users << User.findByName(it)
        }
        return users
    }
}
