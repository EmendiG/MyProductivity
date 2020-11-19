package planner;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarDate {

    int offsetDays;


    public CalendarDate(int offsetDays) {
        this.offsetDays = offsetDays;
    }

    public Calendar getOffsettedCalendar() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_YEAR, (calendar.get(Calendar.DAY_OF_YEAR) - this.offsetDays ) );
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

}
