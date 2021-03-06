package com.onyx.android.sdk.scribble.request;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import com.hanvon.core.Algorithm;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.common.request.RequestManager;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.R;
import com.onyx.android.sdk.scribble.data.NoteBackgroundType;
import com.onyx.android.sdk.scribble.data.NoteDrawingArgs;
import com.onyx.android.sdk.scribble.data.NotePage;
import com.onyx.android.sdk.scribble.shape.RenderContext;
import com.onyx.android.sdk.utils.TestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuzeng on 6/3/16.
 * Requests are used in standalone application or separate page rendering from
 * shape rendering.
 */
public class BaseNoteRequest extends BaseRequest {

    private volatile ShapeDataInfo shapeDataInfo;
    private String docUniqueId;
    private String parentLibraryId;
    private Rect viewportSize;
    private List<PageInfo> visiblePages = new ArrayList<PageInfo>();
    private boolean debugPathBenchmark = false;
    private boolean pauseInputProcessor = true;
    private boolean resumeInputProcessor = false;
    private volatile boolean render = true;
    private int [] renderingBuffer = null;
    private boolean useExternal = false;

    public boolean isResumeInputProcessor() {
        return resumeInputProcessor;
    }

    public void setResumeInputProcessor(boolean resumeInputProcessor) {
        this.resumeInputProcessor = resumeInputProcessor;
    }

    public boolean isPauseInputProcessor() {
        return pauseInputProcessor;
    }

    public void setPauseInputProcessor(boolean pauseInputProcessor) {
        this.pauseInputProcessor = pauseInputProcessor;
    }

    public boolean isRender() {
        return render;
    }

    public void setRender(boolean render) {
        this.render = render;
    }

    public BaseNoteRequest() {
        setAbortPendingTasks(true);
    }

    public void setDocUniqueId(final String id) {
        docUniqueId = id;
    }

    public final String getDocUniqueId() {
        return docUniqueId;
    }

    public String getParentLibraryId() {
        return parentLibraryId;
    }

    public void setParentLibraryId(String parentLibraryId) {
        this.parentLibraryId = parentLibraryId;
    }

    public void setViewportSize(final Rect size) {
        viewportSize = size;
    }

    public final Rect getViewportSize() {
        return viewportSize;
    }

    public void setVisiblePages(final List<PageInfo> pages) {
        visiblePages.addAll(pages);
    }

    public final List<PageInfo> getVisiblePages() {
        return visiblePages;
    }

    public void beforeExecute(final NoteViewHelper helper) {
        helper.getRequestManager().acquireWakeLock(getContext(), getClass().getSimpleName());
        if (isPauseInputProcessor()) {
            helper.pauseDrawing();
        }
        benchmarkStart();
        invokeStartCallback(helper.getRequestManager());
    }

    private void invokeStartCallback(final RequestManager requestManager) {
        if (getCallback() == null) {
            return;
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getCallback().start(BaseNoteRequest.this);
            }
        };
        if (isRunInBackground()) {
            requestManager.getLooperHandler().post(runnable);
        } else {
            runnable.run();
        }
    }

    public void execute(final NoteViewHelper helper) throws Exception {
    }

    public void afterExecute(final NoteViewHelper helper) {
        if (getException() != null) {
            getException().printStackTrace();
        }
        benchmarkEnd();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isRender()) {
                    synchronized (helper) {
                        helper.copyBitmap();
                    }
                }
                helper.enableScreenPost(true);
                if (getCallback() != null) {
                    getCallback().done(BaseNoteRequest.this, getException());
                }
                if (isResumeInputProcessor()) {
                    helper.resumeDrawing();
                }
                helper.getRequestManager().releaseWakeLock();
            }};

        if (isRunInBackground()) {
            helper.getRequestManager().getLooperHandler().post(runnable);
        } else {
            runnable.run();
        }
    }

    public final ShapeDataInfo getShapeDataInfo() {
        if (shapeDataInfo == null) {
            shapeDataInfo = new ShapeDataInfo();
        }
        return shapeDataInfo;
    }

    public void renderVisiblePages(final NoteViewHelper parent) {
        synchronized (parent) {
            Bitmap bitmap = parent.updateRenderBitmap(getViewportSize());
            bitmap.eraseColor(Color.WHITE);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = preparePaint(parent);

            drawBackground(canvas, paint, parent.getNoteDocument().getBackground());
            prepareRenderingBuffer(bitmap);

            final Matrix renderMatrix = new Matrix();
            final RenderContext renderContext = RenderContext.create(bitmap, canvas, paint, renderMatrix);
            for (PageInfo page : getVisiblePages()) {
                final NotePage notePage = parent.getNoteDocument().getNotePage(getContext(), page.getName());
                notePage.render(renderContext, null);
            }
            flushRenderingBuffer(bitmap);
            drawRandomTestPath(canvas, paint);
        }
    }

    private Paint preparePaint(final NoteViewHelper parent) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(parent.getNoteDocument().getStrokeWidth());
        return paint;
    }

    private void prepareRenderingBuffer(final Bitmap bitmap) {
        if (!useExternal) {
            return;
        }
        Algorithm.initializeEx(bitmap.getWidth(), bitmap.getHeight(), bitmap);
    }

    private void flushRenderingBuffer(final Bitmap bitmap) {
    }

    private void drawBackground(final Canvas canvas, final Paint paint,int bgType) {
        int bgResID = 0;
        switch (bgType) {
            case NoteBackgroundType.EMPTY:
                return;
            case NoteBackgroundType.LINE:
                bgResID = R.drawable.scribble_back_ground_line;
                break;
            case NoteBackgroundType.GRID:
                bgResID = R.drawable.scribble_back_ground_grid;
                break;
            case NoteBackgroundType.MUSIC:
                bgResID = R.drawable.scribble_back_ground_music;
                break;
            case NoteBackgroundType.ENGLISH:
                bgResID = R.drawable.scribble_back_ground_english;
                break;
            case NoteBackgroundType.MATS:
                bgResID = R.drawable.scribble_back_ground_mats;
                break;
        }
        drawBackgroundResource(canvas, paint, bgResID);

    }

    private void drawBackgroundResource(Canvas canvas, Paint paint, int resID) {
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), resID);
        Rect src = new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1);
        Rect dest = new Rect(0, 0, canvas.getWidth() - 1, canvas.getHeight() - 1);
        canvas.drawBitmap(bitmap, src, dest, paint);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private boolean isRenderRandomTestPath() {
        return debugPathBenchmark;
    }

    private void drawRandomTestPath(final Canvas canvas, final Paint paint) {
        if (!isRenderRandomTestPath()) {
            return;
        }
        Path path = new Path();
        int width = getViewportSize().width();
        int height = getViewportSize().height();
        int max = TestUtils.randInt(0, 1000);
        path.moveTo(TestUtils.randInt(0, width), TestUtils.randInt(0, height));
        for(int i = 0; i < max; ++i) {
            float xx = TestUtils.randInt(0, width);
            float yy = TestUtils.randInt(0, height);
            float xx2 = TestUtils.randInt(0, width);
            float yy2 = TestUtils.randInt(0, height);
            path.quadTo((xx + xx2) / 2, (yy + yy2) / 2, xx2, yy2);
            if (isAbort()) {
                return;
            }
        }
        long ts = System.currentTimeMillis();
        canvas.drawPath(path, paint);
    }

    public void currentPageAsVisiblePage(final NoteViewHelper helper) {
        final NotePage notePage = helper.getNoteDocument().getCurrentPage(getContext());
        getVisiblePages().clear();
        PageInfo pageInfo = new PageInfo(notePage.getPageUniqueId(), getViewportSize().width(), getViewportSize().height());
        pageInfo.updateDisplayRect(new RectF(0, 0, getViewportSize().width(), getViewportSize().height()));
        getVisiblePages().add(pageInfo);
    }

    public void renderCurrentPage(final NoteViewHelper helper) {
        if (!isRender()) {
            return;
        }
        currentPageAsVisiblePage(helper);
        renderVisiblePages(helper);
    }

    public void updateShapeDataInfo(final NoteViewHelper parent) {
        final ShapeDataInfo shapeDataInfo = getShapeDataInfo();
        parent.updateShapeDataInfo(getContext(), shapeDataInfo);
    }

    public void ensureDocumentOpened(final NoteViewHelper parent) {
        if (!parent.getNoteDocument().isOpen()) {
            parent.getNoteDocument().open(getContext(),
                    getDocUniqueId(),
                    getParentLibraryId());
        }
    }

    public void syncDrawingArgs(final NoteDrawingArgs args) {
        getShapeDataInfo().getDrawingArgs().syncFrom(args);
    }

    public final NoteDrawingArgs getDrawingArgs() {
        return getShapeDataInfo().getDrawingArgs();
    }
}
