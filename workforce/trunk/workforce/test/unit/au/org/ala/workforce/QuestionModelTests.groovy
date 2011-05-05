package au.org.ala.workforce

import grails.test.GrailsUnitTestCase

/**
 * Created by markew
 * Date: Feb 18, 2011
 * Time: 12:02:34 PM
 */
class QuestionModelTests extends GrailsUnitTestCase {

    QuestionModel qm1, qm2, qm3, qm4

    protected void setUp() {
        super.setUp()
        qm1 = new QuestionModel()
        qm1.level = 1
        qm1.questionNumber = 1
        qm1.hash = 2**24 + 2**16

        qm2 = new QuestionModel()
        qm2.level = 2
        qm2.questionNumber = 1
        qm2.hash = 2**24 + 2**16 + 2**8
        qm2.owner = qm1
        qm1.questions = [qm2]

        qm3 = new QuestionModel()
        qm3.level = 3
        qm3.questionNumber = 1
        qm3.hash = 2**24 + 2**16 + 2**8 + 1
        qm3.owner = qm2
        qm2.questions = [qm3]

        qm4 = new QuestionModel()
        qm4.level = 3
        qm4.questionNumber = 2
        qm4.hash = 2**24 + 2**16 + 2*2**8 + 1
        qm4.owner = qm2
        qm2.questions << qm4
    }

    void testParseIdent() {
        assertEquals([1,1,1], QuestionModel.parseIdent('q1_1_1'))
        assertEquals([1,1,0], QuestionModel.parseIdent('q1_1'))
        assertEquals([1,0,0], QuestionModel.parseIdent('q1'))
        assertEquals([4,3,2], QuestionModel.parseIdent('q4_3_2'))
    }

    void testIdent() {
        assertEquals('q1', qm1.ident())
        assertEquals('q1_1', qm2.ident())
        assertEquals('q1_1_1', qm3.ident())
        assertEquals('q1_1_2', qm4.ident())
    }

    void testValidate_Required() {
        QuestionModel qm = new QuestionModel()
        qm.level = 1
        qm.questionNumber = 1
        qm.datatype = AnswerDataType.bool
        qm.required = true

        def errors = qm.validate()
        assertEquals(1, errors.size())
        assertEquals('An answer is required', errors.q1)
    }

    void testGetQuestionFromPath() {
        /*
        Model is:   qm1
                     |
                    qm2
                     |
                   ______
                   |    |
                  qm3  qm4
         */

        assertEquals('q1_1_1', qm4.getQuestionFromPath('../1')?.ident())
    }

    void testSaveAnswer() {
        mockDomain Answer

        qm3.answerValueStr = '666'

        assert qm3.saveAnswer(1)

        assert Answer.get(1).answerValue == '666'
        assert Answer.get(1).questionId == qm3.hash
    }

    void testEstimateHeight() {
        qm1.qtext = 'Ask a question'
        qm3.qtext = 'sub-question'
        qm4.qtext = 'sub-question'
        assert qm1.estimateHeight() == 3
        assert qm2.estimateHeight() == 2
        assert qm4.estimateHeight() == 1
    }
}
