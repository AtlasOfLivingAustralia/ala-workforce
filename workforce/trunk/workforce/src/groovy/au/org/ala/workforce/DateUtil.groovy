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

    static int getCurrentYear() {
        def c = Calendar.instance
        return c.get(Calendar.YEAR)
    }

    static String getNiceDateFromSqlDate(String date) {
        def year = date.substring(0, 4)
        def numMonth = date.substring(5, 7) as int
        def month = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'][numMonth - 1]
        def day = date.substring(8, 10)
        def time = date.substring(11, 16)
        return "${day} ${month} ${year} ${time}"
    }
}
