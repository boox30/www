package com.onyx.kreader.host.request;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.kreader.ui.actions.BaseAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;

/**
 * Created by ming on 16/9/30.
 */
public class AnalyzeWordAction extends BaseAction{

    private String text;
    private boolean isWord;

    public AnalyzeWordAction(String text) {
        this.text = text;
    }

    @Override
    public void execute(ReaderDataHolder readerDataHolder, final BaseCallback baseCallback) {
        final AnalyzeWordRequest analyzeWordRequest = new AnalyzeWordRequest(text);
        readerDataHolder.submitNonRenderRequest(analyzeWordRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                isWord = analyzeWordRequest.isWord();
                baseCallback.done(request, e);
            }
        });
    }

    public boolean isWord() {
        return isWord;
    }
}
