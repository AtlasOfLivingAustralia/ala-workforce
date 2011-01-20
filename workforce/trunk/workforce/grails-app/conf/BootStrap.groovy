class BootStrap {

    def dataLoaderService

    def init = { servletContext ->
        dataLoaderService.loadQuestionSet()
    }

    def destroy = {
    }
}
