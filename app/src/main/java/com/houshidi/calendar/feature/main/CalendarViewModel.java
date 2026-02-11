package com.houshidi.calendar.feature.main;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.houshidi.calendar.R;
import com.houshidi.calendar.domain.lunar.LunarCalendarHelper;
import com.houshidi.calendar.feature.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarViewModel extends AndroidViewModel {

    private final MutableLiveData<String> currentMonthTitle = new MutableLiveData<>();
    private final MutableLiveData<CalendarDayModel> selectedDay = new MutableLiveData<>();
    private final MutableLiveData<Boolean> startFromMonday = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> currentPagerPosition = new MutableLiveData<>(1200); // Center of 2400

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        startFromMonday.setValue(SettingsActivity.getStartFromMonday(application));
        resetToToday();
    }

    public LiveData<String> getCurrentMonthTitle() {
        return currentMonthTitle;
    }

    public LiveData<CalendarDayModel> getSelectedDay() {
        return selectedDay;
    }
    
    public LiveData<Boolean> getStartFromMonday() {
        return startFromMonday;
    }

    public LiveData<Integer> getCurrentPagerPosition() {
        return currentPagerPosition;
    }

    public void resetToToday() {
        currentPagerPosition.setValue(1200);
        
        Calendar today = Calendar.getInstance();
        normalizeToStartOfDay(today);
        
        selectDay(new CalendarDayModel(
                String.valueOf(today.get(Calendar.DAY_OF_MONTH)),
                formatLunarDay(LunarCalendarHelper.fromGregorian(today.getTimeInMillis())),
                true,
                true,
                today.getTimeInMillis()
        ));
    }

    public void setPagerPosition(int position) {
        currentPagerPosition.setValue(position);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        normalizeToStartOfDay(cal);
        cal.add(Calendar.MONTH, position - 1200);
        currentMonthTitle.setValue(cal.get(Calendar.YEAR) + "年" + (cal.get(Calendar.MONTH) + 1) + "月");
    }

    public void setStartFromMonday(boolean fromMonday) {
        startFromMonday.setValue(fromMonday);
    }

    /** Reload week start from SharedPreferences (e.g. after returning from Settings). */
    public void loadStartFromMondayFromPrefs() {
        startFromMonday.setValue(SettingsActivity.getStartFromMonday(getApplication()));
    }

    public void toggleStartOfWeek() {
        startFromMonday.setValue(!Boolean.TRUE.equals(startFromMonday.getValue()));
    }

    public void selectDay(CalendarDayModel day) {
        selectedDay.setValue(day);
    }

    /** Full lunar string (month + day) for detail display, e.g. "正月十二". */
    public String formatLunarMonthDay(long timeMillis) {
        LunarCalendarHelper.LunarDate lunar = LunarCalendarHelper.fromGregorian(timeMillis);
        int resId = getApplication().getResources().getIdentifier("lunar_month_" + lunar.month, "string", getApplication().getPackageName());
        String monthName = (resId != 0) ? getApplication().getString(resId) : "";
        String monthPart = (lunar.isLeap ? getApplication().getString(R.string.lunar_leap_prefix) : "") + monthName;
        String dayPart = formatLunarDayPart(lunar.day);
        return monthPart + dayPart;
    }

    private String formatLunarDayPart(int day) {
        if (day <= 10) return getApplication().getString(R.string.lunar_day_chu) + getLunarDigit(day);
        if (day < 20) return getApplication().getString(R.string.lunar_day_shi) + getLunarDigit(day % 10);
        if (day == 20) return "二十";
        if (day < 30) return getApplication().getString(R.string.lunar_day_nian) + getLunarDigit(day % 10);
        if (day == 30) return getApplication().getString(R.string.lunar_day_san);
        return "";
    }

    public List<CalendarDayModel> calculateDaysForMonth(Calendar monthCal, boolean fromMonday) {
        List<CalendarDayModel> dayList = new ArrayList<>();
        Calendar tempCal = (Calendar) monthCal.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        normalizeToStartOfDay(tempCal);
        
        int firstDayOfWeek = fromMonday ? Calendar.MONDAY : Calendar.SUNDAY;
        int dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        
        int offset = dayOfWeek - firstDayOfWeek;
        if (offset < 0) offset += 7;
        
        tempCal.add(Calendar.DAY_OF_MONTH, -offset);

        Calendar today = Calendar.getInstance();
        normalizeToStartOfDay(today);

        for (int i = 0; i < 42; i++) {
            boolean isToday = isSameDay(tempCal, today);
            boolean isCurrentMonth = tempCal.get(Calendar.MONTH) == monthCal.get(Calendar.MONTH);
            
            LunarCalendarHelper.LunarDate lunar = LunarCalendarHelper.fromGregorian(tempCal.getTimeInMillis());
            String lunarStr = formatLunarDay(lunar);

            dayList.add(new CalendarDayModel(
                    String.valueOf(tempCal.get(Calendar.DAY_OF_MONTH)),
                    lunarStr,
                    isToday,
                    isCurrentMonth,
                    tempCal.getTimeInMillis()
            ));
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dayList;
    }

    private void normalizeToStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private String formatLunarDay(LunarCalendarHelper.LunarDate lunar) {
        if (lunar.day == 1) {
            int resId = getApplication().getResources().getIdentifier("lunar_month_" + lunar.month, "string", getApplication().getPackageName());
            String monthName = (resId != 0) ? getApplication().getString(resId) : "";
            return (lunar.isLeap ? getApplication().getString(R.string.lunar_leap_prefix) : "") + monthName;
        }
        
        if (lunar.day <= 10) return getApplication().getString(R.string.lunar_day_chu) + getLunarDigit(lunar.day);
        if (lunar.day < 20) return getApplication().getString(R.string.lunar_day_shi) + getLunarDigit(lunar.day % 10);
        if (lunar.day == 20) return "二十";
        if (lunar.day < 30) return getApplication().getString(R.string.lunar_day_nian) + getLunarDigit(lunar.day % 10);
        if (lunar.day == 30) return getApplication().getString(R.string.lunar_day_san);
        return "";
    }

    private String getLunarDigit(int d) {
        String[] digits = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        return digits[d];
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
