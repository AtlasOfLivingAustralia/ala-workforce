package au.org.ala.workforce

class AdminController {

    def modelLoaderService

    def dashboard = {

        def setId = params.set.toInteger() ?: 1
        def qset = QuestionSet.findBySetId(setId)
        def userId = ((org.jasig.cas.client.authentication.AttributePrincipal) request.getUserPrincipal()).getAttributes().get("userid")

        // render the page
        render(view:'dashboard', model:[qset: qset, user: userId])
    }

    def index = {
        render(view:'../index', model:[admin:true])
    }
}
