package Tools;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Test {

    static public void main(String args[]) throws ParseException {

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.CANADA);
        Date date = dateFormat.parse("2019-11-10");
        String day = "10-11-2019";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-mm-yyyy HH");
        Calendar calendar = Calendar.getInstance();
        //calendar.set(Integer.parseInt("10"), Integer.parseInt("11"), Integer.parseInt("2019"), Integer.parseInt("08"), 0);
        calendar.set(2019,10,9,15,0, 0);
        //calendar.setTime(dateFormat.parse(day));
        //calendar.getTime();
        String time = calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":00";
        System.out.println("Calendar time: " + calendar);
        System.out.println("Current time: " + calendar.getTime());
        System.out.println("Time: " + time);

        System.out.println("Boolean: " + Arrays.toString(new Boolean[]{true, false}));
    }
}
