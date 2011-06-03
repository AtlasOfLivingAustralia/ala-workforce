package au.org.ala.workforce

/**
 * Created by IntelliJ IDEA.
 * User: peterflemming
 * Date: 3/06/11
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
class DateUtil {

    static int getYear(String year) {
        if (year) {
            return year as int
        } else {
            def c = Calendar.instance
            return c.get(Calendar.YEAR)
        }
    }
}
