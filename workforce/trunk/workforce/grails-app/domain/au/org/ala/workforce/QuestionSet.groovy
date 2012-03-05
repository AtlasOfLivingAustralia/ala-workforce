package au.org.ala.workforce

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONException

class QuestionSet {

    int setId                       // the number of the set (integer identifier)
    String title                    // name of the set
    String shortName                // short display name for reports, breadcrumb trails, etc
    String pageSequence             // pagination of questions eg [{from:1,to:1},{from:2,to:6}]
    String requiredRole             // the role that the user must have to access this survey - blank = everyone
    String knownQuestions           // question numbers cross-referenced for charting eg [{"gender":15},{"ageGroup":16}]

    static constraints = {
        setId(unique:true)
        pageSequence(nullable:true)
        shortName(nullable:true)
        requiredRole(nullable:true)
        knownQuestions(nullable:true)
    }

    static transients = ['paginationData','page','totalPages']

    boolean hasPageSequence() {
        pageSequence as Boolean
    }

    /**
     * Returns a list of maps that represent the pagination of questions.
     *
     * Each page is represented by an entry in the list.
     * Each entry has the properties:
     *  'from' - starting question number
     *  'to' - ending question number
     *  'pageNumber' - the one-based number of the page
     *
     * @return list of maps
     */
    def getPaginationData() {
        if (!pageSequence) {
            return []
        }
        try {
            List pages = JSON.parse(pageSequence)
            pages.eachWithIndex {page, i -> page.pageNumber = i + 1}
            return pages
        } catch (JSONException e) {
            log.error "unable to parse page sequence: ${pageSequence} - ${e.getMessage()}"
            return []
        }
    }

    /**
     * Returns a map representing the page that contains the specified question.
     *
     * Properties are:
     * 'from' - the start question number
     * 'to' - the end question number
     * 'pageNumber' - the number of this page in the sequence (one-based)
     * 'totalPages' - the total number of pages in the question set
     *
     * @param questionNumber
     * @return
     */
    def findPageByQuestionNumber(int questionNumber) {
        def pages = getPaginationData()
        for (int i = 0; i < pages.size(); i++) {
            if (questionNumber in pages[i].from..pages[i].to) {
                pages[i].pageNumber = i + 1
                pages[i].totalPages = pages.size()
                return pages[i]
            }
        }
        // no page found
        return null
    }

    /**
     * Returns a map representing the specified page.
     *
     * Properties are:
     * 'from' - the start question number
     * 'to' - the end question number
     * 'pageNumber' - the number of this page in the sequence (one-based)
     * 'totalPages' - the total number of pages in the question set
     *
     * @param pageNumber the page to return
     * @return
     */
    def getPage(int pageNumber) {
        def pages = getPaginationData()
        if (pageNumber in 1..pages.size()) {
            def page = getPaginationData()[pageNumber - 1] // list is zero-base but page numbers are one-based
            page.pageNumber = pageNumber
            page.totalPages = pages.size()
            return page
        }
        else {
            return null
        }
    }

    /**
     * Returns the total number of pages of questions.
     *
     * @return
     */
    def getTotalPages() {
        return getPaginationData()?.size() ?: 0
    }

    String getKnownQuestionNumber(String question) {
        if (knownQuestions) {
            Map knownQs = JSON.parse(knownQuestions)
            return knownQs[question]
        } else {
            return null
        }
    }
}
