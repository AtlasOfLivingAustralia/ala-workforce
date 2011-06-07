package au.org.ala.workforce

class Institution {

    String name         // institution name
    String code         // institution code
    String account      // user account that is allowed to submit the survey
    int setId           // question set the institution belongs to
    String uid          // identifier of the institution/collection in the ALA collectory

    static constraints = {
    }

    static listInstitutionsForSet(int set) {
        return Institution.findAllBySetId(set, [sort:'name'])
    }
}
