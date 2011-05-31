class BootStrap {

    def dataLoaderService

    def init = { servletContext ->
        dataLoaderService.clearQuestionSets()
        dataLoaderService.loadQuestionSet(1)
        dataLoaderService.loadQuestionSet(2)
    }

    def destroy = {
    }
}
