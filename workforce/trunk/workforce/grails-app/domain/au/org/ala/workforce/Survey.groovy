package au.org.ala.workforce

class Survey {

    int year                        // Year of the survey
    SurveyType type                 // Survey type - currently Personal or Institutional
    //int questionSetId               // Question set associated with this survey
    int priorYear1                  // Earlier prior year for funding or publications
    int priorYear2                  // Later prior year for funding or publications

    static mapping = {
        table 'survey'
        version false
        id column: 'question_set_id'
    }

    static void init() {
        def surveys = Survey.findAll()
        if (surveys.size() == 0) {
            Survey survey = new Survey(year: 2011, priorYear1: 2009, priorYear2: 2010, type: 'personal')
            survey.save()
            survey = new Survey(year: 2011, priorYear1: 2009, priorYear2: 2010, type: 'institutional')
            survey.save()
        }
    }

    static int getCurrentQSetId(SurveyType type) {
        int year = DateUtil.getCurrentYear()
        Survey s = Survey.findByYearAndType(year, type)
        return s.id
    }

    static Map getSurveyMetadata(int qset) {
        Survey s = Survey.findById(qset)
        return [year1: s.priorYear1, year2: s.priorYear2]
    }

    static String getDescription(int qset) {
        Survey s = Survey.findById(qset)
        return "${s.type}-${s.year}"
    }

    static String getType(int qset) {
        Survey s = Survey.findById(qset)
        return s.type
    }
}