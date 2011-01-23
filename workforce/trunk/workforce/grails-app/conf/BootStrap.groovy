class BootStrap {

    def dataLoaderService

    def init = { servletContext ->
        dataLoaderService.loadQuestionSet(servletContext.getResource('metadata/question-set.json').text)
    }

    def destroy = {
    }
}
