package au.org.ala.workforce

import grails.test.*

class ListLoaderServiceTests extends GrailsUnitTestCase {
    ListLoaderService service

    protected void setUp() {
        super.setUp()
        service = new ListLoaderService()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testLoad() {
        service.load()
        assert ListLoaderService.states
        assert ListLoaderService.states.size() == 8

    }
}
