package com.onyx.kreader.ui.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.reader.host.request.GetPositionFromPageNumberRequest;
import com.onyx.kreader.ui.data.ReaderDataHolder;

import java.util.List;

/**
 * Created by ming on 2017/2/24.
 */

public class GetPositionFromPageNumberAction extends BaseAction {

    private List<String> documentPositions;
    private List<Integer> pageNumbers;

    public GetPositionFromPageNumberAction(List<Integer> pageNumbers) {
        this.pageNumbers = pageNumbers;
    }

    @Override
    public void execute(ReaderDataHolder readerDataHolder, final BaseCallback callback) {
        final GetPositionFromPageNumberRequest readerRequest = new GetPositionFromPageNumberRequest(pageNumbers);
        readerDataHolder.submitNonRenderRequest(readerRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                documentPositions = readerRequest.getDocumentPositions();
                callback.invoke(callback, request, e);
            }
        });
    }

    public List<String> getDocumentPositions() {
        return documentPositions;
    }
}