package com.houshidi.calendar.domain.lunar;

import java.util.Calendar;
import java.util.Date;

/**
 * Lunar Calendar Conversion Helper.
 * Optimizations:
 * 1. Static constant data table for fast lookup (O(1) by year).
 * 2. Minimized object allocation during conversion.
 */
public class LunarCalendarHelper {

    // Lunar data from 1900 to 2100
    // Each int represents: 
    // 0-3 bit: leap month, 4-15 bit: month days (1 for 30 days, 0 for 29), 16-19 bit: leap month days
    private static final int[] LUNAR_INFO = {
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
            0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
            0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
            0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
            0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
            0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
            0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
            0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
            0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
            0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
            0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
            0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
            0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
            0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
            0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
            0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
            0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,
            0x0d520
    };

    private static final long BASE_TIME;
    static {
        Calendar cal = Calendar.getInstance();
        cal.set(1900, 0, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        BASE_TIME = cal.getTimeInMillis();
    }

    public static class LunarDate {
        public int year;
        public int month;
        public int day;
        public boolean isLeap;

        @Override
        public String toString() {
            return "LunarDate{" + year + "-" + month + "-" + day + (isLeap ? "(Leap)" : "") + "}";
        }
    }

    /**
     * Convert Gregorian date to Lunar date.
     * Complexity: O(n) where n is number of years since 1900 (max 200).
     */
    public static LunarDate fromGregorian(long timeMillis) {
        long offset = (timeMillis - BASE_TIME) / 86400000L;
        int lunarYear, lunarMonth, lunarDay;
        boolean isLeap = false;

        int i, temp = 0;
        for (i = 1900; i < 2101 && offset > 0; i++) {
            temp = getLunarYearDays(i);
            if (offset < temp) break;
            offset -= temp;
        }
        lunarYear = i;

        int leap = getLeapMonth(lunarYear);
        for (i = 1; i < 13 && offset > 0; i++) {
            // Check leap month
            if (leap > 0 && i == (leap + 1) && !isLeap) {
                --i;
                isLeap = true;
                temp = getLeapMonthDays(lunarYear);
            } else {
                temp = getLunarMonthDays(lunarYear, i);
            }

            if (isLeap && i == (leap + 1)) isLeap = false;

            if (offset < temp) break;
            offset -= temp;
        }
        lunarMonth = i;
        lunarDay = (int) offset + 1;

        LunarDate ld = new LunarDate();
        ld.year = lunarYear;
        ld.month = lunarMonth;
        ld.day = lunarDay;
        ld.isLeap = isLeap;
        return ld;
    }

    private static int getLunarYearDays(int year) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((LUNAR_INFO[year - 1900] & i) != 0) sum += 1;
        }
        return sum + getLeapMonthDays(year);
    }

    private static int getLeapMonth(int year) {
        return LUNAR_INFO[year - 1900] & 0xf;
    }

    private static int getLeapMonthDays(int year) {
        if (getLeapMonth(year) != 0) {
            return (LUNAR_INFO[year - 1900] & 0xf0000) != 0 ? 30 : 29;
        }
        return 0;
    }

    private static int getLunarMonthDays(int year, int month) {
        return (LUNAR_INFO[year - 1900] & (0x10000 >> month)) != 0 ? 30 : 29;
    }
}
