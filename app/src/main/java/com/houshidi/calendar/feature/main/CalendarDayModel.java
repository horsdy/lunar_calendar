package com.houshidi.calendar.feature.main;

/**
 * Model representing a single day in the calendar grid.
 */
public class CalendarDayModel {
    public final String day;          // Gregorian day (1, 2, 3...)
    public final String lunarDay;     // Lunar day or month name
    public final boolean isToday;
    public final boolean isCurrentMonth;
    public final long timeMillis;

    public CalendarDayModel(String day, String lunarDay, boolean isToday, boolean isCurrentMonth, long timeMillis) {
        this.day = day;
        this.lunarDay = lunarDay;
        this.isToday = isToday;
        this.isCurrentMonth = isCurrentMonth;
        this.timeMillis = timeMillis;
    }
}
