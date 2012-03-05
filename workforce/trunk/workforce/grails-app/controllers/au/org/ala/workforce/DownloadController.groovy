package au.org.ala.workforce

import au.com.bytecode.opencsv.CSVWriter

class DownloadController {

    def modelLoaderService

    def download = {

        cache false
        def setId = params.set as int ?: Survey.getCurrentQSetId(SurveyType.personal)
        def qset = QuestionSet.findBySetId(setId)
        def year = params.year ?: ConfigData.getSurveyYear()

        String csvResultFile = "ABRS ${qset.shortName} ${year}.csv";
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Content-Disposition", "attachment;filename=" + csvResultFile);
        response.setContentType("text/plain");
        final CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        def topLevelQuestions = Question.findAllByLevel2AndQset(0, setId)

        // write header row
        def questions = []
        topLevelQuestions.each { question ->
            questions <<  modelLoaderService.loadQuestionWithAnswer(setId, question.level1, [:])
        }
        csvWriter.writeNext(getHeaderRow(questions) as String[])

        // write answers for each completed survey
        def users = Event.usersCompletedForYear(setId, year as String)
        users.each { userid ->
            def answers = Answer.getAnswers(setId, userid as int, year)
            def answerRow = [userid]
            questions = []
            topLevelQuestions.each { question ->
                questions <<  modelLoaderService.loadQuestionWithAnswer(setId, question.level1, answers)
            }
            questions.each { model ->
                addAnswer(model, answerRow)
            }

            csvWriter.writeNext(answerRow as String[])
        }

        csvWriter.close()
        return null
    }

    List getHeaderRow(List<QuestionModel> questions) {
        def headerRow = ['User']

        questions.each {
            addQuestionNumber(it, headerRow)
        }

        return headerRow
    }

    def addQuestionNumber(QuestionModel model, List row) {
        if (model.guid) {
            switch (model.level) {
                case 1:
                    row << "${model.questionNumber}"
                    break
                case 2:
                    row << "${model.owner.questionNumber}.${model.questionNumber}"
                    break
                case 3:
                    row << "${model.owner.owner.questionNumber}.${model.owner.questionNumber}.${model.questionNumber}"
                    break
                default:
                    row << '?'
                    break
            }
        }

        model.questions.each {
            addQuestionNumber(it, row)
        }
    }

    def addAnswer(QuestionModel model, List row) {
        if (model.guid) {
            if (model.answerValueStr) {
                if (model.answerValueStr == 'on') {
                    row << model.qtext
                } else {
                    row << model.answerValueStr
                }
            } else {
                row << ''
            }
        }

        model.questions.each {
            addAnswer(it, row)
        }
    }
}

