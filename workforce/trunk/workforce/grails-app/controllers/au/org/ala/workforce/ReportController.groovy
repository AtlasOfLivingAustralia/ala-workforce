package au.org.ala.workforce

import org.apache.fop.svg.PDFTranscoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import grails.converters.JSON

class ReportController {

    def modelLoaderService
    def aggregationService

    /**
     * Display questions that have answers.
     *
     */
    def answers = {
        cache false
        
        def questionList = []

        def setId = params.set as int ?: Survey.getCurrentQSetId(SurveyType.personal)
        def qset = QuestionSet.findBySetId(setId)

        def year = params.year ?:  ConfigData.getSurveyYear()
        def users = User.getUsersWithAnswers(setId, year)

        def loggedInUserId = request.userPrincipal.attributes.userid as int
        def userId
        User user
        if (request.isUserInRole('ROLE_ABRS_ADMIN') || request.isUserInRole('ROLE_ADMIN')) {
            if (params.id) {
                userId = params.id as int
                user = users.find{it.userid == userId}
            } else {
                user = users[0]
                userId = user.userid
            }
        } else {
            userId = loggedInUserId
            user = User.findByUserid(userId)
        }

        def questions = Question.findAllByLevel2AndQset(0, setId)

        def answers = Answer.getAnswers(setId, userId, year)
        questions.each {
            questionList <<  modelLoaderService.loadQuestionWithAnswer(setId, it.level1, answers)
        }

        assert questionList

        // render the page
        [qset: qset, questions: questionList, users: users, user: user]
    }

    
    /**
     * Display a single question with answers.
     *
     */
    def singleQuestionAnswer = {

        def questionId = params.qid.toInteger() ?: 1
        def setId = params.set.toInteger() ?: Survey.getCurrentQSetId(SurveyType.personal)
        def qset = QuestionSet.findBySetId(setId)
        def loggedInUserId = request.userPrincipal.attributes.userid as int
        def userId = params.id as int ?: loggedInUserId
        if (userId != loggedInUserId && !(request.isUserInRole('ROLE_ABRS_ADMIN') || request.isUserInRole('ROLE_ADMIN'))) {
            userId = loggedInUserId
        }
        def user = User.findByUserid(userId)
        def year = params.year ?: ConfigData.getSurveyYear()
        def answers = Answer.getAnswers(setId, userId, year)
        def question = modelLoaderService.loadQuestionWithAnswer(setId, questionId, answers)

        // render the page
        render(view:'answer', model:[qset: qset, question: question, user: user])
    }

    /**
     * Display charts for a survey question.
     *
     */
    def charts = {
        cache false

        def chartData = []
        def title
        def setId
        def survey
        def questionId
        def questionText
        def year
        def userId
        def fromFile = false

        if (params.file) {
            def file = params.file
            survey = params.survey
            def json = JSON.parse(new FileInputStream("/data/workforce/charts/${survey.replace(' ', '-')}/${file}"), 'UTF-8')
            chartData = json.chartData
            title = json.title
            setId = json.setId
            questionId = json.qId
            questionText = json.qText
            userId = json.userId
            year = json.year
            fromFile = true
        } else {
            questionId = params.qid as int ?: 1
            userId = params.uid as int
            setId = params.set as int ?: Survey.getCurrentQSetId(SurveyType.personal)
            def qset = QuestionSet.findBySetId(setId)
            title = qset.title
            survey = qset.shortName
            year = params.year ?: ConfigData.getSurveyYear()

            def question = modelLoaderService.loadQuestion(setId, questionId)
            questionText = question.shorttext
            aggregationService.getAggregatedData(question, qset, chartData)
        }

        def model = [chartData: chartData, title: title, setId: setId, survey: survey, qId: questionId, qText: questionText, year: year, userId: userId]

        // Create chart model as encoded JSON string
        def jsonData = model as JSON
        def jsonString = jsonData.toString(true)
        def encodedString = jsonString.bytes.encodeBase64().toString()
        model.json = encodedString
        model.fromFile = fromFile
        
        // render the page
        model
    }

    def save = {
        // Create dirs to hold charts
        def parentDir = '/data/workforce/charts'
        def surveyDir = parentDir + '/' + params.survey.replace(' ', '-')
        def yearDir = surveyDir + '/' + params.year
        def questionDir = yearDir + '/Question-' + params.qnum
        new File(parentDir).mkdir()
        new File(surveyDir).mkdir()
        new File(yearDir).mkdir()
        new File(questionDir).mkdir()

        // Save json chart data
        def jf = new File(questionDir, 'charts.json')
        def jsonString = new String(params.json.decodeBase64())
        jf.write(jsonString)

        // Save charts as svg and pdf
        def index = 0
        while(true) {
            def chart = 'chart' + index++
            if (params[chart]) {
                def f = new File(questionDir, chart + '.svg')
                f.write(params[chart])

                PDFTranscoder t = new PDFTranscoder()
                Reader reader = new StringReader(params[chart])
                TranscoderInput input = new TranscoderInput(reader)
                OutputStream ostream = new FileOutputStream(new File(questionDir, chart + '.pdf'))
                TranscoderOutput output = new TranscoderOutput(ostream)
                t.transcode(input, output)

                ostream.flush()
                ostream.close()
            } else {
                break
            }
        }
        if (params.fromFile.equals('true')) {
            redirect(url: "/workforce/admin/set/${params.qsetId}")
        } else {
            redirect(url: "${params.qsetId}/user/${params.userId}#Q${params.qnum}")
        }
    }
}
