package au.org.ala.workforce

class User {

    String name

    static constraints = {}

    static transients = ['user']

    /**
     * Gets the user that matches the specified name - creating the user entry if it doesn't exist.
     *
     * @param name
     * @return the user
     */
    static User getUser(String name) {
        User u = User.findByName(name)
        if (!u) {
            u = new User(name: name)
            u.save(flush:true)
        }
        return u
    }
    
}
