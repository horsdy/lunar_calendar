package com.houshidi.calendar.feature.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.houshidi.calendar.R;

/**
 * Settings screen (F-005, S-001): week start, version, author, contact.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "calendar_prefs";
    private static final String KEY_WEEK_START_MONDAY = "week_start_monday";

    public static boolean getStartFromMonday(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_WEEK_START_MONDAY, true);
    }

    public static void setStartFromMonday(Context context, boolean fromMonday) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_WEEK_START_MONDAY, fromMonday)
                .apply();
    }

    private SharedPreferences prefs;
    private Spinner spinnerWeekStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 强制显示状态栏（整块状态栏由系统绘制）
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        setContentView(R.layout.activity_settings);

        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .show(WindowInsetsCompat.Type.statusBars());

        // 为状态栏预留 padding，避免内容贴边
        View root = findViewById(R.id.settings_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 一周的开始日：下拉框 周一 / 周日，默认周一
        spinnerWeekStart = findViewById(R.id.spinner_week_start);
        String[] options = new String[]{
                getString(R.string.option_monday),
                getString(R.string.option_sunday)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeekStart.setAdapter(adapter);

        boolean fromMonday = getStartFromMonday(this);
        spinnerWeekStart.setSelection(fromMonday ? 0 : 1);

        spinnerWeekStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setStartFromMonday(SettingsActivity.this, position == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // 版本（从 PackageManager 读取，不依赖 BuildConfig）
        TextView tvVersion = findViewById(R.id.tv_version_value);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersion.setText(versionName != null ? versionName : "1.0");
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("1.0");
        }

        // 作者、联系方式
        TextView tvAuthor = findViewById(R.id.tv_author_value);
        TextView tvContact = findViewById(R.id.tv_contact_value);
        tvAuthor.setText(R.string.settings_author_value);
        tvContact.setText(R.string.settings_contact_value);
    }
}
