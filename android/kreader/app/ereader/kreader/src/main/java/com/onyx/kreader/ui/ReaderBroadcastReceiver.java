package com.onyx.kreader.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import com.onyx.android.sdk.utils.Debug;
import com.onyx.kreader.ui.events.CloseScribbleMenuEvent;
import com.onyx.kreader.ui.events.ForceCloseEvent;
import com.onyx.kreader.ui.events.GotoPageLinkEvent;
import com.onyx.kreader.ui.events.MoveTaskToBackEvent;
import com.onyx.kreader.ui.events.ResizeReaderWindowEvent;
import com.onyx.kreader.ui.events.DocumentActivatedEvent;
import com.onyx.kreader.ui.events.ShowTabHostMenuDialogEvent;
import com.onyx.kreader.ui.events.StopNoteEvent;
import com.onyx.kreader.ui.events.UpdateTabWidgetVisibilityEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by joy on 2/23/17.
 */

public class ReaderBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_START_READER = "com.onyx.kreader.action.START_READER";
    public static final String ACTION_CLOSE_READER = "com.onyx.kreader.action.CLOSE_READER";
    public static final String ACTION_STOP_NOTE = "com.onyx.kreader.action.QUIT_SCRIBBLE_MODE";
    public static final String ACTION_MOVE_TASK_TO_BACK = "com.onyx.kreader.action.MOVE_TASK_TO_BACK";
    public static final String ACTION_RESIZE_WINDOW = "com.onyx.kreader.action.RESIZE_WINDOW";
    public static final String ACTION_DOCUMENT_ACTIVATED = "com.onyx.kreader.action.DOCUMENT_ACTIVATED";
    public static final String ACTION_UPDATE_TAB_WIDGET_VISIBILITY = "com.onyx.kreader.action.UPDATE_TAB_WIDGET_VISIBILITY";
    public static final String ACTION_GOTO_PAGE_LINK = "com.onyx.kreader.action.GOTO_PAGE_LINK";
    public static final String ACTION_SHOW_TAB_HOST_MENU_DIALOG = "com.onyx.kreader.action.SHOW_TAB_HOST_MENU_DIALOG";
    public static final String ACTION_ENABLE_DEBUG_LOG = "com.onyx.kreader.action.ENABLE_DEBUG_LOG";
    public static final String ACTION_DISABLE_DEBUG_LOG = "com.onyx.kreader.action.DISABLE_DEBUG_LOG";

    public static final String TAG_TARGET_ACTIVITY = "com.onyx.kreader.TARGET_ACTIVITY";
    public static final String TAG_WINDOW_GRAVITY = "com.onyx.kreader.WINDOW_GRAVITY";
    public static final String TAG_WINDOW_WIDTH = "com.onyx.kreader.WINDOW_WIDTH";
    public static final String TAG_WINDOW_HEIGHT = "com.onyx.kreader.WINDOW_HEIGHT";
    public static final String TAG_WINDOW_LEFT = "com.onyx.kreader.WINDOW_LEFT";
    public static final String TAG_WINDOW_TOP = "com.onyx.kreader.WINDOW_TOP";
    public static final String TAG_DOCUMENT_PATH = "com.onyx.kreader.DOCUMENT_PATH";
    public static final String TAG_TAB_WIDGET_VISIBLE = "com.onyx.kreader.TAB_WIDGET_VISIBLE";
    public static final String TAG_PAGE_LINK = "com.onyx.kreader.PAGE_LINK";
    public static final String TAG_ENABLE_DEBUG = "com.onyx.kreader.ENABLE_DEBUG";
    public static final String TAG_SIDE_NOTE_MODE = "com.onyx.kreader.SIDE_NOTE_MODE";
    public static final String TAG_SIDE_READING_MODE = "com.onyx.kreader.SIDE_READING_MODE";

    private static EventBus eventBus;

    public static void setEventBus(EventBus eventBus) {
        ReaderBroadcastReceiver.eventBus = eventBus;
    }

    public static void sendStartReaderIntent(Context context, Intent intent, Class receiverClazz, Class activityClazz) {
        intent.setComponent(new ComponentName(context, receiverClazz));
        intent.setAction(ACTION_START_READER);
        intent.putExtra(TAG_TARGET_ACTIVITY, activityClazz.getCanonicalName());
        sendBroadcast(context, intent);
    }

    public static void sendCloseReaderIntent(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_CLOSE_READER);
        sendBroadcast(context, intent);
    }

    public static void sendStopNoteIntent(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_STOP_NOTE);
        sendBroadcast(context, intent);
    }

    public static void sendMoveTaskToBackIntent(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_MOVE_TASK_TO_BACK);
        sendBroadcast(context, intent);
    }

    public static void sendResizeReaderWindowIntent(Context context, Class clazz, int gravity, int width, int height) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_RESIZE_WINDOW);
        intent.putExtra(TAG_WINDOW_GRAVITY, gravity);
        intent.putExtra(TAG_WINDOW_WIDTH, width);
        intent.putExtra(TAG_WINDOW_HEIGHT, height);
        sendBroadcast(context, intent);
    }

    public static void sendDocumentActivatedIntent(Context context, Class clazz, String path) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_DOCUMENT_ACTIVATED);
        intent.putExtra(TAG_DOCUMENT_PATH, path);
        sendBroadcast(context, intent);
    }

    public static void sendUpdateTabWidgetVisibilityIntent(Context context, Class clazz, boolean visible) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_UPDATE_TAB_WIDGET_VISIBILITY);
        intent.putExtra(TAG_TAB_WIDGET_VISIBLE, visible);
        sendBroadcast(context, intent);
    }

    public static void sendGotoPageLinkIntent(Context context, Class clazz, String link) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_GOTO_PAGE_LINK);
        intent.putExtra(TAG_PAGE_LINK, link);
        sendBroadcast(context, intent);
    }

    public static void sendShowTabHostMenuDialogIntent(Context context, Class clazz, int dialogWindowLeft, int dialogWindowTop) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_SHOW_TAB_HOST_MENU_DIALOG);
        intent.putExtra(TAG_WINDOW_LEFT, dialogWindowLeft);
        intent.putExtra(TAG_WINDOW_TOP, dialogWindowTop);
        sendBroadcast(context, intent);
    }

    public static void sendEnableDebugLogIntent(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_ENABLE_DEBUG_LOG);
        sendBroadcast(context, intent);
    }

    public static void sendDisableDebugLogIntent(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_DISABLE_DEBUG_LOG);
        sendBroadcast(context, intent);
    }

    private static void sendBroadcast(Context context, Intent intent) {
        Debug.d(ReaderBroadcastReceiver.class, "sendBroadcast: " + intent);
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Debug.d(getClass(), "onReceive: " + intent);
        if (intent.getAction().equals(ACTION_START_READER)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setComponent(new ComponentName(context, intent.getStringExtra(TAG_TARGET_ACTIVITY)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }
        if (eventBus == null) {
            Debug.d(getClass(), "null bus");
            return;
        }
        if (intent.getAction().equals(ACTION_CLOSE_READER)) {
            eventBus.post(new ForceCloseEvent());
        } else if (intent.getAction().equals(ACTION_STOP_NOTE)) {
            eventBus.post(new StopNoteEvent());
        } else if (intent.getAction().equals(ACTION_MOVE_TASK_TO_BACK)) {
            eventBus.post(new MoveTaskToBackEvent());
        } else if (intent.getAction().equals(ACTION_RESIZE_WINDOW)) {
            eventBus.post(new ResizeReaderWindowEvent(
                    intent.getIntExtra(TAG_WINDOW_GRAVITY, Gravity.BOTTOM),
                    intent.getIntExtra(TAG_WINDOW_WIDTH, WindowManager.LayoutParams.MATCH_PARENT),
                    intent.getIntExtra(TAG_WINDOW_HEIGHT, WindowManager.LayoutParams.MATCH_PARENT)));
        } else if (intent.getAction().equals(ACTION_DOCUMENT_ACTIVATED)) {
            eventBus.post(new DocumentActivatedEvent(intent.getStringExtra(TAG_DOCUMENT_PATH)));
        } else if (intent.getAction().equals(ACTION_UPDATE_TAB_WIDGET_VISIBILITY)) {
            boolean visible = intent.getBooleanExtra(ReaderBroadcastReceiver.TAG_TAB_WIDGET_VISIBLE, true);
            eventBus.post(new UpdateTabWidgetVisibilityEvent(visible));
        } else if (intent.getAction().equals(ACTION_GOTO_PAGE_LINK)) {
            String link = intent.getStringExtra(ReaderBroadcastReceiver.TAG_PAGE_LINK);
            eventBus.post(new GotoPageLinkEvent(link));
        } else if (intent.getAction().equals(ACTION_SHOW_TAB_HOST_MENU_DIALOG)) {
            eventBus.post(new ShowTabHostMenuDialogEvent(intent.getIntExtra(TAG_WINDOW_LEFT, 0),
                    intent.getIntExtra(TAG_WINDOW_TOP, 0)));
        } else if (intent.getAction().equals(ACTION_ENABLE_DEBUG_LOG)) {
            Debug.setDebug(true);
        } else if (intent.getAction().equals(ACTION_DISABLE_DEBUG_LOG)) {
            Debug.setDebug(false);
        }
    }
}
