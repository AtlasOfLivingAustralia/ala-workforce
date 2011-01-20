package au.org.ala.workforce

/**
 * Created by markew
 * Date: Dec 13, 2010
 * Time: 10:20:55 AM
 */
enum QuestionType {
    rank, pick, range, matrix, group, none

    static list() {
        [rank, pick, range, matrix, group, none]
    }
}
