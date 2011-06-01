class BootStrap {

    def dataLoaderService, listLoaderService

    def init = { servletContext ->
        listLoaderService.load()
        dataLoaderService.clearQuestionSets()
        dataLoaderService.loadQuestionSet(1)
        dataLoaderService.loadQuestionSet(2)
    }

    def destroy = {
    }
}
