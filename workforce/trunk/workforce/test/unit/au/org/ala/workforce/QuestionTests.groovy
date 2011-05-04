package au.org.ala.workforce

import grails.test.*

class QuestionTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testHash() {
        assert new Question(qset: 1, level1: 1, level2: 1, level3: 1).makeHash() == 16843009
        assert new Question(qset: 1, level1: 14, level2: 1, level3: 5).makeHash() == 17694981
        assert new Question(qset: 20, level1: 200, level2: 200, level3: 200).makeHash() == 348702920
    }
}
