import au.org.ala.workforce.Survey
import au.org.ala.workforce.SurveyType
import au.org.ala.workforce.Config

class BootStrap {

    def dataLoaderService, listLoaderService

    def init = { servletContext ->
        listLoaderService.load()
        dataLoaderService.clearQuestionSets()
        Config.init()
        Survey.init()
        dataLoaderService.loadQuestionSet(Survey.getCurrentQSetId(SurveyType.personal))
        dataLoaderService.loadQuestionSet(Survey.getCurrentQSetId(SurveyType.institutional))
    }

    def destroy = {
    }
}
