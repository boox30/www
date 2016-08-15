package com.onyx.kreader.host.request;

import com.onyx.kreader.common.BaseReaderRequest;
import com.onyx.kreader.host.wrapper.Reader;

/**
 * Created by zhuzeng on 10/5/15.
 * redraw request
 */
public class RenderRequest extends BaseReaderRequest {

    public RenderRequest() {
        super();
    }

    public void execute(final Reader reader) throws Exception {
        prepareRenderBitmap(reader);
        reader.getReaderLayoutManager().drawVisiblePages(reader, getRenderBitmap(), createReaderViewInfo());
    }

}
