import au.org.ala.workforce.Survey
import au.org.ala.workforce.SurveyType
import au.org.ala.workforce.ConfigData

class BootStrap {

    def dataLoaderService, listLoaderService

    def init = { servletContext ->
        listLoaderService.load()
        ConfigData.init()
        Survey.init()
        dataLoaderService.clearQuestionSets()
        dataLoaderService.loadQuestionSet(Survey.getCurrentQSetId(SurveyType.personal))
        dataLoaderService.loadQuestionSet(Survey.getCurrentQSetId(SurveyType.institutional))
    }

    def destroy = {
    }
}
