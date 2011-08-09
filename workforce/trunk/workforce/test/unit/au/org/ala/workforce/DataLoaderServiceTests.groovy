package au.org.ala.workforce

import grails.test.GrailsUnitTestCase

class DataLoaderServiceTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSubstitutePlaceholders() {
        def text = 'The first year = ${year1} and the second year = ${year2}'
        def years = ['year1': '2009', 'year2': '2010']
        assert 'The first year = 2009 and the second year = 2010' == substitutePlaceholders(text, years)
    }

    String substitutePlaceholders(String text, Map priorYears) {
        return text.replaceAll(/\$\{(\w+)\}/) { all, placeholder ->  priorYears[placeholder] }
    }

}
