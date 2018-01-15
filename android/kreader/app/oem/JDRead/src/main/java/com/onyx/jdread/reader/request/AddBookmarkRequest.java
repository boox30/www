package com.onyx.jdread.reader.request;

import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.model.Bookmark;
import com.onyx.android.sdk.reader.dataprovider.ContentSdkDataUtils;
import com.onyx.android.sdk.reader.utils.PagePositionUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.reader.data.ReaderDataHolder;
import com.onyx.jdread.reader.layout.LayoutProviderUtils;

/**
 * Created by huxiaomao on 2018/1/9.
 */

public class AddBookmarkRequest extends ReaderBaseRequest {
    private PageInfo pageInfo;
    private ReaderDataHolder readerDataHolder;

    public AddBookmarkRequest(ReaderDataHolder readerDataHolder,PageInfo pageInfo) {
        this.pageInfo = pageInfo;
        this.readerDataHolder = readerDataHolder;
    }

    @Override
    public AddBookmarkRequest call() throws Exception {
        ContentSdkDataUtils.getDataProvider().addBookmark(createBookmark());
        LayoutProviderUtils.updateReaderViewInfo(readerDataHolder.getReader(), getReaderViewInfo(), readerDataHolder.getReader().getReaderHelper().getReaderLayoutManager());
        return this;
    }

    private Bookmark createBookmark() {
        Bookmark bookmark = new Bookmark();
        bookmark.setIdString(readerDataHolder.getReader().getReaderHelper().getDocumentMd5());
        bookmark.setApplication(readerDataHolder.getReader().getReaderHelper().getPlugin().displayName());
        bookmark.setPosition(pageInfo.getPosition());
        bookmark.setPageNumber(PagePositionUtils.getPageNumber(pageInfo.getName()));
        bookmark.setQuote(getQuote(readerDataHolder.getReader().getReaderHelper().getDocument().getPageText(pageInfo.getPosition())));
        return bookmark;
    }

    private String getQuote(String pageText) {
        if (StringUtils.isNullOrEmpty(pageText)) {
            return "";
        }
        if (pageText.length() <= 50) {
            return pageText;
        } else {
            return pageText.substring(0, 50);
        }
    }
}
