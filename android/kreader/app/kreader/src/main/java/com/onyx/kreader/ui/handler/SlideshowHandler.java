package com.onyx.kreader.ui.handler;

import android.app.AlarmManager;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.common.request.WakeLockHolder;
import com.onyx.kreader.R;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.kreader.device.ReaderDeviceManager;
import com.onyx.kreader.ui.actions.GotoPageAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.data.SingletonSharedPreference;
import com.onyx.kreader.ui.dialog.DialogSlideshowStatistic;
import com.onyx.kreader.ui.events.UpdateSlideshowEvent;
import com.onyx.kreader.ui.view.SlideshowStatusBar;
import com.onyx.android.sdk.utils.DeviceUtils;

import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by joy on 7/29/16.
 */
public class SlideshowHandler extends BaseHandler {

    private static String slideShowAction = SlideshowHandler.class.getCanonicalName();
    private static Intent intent = new Intent(slideShowAction);
    private static final String TAG = SlideshowHandler.class.getSimpleName();
    private boolean activated;
    private ReaderDataHolder readerDataHolder;
    private RelativeLayout parent;
    private SlideshowStatusBar slideshowStatusBar;
    private int maxPageCount;
    private int pageCount;
    private int startBatteryPercent;
    private Calendar startTime;
    private int intervalInSeconds = 3;
    private WakeLockHolder wakeLockHolder = new WakeLockHolder();
    private PendingIntent pendingIntent;
    private boolean screenOff = false;

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
            Debug.d(getClass(), "onReceive: " + intent.getAction());
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                screenOff = true;
            } else if (action.equals(Intent.ACTION_SCREEN_ON)){
                screenOff = false;
                setAlarm();
            } else if (action.equals(slideShowAction)) {
                nextScreen(context);
            }
        }
    };

    private void nextScreen(Context context) {
        if (!screenOff) {
            wakeLockHolder.acquireWakeLock(context, WakeLockHolder.FULL_FLAGS, TAG, 1000);
            if (!activated) {
                return;
            }
            loopNextScreen();
            setAlarm();
            //just keep screen on
            sendKeyCode(KeyEvent.KEYCODE_0);
        }
    }

    private void sendKeyCode(final int keyCode) {
        new Thread () {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    Log.e(TAG, "send keycode error!");
                }
            }
        }.start();
    }

    public static HandlerInitialState createInitialState(RelativeLayout parentLayout, int maxPageCount, int intervalInSeconds) {
        HandlerInitialState state = new HandlerInitialState();
        state.slideShowParentLayout = parentLayout;
        state.slideShowMaxPageCount = maxPageCount;
        state.slideShowIntervalInSeconds = intervalInSeconds;
        return state;
    }

    public SlideshowHandler(HandlerManager parent) {
        super(parent);
        readerDataHolder = getParent().getReaderDataHolder();
        pendingIntent = PendingIntent.getBroadcast(readerDataHolder.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onActivate(ReaderDataHolder readerDataHolder, final HandlerInitialState initialState) {
        if (initialState == null) {
            throw new IllegalArgumentException();
        }

        parent = initialState.slideShowParentLayout;
        maxPageCount = initialState.slideShowMaxPageCount;
        intervalInSeconds = initialState.slideShowIntervalInSeconds;

        activated = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(intent.getAction());
        readerDataHolder.getContext().registerReceiver(broadcastReceiver, filter);
        readerDataHolder.getEventBus().register(this);

        startSlideShow();
    }

    @Override
    public void onDeactivate(ReaderDataHolder readerDataHolder) {
        activated = false;
        readerDataHolder.getContext().unregisterReceiver(broadcastReceiver);
        readerDataHolder.getEventBus().unregister(this);
        AlarmManager am = (AlarmManager)readerDataHolder.getContext().getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.cancel(pendingIntent);
        }
        if (getSlideshowStatusBar().getVisibility() != View.GONE) {
            getSlideshowStatusBar().setVisibility(View.GONE);
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
            case KeyEvent.KEYCODE_0:
                return true;
            case KeyEvent.KEYCODE_BACK:
                quit();
                return true;
            default:
                Toast.makeText(readerDataHolder.getContext(),
                        readerDataHolder.getContext().getString(R.string.slideshow_quit_warning),
                        Toast.LENGTH_SHORT).show();
                return true;
        }
    }

    @Override
    public boolean onTouchEvent(ReaderDataHolder readerDataHolder, MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDown(ReaderDataHolder readerDataHolder, MotionEvent e) {
        Toast.makeText(readerDataHolder.getContext(),
                readerDataHolder.getContext().getString(R.string.slideshow_quit_warning),
                Toast.LENGTH_SHORT).show();
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

    public void startSlideShow() {
        pageCount = 0;
        startBatteryPercent = DeviceUtils.getBatteryPecentLevel(readerDataHolder.getContext());
        startTime = Calendar.getInstance();
        setAlarm();
        updateSlideshowStatusBar(null);
    }

    @Subscribe
    public void updateSlideshowStatusBar(UpdateSlideshowEvent event) {
        if (!SingletonSharedPreference.isReaderStatusBarEnabled(readerDataHolder.getContext())) {
            int battery = DeviceUtils.getBatteryPecentLevel(readerDataHolder.getContext());
            getSlideshowStatusBar().updateValue(maxPageCount, pageCount + 1,
                    startBatteryPercent, battery);
            getSlideshowStatusBar().setVisibility(View.VISIBLE);
        } else {
            hideSlideshowStatusBar();
        }
    }

    private void setAlarm() {
        AlarmManager am = (AlarmManager)readerDataHolder.getContext().getSystemService(ALARM_SERVICE);
        if (am != null && !screenOff) {
            am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + intervalInSeconds * 1000,
                    pendingIntent);
        }
    }

    private void loopNextScreen() {
        ReaderDeviceManager.holdDisplayUpdate(readerDataHolder.getContext(), getSlideshowStatusBar());
        pageCount++;
        if (readerDataHolder.getReaderViewInfo().canNextScreen) {
            nextScreen(readerDataHolder, pageLimitCallback);
        } else {
            new GotoPageAction(0).execute(readerDataHolder, pageLimitCallback);
        }
    }

    private void quit() {
        readerDataHolder.getHandlerManager().setActiveProvider(HandlerManager.READING_PROVIDER);
        hideSlideshowStatusBar();
        showStatisticDialog();
    }

    private void hideSlideshowStatusBar() {
        getSlideshowStatusBar().setVisibility(View.GONE);
    }

    private SlideshowStatusBar getSlideshowStatusBar() {
        if (slideshowStatusBar == null) {
            slideshowStatusBar = new SlideshowStatusBar(readerDataHolder.getContext(), parent);
        }
        return slideshowStatusBar;
    }

    private void showStatisticDialog() {
        Calendar endTime = Calendar.getInstance();
        int endBatteryPercent = DeviceUtils.getBatteryPecentLevel(readerDataHolder.getContext());
        DialogSlideshowStatistic dlg = new DialogSlideshowStatistic(readerDataHolder.getContext(), startTime, endTime,
                pageCount, startBatteryPercent, endBatteryPercent);
        readerDataHolder.trackDialog(dlg);
        dlg.show();
    }
}
