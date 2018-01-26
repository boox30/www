package com.onyx.android.sdk.scribble.request.shape;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceView;

import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.data.TouchPointList;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;
import com.onyx.android.sdk.scribble.shape.Shape;

import java.util.List;

/**
 * Created by zhuzeng on 7/3/16.
 */
public class ShapeRemoveByPointListRequest extends BaseNoteRequest {

    private volatile TouchPointList touchPointList;
    private volatile List<Shape> stash = null;
    private volatile SurfaceView surfaceView;

    public ShapeRemoveByPointListRequest(final TouchPointList list, final List<Shape> s, final SurfaceView view) {
        touchPointList = list;
        stash = s;
        surfaceView = view;
        setPauseInputProcessor(true);
        setRender(true);
    }

    public void execute(final NoteViewHelper helper) throws Exception {
        setResumeInputProcessor(helper.useDFBForCurrentState());
        benchmarkStart();
        if (stash != null) {
            helper.getNoteDocument().getCurrentPage(getContext()).addShapeList(stash);
        }
        helper.getNoteDocument().removeShapesByTouchPointList(getContext(), touchPointList, 1.0f);
        renderCurrentPage(helper);
        updateShapeDataInfo(helper);
        Log.e("############", "erase takes: " + benchmarkEnd());
        renderToScreen(helper);
    }

    private void renderToScreen(final NoteViewHelper helper) {
        EpdController.enablePost(surfaceView, 1);
        if (surfaceView == null) {
            return;
        }
        Rect rect = getViewportSize();
        Canvas canvas = surfaceView.getHolder().lockCanvas(rect);
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Bitmap bitmap = helper.getRenderBitmap();
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
        surfaceView.getHolder().unlockCanvasAndPost(canvas);
    }

}
