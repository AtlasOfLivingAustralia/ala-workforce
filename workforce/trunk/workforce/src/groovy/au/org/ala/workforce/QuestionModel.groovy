package au.org.ala.workforce

import grails.converters.deep.JSON
import static AnswerDataType.*
import java.text.NumberFormat
import java.text.ParseException

/**
 * OO representation of a question.
 *
 * Created by markew
 * Date: Dec 14, 2010
 * Time: 8:50:48 AM
 */
class QuestionModel {

    int questionNumber              // the ordinal for this level of question
    int level                       // the level this question has in the hierarchy
    String label                    // type of displayed label, eg none, 3, b), iii)
    String qtext                    // the question text
    QuestionType qtype              // question type, eg rank, pick, range, matrix, group, none
    Object qdata                    // json string describing data - format depends on the qType
    String instruction              // optional instructions
    AnswerType atype = AnswerType.text
                                    // the type of the answer widget, eg range, radio, percent, text, rank, boolean, none
    AnswerDataType datatype = AnswerDataType.text
                                    // the data type of the answer, eg bool, number, text, percent, numberRange
    String alabel                   // text that labels the answer widget - may be units eg 'hours per week'
    Object adata                    // data for a answer , eg a pick list - may be a reference to an external list eg states of australia
    String displayHint              // suggested form of display, eg dropdown, radio, checkbox
    String layoutHint               // directs layout of child questions
    boolean required                // the answer may not be blank

    QuestionModel owner            // reverse link

    List<QuestionModel> questions = [] // sub questions

    QuestionModel() {}

    QuestionModel(record) {

        // determine level from the values of level1, level2 and level 3
        if (record.level3 > 0) {
            this.level = 3
        } else if (record.level2 > 0) {
            this.level = 2
        } else {
            this.level = 1
        }

        // the number of this question is the value at its level
        this.questionNumber = record."level${this.level}"

        // JSON objects
        if (record.adata) {
            this.adata = JSON.parse(record.adata)
        }
        if (record.qdata) {
            this.qdata = JSON.parse(record.qdata)
        }

        // other properties
        ['atype','qtype','label','qtext','instruction','alabel','displayHint','layoutHint','datatype','required'].each {
            this."${it}" = record."${it}"
        }

    }

    def validate(params) {
        def valid = true
        def reason = ''
        println "validating ${ident()} atype=${atype} datatype=${datatype}"
        if (atype != AnswerType.none) {
            def myAnswer = params."${ident()}"
            println "answer is ${myAnswer}"
            if (myAnswer) {
                // validate my answer
                switch (datatype) {
                    case bool:
                        valid = myAnswer
                        break
                    case text:
                        valid = myAnswer  // only has to exist
                        break
                    case number:
                        try {
                            NumberFormat.getInstance().parse(myAnswer)
                        } catch (ParseException e) {
                            valid = false
                            reason = "${myAnswer} is not a valid number"
                        }
                        break
                    case percent:
                        try {
                            def val = NumberFormat.getInstance().parse(myAnswer)
                            if (!val in 0..100) {
                                valid = false
                                reason = "A percentage must be between 0 and 100"
                            }
                        } catch (ParseException e) {
                            valid = false
                            reason = "${myAnswer} is not a valid number"
                        }
                        break
                    case numberRange:
                        def numbers = myAnswer.tokenize('-')
                        if (numbers.size() == 2) {
                            try {
                                NumberFormat.getInstance().parse(numbers[0])
                                NumberFormat.getInstance().parse(numbers[1])
                            } catch (ParseException e) {
                                valid = false
                                reason = "A number range must contain two valid numbers"  // not a message that a user should see
                            }
                        } else {
                            valid = false
                            reason = "A number range must be in the form nnn-mmm"  // not a message that a user should see
                        }
                        break
                    default:
                        valid = false
                        reason = "${datatype} is not a known datatype"
                }
            } else {
                if (required) {
                    println "answer for ${ident()} is not valid because no value was entered and it is required"
                    valid = false
                    reason = "An answer is required"
                }
            }
        }

        // add errors to a map
        Map<String,String> errors = new HashMap<String, String>()
        if (!valid) {
            errors.put ident(), reason
        }
        // check sub-questions
        questions.each {
            errors += it.validate(params)
        }
        return errors
    }

    String ident() {
        switch (level) {
            case 1:
                return "q" + questionNumber
            case 2:
                return "q" + owner.questionNumber + "_" + questionNumber
            case 3:
                return "q" + owner.owner.questionNumber + "_" + owner.questionNumber + "_" + questionNumber
            default:
                return "error"
        } 
    }

    int level1() {
        switch (level) {
            case 1:
                return questionNumber
            case 2:
                return owner.questionNumber
            case 3:
                return owner.owner.questionNumber
            default:
                return -1
        }
    }

    int level2() {
        switch (level) {
            case 1:
                return 0
            case 2:
                return questionNumber
            case 3:
                return owner.questionNumber
            default:
                return -1
        }
    }

    int level3() {
        switch (level) {
            case 1:
                return 0
            case 2:
                return 0
            case 3:
                return questionNumber
            default:
                return -1
        }
    }

    /**
     * Returns the number of 'rows' required to layout this question.
     * @return positive integer
     */
    int calculateDisplayRows() {
        // hack for now
        int rows = questions.size()
        if (qtext) rows++
        if (instruction) rows++
        return rows;
    }

    def String toString() {
        return ident() + ":\n" +
                "text=${qtext}\n" +
                "qtype=${qtype}\n" +
                "qdata=${qdata}\n" +
                "instruction=${instruction}\n" +
                "atype=${atype}\n" +
                "dtatype=${datatype}\n" +
                "alabel=${alabel}\n" +
                "adata=${adata}\n" +
                "displayHint=${displayHint}\n"
    }

    /**
     * Reverses the ident transform.
     * @param ident
     * @return Returns the question number for each level as a list [level1, level2, level3]
     */
    static List parseIdent(String ident) {
        def strs = ident?.tokenize('_')
        // TODO: needs validity checking and an INVALID_IDENT exception
        def s1 = strs[0]
        def s2 = ""
        def s3 = ""
        s1 = s1[1..-1] // string the q
        if (strs.size > 1) {
            s2 = strs[1]
        }
        if (strs.size() > 2) {
            s3 = strs[3]
        }
        try {
            def nf = NumberFormat.getInstance()
            def l1 = nf.parse(s1)
            def l2 = nf.parse(s2)
            return [l1,l2,l3]
        } catch (ParseException e) {
            return []
        }
    }
}
