package com.onyx.kreader.ui.data;

import android.content.Context;
import android.graphics.Rect;

import com.alibaba.fastjson.JSON;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;
import com.onyx.android.sdk.scribble.request.ShapeDataInfo;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.kreader.common.BaseReaderRequest;
import com.onyx.kreader.common.Debug;
import com.onyx.kreader.common.ReaderUserDataInfo;
import com.onyx.kreader.common.ReaderViewInfo;
import com.onyx.kreader.host.request.PreRenderRequest;
import com.onyx.kreader.host.request.RenderRequest;
import com.onyx.kreader.host.wrapper.Reader;
import com.onyx.kreader.ui.handler.HandlerManager;
import com.onyx.kreader.ui.highlight.ReaderSelectionManager;
import com.onyx.kreader.utils.PagePositionUtils;

/**
 * Created by ming on 16/7/27.
 */
public class ReaderDataHolder {
    private static final String TAG = ReaderDataHolder.class.getSimpleName();

    public interface CallBack {
        void onRenderRequestFinished();
    }

    private Context context;
    private Reader reader;
    private ReaderViewInfo readerViewInfo;
    private ReaderUserDataInfo readerUserDataInfo;
    private ShapeDataInfo shapeDataInfo;
    private boolean preRender = true;
    private boolean preRenderNext = true;
    private CallBack callBack;
    private int displayWidth;
    private int displayHeight;
    private HandlerManager handlerManager;
    private ReaderSelectionManager selectionManager;

    public ReaderDataHolder(Context context,HandlerManager handlerManager){
        this.context = context;
        this.handlerManager = handlerManager;
    }

    public Context getContext() {
        return context;
    }

    public void saveReaderViewInfo(final BaseReaderRequest request) {
        Debug.d(TAG, "saveReaderViewInfo: " + JSON.toJSONString(request.getReaderViewInfo().getFirstVisiblePage()));
        readerViewInfo = request.getReaderViewInfo();
    }

    public void saveReaderUserDataInfo(final BaseReaderRequest request) {
        readerUserDataInfo = request.getReaderUserDataInfo();
    }

    public void saveShapeDataInfo(final BaseNoteRequest request) {
        shapeDataInfo = request.getShapeDataInfo();
    }

    public boolean hasShapes() {
        if (shapeDataInfo == null) {
            return false;
        }
        return shapeDataInfo.hasShapes();
    }

    public void resetShapeData() {
        shapeDataInfo = null;
    }

    public final ReaderViewInfo getReaderViewInfo() {
        return readerViewInfo;
    }

    public final ReaderUserDataInfo getReaderUserDataInfo() {
        return readerUserDataInfo;
    }

    public final ShapeDataInfo getShapeDataInfo() {
        return shapeDataInfo;
    }

    public void setPreRenderNext(boolean preRenderNext) {
        this.preRenderNext = preRenderNext;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public String getCurrentPageName() {
        return getReaderViewInfo().getFirstVisiblePage().getName();
    }

    public int getCurrentPage() {
        return PagePositionUtils.getPosition(getCurrentPageName());
    }

    public int getPageCount() {
        return reader.getNavigator().getTotalPage();
    }

    public final PageInfo getFirstPageInfo() {
        return getReaderViewInfo().getFirstVisiblePage();
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public Rect getDisplayRect() {
        return new Rect(0, 0, getDisplayWidth(), getDisplayHeight());
    }

    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }

    public final HandlerManager getHandlerManager() {
        return handlerManager;
    }

    public ReaderSelectionManager getSelectionManager() {
        if (selectionManager == null) {
            selectionManager = new ReaderSelectionManager();
        }
        return selectionManager;
    }

    public String getDocumentPath() {
        return getReaderUserDataInfo().getDocumentPath();
    }

    public String getBookName() {
        Debug.d("getBookName: " + getDocumentPath());
        return FileUtils.getFileName(getDocumentPath());
    }

    public boolean hasBookmark() {
        return getReaderUserDataInfo().hasBookmark(getFirstPageInfo());
    }

    public void submitRequest(final BaseReaderRequest renderRequest) {
        submitRequest(renderRequest, null);
    }

    public void submitRequest(final BaseReaderRequest renderRequest, final BaseCallback callback) {
        beforeSubmitRequest();
        reader.submitRequest(context, renderRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (callback != null) {
                    callback.done(request, e);
                }
                onRenderRequestFinished(renderRequest, e);
                preRenderNext();
            }
        });
    }

    private void beforeSubmitRequest() {
        resetShapeData();
    }

    public void preRenderNext() {
        if (!preRender) {
            return;
        }
        final PreRenderRequest request = new PreRenderRequest(preRenderNext);
        getReader().submitRequest(context, request, null);
    }

    public void onRenderRequestFinished(final BaseReaderRequest request, Throwable e) {
        Debug.d(TAG, "onRenderRequestFinished: " + request + ", " + e);
        if (e != null || request.isAbort()) {
            return;
        }
        saveReaderViewInfo(request);
        saveReaderUserDataInfo(request);
        if (callBack != null){
            callBack.onRenderRequestFinished();
        }
    }

    public void redrawPage() {
        if (getReader() != null) {
            submitRequest(new RenderRequest());
        }
    }
}
