package au.org.ala.workforce

class Survey {

    int year                        // Year of the survey
    SurveyType type                 // Survey type - currently Personal or Institutional
    int questionSetId               // Question set associated with this survey
    int priorYear1                  // Earlier prior year for funding or publications
    int priorYear2                  // Later prior year for funding or publications

    static constraints = {
        questionSetId(unique:true)
    }

    static int getCurrentQSetId(SurveyType type) {
        int year = DateUtil.getCurrentYear()
        Survey s = Survey.findByYearAndType(year, type)
        return s.questionSetId
    }

    static Map getSurveyMetadata(int qset) {
        Survey s = Survey.findByQuestionSetId(qset)
        return [year1: s.priorYear1, year2: s.priorYear2]
    }

    static String getDescription(int qset) {
        Survey s = Survey.findByQuestionSetId(qset)
        return "${s.type}-${s.year}"
    }

    static String getType(int qset) {
        Survey s = Survey.findByQuestionSetId(qset)
        return s.type
    }
}
