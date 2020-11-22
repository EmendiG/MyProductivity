package planner;

public enum Weekdays {

    EVERYDAY    (0, "EVERYDAY"),

    MONDAY      (1,  "Monday"),
    TUESDAY     (2,  "Tuesday"),
    WEDNESDAY   (3,  "Wednesday"),
    THURSDAY    (4,  "Thursday"),
    FRIDAY      (5,  "Friday"),
    SATURDAY    (6,  "Saturday"),
    SUNDAY      (7,  "Sunday");

    String weekday;
    Integer nDay;

    private Weekdays(int nWeek, String name) {
        weekday = name;
        nDay = nWeek;
    }

    public static String getEnumByNDay(int nDay) {
        for (Weekdays weekdays : Weekdays.values())
            if (weekdays.nDay.equals(nDay))
                return weekdays.name();
        return null;
    }

    @Override
    public String toString() {
        return weekday;
    }
}
