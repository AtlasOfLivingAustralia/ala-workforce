package au.org.ala.workforce

/**
 * Created by markew
 * Date: Dec 13, 2010
 * Time: 2:26:48 PM
 */
enum AnswerDataType {
    bool, number, text, numberRange, rank

    static list() {
        [bool, number, text, numberRange, rank]
    }
}
