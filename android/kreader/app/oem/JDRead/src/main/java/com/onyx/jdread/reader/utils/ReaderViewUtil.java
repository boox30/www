package com.onyx.jdread.reader.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceView;
import android.view.View;

import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;

public class ReaderViewUtil {
    private static final String TAG = ReaderViewUtil.class.getSimpleName();
    private static boolean mIsFullUpdate = false;

    public static void clearSurfaceView(SurfaceView surfaceView) {
        Rect rect = getViewportSize(surfaceView);
        Canvas canvas = surfaceView.getHolder().lockCanvas(rect);
        if (canvas == null) {
            return;
        }

        Paint paint = new Paint();
        resetViewPortBackground(canvas, paint, rect);
        surfaceView.getHolder().unlockCanvasAndPost(canvas);
    }

    private static Canvas getCanvasForDraw(SurfaceView surfaceView, Rect rect) {
        if (mIsFullUpdate) {
            EpdController.setViewDefaultUpdateMode(surfaceView, UpdateMode.GC);
        } else {
            EpdController.resetUpdateMode(surfaceView);
        }
        return surfaceView.getHolder().lockCanvas(rect);
    }

    private static void resetViewPortBackground(final Canvas canvas, final Paint paint, final Rect rect) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(rect, paint);
    }

    private static Rect getViewportSize(View view) {
        return new Rect(0, 0, view.getWidth(), view.getHeight());
    }

    public static void setFullUpdate(boolean isFullUpdate) {
        mIsFullUpdate = isFullUpdate;
    }

    private static void resetFullUpdate() {
        mIsFullUpdate = false;
    }

    private static void unlockDrawingCanvas(SurfaceView surfaceView, Canvas canvas) {
        surfaceView.getHolder().unlockCanvasAndPost(canvas);
        mIsFullUpdate = false;
    }

//    //TODO:use drawPage Action to draw in worker thread.direct call will work in UI thread.
//    public static void drawPage(SurfaceView surfaceView, Bitmap viewBitmap, List<Shape> stashShapeList) {
//        if (surfaceView == null || !surfaceView.getHolder().getSurface().isValid()) {
//            Log.e(TAG, "surfaceView is not valid");
//            return;
//        }
//        Rect rect = getViewportSize(surfaceView);
//        Canvas canvas = getCanvasForDraw(surfaceView, rect);
//        if (canvas == null) {
//            resetFullUpdate();
//            return;
//        }
//
//        Paint paint = new Paint();
//        resetViewPortBackground(canvas, paint, rect);
//        if (viewBitmap != null) {
//            canvas.drawBitmap(viewBitmap, 0, 0, paint);
//        }
//        RenderContext renderContext = RenderContext.create(canvas, paint, null);
//        for (Shape shape : stashShapeList) {
//            shape.render(renderContext);
//        }
//        unlockDrawingCanvas(surfaceView, canvas);
//    }
}
