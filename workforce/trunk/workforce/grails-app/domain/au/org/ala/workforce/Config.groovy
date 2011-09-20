package au.org.ala.workforce

class Config {

    int currentSurveyYear   
    String feedbackEmail
    String supportEmail

    static constraints = {
    }

    static void init() {
        def configs = Config.findAll()
        if (configs.size() == 0) {
            Config config = new Config(
                    currentSurveyYear: DateUtil.currentYear(),
                    feedbackEmail: 'abrs@environment.gov.au',
                    supportEmail: 'support@ala.org.au')
            config.save()
        }
    }

    static String getFeedbackAddress() {
        def config = Config.get(1)
        if (config) {
            return config.feedbackEmail
        } else {
            return '???'
        }
    }

    static String getSupportAddress() {
        def config = Config.get(1)
        if (config) {
            return config.supportEmail
        } else {
            return '???'
        }
    }
}
