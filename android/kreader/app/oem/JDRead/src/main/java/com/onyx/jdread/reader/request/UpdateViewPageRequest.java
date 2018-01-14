package com.onyx.jdread.reader.request;

import com.onyx.jdread.reader.data.ReaderDataHolder;

/**
 * Created by huxiaomao on 2017/12/30.
 */

public class UpdateViewPageRequest extends ReaderBaseRequest {
    private ReaderDataHolder readerDataHolder;

    public UpdateViewPageRequest(ReaderDataHolder readerDataHolder) {
        this.readerDataHolder = readerDataHolder;
    }

    @Override
    public UpdateViewPageRequest call() throws Exception {
        updatePageView();
        return this;
    }

    public void updatePageView(){
        // TODO, should be removed as it's accessed in working thread.
        // readerDataHolder.setReaderUserDataInfo(getReaderUserDataInfo());
        readerDataHolder.getReaderViewHelper().draw(readerDataHolder,readerDataHolder.getReader().getReaderHelper().getViewportBitmap().getBitmap());
    }
}
