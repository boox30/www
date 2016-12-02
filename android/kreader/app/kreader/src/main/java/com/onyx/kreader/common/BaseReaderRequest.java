package com.onyx.kreader.common;

import android.util.Log;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.provider.DataProviderManager;
import com.onyx.kreader.api.ReaderException;
import com.onyx.kreader.api.ReaderHitTestManager;
import com.onyx.kreader.api.ReaderSelection;
import com.onyx.kreader.cache.ReaderBitmapImpl;
import com.onyx.kreader.dataprovider.LegacySdkDataUtils;
import com.onyx.kreader.host.wrapper.Reader;
import com.onyx.kreader.utils.PagePositionUtils;

import java.util.List;

/**
 * Created by zhuzeng on 10/4/15.
 */
public abstract class BaseReaderRequest extends BaseRequest {

    private static final String TAG = BaseReaderRequest.class.getSimpleName();
    private volatile boolean saveOptions = false;

    private ReaderBitmapImpl renderBitmap;
    private ReaderViewInfo readerViewInfo;
    private ReaderUserDataInfo readerUserDataInfo;
    private volatile boolean transferBitmap = true;
    private boolean loadPageAnnotation = true;
    private boolean loadBookmark = true;
    private boolean loadPageLinks = true;

    public BaseReaderRequest() {
        super();
    }

    public boolean isTransferBitmap() {
        return transferBitmap;
    }

    public void setTransferBitmap(boolean sync) {
        transferBitmap = sync;
    }

    public void setSaveOptions(boolean save) {
        saveOptions = save;
    }

    public boolean isSaveOptions() {
        return saveOptions;
    }

    public ReaderBitmapImpl getRenderBitmap() {
        return renderBitmap;
    }

    public void drawVisiblePages(final Reader reader) throws ReaderException {
        ReaderDrawContext context = ReaderDrawContext.create(false);
        drawVisiblePages(reader, context);
    }

    public void drawVisiblePages(final Reader reader, ReaderDrawContext context) throws ReaderException {
        if (reader.getReaderLayoutManager().drawVisiblePages(reader, context, createReaderViewInfo())) {
            renderBitmap = context.renderingBitmap;
        }
    }

    public void beforeExecute(final Reader reader) {
        reader.acquireWakeLock(getContext(), getClass().getSimpleName());
        benchmarkStart();
        if (isAbort()) {
            reader.getReaderHelper().setAbortFlag();
        }
        if (getCallback() == null) {
            return;
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getCallback().start(BaseReaderRequest.this);
            }
        };
        if (isRunInBackground()) {
            reader.getLooperHandler().post(runnable);
        } else {
            runnable.run();
        }
    }

    public abstract void execute(final Reader reader) throws Exception;

    /**
     * must not throw out exception from the method
     *
     * @param reader
     */
    public void afterExecute(final Reader reader) {
        try {
            afterExecuteImpl(reader);
        } catch (Throwable tr) {
            Log.w(getClass().getSimpleName(), tr);
        } finally {
            transferBitmapToViewport(reader);
        }
    }

    private void afterExecuteImpl(final Reader reader) throws Throwable {
        dumpException();
        benchmarkEnd();
        loadUserData(reader);
        cleanup(reader);
    }

    private void dumpException() {
        if (hasException()) {
            Log.w(TAG, getException());
        }
    }

    private void transferBitmapToViewport(final Reader reader) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isTransferBitmap() && getRenderBitmap() != null) {
                    reader.transferRenderBitmapToViewport(getRenderBitmap());
                }
                BaseCallback.invoke(getCallback(), BaseReaderRequest.this, getException());
                reader.releaseWakeLock(BaseReaderRequest.this.getClass().getSimpleName());
        }};

        if (isRunInBackground()) {
            reader.getLooperHandler().post(runnable);
        } else {
            runnable.run();
        }
        if (getRenderBitmap() != null && !isTransferBitmap()){
            reader.returnBitmapToCache(getRenderBitmap());
        }
    }

    public final ReaderViewInfo getReaderViewInfo() {
        return readerViewInfo;
    }

    public final ReaderUserDataInfo getReaderUserDataInfo() {
        if (readerUserDataInfo == null) {
            readerUserDataInfo = new ReaderUserDataInfo();
        }
        return readerUserDataInfo;
    }

    public ReaderViewInfo createReaderViewInfo() {
        readerViewInfo = new ReaderViewInfo();
        return readerViewInfo;
    }

    public void saveReaderOptions(final Reader reader) {
        reader.saveOptions();
        saveToDocumentOptionsProvider(reader);
        saveToLegacyDataProvider(reader);
    }

    private void saveToDocumentOptionsProvider(final Reader reader) {
        DataProviderManager.getDataProvider().saveDocumentOptions(getContext(),
                reader.getDocumentPath(),
                reader.getDocumentMd5(),
                reader.getDocumentOptions().toJSONString());
    }

    private void saveToLegacyDataProvider(final Reader reader) {
        try {
            if (reader.getNavigator() != null) {
                int currentPage = PagePositionUtils.getPageNumber(reader.getReaderLayoutManager().getCurrentPageInfo() != null ?
                        reader.getReaderLayoutManager().getCurrentPageInfo().getName() :
                        reader.getNavigator().getInitPosition());
                int totalPage = reader.getNavigator().getTotalPage();
                LegacySdkDataUtils.updateProgress(getContext(), reader.getDocumentPath(),
                        currentPage, totalPage);
            }
        } catch (Throwable tr) {
            Log.w(TAG, this.getClass().toString());
            Log.w(TAG, tr);
        }
    }

    private void loadUserData(final Reader reader) {
        getReaderUserDataInfo().setDocumentPath(reader.getDocumentPath());
        getReaderUserDataInfo().setDocumentMetadata(reader.getDocumentMetadata());
        if (readerViewInfo != null && loadPageAnnotation) {
            getReaderUserDataInfo().loadPageAnnotations(getContext(), reader, readerViewInfo.getVisiblePages());
            if (!reader.getRendererFeatures().supportScale()) {
                // if document doesn't support scale, means it's a flow document,
                // then we need update selection rectangles as they may change
                for (PageInfo pageInfo : readerViewInfo.getVisiblePages()) {
                    List<PageAnnotation> annotations = getReaderUserDataInfo().getPageAnnotations(pageInfo);
                    if (annotations == null) {
                        continue;
                    }
                    for (PageAnnotation annotation : annotations) {
                        ReaderHitTestManager hitTestManager = reader.getReaderHelper().getHitTestManager();
                        ReaderSelection selection = hitTestManager.select(pageInfo.getPosition(),
                                annotation.getAnnotation().getLocationBegin(),
                                annotation.getAnnotation().getLocationEnd());
                        annotation.getRectangles().clear();
                        if (selection != null) {
                            annotation.getRectangles().addAll(selection.getRectangles());
                        }
                    }
                }
            }
        }
        if (readerViewInfo != null && loadBookmark) {
            getReaderUserDataInfo().loadBookmarks(getContext(), reader, readerViewInfo.getVisiblePages());
        }
        if (readerViewInfo != null && loadPageLinks) {
            getReaderUserDataInfo().loadPageLinks(getContext(), reader, readerViewInfo.getVisiblePages());
        }
    }

    private void cleanup(final Reader reader) {
        reader.getReaderHelper().clearAbortFlag();
        reader.getReaderHelper().setLayoutChanged(false);
    }

    public void setLoadPageAnnotation(boolean loadPageAnnotation) {
        this.loadPageAnnotation = loadPageAnnotation;
    }

    public void setLoadBookmark(boolean loadBookmark) {
        this.loadBookmark = loadBookmark;
    }
}
