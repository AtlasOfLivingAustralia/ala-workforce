package au.org.ala.workforce

class User {

    String name
    int userid

    static mapping = {
        userid index:'userid_idx'
    }
    
    static constraints = {
        userid(unique: true)
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
            u.save(flush:true)
        }
        return u
    }
    
}
