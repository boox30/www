package com.onyx.edu.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.RequestManager;
import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.data.NoteDocument;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;
import com.onyx.android.sdk.scribble.request.ShapeDataInfo;
import com.onyx.android.sdk.scribble.shape.Shape;
import com.onyx.edu.note.actions.scribble.DocumentFlushAction;
import com.onyx.android.sdk.scribble.utils.NoteViewUtil;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by solskjaer49 on 2017/2/11 18:44.
 */

public class NoteManager {
    private static final String TAG = NoteManager.class.getSimpleName();

    public RequestManager getRequestManager() {
        return requestManager;
    }

    private RequestManager requestManager;
    private NoteViewHelper noteViewHelper;
    private static NoteManager instance;
    //TODO:use WeakReference here avoid context leak in static class as AndroidStudio lint check.
    private WeakReference<Context> contextWeakReference;
    private ShapeDataInfo shapeDataInfo = new ShapeDataInfo();

    private NoteManager(Context context) {
        requestManager = new RequestManager(Thread.NORM_PRIORITY);
        if (noteViewHelper == null) {
            noteViewHelper = new NoteViewHelper();
        }
        contextWeakReference = new WeakReference<>(context.getApplicationContext());
    }

    static public NoteManager sharedInstance(Context context) {
        if (instance == null) {
            instance = new NoteManager(context);
        }
        return instance;
    }

    private Runnable generateRunnable(final BaseNoteRequest request) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    request.beforeExecute(noteViewHelper);
                    request.execute(noteViewHelper);
                } catch (Throwable tr) {
                    request.setException(tr);
                } finally {
                    request.postExecute(noteViewHelper);
                    requestManager.dumpWakelocks();
                    requestManager.removeRequest(request);
                }
            }
        };
    }

    private void beforeSubmit(BaseNoteRequest request) {
        final Rect rect = noteViewHelper.getViewportSize();
        if (rect != null) {
            request.setViewportSize(rect);
        }
    }

    public boolean submitRequest(final BaseNoteRequest request, final BaseCallback callback) {
        beforeSubmit(request);
        if (contextWeakReference.get() != null) {
            return requestManager.submitRequestToMultiThreadPool(contextWeakReference.get(),
                    request, generateRunnable(request), callback);
        } else {
            Log.e(TAG, "Context has been GC");
            return false;
        }
    }

    public boolean submitRequestWithIdentifier(final BaseNoteRequest request, final String identifier,
                                               final BaseCallback callback) {
        beforeSubmit(request);
        request.setIdentifier(identifier);
        if (contextWeakReference.get() != null) {
            return requestManager.submitRequestToMultiThreadPool(contextWeakReference.get(),identifier,
                    request, generateRunnable(request), callback);
        } else {
            Log.e(TAG, "Context has been GC");
            return false;
        }
    }

    public void sync(boolean render,
                     boolean resume, boolean drawToView) {
        syncWithCallback(render, resume, drawToView, null);
    }

    public void syncWithCallback(boolean render,
                                 boolean resume, boolean drawToView,
                                 final BaseCallback callback) {
        final List<Shape> stash = detachStash();
        if (isLineLayoutMode()) {
            stash.clear();
        }
        final DocumentFlushAction action = new DocumentFlushAction(stash,
                render,
                resume,
                drawToView,
                shapeDataInfo.getDrawingArgs());
        action.execute(this, callback);
    }

    public ShapeDataInfo getShapeDataInfo() {
        return shapeDataInfo;
    }

    public void setShapeDataInfo(ShapeDataInfo shapeDataInfo) {
        this.shapeDataInfo = shapeDataInfo;
    }

    //TODO:avoid direct obtain note view helper,because we plan to remove this class.

    public void reset(View view) {
        noteViewHelper.reset(view);
    }

    public View getView() {
        return noteViewHelper.getView();
    }

    public void setView(Context context, SurfaceView surfaceView, NoteViewHelper.InputCallback inputCallback) {
        noteViewHelper.setView(context, surfaceView, inputCallback);
    }

    public void quit() {
        noteViewHelper.quit();
    }

    public boolean isLineLayoutMode() {
        return noteViewHelper.isLineLayoutMode();
    }

    public List<Shape> detachStash() {
        return noteViewHelper.detachStash();
    }

    public void clearSurfaceView(SurfaceView surfaceView) {
        NoteViewUtil.clearSurfaceView(surfaceView);
    }

    public void setDrawing(boolean drawing) {
        noteViewHelper.setDrawing(drawing);
    }

    public boolean isDrawing() {
        return noteViewHelper.isDrawing();
    }

    public NoteDocument getNoteDocument() {
        return noteViewHelper.getNoteDocument();
    }

    public void pauseDrawing(){
        noteViewHelper.pauseDrawing();
    }

    public void resumeDrawing(){
        noteViewHelper.resumeDrawing();
    }

    public Bitmap getViewBitmap() {
        return noteViewHelper.getViewBitmap();
    }

    public Bitmap getRenderBitmap() {
        return noteViewHelper.getRenderBitmap();
    }

    public List<Shape> getDirtyShape() {
        return noteViewHelper.getDirtyStash();
    }
}
