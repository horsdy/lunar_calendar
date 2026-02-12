package com.houshidi.calendar;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.houshidi.calendar.feature.widget.CalendarWidgetProvider;

/**
 * 在进程内运行时注册系统日期/时间广播，确保在系统设置中修改日期时小组件能立即刷新（Android 15 根本方案）。
 */
public class CalendarApplication extends Application {

    private BroadcastReceiver dateChangeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        registerDateChangeReceiver();
    }

    private void registerDateChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        dateChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] ids = appWidgetManager.getAppWidgetIds(
                        new ComponentName(context, CalendarWidgetProvider.class));
                for (int id : ids) {
                    CalendarWidgetProvider.updateAppWidget(context, appWidgetManager, id);
                }
            }
        };

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(dateChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(dateChangeReceiver, filter);
        }
    }
}
