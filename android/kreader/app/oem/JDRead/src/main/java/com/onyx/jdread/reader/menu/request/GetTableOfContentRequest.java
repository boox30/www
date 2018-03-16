package com.onyx.jdread.reader.menu.request;

import com.onyx.android.sdk.reader.api.ReaderDocumentTableOfContent;
import com.onyx.android.sdk.reader.utils.ChapterInfo;
import com.onyx.android.sdk.reader.utils.TocUtils;
import com.onyx.jdread.reader.data.Reader;
import com.onyx.jdread.reader.layout.LayoutProviderUtils;
import com.onyx.jdread.reader.request.ReaderBaseRequest;

import java.util.List;

/**
 * Created by huxiaomao on 2018/1/30.
 */

public class GetTableOfContentRequest extends ReaderBaseRequest {
    private boolean hasToc;
    private List<ChapterInfo> readTocChapterNodeList;

    public GetTableOfContentRequest(Reader reader) {
        super(reader);
    }

    @Override
    public GetTableOfContentRequest call() throws Exception {
        LayoutProviderUtils.updateReaderViewInfo(getReader(),getReaderViewInfo(),getReader().getReaderHelper().getReaderLayoutManager());
        getReaderUserDataInfo().loadDocumentTableOfContent(getReader().getReaderHelper().getContext(), getReader().getReaderHelper().getDocument());

        ReaderDocumentTableOfContent toc = getReaderUserDataInfo().getTableOfContent();
        hasToc = toc != null && !toc.isEmpty();
        if (!hasToc) {
            return this;
        }
        readTocChapterNodeList = TocUtils.buildChapterNodeList(toc);
        updateSetting(getReader());
        return this;
    }

    public boolean isHasToc() {
        return hasToc;
    }

    public List<ChapterInfo> getReadTocChapterNodeList() {
        return readTocChapterNodeList;
    }
}