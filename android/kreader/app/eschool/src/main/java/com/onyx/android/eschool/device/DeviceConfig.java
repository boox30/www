package com.onyx.android.eschool.device;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.onyx.android.eschool.BuildConfig;
import com.onyx.android.sdk.data.GObject;
import com.onyx.android.sdk.utils.RawResourceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by suicheng on 2017/2/17.
 */
public class DeviceConfig {
    static private String TAG = DeviceConfig.class.getSimpleName();
    static public final boolean useDebugConfig = false;

    static private DeviceConfig globalInstance;
    static private Locale currentLocale = null;

    static private Map<String, String> defaultCustomizedIconAppsMap;

    private GObject backend;

    static public final String APP_FILTERS = "app_filters";
    static public final String TEST_APPS = "test_apps";
    static public final String CUSTOMIZED_ICON_APPS_MAPS = "customized_icon_apps_maps";

    static public final String VERIFY_DICTIONARY_TAG = "verify_dictionary";
    static public final String VERIFY_BOOKS_TAG = "verify_books";
    static public final String VERIFY_TTS_TAG = "verify_tts";

    static public DeviceConfig sharedInstance(Context context) {
        if (globalInstance == null) {
            globalInstance = new DeviceConfig(context);
        }
        currentLocale = context.getResources().getConfiguration().locale;
        return globalInstance;
    }

    static public void forceUpdate(Context context) {
        globalInstance = new DeviceConfig(context);
        currentLocale = context.getResources().getConfiguration().locale;
    }

    private DeviceConfig(Context context) {
        backend = objectFromDebugModel(context);
        if (backend != null) {
            Log.i(TAG, "Using debug model.");
            return;
        }

        backend = objectFromManufactureAndModel(context);
        if (backend != null) {
            Log.i(TAG, "Using manufacture model.");
            return;
        }

        backend = objectFromModel(context);
        if (backend != null) {
            Log.i(TAG, "Using device model.");
            return;
        }

        Log.i(TAG, "Using default model.");
        backend = objectFromDefaultOnyxConfig(context);
    }

    private GObject objectFromManufactureAndModel(Context context) {
        final String name = Build.MANUFACTURER + "_" + Build.MODEL;
        return objectFromRawResource(context, name);
    }

    private GObject objectFromDebugModel(Context context) {
        if (BuildConfig.DEBUG && useDebugConfig) {
            return objectFromRawResource(context, "debug");
        }
        return null;
    }

    private GObject objectFromModel(Context context) {
        final String name = Build.MODEL;
        return objectFromRawResource(context, name);
    }

    private GObject objectFromDefaultOnyxConfig(Context context) {
        return objectFromRawResource(context, "onyx");
    }

    private GObject objectFromRawResource(Context context, final String name) {
        GObject object = null;
        try {
            int res = context.getResources().getIdentifier(name.toLowerCase(), "raw", context.getPackageName());
            object = RawResourceUtil.objectFromRawResource(context, res);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return object;
        }
    }

    /**
     * apps should not displayed in application tab. like phone, contacts not suitable for eReader.
     *
     * @return
     */
    public List<String> getAppFilters() {
        if (backend.hasKey(APP_FILTERS)) {
            return backend.getList(APP_FILTERS);
        }
        return null;
    }

    /**
     * Test apps, once finished, it will never displayed in app tab.
     *
     * @return
     */
    public List<String> getTestApps() {
        if (backend.hasKey(TEST_APPS)) {
            return backend.getList(TEST_APPS);
        }
        return null;
    }

    /**
     * Customized Icon apps,e.g Google play.
     *
     * @return Customized Apps And Icon Maps.
     */
    public Map<String, String> getCustomizedIconApps() {
        if (defaultCustomizedIconAppsMap == null) {
            defaultCustomizedIconAppsMap = new HashMap<>();
            defaultCustomizedIconAppsMap.put("com.android.vending", "app_play");
            defaultCustomizedIconAppsMap.put("com.android.browser", "app_browser");
            defaultCustomizedIconAppsMap.put("com.android.calendar", "app_calendar");
            defaultCustomizedIconAppsMap.put("com.android.calculator2", "app_calculator");
            defaultCustomizedIconAppsMap.put("com.android.deskclock", "app_deskclock");
            defaultCustomizedIconAppsMap.put("com.onyx.android.dict", "app_dict");
            defaultCustomizedIconAppsMap.put("com.onyx.dict", "app_dict");
            defaultCustomizedIconAppsMap.put("com.android.providers.downloads.ui", "app_download");
            defaultCustomizedIconAppsMap.put("com.android.email", "app_email");
            defaultCustomizedIconAppsMap.put("com.android.gallery", "app_gallery");
            defaultCustomizedIconAppsMap.put("com.android.music", "app_music");
            defaultCustomizedIconAppsMap.put("com.onyx.android.scribbler", "app_scribbler");
            defaultCustomizedIconAppsMap.put("com.neverland.oreader", "app_oreader");
            defaultCustomizedIconAppsMap.put("com.android.quicksearchbox", "app_search");
            defaultCustomizedIconAppsMap.put("com.android.settings", "app_setting");
            defaultCustomizedIconAppsMap.put("com.android.soundrecorder", "app_recorder");
            defaultCustomizedIconAppsMap.put("com.google.android.gms", "app_google_setting");
            defaultCustomizedIconAppsMap.put("com.onyx.android.note", "app_note");
        }
        if (backend.hasKey(CUSTOMIZED_ICON_APPS_MAPS)) {
            defaultCustomizedIconAppsMap.putAll((Map<String, String>) (backend.getObject(CUSTOMIZED_ICON_APPS_MAPS)));
        }
        return defaultCustomizedIconAppsMap;
    }
}
