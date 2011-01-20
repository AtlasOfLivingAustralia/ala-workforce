package au.org.ala.workforce

class ModelLoaderService {

    static transactional = true

    def loadQuestion(int number) {

        // load all question records for this top-level question
        def records = Question.findAllByLevel1(number)

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
}
