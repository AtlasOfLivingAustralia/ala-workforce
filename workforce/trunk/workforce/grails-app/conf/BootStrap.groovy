class BootStrap {

    def dataLoaderService

    def init = { servletContext ->
        dataLoaderService.clearQuestionSets()
        dataLoaderService.loadQuestionSetXML(servletContext.getResource('metadata/question-set-1.xml').text)
        dataLoaderService.loadQuestionSetXML(servletContext.getResource('metadata/question-set-2.xml').text)
    }

    def destroy = {
    }
}
