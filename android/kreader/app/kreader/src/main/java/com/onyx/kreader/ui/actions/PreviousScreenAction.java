package com.onyx.kreader.ui.actions;

import android.widget.Toast;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.kreader.R;
import com.onyx.kreader.host.request.PreviousScreenRequest;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.events.PageChangedEvent;

/**
 * Created by zhuzeng on 5/17/16.
 */
public class PreviousScreenAction extends BaseAction {

    public void execute(final ReaderDataHolder readerDataHolder, final BaseCallback callback) {
        if (!readerDataHolder.getReaderViewInfo().canPrevScreen) {
            Toast.makeText(readerDataHolder.getContext(), readerDataHolder.getContext().getString(R.string.min_page_toast), Toast.LENGTH_SHORT).show();
            return;
        }
        final PageChangedEvent pageChangedEvent = readerDataHolder.beforePageChange();
        final PreviousScreenRequest request = new PreviousScreenRequest();
        readerDataHolder.submitRenderRequest(request, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                readerDataHolder.afterPageChange(pageChangedEvent);
                hideLoadingDialog();
                BaseCallback.invoke(callback, request, e);
            }

            @Override
            public void progress(BaseRequest request, ProgressInfo info) {
                super.progress(request, info);
                showLoadingDialog(readerDataHolder, R.string.loading);
            }
        });
    }

}
