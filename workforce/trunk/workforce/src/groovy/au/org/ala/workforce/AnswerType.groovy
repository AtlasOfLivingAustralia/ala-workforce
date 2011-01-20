package au.org.ala.workforce

/**
 * Created by markew
 * Date: Dec 13, 2010
 * Time: 2:26:48 PM
 */
enum AnswerType {
    yesno, none, number, text, textarea, percent, rank, externalRef, radio, range

    static list() {
        [yesno, none, number, text, textarea, percent, rank, externalRef, radio, range]
    }
}
