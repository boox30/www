package com.onyx.kreader.host.request;

import android.graphics.Color;
import com.onyx.android.sdk.api.ReaderBitmap;
import com.onyx.kreader.common.BaseReaderRequest;
import com.onyx.kreader.host.wrapper.Reader;

/**
 * Created by zhuzeng on 10/15/15.
 */
public class ReaderDocumentCoverRequest extends BaseReaderRequest {

    private ReaderBitmap cover;

    public ReaderDocumentCoverRequest(ReaderBitmap bitmap) {
        cover = bitmap;
    }

    public void execute(final Reader reader) throws Exception {
        cover.getBitmap().eraseColor(Color.WHITE);
        reader.getDocument().readCover(cover.getBitmap());
    }
}
