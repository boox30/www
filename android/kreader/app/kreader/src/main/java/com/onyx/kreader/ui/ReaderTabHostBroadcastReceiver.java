package com.onyx.kreader.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;

import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.ViewDocumentUtils;

/**
 * Created by joy on 2/23/17.
 */

public class ReaderTabHostBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_TAB_BACK_PRESSED = "com.onyx.kreader.action.TAB_BACK_PRESSED";
    public static final String ACTION_CHANGE_SCREEN_ORIENTATION = "com.onyx.kreader.action.CHANGE_SCREEN_ORIENTATION";
    public static final String ACTION_ENTER_FULL_SCREEN = "com.onyx.kreader.action.ENTER_FULL_SCREEN";
    public static final String ACTION_QUIT_FULL_SCREEN = "com.onyx.kreader.action.QUIT_FULL_SCREEN";

    public static final String TAG_SCREEN_ORIENTATION = "com.onyx.kreader.action.SCREEN_ORIENTATION";

    public static abstract class Callback {
        public abstract void onTabBackPressed();
        public abstract void onChangeOrientation(int orientation);
        public abstract void onEnterFullScreen();
        public abstract void onQuitFullScreen();
        public abstract void onEnableDebugLog();
        public abstract void onDisableDebugLog();
    }

    private static Callback callback;

    public static void setCallback(Callback callback) {
        ReaderTabHostBroadcastReceiver.callback = callback;
    }

    public static void sendTabBackPressedIntent(Context context) {
        Intent intent = new Intent(context, ReaderTabHostBroadcastReceiver.class);
        intent.setAction(ACTION_TAB_BACK_PRESSED);
        context.sendBroadcast(intent);
    }

    public static void sendChangeOrientationIntent(Context context, int orientation) {
        Intent intent = new Intent(context, ReaderTabHostBroadcastReceiver.class);
        intent.setAction(ACTION_CHANGE_SCREEN_ORIENTATION);
        intent.putExtra(TAG_SCREEN_ORIENTATION, orientation);
        context.sendBroadcast(intent);
    }

    public static void sendEnterFullScreenIntent(Context context) {
        Intent intent = new Intent(context, ReaderTabHostBroadcastReceiver.class);
        intent.setAction(ACTION_ENTER_FULL_SCREEN);
        context.sendBroadcast(intent);
    }

    public static void sendQuitFullScreenIntent(Context context) {
        Intent intent = new Intent(context, ReaderTabHostBroadcastReceiver.class);
        intent.setAction(ACTION_QUIT_FULL_SCREEN);
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getSimpleName(), "onReceive: " + intent);
        if (intent.getAction().equals(ACTION_TAB_BACK_PRESSED)) {
            if (callback != null) {
                callback.onTabBackPressed();
            }
        } else if (intent.getAction().equals(ACTION_CHANGE_SCREEN_ORIENTATION)) {
            if (callback != null) {
                callback.onChangeOrientation(intent.getIntExtra(TAG_SCREEN_ORIENTATION,
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
            }
        } else if (intent.getAction().equals(ACTION_ENTER_FULL_SCREEN)) {
            if (callback != null) {
                callback.onEnterFullScreen();
            }
        } else if (intent.getAction().equals(ACTION_QUIT_FULL_SCREEN)) {
            if (callback != null) {
                callback.onQuitFullScreen();
            }
        } else if (intent.getAction().equals(ViewDocumentUtils.ACTION_ENABLE_READER_DEBUG_LOG)) {
            ReaderTabHostActivity.setEnableDebugLog(true);
            if (callback != null) {
                callback.onEnableDebugLog();
            }
        } else if (intent.getAction().equals(ViewDocumentUtils.ACTION_DISABLE_READER_DEBUG_LOG)) {
            ReaderTabHostActivity.setEnableDebugLog(false);
            if (callback != null) {
                callback.onDisableDebugLog();
            }
        }
    }
}
