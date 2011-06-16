package au.org.ala.workforce

import grails.test.*

class QuestionTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testIdent() {
        Question q = new Question(qset: 1, level1: 6, level2: 0, level3: 0)
        assert q.buildIdent() == "6"

        q = new Question(qset: 1, level1: 6, level2: 1, level3: 0)
        assert q.buildIdent() == "6_1"

        q = new Question(qset: 1, level1: 6, level2: 1, level3: 4)
        assert q.buildIdent() == "6_1_4"

    }
}
