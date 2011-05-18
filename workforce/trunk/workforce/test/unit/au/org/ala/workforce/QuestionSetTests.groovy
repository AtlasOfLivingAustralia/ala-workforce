package au.org.ala.workforce

import grails.test.*

class QuestionSetTests extends GrailsUnitTestCase {

    QuestionSet qs

    protected void setUp() {
        super.setUp()
        qs = new QuestionSet(setId:1, title:'test set', pageSequence: '[{"from":1,"to":2},{"from":3,"to":3}]')
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testPagination() {
        def pages = qs.getPaginationData()
        assert pages
        assert pages.size() == 2
        assert pages[0].from == 1
        assert pages[0].to == 2
        assert pages[1].from == 3
        assert pages[1].to == 3
    }

    void testNextPage() {
        def page1 = qs.findPageByQuestionNumber(1)
        assert page1
        assert page1.from == 1
        assert page1.to == 2
        assert page1.pageNumber == 1
        assert page1.totalPages == 2

        page1 = qs.findPageByQuestionNumber(2)
        assert page1
        assert page1.from == 1
        assert page1.to == 2
        assert page1.pageNumber == 1
        assert page1.totalPages == 2

        def page2 = qs.findPageByQuestionNumber(3)
        assert page2
        assert page2.from == 3
        assert page2.to == 3
        assert page2.pageNumber == 2
        assert page2.totalPages == 2
    }
}
