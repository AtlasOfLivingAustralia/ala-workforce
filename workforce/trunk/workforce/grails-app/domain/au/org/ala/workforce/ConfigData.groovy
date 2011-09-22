package au.org.ala.workforce

class ConfigData {

    int currentSurveyYear   
    String feedbackEmail
    String supportEmail

    static constraints = {
    }

    static void init() {
        def configs = ConfigData.findAll()
        if (configs.size() == 0) {
            ConfigData config = new ConfigData(
                    currentSurveyYear: DateUtil.getCurrentYear(),
                    feedbackEmail: 'abrs@environment.gov.au',
                    supportEmail: 'support@ala.org.au')
            config.save()
        }
    }

    static int getSurveyYear() {
        def config = ConfigData.get(1)
        if (config) {
            return config.currentSurveyYear
        } else {
            return DateUtil.getCurrentYear()
        }
    }

    static String getFeedbackAddress() {
        def config = ConfigData.get(1)
        if (config) {
            return config.feedbackEmail
        } else {
            return '???'
        }
    }

    static String getSupportAddress() {
        def config = ConfigData.get(1)
        if (config) {
            return config.supportEmail
        } else {
            return '???'
        }
    }
}
