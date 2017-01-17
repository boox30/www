package com.onyx.kreader.tests;

import android.graphics.RectF;
import android.test.ActivityInstrumentationTestCase2;
import com.onyx.android.sdk.reader.api.*;
import com.onyx.android.sdk.reader.host.impl.ReaderViewOptionsImpl;
import com.onyx.android.sdk.reader.host.layout.PageCropper;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.reader.plugins.neopdf.NeoPdfReaderPlugin;

/**
 * Created by zhuzeng on 5/29/16.
 */
public class PageCropperTest extends ActivityInstrumentationTestCase2<ReaderTestActivity> {

    public PageCropperTest() {
        super(ReaderTestActivity.class);
    }

    public void testCrop() throws Exception {
        ReaderPlugin plugin = new NeoPdfReaderPlugin(getActivity(), null);
        ReaderDocument document = plugin.open("/mnt/sdcard/Books/normal.pdf", null, null);
        assertNotNull(document);
        ReaderViewOptionsImpl viewOptions = new ReaderViewOptionsImpl(1024, 768);
        ReaderView readerView = document.getView(viewOptions);
        assertNotNull(readerView);
        ReaderRenderer renderer = readerView.getRenderer();
        PageCropper cropper = new PageCropper(renderer);
        final String pageName = String.valueOf(0);
        final RectF size  = document.getPageOriginSize(pageName);
        PageInfo pageInfo = new PageInfo(pageName, size.width(), size.height());
        cropper.cropPage(1024, 768, pageInfo);

    }

}
