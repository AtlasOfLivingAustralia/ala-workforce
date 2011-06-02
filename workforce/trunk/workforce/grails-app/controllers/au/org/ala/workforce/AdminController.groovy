package au.org.ala.workforce

class AdminController {

    def modelLoaderService

    def dashboard = {

        def setId = params.set.toInteger() ?: 1
        def qset = QuestionSet.findBySetId(setId)
        def currentYear = new GregorianCalendar().get(Calendar.YEAR).toString()
        def numberStarted = Answer.numberStartedForYear(setId, currentYear)
        def numberCompleted = Event.numberCompletedForYear(setId, currentYear)

        // render the page
        [qset: qset, started: numberStarted, completed: numberCompleted]
    }

    def index = {
        render(view:'../index', model:[admin:true])
    }

    /**
     * Returns the userid attribute of the current authenticated user.
     * @return userid
     */
    int userId() {
        return request.getUserPrincipal().attributes.userid as int
    }
}
