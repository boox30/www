package com.onyx.jdread.setting.model;

import com.onyx.android.sdk.reader.dataprovider.LegacySdkDataUtils;
import com.onyx.android.sdk.reader.device.ReaderDeviceManager;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.R;
import com.onyx.jdread.main.common.JDPreferenceManager;

import java.util.regex.Pattern;

/**
 * Created by li on 2017/12/21.
 */

public class SettingRefreshModel {
    private String[] refreshPages;
    private String currentRefreshPage;
    private static final String REFRESH_RATE = "refresh_rate";

    public String getCurrentRefreshPage() {
        return currentRefreshPage = JDPreferenceManager.getStringValue(REFRESH_RATE, JDReadApplication.getInstance().getResources().getString(R.string.device_setting_ten_pages));
    }

    public void setCurrentPageRefreshPage(String currentPageRefreshTime) {
        JDPreferenceManager.setStringValue(REFRESH_RATE, currentPageRefreshTime);
        String refreshTime = Pattern.compile("[^0-9]").matcher(currentPageRefreshTime).replaceAll("");
        int refreshValue = StringUtils.isNullOrEmpty(refreshTime) ? Integer.MAX_VALUE : Integer.valueOf(refreshTime);
        LegacySdkDataUtils.setScreenUpdateGCInterval(JDReadApplication.getInstance(), refreshValue);
        ReaderDeviceManager.setGcInterval(refreshValue);
    }

    public String[] getRefreshPages() {
        return refreshPages = JDReadApplication.getInstance().getResources().getStringArray(R.array.device_setting_page_refreshes);
    }
}