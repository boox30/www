package com.onyx.kreader.ui.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.onyx.kreader.R;
import com.onyx.kreader.ui.data.SingletonSharedPreference;

public class SystemSettingsActivity extends PreferenceActivity {

    private CheckBoxPreference mEnableSystemStatusBarCheckBox = null;
    private CheckBoxPreference mEnableReaderStatusBarCheckBox = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.system_settings);
        setContentView(R.layout.setting_main);

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onResume() {
        super.onResume();
        boolean isSystemBarEnabled = SingletonSharedPreference.isSystemStatusBarEnabled(this);
        boolean isReaderBarEnabled = SingletonSharedPreference.isReaderStatusBarEnabled(this);
        mEnableSystemStatusBarCheckBox = (CheckBoxPreference)findPreference(getResources().getString(R.string.settings_enable_system_status_bar_key));
        mEnableReaderStatusBarCheckBox = (CheckBoxPreference)findPreference(getResources().getString(R.string.settings_enable_reader_status_bar_key));

        mEnableSystemStatusBarCheckBox.setChecked(isSystemBarEnabled);
        mEnableReaderStatusBarCheckBox.setChecked(isReaderBarEnabled);

        RelativeLayout mBackFunctionLayout = (RelativeLayout) findViewById(R.id.back_function_layout);
        TextView settingTittle=(TextView)findViewById(R.id.settingTittle);

        settingTittle.setText(R.string.settings_system_tittle);
        mBackFunctionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
