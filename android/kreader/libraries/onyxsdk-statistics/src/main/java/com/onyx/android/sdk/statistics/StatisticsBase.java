package com.onyx.android.sdk.statistics;

import android.content.Context;
import android.os.Build;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuzeng on 08/01/2017.
 */

public interface StatisticsBase {

    static final String KEY_TAG = "key";
    static final String CHANNEL_TAG = "channel";

    boolean init(final Context context, final Map<String, String> args);

    void onActivityResume(final Context context);

    void onActivityPause(final Context context);

    void onDocumentOpenedEvent(final Context context, final String path, final String md5);

    void onDocumentClosed(final Context context);

    void onPageChangedEvent(final Context context, final String last, final String current, int duration);

    void onTextSelectedEvent(final Context context, final String text);

    void onAddAnnotationEvent(final Context context, final String originText, final String userNote);

    void onDictionaryLookupEvent(final Context context, final String originText);

}
