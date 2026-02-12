package com.houshidi.calendar;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.houshidi.calendar.feature.main.CalendarDayModel;
import com.houshidi.calendar.feature.main.CalendarPagerAdapter;
import com.houshidi.calendar.feature.main.CalendarViewModel;
import com.houshidi.calendar.feature.settings.SettingsActivity;
import com.houshidi.calendar.feature.widget.CalendarWidgetProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CalendarViewModel viewModel;
    private CalendarPagerAdapter pagerAdapter;
    private ViewPager2 viewPager;
    private SimpleDateFormat detailDateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initViewModel();
    }

    private void initViews() {
        Button btnToday = findViewById(R.id.btn_today);
        ImageButton btnSettings = findViewById(R.id.btn_settings);
        viewPager = findViewById(R.id.vp_calendar);

        btnToday.setOnClickListener(v -> viewModel.resetToToday());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                viewModel.setPagerPosition(position);
                
                // F-003: 切换到另一个月时取消全部item的选中状态
                CalendarDayModel currentSelected = viewModel.getSelectedDay().getValue();
                if (currentSelected != null) {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.setTimeInMillis(currentSelected.timeMillis);
                    
                    Calendar monthCal = Calendar.getInstance();
                    monthCal.set(Calendar.DAY_OF_MONTH, 1);
                    monthCal.add(Calendar.MONTH, position - 1200);
                    
                    if (selectedCal.get(Calendar.YEAR) != monthCal.get(Calendar.YEAR) ||
                        selectedCal.get(Calendar.MONTH) != monthCal.get(Calendar.MONTH)) {
                        viewModel.selectDay(null);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadStartFromMondayFromPrefs();
        }
        refreshCalendarWidget();
    }

    /** 主界面可见时刷新小组件，保证在系统里修改日期后公历与农历能同步（广播可能被系统限制） */
    private void refreshCalendarWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(this, CalendarWidgetProvider.class));
        for (int id : ids) {
            CalendarWidgetProvider.updateAppWidget(this, appWidgetManager, id);
        }
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        viewModel.getStartFromMonday().observe(this, fromMonday -> {
            updateWeekHeaders(fromMonday);
            setupPagerAdapter(fromMonday);
        });

        viewModel.getCurrentMonthTitle().observe(this, title -> {
            TextView tvMonthTitle = findViewById(R.id.tv_month_title);
            tvMonthTitle.setText(title);
        });

        viewModel.getSelectedDay().observe(this, day -> {
            updateDetailSection(day);
            // 刷新当前页面以更新高亮状态
            if (pagerAdapter != null) {
                pagerAdapter.notifyDataSetChanged();
            }
        });

        viewModel.getCurrentPagerPosition().observe(this, position -> {
            if (viewPager.getCurrentItem() != position) {
                viewPager.setCurrentItem(position, true);
            }
        });
    }

    private void setupPagerAdapter(boolean fromMonday) {
        pagerAdapter = new CalendarPagerAdapter(viewModel, fromMonday);
        viewPager.setAdapter(pagerAdapter);
        Integer currentPos = viewModel.getCurrentPagerPosition().getValue();
        if (currentPos != null) {
            viewPager.setCurrentItem(currentPos, false);
        }
    }

    private void updateWeekHeaders(boolean fromMonday) {
        String[] weekDays;
        if (fromMonday) {
            weekDays = new String[]{getString(R.string.mon), getString(R.string.tue), getString(R.string.wed), 
                                   getString(R.string.thu), getString(R.string.fri), getString(R.string.sat), getString(R.string.sun)};
        } else {
            weekDays = new String[]{getString(R.string.sun), getString(R.string.mon), getString(R.string.tue), 
                                   getString(R.string.wed), getString(R.string.thu), getString(R.string.fri), getString(R.string.sat)};
        }

        for (int i = 0; i < 7; i++) {
            int resId = getResources().getIdentifier("tv_week_" + i, "id", getPackageName());
            TextView tv = findViewById(resId);
            if (tv != null) {
                tv.setText(weekDays[i]);
            }
        }
    }

    private void updateDetailSection(CalendarDayModel day) {
        View layoutDetail = findViewById(R.id.layout_detail);
        if (day == null) {
            layoutDetail.setVisibility(View.INVISIBLE);
            return;
        }
        layoutDetail.setVisibility(View.VISIBLE);

        TextView tvGregorian = findViewById(R.id.tv_detail_gregorian);
        TextView tvLunar = findViewById(R.id.tv_detail_lunar);
        TextView tvDelta = findViewById(R.id.tv_detail_delta);

        tvGregorian.setText(detailDateFormat.format(new Date(day.timeMillis)));
        tvLunar.setText("农历 " + viewModel.formatLunarMonthDay(day.timeMillis));

        long diffMillis = day.timeMillis - getTodayStartMillis();
        long diffDays = Math.round(diffMillis / (1000.0 * 60 * 60 * 24));

        if (diffDays == 0) {
            tvDelta.setText("今天");
        } else if (diffDays > 0) {
            tvDelta.setText(diffDays + "天后");
        } else {
            tvDelta.setText(Math.abs(diffDays) + "天前");
        }
    }

    private long getTodayStartMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
