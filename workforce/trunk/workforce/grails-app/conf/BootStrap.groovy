import au.org.ala.workforce.QuestionModel

class BootStrap {

    def dataLoaderService, listLoaderService

    def init = { servletContext ->
        listLoaderService.load()
        dataLoaderService.clearQuestionSets()
        dataLoaderService.loadQuestionSet(QuestionModel.CURRENT_PERSONAL_SURVEY)
        dataLoaderService.loadQuestionSet(QuestionModel.CURRENT_INSTITUTIONAL_SURVEY)
    }

    def destroy = {
    }
}
