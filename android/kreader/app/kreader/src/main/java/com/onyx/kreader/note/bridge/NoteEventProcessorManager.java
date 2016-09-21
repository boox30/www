package com.onyx.kreader.note.bridge;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.scribble.math.OnyxMatrix;
import com.onyx.android.sdk.scribble.utils.DeviceConfig;
import com.onyx.kreader.note.NoteManager;

/**
 * Created by zhuzeng on 9/18/16.
 * Receive events from touch event or input event reader and send render command to screen
 */
public class NoteEventProcessorManager {

    private volatile View view;
    private TouchEventProcessor touchEventProcessor;
    private RawEventProcessor rawEventProcessor;
    private NoteManager noteManager;

    public NoteEventProcessorManager(final NoteManager p) {
        noteManager = p;
    }

    public View getView() {
        return view;
    }

    public void quit() {
        if (rawEventProcessor != null) {
            rawEventProcessor.quit();
        }
    }

    public void update(final View targetView, final DeviceConfig noteConfig) {
        view = targetView;
        OnyxMatrix viewMatrix = new OnyxMatrix();
        viewMatrix.postRotate(noteConfig.getViewPostOrientation());
        viewMatrix.postTranslate(noteConfig.getViewPostTx(), noteConfig.getViewPostTy());
        getTouchEventProcessor().update(targetView, getViewToEpdMatrix(noteConfig));
        getRawEventProcessor().update(getTouchToScreenMatrix(noteConfig), getScreenToViewMatrix(noteConfig));
    }

    private OnyxMatrix getViewToEpdMatrix(final DeviceConfig noteConfig) {
        OnyxMatrix viewMatrix = new OnyxMatrix();
        viewMatrix.postRotate(noteConfig.getViewPostOrientation());
        viewMatrix.postTranslate(noteConfig.getViewPostTx(), noteConfig.getViewPostTy());
        return viewMatrix;
    }

    private Matrix getTouchToScreenMatrix(final DeviceConfig noteConfig) {
        final Matrix screenMatrix = new Matrix();
        screenMatrix.preScale(noteConfig.getEpdWidth() / getTouchWidth(noteConfig),
                noteConfig.getEpdHeight() / getTouchHeight(noteConfig));
        return screenMatrix;
    }

    private Matrix getScreenToViewMatrix(final DeviceConfig noteConfig) {
        if (!noteConfig.useRawInput()) {
            return null;
        }

        int viewPosition[] = {0, 0};
        view.getLocationOnScreen(viewPosition);
        final Matrix viewMatrix = new Matrix();
        viewMatrix.postRotate(noteConfig.getEpdPostOrientation());
        viewMatrix.postTranslate(noteConfig.getEpdPostTx() - viewPosition[0], noteConfig.getEpdPostTy() - viewPosition[1]);
        return viewMatrix;
    }

    private float getTouchWidth(final DeviceConfig noteConfig) {
        float value = Device.currentDevice().getTouchWidth();
        if (value <= 0) {
            return noteConfig.getTouchWidth();
        }
        return value;
    }

    private float getTouchHeight(final DeviceConfig noteConfig) {
        float value = Device.currentDevice().getTouchHeight();
        if (value <= 0) {
            return noteConfig.getTouchHeight();
        }
        return value;
    }

    private TouchEventProcessor getTouchEventProcessor() {
        if (touchEventProcessor == null) {
            touchEventProcessor = new TouchEventProcessor(noteManager);
        }
        return touchEventProcessor;
    }

    private RawEventProcessor getRawEventProcessor() {
        if (rawEventProcessor == null) {
            rawEventProcessor = new RawEventProcessor(noteManager);
        }
        return rawEventProcessor;
    }

    public boolean onTouchEvent(final MotionEvent motionEvent) {
        return onTouchEventDrawing(motionEvent);
    }

    public boolean onTouchEventDrawing(final MotionEvent motionEvent) {
        return getTouchEventProcessor().onTouchEventDrawing(motionEvent);
    }

    public boolean onTouchEventErasing(final MotionEvent motionEvent) {
        return getTouchEventProcessor().onTouchEventErasing(motionEvent);
    }

}
