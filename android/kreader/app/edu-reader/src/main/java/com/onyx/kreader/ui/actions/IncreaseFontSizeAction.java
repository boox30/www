package com.onyx.kreader.ui.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.data.ReaderTextStyle;
import com.onyx.android.sdk.reader.host.request.ChangeStyleRequest;
import com.onyx.kreader.ui.data.ReaderDataHolder;


/**
 * Created by zhuzeng on 9/22/16.
 */
public class IncreaseFontSizeAction extends BaseAction {

    public IncreaseFontSizeAction() {
    }

    public void execute(final ReaderDataHolder readerDataHolder, final BaseCallback callback) {
        ReaderTextStyle style = readerDataHolder.getReaderViewInfo().getReaderTextStyle();
        style.increaseFontSize();
        readerDataHolder.submitRenderRequest(new ChangeStyleRequest(style), callback);
    }


}
