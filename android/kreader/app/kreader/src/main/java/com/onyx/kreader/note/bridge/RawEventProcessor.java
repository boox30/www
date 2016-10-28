package com.onyx.kreader.note.bridge;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.onyx.android.sdk.common.request.SingleThreadExecutor;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.scribble.data.TouchPoint;
import com.onyx.android.sdk.scribble.data.TouchPointList;
import com.onyx.android.sdk.scribble.shape.Shape;
import com.onyx.kreader.note.NoteManager;
import com.onyx.kreader.utils.DeviceUtils;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;

/**
 * Created by zhuzeng on 9/19/16.
 */
public class RawEventProcessor extends NoteEventProcessorBase {


    private static final int EV_SYN = 0x00;
    private static final int EV_KEY = 0x01;
    private static final int EV_ABS = 0x03;

    private static final int ABS_X = 0x00;
    private static final int ABS_Y = 0x01;
    private static final int ABS_PRESSURE = 0x18;

    private static final int BTN_TOUCH = 0x14a;
    private static final int BTN_TOOL_PEN = 0x140;
    private static final int BTN_TOOL_RUBBER = 0x141;
    private static final int BTN_TOOL_PENCIL = 0x143;

    private static final int PEN_SIZE = 0;

    private String inputDevice = "/dev/input/event1";

    private volatile int px, py, pressure;
    private volatile boolean erasing = false;
    private volatile boolean forceDrawing = false;
    private volatile boolean forceErasing = false;
    private volatile boolean pressed = false;
    private volatile boolean lastPressed = false;
    private volatile boolean stop = false;
    private volatile boolean reportData = false;
    private volatile boolean supportScale = true;
    private volatile boolean enableEventProcessor = false;

    private volatile Matrix inputToScreenMatrix;
    private volatile Matrix screenToViewMatrix;
    private volatile float[] srcPoint = new float[2];
    private volatile float[] dstPoint = new float[2];
    private volatile TouchPointList touchPointList;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SingleThreadExecutor singleThreadExecutor;

    public RawEventProcessor(final NoteManager p) {
        super(p);
    }

    public void update(final Matrix screenMatrix, final Matrix viewMatrix, final Rect rect, final Rect excludeRect) {
        this.inputToScreenMatrix = screenMatrix;
        this.screenToViewMatrix = viewMatrix;
        setLimitRect(rect);
        setExcludeRect(excludeRect);
    }

    public void start() {
        stop = false;
        reportData = true;
        clearInternalState();
        submitJob();
    }

    public void resume() {
        clearInternalState();
        reportData = true;
    }

    public void pause() {
        clearInternalState();
        reportData = false;
    }

    public boolean isEnableEventProcessor() {
        return enableEventProcessor;
    }

    public void setEnableEventProcessor(boolean enableEventProcessor) {
        this.enableEventProcessor = enableEventProcessor;
    }

    public void quit() {
        reportData = false;
        stop = true;
        clearInternalState();
        shutdown();
    }

    private void clearInternalState() {
        pressed = false;
        forceDrawing = false;
        forceErasing = false;
        lastPressed = false;
    }

    private void shutdown() {
        if (singleThreadExecutor != null) {
            singleThreadExecutor.shutdown();
            singleThreadExecutor = null;
        }
    }

    private ExecutorService getSingleThreadPool()   {
        if (singleThreadExecutor == null) {
            singleThreadExecutor = new SingleThreadExecutor(Thread.MAX_PRIORITY);
        }
        return singleThreadExecutor.get();
    }

    private void submitJob() {
        getSingleThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    detectInputDevicePath();
                    readLoop();
                } catch (Exception e) {
                }
            }
        });
    }

    private void readLoop() throws Exception {
        DataInputStream in = new DataInputStream(new FileInputStream(inputDevice));
        byte[] data = new byte[16];
        while (!stop) {
            in.readFully(data);
            ByteBuffer wrapped = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            processInputEvent(wrapped.getLong(), wrapped.getShort(), wrapped.getShort(), wrapped.getInt());
        }
    }

    private void detectInputDevicePath() {
        inputDevice = DeviceUtils.detectInputDevicePath();
    }

    private void processInputEvent(long ts, int type, int code, int value) {
        if (type == EV_ABS) {
            if (code == ABS_X) {
                px = value;
            } else if (code == ABS_Y) {
                py = value;
            } else if (code == ABS_PRESSURE) {
                pressure = value;
            }
        } else if (type == EV_SYN) {
            if (pressed) {
                if (!lastPressed) {
                    pressReceived(px, py, pressure, PEN_SIZE, ts, erasing);
                    lastPressed = true;
                } else {
                    moveReceived(px, py, pressure, PEN_SIZE, ts, erasing);
                }
            } else if (lastPressed) {
                releaseReceived(px, py, pressure, PEN_SIZE, ts, erasing);
                lastPressed = false;
            }
        } else if (type == EV_KEY) {
            if (code ==  BTN_TOUCH)  {
                erasing = false;
                pressed = value > 0;
                lastPressed = value <= 0;
            } else if (code == BTN_TOOL_PENCIL || code == BTN_TOOL_PEN) {
                erasing = false;
                forceDrawing = true;
                forceErasing = false;
            } else if (code == BTN_TOOL_RUBBER) {
                pressed = value > 0;
                lastPressed = value <= 0;
                erasing = true;
                forceDrawing = false;
                forceErasing = true;
            }
        }
    }

    /**
     * Use screen matrix to map from touch device to screen with correct orientation.
     * Use view matrix to map from screen to view.
     * finally we getById points inside view. we may need the page matrix
     * to map points from view to page.
     * @param touchPoint
     * @return
     */
    private TouchPoint mapInputToScreenPoint(final TouchPoint touchPoint) {
        dstPoint[0] = touchPoint.x;
        dstPoint[1] = touchPoint.y;
        if (inputToScreenMatrix != null) {
            srcPoint[0] = touchPoint.x;
            srcPoint[1] = touchPoint.y;
            inputToScreenMatrix.mapPoints(dstPoint, srcPoint);
        }
        touchPoint.x = dstPoint[0];
        touchPoint.y = dstPoint[1];
        return touchPoint;
    }

    /**
     * map points from screen to view.
     * @param touchPoint
     * @return
     */
    private TouchPoint mapScreenPointToView(final TouchPoint touchPoint) {
        dstPoint[0] = touchPoint.x;
        dstPoint[1] = touchPoint.y;
        if (screenToViewMatrix != null) {
            srcPoint[0] = touchPoint.x;
            srcPoint[1] = touchPoint.y;
            screenToViewMatrix.mapPoints(dstPoint, srcPoint);
        }
        touchPoint.x = dstPoint[0];
        touchPoint.y = dstPoint[1];
        return touchPoint;
    }

    private boolean addToList(final TouchPoint touchPoint, boolean create) {
        if (touchPointList == null) {
            if (!create) {
                return false;
            }
            touchPointList = new TouchPointList(600);
        }

        if (!inLimitRect(touchPoint.x, touchPoint.y)) {
            return false;
        }

        if (inExcludeRect(touchPoint.x, touchPoint.y)) {
            return false;
        }

        if (touchPoint != null && touchPointList != null) {
            touchPointList.add(touchPoint);
        }
        return true;
    }

    private boolean isReportData() {
        if ((forceDrawing || forceErasing) && supportScale) {
            return true;
        }
        return reportData && enableEventProcessor;
    }

    private boolean inErasing() {
        return erasing || forceErasing;
    }

    private void pressReceived(int x, int y, int pressure, int size, long ts, boolean erasing) {
        if (!isReportData()) {
            return;
        }
        if (inErasing()) {
            erasingPressReceived(x, y, pressure, size, ts);
        } else {
            drawingPressReceived(x, y, pressure, size, ts);
        }
    }

    private void moveReceived(int x, int y, int pressure, int size, long ts, boolean erasing) {
        if (!isReportData()) {
            return;
        }
        if (inErasing()) {
            erasingMoveReceived(x, y, pressure, size, ts);
        } else {
            drawingMoveReceived(x, y, pressure, size, ts);
        }
    }

    private void releaseReceived(int x, int y, int pressure, int size, long ts, boolean erasing) {
        if (!isReportData()) {
            return;
        }
        if (inErasing()) {
            erasingReleaseReceived(x, y, pressure, size, ts);
        } else {
            drawingReleaseReceived(x, y, pressure, size, ts);
        }
    }

    private void erasingPressReceived(int x, int y, int pressure, int size, long ts) {
        invokeRawErasingStart();
        final PageInfo pageInfo = hitTest(x, y);
        if (pageInfo == null) {
            return;
        }
        final TouchPoint touchPoint = new TouchPoint(x, y, pressure, size, ts);
        mapScreenPointToView(touchPoint);
        touchPoint.normalize(pageInfo);
        addToList(touchPoint, true);
    }

    private void erasingMoveReceived(int x, int y, int pressure, int size, long ts) {
        final PageInfo pageInfo = hitTest(x, y);
        if (pageInfo == null) {
            return;
        }
        final TouchPoint touchPoint = new TouchPoint(x, y, pressure, size, ts);
        mapScreenPointToView(touchPoint);
        touchPoint.normalize(pageInfo);
        addToList(touchPoint, true);
    }

    private void erasingReleaseReceived(int x, int y, int pressure, int size, long ts) {
        forceDrawing = false;
        forceErasing = false;
        final PageInfo pageInfo = hitTest(x, y);
        if (pageInfo != null) {
            final TouchPoint touchPoint = new TouchPoint(x, y, pressure, size, ts);
            mapScreenPointToView(touchPoint);
            touchPoint.normalize(pageInfo);
            addToList(touchPoint, true);
        }
        invokeRawErasingFinish();
    }

    private void drawingPressReceived(int x, int y, int pressure, int size, long ts) {
        invokeDFBShapeStart();
        final TouchPoint touchPoint = new TouchPoint(x, y, pressure, size, ts);
        final TouchPoint screen = new TouchPoint(mapInputToScreenPoint(touchPoint));
        mapScreenPointToView(touchPoint);
        if (!checkTouchPoint(touchPoint, screen)) {
            return;
        }
        getNoteManager().collectPoint(getLastPageInfo(), touchPoint, screen, true, false);
    }

    private void drawingMoveReceived(int x, int y, int pressure, int size, long ts) {
        final TouchPoint touchPoint = new TouchPoint(x, y, pressure, size, ts);
        final TouchPoint screen = new TouchPoint(mapInputToScreenPoint(touchPoint));
        mapScreenPointToView(touchPoint);
        if (!checkTouchPoint(touchPoint, screen)) {
            return;
        }
        getNoteManager().collectPoint(getLastPageInfo(), touchPoint, screen, true, false);
    }

    private void drawingReleaseReceived(int x, int y, int pressure, int size, long ts) {
        final TouchPoint touchPoint = new TouchPoint(x, y, pressure, size, ts);
        final TouchPoint screen = new TouchPoint(mapInputToScreenPoint(touchPoint));
        mapScreenPointToView(touchPoint);
        if (!checkTouchPoint(touchPoint, screen)) {
            return;
        }
        finishCurrentShape(getLastPageInfo(), touchPoint, screen, false);
    }

    private boolean checkTouchPoint(final TouchPoint touchPoint, final TouchPoint screen) {
        if (hitTest(touchPoint.x, touchPoint.y) == null || !inLimitRect(touchPoint.x, touchPoint.y) || inExcludeRect(touchPoint.x, touchPoint.y)) {
            finishCurrentShape(getLastPageInfo(), touchPoint, screen, false);
            return false;
        }
        return true;
    }

    private void finishCurrentShape(final PageInfo pageInfo, final TouchPoint normal, final TouchPoint screen, boolean create) {
        final Shape shape = getNoteManager().getCurrentShape();
        resetLastPageInfo();
        invokeDFBShapeFinished(shape);
        getNoteManager().resetCurrentShape();
    }

    private void invokeRawErasingStart() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                getCallback().onRawErasingStart();
            }
        });
    }

    private void invokeRawErasingFinish() {
        final TouchPointList list = touchPointList;
        handler.post(new Runnable() {
            @Override
            public void run() {
                getCallback().onRawErasingFinished(list);
            }
        });
    }

    private void invokeDFBShapeStart() {
        final boolean shortcut = forceDrawing;
        handler.post(new Runnable() {
            @Override
            public void run() {
                getCallback().onDFBShapeStart(shortcut);
            }
        });
    }

    private void invokeDFBShapeFinished(final Shape shape) {
        final boolean shortcut = forceDrawing;
        forceDrawing = false;
        forceErasing = false;
        if (shape == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                getCallback().onDFBShapeFinished(shape, shortcut);
            }
        });
    }
}
