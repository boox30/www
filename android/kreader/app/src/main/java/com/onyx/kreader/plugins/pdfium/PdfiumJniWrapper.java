package com.onyx.kreader.plugins.pdfium;

import android.graphics.Bitmap;
import com.onyx.kreader.api.ReaderSelection;
import com.onyx.kreader.utils.StringUtils;

import java.util.List;

/**
 * Created by zengzhu on 2/3/16.
 * javah -classpath ./bin/classes:/opt/adt-bundle-linux/sdk/platforms/android-8/android.jar:./com/onyx/kreader/plugins/pdfium/ -jni com.onyx.kreader.plugins.pdfium.PdfiumJniWrapper
 * http://cdn01.foxitsoftware.com/pub/foxit/manual/enu/FoxitPDF_SDK20_Guide.pdf
 * https://src.chromium.org/svn/trunk/src/pdf/pdfium/
 */
public class PdfiumJniWrapper {

    static{
        System.loadLibrary("onyx_pdfium");
    }

    static public long NO_ERROR = 0;
    static public long ERROR_UNKNOWN = 1;
    static public long ERROR_FILE_NOT_FOUND = 2;
    static public long ERROR_FILE_INVALID = 3;
    static public long ERROR_PASSWORD_INVALID = 4;
    static public long ERROR_SECURITY = 5;
    static public long ERROR_PAGE_NOT_FOUND = 6;

    private static int sPluginId = -1;

    private synchronized static int nextId() {
        sPluginId++;
        return sPluginId;
    }

    public native boolean nativeInitLibrary();
    public native boolean nativeDestroyLibrary();

    private native long nativeOpenDocument(int id, final String path, final String password);
    private native boolean nativeCloseDocument(int id);

    private native int nativeMetadata(int id, final String tag, byte [] data);

    private native int nativePageCount(int id);
    private native boolean nativePageSize(int id, int page, float []size);

    private native boolean nativeRenderPage(int id, int page, int x, int y, int width, int height, int rotation, final Bitmap bitmap);

    private native int nativeHitTest(int id, int page, int x, int y, int width, int height, int rotation, int startX, int startY, int endX, int endY, final PdfiumSelection selection);

    private native int nativeSelection(int id, int page, int x, int y, int width, int height, int rotation, int startCharIndex, int endCharIndex, final PdfiumSelection selection);

    private native int nativeSearchInPage(int id, int page, int x, int y, int width, int height, int rotation, final byte [] buffer, boolean caseSensitive, boolean matchWholeWord, final List<ReaderSelection> list);

    private native byte [] nativeGetPageText(int id, int page);

    private int id;
    private String filePath = null;

    public PdfiumJniWrapper() {
        id = nextId();
    }

    public String getFilePath() {
        return filePath;
    }

    public long openDocument(final String path, final String password) {
        filePath = path;
        return nativeOpenDocument(id, path, password);
    }

    public boolean closeDocument() {
        boolean succ = nativeCloseDocument(id);
        filePath = null;
        return succ;
    }

    public String metadataString(final String tag) {
        byte [] data  = new byte[4096];
        int size = nativeMetadata(id, tag, data);
        return StringUtils.utf16le(data);
    }

    public int pageCount() {
        return nativePageCount(id);
    }
    public boolean pageSize(int page, float []size) {
        return nativePageSize(id, page, size);
    }

    /**
     * // scale can be calculated from: xScale = widthInBitmap / naturalWidth; yScale = heightInBitmap / naturalHeight;
     * // position is xInBitmap, yInBitmap
     * @param page page index
     * @param xInBitmap content x position in bitmap
     * @param yInBitmap content x position in bitmap
     * @param widthInBitmap content rendering width
     * @param heightInBitmap content rendering height
     * @param bitmap target bitmap
     * @return
     */
    public boolean drawPage(int page, int xInBitmap, int yInBitmap, int widthInBitmap, int heightInBitmap, int rotation, final Bitmap bitmap) {
        return nativeRenderPage(id, page, xInBitmap, yInBitmap, widthInBitmap, heightInBitmap,  rotation, bitmap);
    }

    public int hitTest(int page, int x, int y, int width, int height, int rotation, int startX, int startY, int endX, int endY, final PdfiumSelection selection) {
        return nativeHitTest(id, page, x, y, width, height, rotation, startX, startY, endX, endY, selection);
    }

    public int selection(int page, int x, int y, int width, int height, int rotation, int startCharIndex, int endCharIndex, final PdfiumSelection selection) {
        return nativeSelection(id, page, x, y, width, height, rotation, startCharIndex, endCharIndex, selection);
    }

    public void searchInPage(int page, int x, int y, int width, int height, int rotation, final String text, boolean caseSensitive, boolean matchWholeWord, final List<ReaderSelection> list) {
        nativeSearchInPage(id, page, x, y, width, height, rotation, StringUtils.utf16leBuffer(text), caseSensitive, matchWholeWord, list);
    }

    public String getPageText(int page) {
        byte [] data = nativeGetPageText(id, page);
        if (data == null) {
            return null;
        }
        return StringUtils.utf16le(data);
    }

}
