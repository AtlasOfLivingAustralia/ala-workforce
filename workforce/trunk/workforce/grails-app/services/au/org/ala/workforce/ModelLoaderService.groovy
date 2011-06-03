package au.org.ala.workforce

class ModelLoaderService {

    static transactional = true

    /**
     * Loads a top-level question and all its sub-questions.
     *
     * @param set
     * @param questionNumber
     * @return
     */
    def loadQuestion(int set, int questionNumber) {

        /*
         * Questions are stored as a flat list in the database.
         *
         * Relationships are implicit in the 3 levels of question numbering.
         *
         * This method builds the specified question and its sub-questions into
         * a hierarchy of question models.
         */

        // load all question records for this top-level question
        def records = Question.findAllByLevel1AndQset(questionNumber, set)

        if (!records) {return null}

        // get the top-level question
        def top = records.find {it.level2 == 0 && it.level3 == 0}

        QuestionModel qtop = new QuestionModel(top)

        // add second level questions
        records.findAll {it.level2 > 0 && it.level3 == 0} .each { r2 ->

            QuestionModel q2 = new QuestionModel(r2)

            // add to top
            qtop.questions << q2
            q2.owner = qtop

            // add third level questions
            records.findAll {it.level2 == r2.level2 && it.level3 > 0} .each { r3 ->

                QuestionModel q3 = new QuestionModel(r3)

                // add to parent
                q2.questions << q3
                q3.owner = q2
            }
        }

        return qtop
    }

    /**
     * Load metadata for the question and inject the most recent answer for this user.
     *
     * @param set the question set
     * @param questionNumber the question
     * @param userId the user
     * @param year the year
     * @return question model with latest answer (if any)
     */
    def loadQuestionWithAnswer(int set, int questionNumber, int userId, int year) {

        // load the question model
        QuestionModel model = loadQuestion(set, questionNumber)
        assert model

        // find the answers
        loadAnswers(model, userId, year)

        return model
    }

    /**
     * Find and inject the latest answer for the question and all its child questions.
     *
     * @param model the question model
     * @param userId the user
     * @param year the year
     * @return model populated with most recent answers
     */
    def loadAnswers(QuestionModel model, int userId, int year) {
        // get all answers for the question for this user
        def result
        if (model.guid) {
            def from = new GregorianCalendar(year, Calendar.JANUARY, 1)
            def to = new GregorianCalendar(year+1, Calendar.JANUARY, 1)
            result = Answer.executeQuery(
                    "select answerValue from Answer " +
                    "where setId = :set " +
                    "and guid = :guid " +
                    "and userId = :user " +
                    "and lastUpdated >= :from and lastUpdated < :to " +
                    "order by lastUpdated desc", [set: model.qset, guid: model.guid, user: userId, from: from.time, to: to.time])
        }

        if (result) {
            // select the most recent answer
            def answer = result[0]

            // inject answer into question model
            model.setAnswerValueStr(answer)
        }

        // call recursively for child questions
        model.questions.each {
            loadAnswers(it, userId, year)
        }
    }
}
