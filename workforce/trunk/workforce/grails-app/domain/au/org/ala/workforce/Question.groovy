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
    String guid                     // a unique identifier of the question - independent of question set or question hierarchy
    String label                    // type of displayed label, eg none, 3, b), iii)
    String qtext                    // the question text
    String subtext                  // sub-heading to the question text
    String shorttext                // abbreviated question text for answer report
    String aggregationText          // question label for aggregation report
    QuestionType qtype              // question type, eg ranked, matrix, pick-one
    String qdata                    // json string describing data - format depends on the qType
    String instruction              // optional instructions
    String instructionPosition      // optional positioning for instructions eg "top", default is "bottom"
    AnswerType atype                // the type of the answer widget, eg range, radio, percent, text, rank, boolean, none
    AnswerDataType datatype         // the data type of the answer, eg bool, number, percent, text
    String alabel                   // text that labels the answer widget - may be units eg 'hours per week'
    String adata                    // data for a answer , eg a pick list - may be a reference to an external list eg states of australia
    String displayHint              // suggested form of display, eg dropdown, radio, checkbox
    String layoutHint               // directs layout of child questions
    boolean required                // the answer (if there is one) may not be blank
    String requiredIf               // must have an answer if the condition is true
    String validation               // cross-question validation
    String onchangeAction           // js function to call when answer value is changed
    String dependentOn              // the entire question can be dependent on the answer to another question - this holds the path and the condition
    String aggregation              // json string describing aggregation instructions for a question

    static constraints = {
        level1(min:0)
        level2(min:0)
        level3(min:0)
        guid(nullable:true, maxSize:36)
        label(nullable:true)
        qtext(nullable:true, maxSize:2048)
        subtext(nullable:true, maxSize:2048)
        shorttext(nullable:true, maxSize:512)
        aggregationText(nullable:true, maxSize:512)
        qtype(nullable:false)
        qdata(nullable:true, maxSize:20000)
        instruction(nullable:true, maxSize:2048)
        instructionPosition(nullable:true, maxSize:48)
        atype(nullable:false)
        datatype(nullable:false)
        alabel(nullable:true)
        adata(nullable:true, maxSize:2048)
        displayHint(nullable:true)
        layoutHint(nullable:true)
        requiredIf(nullable:true, maxSize:1024)
        dependentOn(nullable:true, maxSize:1024)
        validation(nullable:true, maxSize: 1024)
        onchangeAction(nullable: true, maxSize: 1024)
        aggregation(nullable: true, maxSize: 10240)
    }

    // make the hash an index
    static mapping = {
        guid index:'guid_idx'
    }

    String buildIdent() {
        String ident = level1
        if (level2) {
            ident += "_" + level2
        }
        if (level3) {
            ident += "_" + level3
        }
        return ident
    }
}
