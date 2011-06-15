package au.org.ala.workforce

/**
 * Created by markew
 * Date: Dec 13, 2010
 * Time: 2:26:48 PM
 */
enum AnswerType {
    bool, none, number, text, textarea, percent, rank, externalRef, radio, range, preload, calculate

    static list() {
        [bool, none, number, text, textarea, percent, rank, externalRef, radio, range, preload, calculate]
    }
}
