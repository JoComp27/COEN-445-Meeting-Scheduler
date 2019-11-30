package Tools;

import java.util.Calendar;

public class CalendarUtil {

    public static String calendarToString(Calendar calendar){

        String calendarString = calendar.get(Calendar.YEAR) + ":" + calendar.get(Calendar.MONTH) + ":" + calendar.get(Calendar.DAY_OF_MONTH) +  ":" + calendar.get(Calendar.HOUR_OF_DAY);
        return calendarString;
    }

    public static Calendar stringToCalendar(String calendarString) {

        String[] cal = calendarString.split(":");

        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(cal[0]), Integer.parseInt(cal[1]), Integer.parseInt(cal[2]), Integer.parseInt(cal[3]), 0, 0);

        return c;
    }

}
