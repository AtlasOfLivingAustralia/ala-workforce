package au.org.ala.workforce

import grails.test.*

class AnswerIntegrationTests extends GrailsUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGetAnswers() {
        def answers = Answer.getAnswers(1, 11, 2011)
        assert answers != null
    }

}
