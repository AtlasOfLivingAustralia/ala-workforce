package au.org.ala.workforce

class AdminController {

    def modelLoaderService

    def dashboard = {

        def setId = params.set.toInteger() ?: 1
        def qset = QuestionSet.findBySetId(setId)
        def currentYear = new GregorianCalendar().get(Calendar.YEAR).toString()
        def numberStarted = Answer.numberStartedForYear(setId, currentYear)
        def numberCompleted = Event.numberCompletedForYear(setId, currentYear)
        def total
        switch (setId) {
            case 2: total = Institution.countBySetId(2); break
            default: total = ((numberCompleted/100 as int) + 1) * 100
        }

        // render the page
        [qset: qset, year: currentYear, started: numberStarted, completed: numberCompleted, total: total]
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
