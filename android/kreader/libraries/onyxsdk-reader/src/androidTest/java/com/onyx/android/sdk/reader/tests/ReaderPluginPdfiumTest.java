package com.onyx.android.sdk.reader.tests;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.Suppress;
import com.onyx.android.sdk.api.ReaderBitmap;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.ReaderBitmapImpl;
import com.onyx.android.sdk.reader.api.ReaderRichMedia;
import com.onyx.android.sdk.utils.BitmapUtils;
import com.onyx.android.sdk.utils.HashUtils;
import com.onyx.android.sdk.utils.TestUtils;
import com.onyx.android.sdk.reader.api.ReaderDocument;
import com.onyx.android.sdk.reader.api.ReaderNavigator;
import com.onyx.android.sdk.reader.api.ReaderPlugin;
import com.onyx.android.sdk.reader.api.ReaderRenderer;
import com.onyx.android.sdk.reader.api.ReaderSentence;
import com.onyx.android.sdk.reader.api.ReaderView;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.reader.host.impl.ReaderDocumentMetadataImpl;
import com.onyx.android.sdk.reader.host.impl.ReaderViewOptionsImpl;
import com.onyx.android.sdk.reader.host.math.PageManager;
import com.onyx.android.sdk.reader.plugins.neopdf.NeoPdfReaderPlugin;

import java.util.List;

/**
 * Created by zhuzeng on 10/5/15.
 */
public class ReaderPluginPdfiumTest extends ApplicationTestCase<Application> {

    public ReaderPluginPdfiumTest() {
        super(Application.class);
    }

    @Suppress
    public void testPluginUsage() throws Exception {
        ReaderPlugin plugin = new NeoPdfReaderPlugin(getContext(), null);
        ReaderDocument document = plugin.open("/mnt/sdcard/Books/normal.pdf", null, null);
        assertNotNull(document);
        ReaderViewOptionsImpl viewOptions = new ReaderViewOptionsImpl(1024, 768);
        ReaderView readerView = document.getView(viewOptions);
        assertNotNull(readerView);
        ReaderBitmap readerBitmap = new ReaderBitmapImpl(viewOptions.getViewWidth(), viewOptions.getViewHeight(), Bitmap.Config.ARGB_8888);
        ReaderNavigator navigator = readerView.getNavigator();
        assertNotNull(navigator);
        ReaderRenderer renderer = readerView.getRenderer();
        assertNotNull(renderer);
        String initPosition = navigator.getInitPosition();
        assertNotNull(initPosition);
        assertTrue(renderer.draw(initPosition, -1, 0, new RectF(0, 0, readerBitmap.getBitmap().getWidth(), readerBitmap.getBitmap().getHeight()), null, null, readerBitmap.getBitmap()));
        document.close();
    }

    @Suppress
    public void testPluginRendering() throws Exception {
        ReaderPlugin plugin = new NeoPdfReaderPlugin(getContext(), null);
        ReaderDocument document = plugin.open("/mnt/sdcard/Books/normal.pdf", null, null);
        assertNotNull(document);

        ReaderViewOptionsImpl viewOptions = new ReaderViewOptionsImpl(TestUtils.randInt(1000, 2000), TestUtils.randInt(1000, 2000));
        ReaderView readerView = document.getView(viewOptions);
        assertNotNull(readerView);
        ReaderBitmap readerBitmap = new ReaderBitmapImpl(viewOptions.getViewWidth(), viewOptions.getViewHeight(), Bitmap.Config.ARGB_8888);
        ReaderNavigator navigator = readerView.getNavigator();
        assertNotNull(navigator);
        ReaderRenderer renderer = readerView.getRenderer();
        assertNotNull(renderer);
        String initPosition = navigator.getInitPosition();
        assertNotNull(initPosition);


        PageManager pageManager = new PageManager();
        RectF viewport = new RectF(0, 0, viewOptions.getViewWidth(), viewOptions.getViewHeight());
        pageManager.setViewportRect(viewport);

        RectF size = document.getPageOriginSize(initPosition);
        PageInfo pageInfo = new PageInfo(initPosition, size.width(), size.height());
        pageManager.add(pageInfo);
        pageManager.scaleToPage(pageInfo.getName());
        PageInfo result = pageManager.getFirstVisiblePage();
        RectF displayRect = result.getDisplayRect();
        assertTrue(renderer.draw(initPosition, result.getActualScale(), result.getPageDisplayOrientation(), displayRect, null, null, readerBitmap.getBitmap()));
        BitmapUtils.saveBitmap(readerBitmap.getBitmap(), "/mnt/sdcard/1.png");
        document.close();
    }

    @Suppress
    public void testReaderSentence() throws Exception {
        ReaderPlugin plugin = new NeoPdfReaderPlugin(getContext(), null);
        ReaderDocument document = plugin.open("/mnt/sdcard/Books/西游记.pdf", null, null);
        assertNotNull(document);

        String page = "1";
        ReaderSentence sentence = document.getSentence(page, "");
        assertNotNull(sentence);
        Debug.d("sentence text: " + sentence.getReaderSelection().getText());
        while (sentence != null && !sentence.isEndOfScreen() && !sentence.isEndOfDocument()) {
            sentence = document.getSentence(page, sentence.getNextPosition());
            Debug.d("sentence text: " + sentence.getReaderSelection().getText());
        }
    }

    public void testMetadata() throws Exception {
        ReaderPlugin plugin = new NeoPdfReaderPlugin(getContext(), null);
        ReaderDocument document = plugin.open("/mnt/sdcard/Books/pdf_reference_1-7.pdf", null, null);
        assertNotNull(document);

        assertTrue(document.readMetadata(new ReaderDocumentMetadataImpl()));
    }

    public void testCover() throws Exception {
        ReaderPlugin plugin = new NeoPdfReaderPlugin(getContext(), null);
        ReaderDocument document = plugin.open("/mnt/sdcard/Books/pdf_reference_1-7.pdf", null, null);
        assertNotNull(document);

        Bitmap bitmap = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888);
        assertTrue(document.readCover(bitmap));
        assertTrue(BitmapUtils.saveBitmap(bitmap, "/sdcard/cover.png"));
    }

    public void testRichMedia() throws Exception {
        ReaderPlugin plugin = new NeoPdfReaderPlugin(getContext(), null);
        ReaderDocument document = plugin.open("/mnt/sdcard/audio.pdf", null, null);
        assertNotNull(document);

        List<ReaderRichMedia> list = ((ReaderNavigator)document).getRichMedias("0");
        assertNotNull(list);
        assertEquals(list.size(), 2);
        for (ReaderRichMedia media : list) {
            Debug.e(getClass(), "name: %s, rect: %s, length: %d, md5: %s", media.getName(), media.getRectangle().toString(), media.getData().length, HashUtils.computeMD5(media.getData()));
        }
    }

}