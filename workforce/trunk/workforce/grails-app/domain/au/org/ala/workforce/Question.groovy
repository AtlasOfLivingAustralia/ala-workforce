package au.org.ala.workforce

/**
 * Metadata that describes a question.
 *
 * Granularity: a question elicits a single piece of data eg a number, a selection from a list
 */
class Question {

    int qset                        // a unique int that identifies the question qset
    int level1                      // main question number as in Q1, Q2, etc
    int level2                      // question number within main question
    int level3                      // sub-question within a question
    int hash                        // an integer that is a unique hash of the set and levels - uniquely identifies this question
    String label                    // type of displayed label, eg none, 3, b), iii)
    String qtext                    // the question text
    String shorttext                // abbreviated question text for answer report
    QuestionType qtype              // question type, eg ranked, matrix, pick-one
    String qdata                    // json string describing data - format depends on the qType
    String instruction              // optional instructions
    AnswerType atype                // the type of the answer widget, eg range, radio, percent, text, rank, boolean, none
    AnswerDataType datatype         // the data type of the answer, eg bool, number, percent, text
    String alabel                   // text that labels the answer widget - may be units eg 'hours per week'
    String adata                    // data for a answer , eg a pick list - may be a reference to an external list eg states of australia
    String displayHint              // suggested form of display, eg dropdown, radio, checkbox
    String layoutHint               // directs layout of child questions
    boolean required                // the answer (if there is one) may not be blank
    String requiredIf               // must have an answer if the condition is true
    String validation               // cross-question validation
    int heightHint                  // an estimate of the laid-out height of the question for pagination (this is
                                    // temporary as this will eventually be calculated)

    static constraints = {
        level1(min:0)
        level2(min:0)
        level3(min:0)
        label(nullable:true)
        qtext(nullable:true, maxSize:2048)
        shorttext(nullable:true, maxSize:2048)
        qtype(nullable:false)
        qdata(nullable:true, maxSize:2048)
        instruction(nullable:true, maxSize:2048)
        atype(nullable:false)
        datatype(nullable:false)
        alabel(nullable:true)
        adata(nullable:true, maxSize:2048)
        displayHint(nullable:true)
        layoutHint(nullable:true)
        requiredIf(nullable:true, maxSize:1024)
        validation(nullable:true, maxSize: 1024)
    }

    // make the hash an index
    static mapping = {
        hash index:'hash_idx'
    }

    // calculate the hash before initial save
    def beforeInsert() {
        hash = makeHash()
    }

    // make a unique integer identifier from the set and levels values
    int makeHash() {
        return qset * 16777216 + level1 * 65536 + level2 * 256 + level3
    }
}
