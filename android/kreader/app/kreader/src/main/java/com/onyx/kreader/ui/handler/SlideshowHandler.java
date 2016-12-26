package com.onyx.kreader.ui.handler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.common.request.WakeLockHolder;
import com.onyx.kreader.common.Debug;
import com.onyx.kreader.ui.actions.GotoPageAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.dialog.DialogSlideshowStatistic;
import com.onyx.kreader.utils.DeviceUtils;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by joy on 7/29/16.
 */
public class SlideshowHandler extends BaseHandler {

    private static Intent intent = new Intent(SlideshowHandler.class.getCanonicalName());
    private static final String TAG = SlideshowHandler.class.getSimpleName();
    private boolean activated;
    private ReaderDataHolder readerDataHolder;
    private int maxPageCount;
    private int pageCount;
    private int startBatteryPercent;
    private Calendar startTime;
    private int intervalInSeconds = 3;
    private WakeLockHolder wakeLockHolder = new WakeLockHolder();
    private PendingIntent pendingIntent;

    private BaseCallback pageLimitCallback = new BaseCallback() {
        @Override
        public void done(BaseRequest request, Throwable e) {
            if (pageCount >= maxPageCount) {
                quit();
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            wakeLockHolder.acquireWakeLockWithTimeout(context, TAG, 1000);
            Debug.d(getClass(), "onReceive: " + intent.getAction());
            if (!activated) {
                return;
            }
            loopNextScreen();
            setAlarm();
        }
    };

    public SlideshowHandler(HandlerManager parent) {
        super(parent);
        readerDataHolder = getParent().getReaderDataHolder();
        pendingIntent = PendingIntent.getBroadcast(readerDataHolder.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onActivate(ReaderDataHolder readerDataHolder) {
        activated = true;
        readerDataHolder.getContext().registerReceiver(broadcastReceiver, new IntentFilter(intent.getAction()));
    }

    @Override
    public void onDeactivate(ReaderDataHolder readerDataHolder) {
        activated = false;
        readerDataHolder.getContext().unregisterReceiver(broadcastReceiver);
        AlarmManager am = (AlarmManager)readerDataHolder.getContext().getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.cancel(pendingIntent);
        }
        wakeLockHolder.releaseWakeLock();
    }

    @Override
    public boolean onKeyDown(ReaderDataHolder readerDataHolder, int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    public boolean onKeyUp(ReaderDataHolder readerDataHolder, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                quit();
                return true;
            default:
                Toast.makeText(readerDataHolder.getContext(), "Press back to quit slideshow mode!", Toast.LENGTH_SHORT).show();
                return true;
        }
    }

    @Override
    public boolean onTouchEvent(ReaderDataHolder readerDataHolder, MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDown(ReaderDataHolder readerDataHolder, MotionEvent e) {
        Toast.makeText(readerDataHolder.getContext(), "Press back to quit slideshow mode!", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onActionUp(ReaderDataHolder readerDataHolder, float startX, float startY, float endX, float endY) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(ReaderDataHolder readerDataHolder, MotionEvent e) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(ReaderDataHolder readerDataHolder, MotionEvent e) {
        return true;
    }

    @Override
    public void onLongPress(ReaderDataHolder readerDataHolder, float x1, float y1, float x2, float y2) {
        return;
    }

    @Override
    public boolean onScaleBegin(ReaderDataHolder readerDataHolder, ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public boolean onScale(ReaderDataHolder readerDataHolder, ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public boolean onScaleEnd(ReaderDataHolder readerDataHolder, ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public boolean onFling(ReaderDataHolder readerDataHolder, MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    @Override
    public boolean onScroll(ReaderDataHolder readerDataHolder, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public boolean onScrollAfterLongPress(ReaderDataHolder readerDataHolder, float x1, float y1, float x2, float y2) {
        return true;
    }

    public void setInterval(int seconds) {
        intervalInSeconds = seconds;
    }

    public void start(int maxPageCount) {
        this.maxPageCount = maxPageCount;
        pageCount = 0;
        startBatteryPercent = DeviceUtils.getBatteryPecentLevel(readerDataHolder.getContext());
        startTime = Calendar.getInstance();
        setAlarm();
    }

    private void setAlarm() {
        AlarmManager am = (AlarmManager)readerDataHolder.getContext().getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + intervalInSeconds * 1000,
                    pendingIntent);
        }
    }

    private void loopNextScreen() {
        pageCount++;
        if (readerDataHolder.getReaderViewInfo().canNextScreen) {
            nextScreen(readerDataHolder, pageLimitCallback);
        } else {
            new GotoPageAction(0).execute(readerDataHolder, pageLimitCallback);
        }
    }

    private void quit() {
        readerDataHolder.getHandlerManager().setActiveProvider(HandlerManager.READING_PROVIDER);
        showStatisticDialog();
    }

    private void showStatisticDialog() {
        Calendar endTime = Calendar.getInstance();
        int endBatteryPercent = DeviceUtils.getBatteryPecentLevel(readerDataHolder.getContext());
        new DialogSlideshowStatistic(readerDataHolder.getContext(), startTime, endTime,
                pageCount, startBatteryPercent, endBatteryPercent).show();
    }
}
