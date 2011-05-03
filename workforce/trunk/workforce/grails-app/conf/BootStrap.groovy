class BootStrap {

    def dataLoaderService

    def init = { servletContext ->
        dataLoaderService.clearQuestionSets()
        dataLoaderService.loadQuestionSetXML(servletContext.getResource('metadata/question-set-1.xml').text, 1)
        dataLoaderService.loadQuestionSetXML(servletContext.getResource('metadata/question-set-2.xml').text, 2)
    }

    def destroy = {
    }
}
