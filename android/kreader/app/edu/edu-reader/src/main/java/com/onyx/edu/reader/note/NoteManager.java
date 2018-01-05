package com.onyx.edu.reader.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.RequestManager;
import com.onyx.android.sdk.common.request.WakeLockHolder;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.ReaderBitmapImpl;
import com.onyx.android.sdk.scribble.api.TouchHelper;
import com.onyx.android.sdk.scribble.asyncrequest.ConfigManager;
import com.onyx.android.sdk.scribble.data.NoteDrawingArgs;
import com.onyx.android.sdk.scribble.data.NoteModel;
import com.onyx.android.sdk.scribble.data.NotePage;
import com.onyx.android.sdk.scribble.data.TouchPoint;
import com.onyx.android.sdk.scribble.shape.BaseShape;
import com.onyx.android.sdk.scribble.shape.RenderContext;
import com.onyx.android.sdk.scribble.shape.Shape;
import com.onyx.android.sdk.scribble.shape.ShapeFactory;
import com.onyx.android.sdk.scribble.utils.DeviceConfig;
import com.onyx.android.sdk.scribble.utils.InkUtils;
import com.onyx.android.sdk.scribble.utils.MappingConfig;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.RectUtils;
import com.onyx.edu.reader.note.data.ReaderNoteDataInfo;
import com.onyx.edu.reader.note.data.ReaderNoteDocument;
import com.onyx.edu.reader.note.data.ReaderNotePage;
import com.onyx.edu.reader.note.request.ReaderBaseNoteRequest;
import com.onyx.edu.reader.ui.data.ReaderDataHolder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhuzeng on 9/2/16.
 */
public class NoteManager {

    private static final String TAG = NoteManager.class.getSimpleName();
    private RequestManager requestManager = new RequestManager(Thread.NORM_PRIORITY);
    private TouchHelper touchHelper;
    private ReaderNoteDocument noteDocument = new ReaderNoteDocument();
    private ReaderBitmapImpl renderBitmapWrapper = new ReaderBitmapImpl();
    private ReaderBitmapImpl viewBitmapWrapper = new ReaderBitmapImpl();

    private ReaderBitmapImpl reviewBufferBitmap = new ReaderBitmapImpl();
    private ReaderBitmapImpl reviewBitmapWrapper = new ReaderBitmapImpl();

    private volatile View view;

    private RenderContext noteRenderContext = new RenderContext();
    private RenderContext reviewRenderContext = new RenderContext();

    private ShapeEventHandler shapeEventHandler;

    private List<Shape> shapeStash = new ArrayList<>();
    private DeviceConfig noteConfig;
    private MappingConfig mappingConfig;
    private List<PageInfo> visiblePages = new ArrayList<>();
    private EventBus eventBus;
    private boolean sideNoting = false;
    private ReaderDataHolder parent;
    private ReaderNoteDataInfo noteDataInfo = new ReaderNoteDataInfo();
    private RectF visibleDrawRectF;
    private List<RectF> excludeRect;
    private volatile boolean enableRawEventProcessor = false;
    private AtomicBoolean noteDirty = new AtomicBoolean(false);
    private volatile boolean enableShortcutDrawing = false;
    private volatile boolean enableShortcutErasing = false;
    private boolean useWakeLock = true;
    private WakeLockHolder wakeLockHolder = new WakeLockHolder(false);

    public NoteManager(final ReaderDataHolder p, final EventBus bus) {
        parent = p;
        eventBus = bus;
        shapeEventHandler = new ShapeEventHandler(this);
        shapeEventHandler.registerEventBus();
        ConfigManager.init(p.getContext().getApplicationContext());
        BaseShape.setUseRawInput(ConfigManager.getInstance().getDeviceConfig().useRawInput());
        getTouchHelper().setUseRawInput(ConfigManager.getInstance().getDeviceConfig().useRawInput());
    }

    public final RequestManager getRequestManager() {
        return requestManager;
    }

    public final ReaderNoteDocument getNoteDocument() {
        return noteDocument;
    }

    public void startRawEventProcessor() {
        getTouchHelper().startRawDrawing();
    }

    public void enableRawEventProcessor(boolean enable) {
        enableRawEventProcessor = enable;
        if (!enable) {
            releaseWakeLock();
        }
    }

    public void stopRawEventProcessor() {
        releaseWakeLock();
        getTouchHelper().stopRawDrawing();
    }

    public void pauseRawEventProcessor() {
        releaseWakeLock();
        getTouchHelper().pauseRawDrawing();
    }

    public void resumeRawEventProcessor(final Context context) {
        if (enableRawEventProcessor) {
            acquireWakeLock(context);
        }
        getTouchHelper().resumeRawDrawing();
    }

    private void acquireWakeLock(final Context context) {
        if (useWakeLock) {
            wakeLockHolder.acquireWakeLock(context, WakeLockHolder.FULL_FLAGS, TAG, 40 * 60 * 1000);
        }
    }

    private void releaseWakeLock() {
        wakeLockHolder.releaseWakeLock();
    }

    private void initNoteArgs(final Context context) {
        noteConfig = DeviceConfig.sharedInstance(context, "note");
        mappingConfig = MappingConfig.sharedInstance(context, "note");
        enableShortcutDrawing = (noteConfig.isShortcutDrawingEnabled() && noteConfig.supportBigPen());
        enableShortcutErasing = (noteConfig.isShortcutErasingEnabled() && noteConfig.supportBigPen());
        NoteModel.setDefaultEraserRadius(noteConfig.getEraserRadius());
        NoteModel.setDefaultStrokeColor(noteConfig.getDefaultStrokeColor());
        InkUtils.setPressureEntries(mappingConfig.getPressureList());

        getTouchHelper().getTouchReader().setSupportBigPen(noteConfig.supportBigPen());
    }

    public void updateHostView(final Context context, final View sv, final Rect visibleDrawRect, final List<RectF> excludeRect, int orientation) {
        if (noteConfig == null || mappingConfig == null) {
            initNoteArgs(context);
        }
        view = sv;
        getTouchHelper().setup(view);

        getTouchHelper().setLimitRect(visibleDrawRect, RectUtils.toRectList(excludeRect));
        setVisibleDrawRectF(new RectF(visibleDrawRect.left, visibleDrawRect.top, visibleDrawRect.right, visibleDrawRect.bottom));
        setExcludeRectFs(excludeRect);
    }

    private void setExcludeRectFs(final List<RectF> exclude) {
        if (exclude == null) {
            return;
        }
        excludeRect = new ArrayList<>();
        for (RectF rectF : exclude) {
            excludeRect.add(new RectF(rectF.left, rectF.top, rectF.right, rectF.bottom));
        }
    }

    public final TouchHelper getTouchHelper() {
        if (touchHelper == null) {
            touchHelper = new TouchHelper(getEventBus());
        }
        return touchHelper;
    }

    public View getHostView() {
        return view;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public final DeviceConfig getNoteConfig() {
        return noteConfig;
    }

    public void updateShapeDataInfo(final Context context, final ReaderNoteDataInfo shapeDataInfo) {
        shapeDataInfo.updateDrawingArgs(getNoteDocument().getNoteDrawingArgs());
        shapeDataInfo.setDocumentUniqueId(getNoteDocument().getDocumentUniqueId());
    }

    public ReaderDataHolder getParent() {
        return parent;
    }

    public void enableScreenPost(boolean enable) {
        if (view != null) {
            EpdController.enablePost(view, enable ? 1 : 0);
        }
    }

    public Bitmap updateRenderBitmap(final Rect viewportSize) {
        renderBitmapWrapper.update(viewportSize.width(), viewportSize.height(), Bitmap.Config.ARGB_8888);
        return renderBitmapWrapper.getBitmap();
    }

    public Bitmap createReviewBufferBitmap(final Rect viewportSize) {
        reviewBufferBitmap.update(viewportSize.width() / 2, viewportSize.height() / 2 , Bitmap.Config.ARGB_8888);
        return reviewBufferBitmap.getBitmap();
    }

    public void updateReviewBufferBitmap(final Bitmap src) {
        reviewBufferBitmap.attach(src);
    }

    public Bitmap getReviewBitmap() {
        if (reviewBitmapWrapper == null) {
            return null;
        }
        return reviewBitmapWrapper.getBitmap();
    }

    // copy from renderNoteShapes bitmap to view bitmap.
    public void copyNoteBitmap() {
        if (renderBitmapWrapper == null) {
            return;
        }
        final Bitmap bitmap = renderBitmapWrapper.getBitmap();
        if (bitmap == null) {
            return;
        }
        viewBitmapWrapper.copyFrom(bitmap);
    }

    public void copyReviewBitmap() {
        if (reviewBufferBitmap == null) {
            return;
        }
        final Bitmap bitmap = reviewBufferBitmap.getBitmap();
        if (bitmap == null) {
            return;
        }
        reviewBitmapWrapper.copyFrom(bitmap);
    }

    public Bitmap getViewBitmap() {
        if (viewBitmapWrapper == null) {
            return null;
        }
        return viewBitmapWrapper.getBitmap();
    }

    public void submit(final Context context, final ReaderBaseNoteRequest request, final BaseCallback callback) {
        beforeSubmit(context, Integer.MAX_VALUE, request, callback);
        getRequestManager().submitRequest(context, request, generateRunnable(request), callback);
    }

    public void submitWithUniqueId(final Context context, int uniqueId, final ReaderBaseNoteRequest request, final BaseCallback callback) {
        beforeSubmit(context, uniqueId, request, callback);
        getRequestManager().submitRequest(context, request, generateRunnable(request), callback);
    }


    public void submitRequestWithIdentifier(final Context context,
                                            final String identifier,
                                            final ReaderBaseNoteRequest request,
                                            final BaseCallback callback) {
        beforeSubmit(context, Integer.MAX_VALUE, request, callback);
        getRequestManager().submitRequest(context, identifier, request, generateRunnable(request), callback);
    }

    private void beforeSubmit(final Context context, int uniqueId, final ReaderBaseNoteRequest request, final BaseCallback callback) {
        final Rect rect = getViewportSize();
        if (rect != null) {
            request.setViewportSize(rect);
        }
        request.setAssociatedUniqueId(uniqueId);
        if (request.isResetNoteDataInfo()) {
            resetNoteDataInfo();
        }
    }

    private final Runnable generateRunnable(final ReaderBaseNoteRequest request) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    request.beforeExecute(NoteManager.this);
                    request.execute(NoteManager.this);
                } catch (java.lang.Exception exception) {
                    Debug.d(exception.toString());
                    request.setException(exception);
                } finally {
                    request.afterExecute(NoteManager.this);
                    getRequestManager().dumpWakelocks();
                    getRequestManager().removeRequest(request);
                }
            }
        };
        return runnable;
    }

    private Rect getViewportSize() {
        if (view != null) {
            return new Rect(0, 0, view.getWidth(), view.getHeight());
        }
        return null;
    }

    public void onNewStash(final Shape shape) {
        shapeStash.add(shape);
    }

    public final NoteDrawingArgs getNoteDrawingArgs() {
        return noteDocument.getNoteDrawingArgs();
    }

    public void setCurrentShapeType(int type) {
        getNoteDrawingArgs().setCurrentShapeType(type);
    }

    public void setCurrentShapeColor(int color) {
        getNoteDrawingArgs().setStrokeColor(color);
    }

    public void restoreCurrentShapeType() {
        getNoteDrawingArgs().restoreCurrentShapeType();
    }

    public void setCurrentStrokeWidth(float w) {
        getNoteDrawingArgs().strokeWidth = w;
        getTouchHelper().setStrokeWidth(w);
    }


    public Shape getCurrentShape() {
        return shapeEventHandler.getCurrentShape();
    }

    public TouchPoint getEraserPoint() {
        return shapeEventHandler.getEraserPoint();
    }

    public boolean isDFBForCurrentShape() {
        return ShapeFactory.isDFBShape(getNoteDrawingArgs().getCurrentShapeType());
    }

    public boolean isEraser() {
        return getNoteDrawingArgs().getCurrentShapeType() == ShapeFactory.SHAPE_ERASER;
    }

    public boolean isInSelection() {
        return getNoteDrawingArgs().getCurrentShapeType() == ShapeFactory.SHAPE_SELECTOR;
    }

    public void resetSelection() {
        if (isInSelection()) {
            getNoteDrawingArgs().resetCurrentShapeType();
        }
    }

    public void setVisiblePages(final List<PageInfo> list) {
        visiblePages.clear();
        visiblePages.addAll(list);
    }


    public List<PageInfo> getVisiblePages() {
        return visiblePages;
    }

    public void restoreStrokeWidth() {
        if (noteDocument != null && noteDocument.isOpen()) {
            setCurrentStrokeWidth(noteDocument.getStrokeWidth());
        }
    }


    public boolean inScribbleRect(TouchPoint touchPoint) {
        return inLimitRect(touchPoint.x, touchPoint.y) && !inExcludeRect(touchPoint.x, touchPoint.y);
    }

    private boolean inLimitRect(final float x, final float y) {
        return visibleDrawRectF != null && visibleDrawRectF.contains(x, y);
    }

    private boolean inExcludeRect(final float x, final float y) {
        if (excludeRect == null || excludeRect.size() == 0) {
            return false;
        }
        for (RectF rect : excludeRect) {
            if (rect.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public final RenderContext getNoteRenderContext() {
        return noteRenderContext;
    }

    public RenderContext getReviewRenderContext() {
        return reviewRenderContext;
    }

    public final List<Shape> detachShapeStash() {
        final List<Shape> list = shapeStash;
        shapeStash = new ArrayList<>();
        return list;
    }

    public final List<Shape> getShapeStash() {
        return shapeStash;
    }

    public boolean hasShapeStash() {
        return shapeStash.size() > 0;
    }

    @Nullable
    public ReaderNotePage getReaderNotePage(final Context context, final String pageName, int subPageIndex) {
        return getNoteDocument().loadPage(context, pageName, subPageIndex);
    }

    @Nullable
    public ReaderNotePage getReaderNotePage(final Context context, final String pageName) {
        return getReaderNotePage(context, pageName, 0);
    }

    public void undo(final Context context, final String pageName) {
        final ReaderNotePage readerNotePage = getReaderNotePage(context, pageName);
        if (readerNotePage != null) {
            readerNotePage.undo();
        }
    }

    public void redo(final Context context, final String pageName) {
        final ReaderNotePage readerNotePage = getReaderNotePage(context, pageName);
        if (readerNotePage != null) {
            readerNotePage.redo();
        }
    }

    public void saveNoteDataInfo(final ReaderBaseNoteRequest request) {
        noteDataInfo = request.getNoteDataInfo();
        noteDataInfo.setRequestFinished(true);
    }

    public final ReaderNoteDataInfo getNoteDataInfo() {
        return noteDataInfo;
    }

    public void resetNoteDataInfo() {
        noteDataInfo.setRequestFinished(false);
    }

    public void ensureContentRendered() {
        if (getNoteDataInfo().isRequestFinished()) {
            getNoteDataInfo().setContentRendered(true);
        }
    }

    public void setVisibleDrawRectF(RectF visibleDrawRectF) {
        this.visibleDrawRectF = visibleDrawRectF;
    }

    public boolean inVisibleDrawRectF(float x, float y){
        return visibleDrawRectF.contains(x, y);
    }

    public RectF getVisibleDrawRectF() {
        return visibleDrawRectF;
    }

    public boolean isNoteDirty() {
        return noteDirty.get();
    }

    public void setNoteDirty(boolean dirty) {
        noteDirty.set(dirty);
    }

    public void setEnableShortcutDrawing(boolean enableShortcutDrawing) {
        if (noteConfig != null && noteConfig.isShortcutDrawingEnabled() && noteConfig.supportBigPen()) {
            this.enableShortcutDrawing = enableShortcutDrawing;
        }
    }

    public void setEnableShortcutErasing(boolean enableShortcutErasing) {
        if (noteConfig != null && noteConfig.isShortcutDrawingEnabled() && noteConfig.supportBigPen()) {
            this.enableShortcutErasing = enableShortcutErasing;
        }
    }

    public boolean isSideNoting() {
        return sideNoting;
    }

    public void setSideNoting(boolean sideNoting) {
        this.sideNoting = sideNoting;
    }
}
