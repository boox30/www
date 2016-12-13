package com.onyx.kreader.utils;

import android.content.Context;
import android.os.Build;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.onyx.android.sdk.utils.RawResourceUtil;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.kreader.BuildConfig;
import com.onyx.kreader.R;
import com.onyx.kreader.common.Debug;
import com.onyx.kreader.ui.data.SingletonSharedPreference;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by ming on 16/10/18.
 */
@SuppressWarnings("unused")
public class DeviceConfig {

    private static DeviceConfig ourInstance;
    private static final boolean useDebugConfig = false;

    private boolean ttsEnabled = false;
    private boolean hasFrontLight = true;

    private Map<String, Map<String, JSONObject>> keyBinding = null;

    private boolean deleteAcsmAfterFulfillment = false;
    private boolean supportZipCompressedBooks = false;
    private boolean disableDictionaryFunc = false;
    private boolean disableFontFunc = false;
    private boolean disableNoteFunc = false;
    private boolean disableRotationFunc = false;
    private boolean useBigPen = false;
    private boolean defaultUseSystemStatusBar = false;
    private boolean defaultUseReaderStatusBar = false;
    private boolean defaultShowDocTitleInStatusBar = false;
    private boolean disableNavigation = false;
    private boolean hideSelectionModeUiOption = false;
    private boolean hideControlSettings = false;
    private boolean askForClose = false;
    private boolean supportColor = false;
    private int defaultGamma = 150;
    private int fixedGamma = 0;
    private boolean supportBrushPen = false;

    private int rotationOffset = 0;
    private int dialogNavigationSettingsSubScreenLandscapeRows = -1;
    private int dialogNavigationSettingsSubScreenLandscapeColumns = -1;
    private int selectionOffsetX = 15;
    private int defaultFontSize = 0;
    private int selectionMoveDistanceThreshold = 8;
    private int gcInterval = 0;
    private int frontLight = 0;
    private boolean exitAfterFinish = false;

    // in seconds
    private int slideshowMinimumInterval = 1;
    private int slideshowMaximumInterval = 60;
    private int defaultSlideshowInterval = 20;

    private int slideshowMinimumPages = 1;
    private int slideshowMaximumPages = 6000;
    private int defaultSlideshowPages = 2000;

    private String defaultAnnotationHighlightStyle = "Highlight";

    private DeviceConfig(Context context) {
        String content = readConfig(context);
        if (!StringUtils.isNullOrEmpty(content)) {
            ourInstance = JSON.parseObject(content, DeviceConfig.class);
        }
        if (ourInstance == null) {
            ourInstance = new DeviceConfig();
        }
    }

    private String readConfig(Context context) {
        String content = readFromDebugModel(context);

        if (StringUtils.isNullOrEmpty(content)) {
            content = readFromManufactureAndModel(context);
        }

        if (StringUtils.isNullOrEmpty(content)) {
            content = readFromModel(context);
        }

        if (StringUtils.isNullOrEmpty(content)) {
            content = readFromGeneralModel(context);
        }

        if (StringUtils.isNullOrEmpty(content)) {
            content = readFromBrand(context);
        }

        if (StringUtils.isNullOrEmpty(content)) {
            content = readFromDefaultOnyxConfig(context);
        }
        return content;
    }

    private String readFromBrand(Context context) {
        return contentFromRawResource(context, Build.BRAND);
    }

    private String readFromManufactureAndModel(Context context) {
        final String name = Build.MANUFACTURER + "_" + Build.MODEL;
        return contentFromRawResource(context, name);
    }

    private String readFromDebugModel(Context context) {
        if (BuildConfig.DEBUG && useDebugConfig) {
            return contentFromRawResource(context, "debug");
        }
        return null;
    }

    private String readFromModel(Context context) {
        final String name = Build.MODEL;
        return contentFromRawResource(context, name);
    }

    private String readFromGeneralModel(Context context) {
        final String model = Build.MODEL.toLowerCase();
        Field[] rawFields = R.raw.class.getFields();
        for (Field field : rawFields) {
            String fieldName = field.getName();
            if (model.startsWith(fieldName)) {
                return contentFromRawResource(context, fieldName);
            }
        }
        return null;
    }

    private String readFromDefaultOnyxConfig(Context context) {
        return contentFromRawResource(context, "onyx");
    }

    private String contentFromRawResource(Context context, String name) {
        String content = "";
        try {
            int res = context.getResources().getIdentifier(name.toLowerCase(), "raw", context.getPackageName());
            content = RawResourceUtil.contentOfRawResource(context, res);
        } catch (Exception e) {
            Debug.w(getClass(), e);
        }
        return content;
    }

    public DeviceConfig() {
    }

    static public DeviceConfig sharedInstance(Context context) {
        if (ourInstance == null) {
            new DeviceConfig(context);
        }
        return ourInstance;
    }

    public boolean isTtsEnabled() {
        return ttsEnabled;
    }

    public void setTtsEnabled(boolean ttsEnabled) {
        this.ttsEnabled = ttsEnabled;
    }

    public boolean hasFrontLight() {
        return hasFrontLight;
    }

    public void setHasFrontLight(boolean hasFrontLight) {
        this.hasFrontLight = hasFrontLight;
    }

    public Map<String, Map<String, JSONObject>> getKeyBinding() {
        return keyBinding;
    }

    public void setKeyBinding(Map<String, Map<String, JSONObject>> keyBinding) {
        this.keyBinding = keyBinding;
    }

    public boolean isDeleteAcsmAfterFulfillment() {
        return deleteAcsmAfterFulfillment;
    }

    public void setDeleteAcsmAfterFulfillment(boolean deleteAcsmAfterFulfillment) {
        this.deleteAcsmAfterFulfillment = deleteAcsmAfterFulfillment;
    }

    public boolean isSupportZipCompressedBooks() {
        return supportZipCompressedBooks;
    }

    public void setSupportZipCompressedBooks(boolean supportZipCompressedBooks) {
        this.supportZipCompressedBooks = supportZipCompressedBooks;
    }

    public boolean isDisableDictionaryFunc() {
        return disableDictionaryFunc;
    }

    public void setDisableDictionaryFunc(boolean disableDictionaryFunc) {
        this.disableDictionaryFunc = disableDictionaryFunc;
    }

    public boolean isDisableFontFunc() {
        return disableFontFunc;
    }

    public void setDisableFontFunc(boolean disableFontFunc) {
        this.disableFontFunc = disableFontFunc;
    }

    public boolean isDisableNoteFunc() {
        return disableNoteFunc;
    }

    public void setDisableNoteFunc(boolean disableNoteFunc) {
        this.disableNoteFunc = disableNoteFunc;
    }

    public boolean isDisableRotationFunc() {
        return disableRotationFunc;
    }

    public void setDisableRotationFunc(boolean disableRotationFunc) {
        this.disableRotationFunc = disableRotationFunc;
    }

    public boolean isUseBigPen() {
        return useBigPen;
    }

    public void setUseBigPen(boolean useBigPen) {
        this.useBigPen = useBigPen;
    }

    public boolean isSupportBrushPen() {
        return supportBrushPen;
    }

    public void setSupportBrushPen(boolean supportBrushPen) {
        this.supportBrushPen = supportBrushPen;
    }

    public boolean isDefaultUseSystemStatusBar() {
        return defaultUseSystemStatusBar;
    }

    public void setDefaultUseSystemStatusBar(boolean defaultUseSystemStatusBar) {
        this.defaultUseSystemStatusBar = defaultUseSystemStatusBar;
    }

    public boolean isDefaultUseReaderStatusBar() {
        return defaultUseReaderStatusBar;
    }

    public void setDefaultUseReaderStatusBar(boolean defaultUseReaderStatusBar) {
        this.defaultUseReaderStatusBar = defaultUseReaderStatusBar;
    }

    public boolean isDefaultShowDocTitleInStatusBar() {
        return defaultShowDocTitleInStatusBar;
    }

    public void setDefaultShowDocTitleInStatusBar(boolean defaultShowDocTitleInStatusBar) {
        this.defaultShowDocTitleInStatusBar = defaultShowDocTitleInStatusBar;
    }

    public boolean isDisableNavigation() {
        return disableNavigation;
    }

    public void setDisableNavigation(boolean disableNavigation) {
        this.disableNavigation = disableNavigation;
    }

    public boolean isHideSelectionModeUiOption() {
        return hideSelectionModeUiOption;
    }

    public void setHideSelectionModeUiOption(boolean hideSelectionModeUiOption) {
        this.hideSelectionModeUiOption = hideSelectionModeUiOption;
    }

    public boolean isHideControlSettings() {
        return hideControlSettings;
    }

    public void setHideControlSettings(boolean hideControlSettings) {
        this.hideControlSettings = hideControlSettings;
    }

    public int getRotationOffset() {
        return rotationOffset;
    }

    public void setRotationOffset(int rotationOffset) {
        this.rotationOffset = rotationOffset;
    }

    public int getDialogNavigationSettingsSubScreenLandscapeRows() {
        return dialogNavigationSettingsSubScreenLandscapeRows;
    }

    public void setDialogNavigationSettingsSubScreenLandscapeRows(int dialogNavigationSettingsSubScreenLandscapeRows) {
        this.dialogNavigationSettingsSubScreenLandscapeRows = dialogNavigationSettingsSubScreenLandscapeRows;
    }

    public int getDialogNavigationSettingsSubScreenLandscapeColumns() {
        return dialogNavigationSettingsSubScreenLandscapeColumns;
    }

    public void setDialogNavigationSettingsSubScreenLandscapeColumns(int dialogNavigationSettingsSubScreenLandscapeColumns) {
        this.dialogNavigationSettingsSubScreenLandscapeColumns = dialogNavigationSettingsSubScreenLandscapeColumns;
    }

    public int getSelectionOffsetX() {
        return selectionOffsetX;
    }

    public void setSelectionOffsetX(int selectionOffsetX) {
        this.selectionOffsetX = selectionOffsetX;
    }

    public int getDefaultFontSize() {
        return defaultFontSize;
    }

    public void setDefaultFontSize(int defaultFontSize) {
        this.defaultFontSize = defaultFontSize;
    }

    public int getSelectionMoveDistanceThreshold() {
        return selectionMoveDistanceThreshold;
    }

    public void setSelectionMoveDistanceThreshold(int selectionMoveDistanceThreshold) {
        this.selectionMoveDistanceThreshold = selectionMoveDistanceThreshold;
    }

    public int getGcInterval() {
        return gcInterval;
    }

    public void setGcInterval(int gcInterval) {
        this.gcInterval = gcInterval;
    }

    public String getDefaultAnnotationHighlightStyle() {
        return defaultAnnotationHighlightStyle;
    }

    public void setDefaultAnnotationHighlightStyle(String defaultAnnotationHighlightStyle) {
        this.defaultAnnotationHighlightStyle = defaultAnnotationHighlightStyle;
    }

    public boolean isSupportColor() {
        return supportColor;
    }

    public void setSupportColor(boolean supportColor) {
        this.supportColor = supportColor;
    }

    public boolean isAskForClose() {
        return askForClose;
    }

    public void setAskForClose(boolean askForClose) {
        this.askForClose = askForClose;
    }

    public int getDefaultGamma() {
        return defaultGamma;
    }

    public void setDefaultGamma(int defaultGamma) {
        this.defaultGamma = defaultGamma;
    }

    public int getFixedGamma() {
        return fixedGamma;
    }

    public void setFixedGamma(int fixedGamma) {
        this.fixedGamma = fixedGamma;
    }

    public boolean isExitAfterFinish() {
        return exitAfterFinish;
    }

    public void setExitAfterFinish(boolean exitAfterFinish) {
        this.exitAfterFinish = exitAfterFinish;
    }

    public int getSlideshowMinimumInterval() {
        return slideshowMinimumInterval;
    }

    public void setSlideshowMinimumInterval(int slideshowMinimumInterval) {
        this.slideshowMinimumInterval = slideshowMinimumInterval;
    }

    public int getSlideshowMaximumInterval() {
        return slideshowMaximumInterval;
    }

    public void setSlideshowMaximumInterval(int slideshowMaximumInterval) {
        this.slideshowMaximumInterval = slideshowMaximumInterval;
    }

    public int getDefaultSlideshowInterval() {
        return defaultSlideshowInterval;
    }

    public void setDefaultSlideshowInterval(int defaultSlideshowInterval) {
        this.defaultSlideshowInterval = defaultSlideshowInterval;
    }

    public int getSlideshowMinimumPages() {
        return slideshowMinimumPages;
    }

    public void setSlideshowMinimumPages(int slideshowMinimumPages) {
        this.slideshowMinimumPages = slideshowMinimumPages;
    }

    public int getSlideshowMaximumPages() {
        return slideshowMaximumPages;
    }

    public void setSlideshowMaximumPages(int slideshowMaximumPages) {
        this.slideshowMaximumPages = slideshowMaximumPages;
    }

    public int getDefaultSlideshowPages() {
        return defaultSlideshowPages;
    }

    public void setDefaultSlideshowPages(int defaultSlideshowPages) {
        this.defaultSlideshowPages = defaultSlideshowPages;
    }

    public int getFrontLight() {
        return frontLight;
    }

    public void setFrontLight(int frontLight) {
        this.frontLight = frontLight;
    }

    public SingletonSharedPreference.AnnotationHighlightStyle defaultAnnotationHighlightStyle() {
        SingletonSharedPreference.AnnotationHighlightStyle style = SingletonSharedPreference.AnnotationHighlightStyle.Underline;
        try {
            style = Enum.valueOf(SingletonSharedPreference.AnnotationHighlightStyle.class, defaultAnnotationHighlightStyle);
        } catch (Exception e) {
            Debug.w(getClass(), e);
        }
        return style;
    }
}

