package com.houshidi.calendar.feature.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.houshidi.calendar.MainActivity;
import com.houshidi.calendar.R;
import com.houshidi.calendar.domain.lunar.LunarCalendarHelper;

import java.util.Calendar;

public class CalendarWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // W-002: First row 公历 月/日 星期（如：2月9日 周一）
        String weekStr = getWeekdayString(cal.get(Calendar.DAY_OF_WEEK));
        String gregorianStr = month + "月" + day + "日 " + weekStr;

        // W-002: Second row 农历 月/日（如：正月十二）
        LunarCalendarHelper.LunarDate lunar = LunarCalendarHelper.fromGregorian(cal.getTimeInMillis());
        String lunarStr = formatLunarForWidget(context, lunar);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_calendar);
        views.setTextViewText(R.id.widget_tv_gregorian, gregorianStr);
        views.setTextViewText(R.id.widget_tv_lunar, lunarStr);

        // W-006: 点击打开主应用
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static String getWeekdayString(int dayOfWeek) {
        // Calendar.SUNDAY=1 .. SATURDAY=7
        String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        return weekdays[dayOfWeek - 1];
    }

    private static String formatLunarForWidget(Context context, LunarCalendarHelper.LunarDate lunar) {
        String monthName = context.getString(
            context.getResources().getIdentifier("lunar_month_" + lunar.month, "string", context.getPackageName())
        );
        if (lunar.isLeap) monthName = context.getString(R.string.lunar_leap_prefix) + monthName;

        String dayName = getLunarDayName(context, lunar.day);
        return monthName + dayName;
    }

    private static String getLunarDayName(Context context, int day) {
        if (day <= 10) return context.getString(R.string.lunar_day_chu) + getDigit(day);
        if (day < 20) return context.getString(R.string.lunar_day_shi) + getDigit(day % 10);
        if (day == 20) return "二十";
        if (day < 30) return context.getString(R.string.lunar_day_nian) + getDigit(day % 10);
        if (day == 30) return context.getString(R.string.lunar_day_san);
        return "";
    }

    private static String getDigit(int d) {
        String[] digits = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        return digits[d];
    }
}
