package au.org.ala.workforce

class Survey {

    int year                        // Year of the survey
    SurveyType type                 // Survey type - currently Personal or Institutional
    //int questionSetId             // Question set associated with this survey
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
            def year = DateUtil.currentYear()
            Survey survey = new Survey(year: year, priorYear1: year-2, priorYear2: year-1, type: 'personal')
            survey.save()
            survey = new Survey(year: year, priorYear1: year-2, priorYear2: year-1, type: 'institutional')
            survey.save()
        }
    }

    static int getCurrentQSetId(SurveyType type) {
        Survey s = Survey.findByYearAndType(ConfigData.getSurveyYear(), type)
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
