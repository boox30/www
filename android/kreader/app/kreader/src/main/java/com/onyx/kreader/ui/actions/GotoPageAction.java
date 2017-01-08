package com.onyx.kreader.ui.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.kreader.common.BaseReaderRequest;
import com.onyx.kreader.host.request.GotoPageRequest;
import com.onyx.kreader.host.request.GotoPositionRequest;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.events.PageChangedEvent;

/**
 * Created by zhuzeng on 5/18/16.
 */
public class GotoPageAction extends BaseAction {
    private int page;
    private boolean abortPendingTasks;

    public GotoPageAction(final int page) {
        this(page, false);
    }

    public GotoPageAction(final int page, final boolean abortPendingTasks) {
        this.page = page;
        this.abortPendingTasks = abortPendingTasks;
    }

    public void execute(final ReaderDataHolder readerDataHolder) {
        final PageChangedEvent pageChangedEvent = PageChangedEvent.beforePageChange(readerDataHolder);
        execute(readerDataHolder, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                pageChangedEvent.afterPageChange(readerDataHolder);
            }
        });
    }

    @Override
    public void execute(ReaderDataHolder readerDataHolder, BaseCallback baseCallback) {
        GotoPageRequest request = new GotoPageRequest(page);
        request.setAbortPendingTasks(abortPendingTasks);
        readerDataHolder.submitRenderRequest(request, baseCallback);
    }
}
