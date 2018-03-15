package com.onyx.android.sdk.reader.plugins.neopdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.onyx.android.sdk.reader.api.ReaderCallback;
import com.onyx.android.sdk.reader.api.ReaderChineseConvertType;
import com.onyx.android.sdk.reader.api.ReaderDocumentCategory;
import com.onyx.android.sdk.reader.api.ReaderFormField;
import com.onyx.android.sdk.reader.api.ReaderFormManager;
import com.onyx.android.sdk.reader.api.ReaderImage;
import com.onyx.android.sdk.reader.api.ReaderRichMedia;
import com.onyx.android.sdk.reader.host.impl.ReaderDocumentMetadataImpl;
import com.onyx.android.sdk.reader.host.options.BaseOptions;
import com.onyx.android.sdk.utils.Benchmark;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.android.sdk.reader.api.ReaderDocument;
import com.onyx.android.sdk.reader.api.ReaderDocumentMetadata;
import com.onyx.android.sdk.reader.api.ReaderDocumentOptions;
import com.onyx.android.sdk.reader.api.ReaderDocumentTableOfContent;
import com.onyx.android.sdk.reader.api.ReaderDrmManager;
import com.onyx.android.sdk.reader.api.ReaderException;
import com.onyx.android.sdk.reader.api.ReaderHitTestArgs;
import com.onyx.android.sdk.reader.api.ReaderHitTestManager;
import com.onyx.android.sdk.reader.api.ReaderHitTestOptions;
import com.onyx.android.sdk.reader.api.ReaderNavigator;
import com.onyx.android.sdk.reader.api.ReaderPlugin;
import com.onyx.android.sdk.reader.api.ReaderPluginOptions;
import com.onyx.android.sdk.reader.api.ReaderRenderer;
import com.onyx.android.sdk.reader.api.ReaderRendererFeatures;
import com.onyx.android.sdk.reader.api.ReaderSearchManager;
import com.onyx.android.sdk.reader.api.ReaderSearchOptions;
import com.onyx.android.sdk.reader.api.ReaderSelection;
import com.onyx.android.sdk.reader.api.ReaderSentence;
import com.onyx.android.sdk.reader.api.ReaderTextSplitter;
import com.onyx.android.sdk.reader.api.ReaderTextStyleManager;
import com.onyx.android.sdk.reader.api.ReaderView;
import com.onyx.android.sdk.reader.api.ReaderViewOptions;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.data.ReaderTextStyle;
import com.onyx.android.sdk.reader.utils.PagePositionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuzeng on 10/5/15.
 */
public class NeoPdfReaderPlugin implements ReaderPlugin,
        ReaderDocument,
        ReaderView,
        ReaderRenderer,
        ReaderNavigator,
        ReaderSearchManager,
        ReaderTextStyleManager,
        ReaderDrmManager,
        ReaderFormManager,
        ReaderHitTestManager,
        ReaderRendererFeatures {

    private static final Class TAG = NeoPdfReaderPlugin.class;
    private static final int JDPDF_HEAD_LENGTH = 4;
    private static final int JDPDF_ONE_BYTE = 0x25;
    private static final int JDPDF_TWO_BYTE = 0x50;
    private static final int JDPDF_THREE_BYTE = 0x44;
    private static final int JDPDF_FOUR_BYTE = 0x46;
    private Benchmark benchmark = new Benchmark();

    private NeoPdfJniWrapper impl;
    private ReaderCallback readerCallback;
    private String documentPath;

    List<ReaderSelection> searchResults = new ArrayList<>();
    Map<String, List<ReaderSelection>> pageLinks = new HashMap<>();

    private ReaderViewOptions readerViewOptions;
    private boolean customFormEnabled;

    public NeoPdfReaderPlugin(final Context context, final ReaderPluginOptions pluginOptions) {
    }

    public NeoPdfJniWrapper getPluginImpl() {
        if (impl == null) {
            impl = new NeoPdfJniWrapper();
            impl.nativeInitLibrary();
        }
        return impl;
    }

    public String displayName() {
        return NeoPdfReaderPlugin.class.getSimpleName();
    }

    @Override
    public void setReaderCallback(ReaderCallback callback) {
        readerCallback = callback;
    }

    static public boolean accept(final String path) {
        String string = path.toLowerCase();
        if (string.endsWith(".pdf")) {
            return true;
        }
        if(isJDPDF(path)){
            return true;
        }
        return false;
    }

    public static boolean isJEB(final String path) {
        String extension = FileUtils.getFileExtension(path);
        if (extension.contentEquals("jeb")) {
            return true;
        }
        return false;
    }

    public static boolean isJDPDF(final String path) {
        boolean bRet = false;
        FileInputStream fs = null;
        try {
            if (isJEB(path)) {
                if (FileUtils.fileExist(path)) {
                    File file = new File(path);
                    fs = new FileInputStream(file);
                    byte[] head = new byte[JDPDF_HEAD_LENGTH];
                    fs.read(head, 0, JDPDF_HEAD_LENGTH);
                    if (head[0] == JDPDF_ONE_BYTE &&
                            head[1] == JDPDF_TWO_BYTE &&
                            head[2] == JDPDF_THREE_BYTE &&
                            head[3] == JDPDF_FOUR_BYTE) {
                        bRet = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeQuietly(fs);
        }
        return bRet;
    }

    @Override
    public float getProgress(String position) {
        int currentPage = PagePositionUtils.getPageNumber(position);
        float totalPage = getTotalPage();
        return (currentPage / totalPage) * 100;
    }

    public ReaderDocument open(final String path, final ReaderDocumentOptions documentOptions, final ReaderPluginOptions pluginOptions) throws ReaderException {
        String docPassword = "";
        String archivePassword = "";
        documentPath = path;
        if (documentOptions != null) {
            docPassword = documentOptions.getDocumentPassword();
            archivePassword = documentOptions.getDocumentPassword();
        }
        long ret = getPluginImpl().openDocument(path, docPassword);
        if (ret == NeoPdfJniWrapper.NO_ERROR) {
            if (documentOptions != null) {
                customFormEnabled = documentOptions.isCustomFormEnabled();
                getPluginImpl().setRenderFormFields(!customFormEnabled);
            }
            if (readerCallback != null) {
                readerCallback.onDocumentLoadSuccess();
            }
            return this;
        }
        if (ret == NeoPdfJniWrapper.ERROR_PASSWORD_INVALID) {
            throw ReaderException.passwordRequired();
        } else if (ret == NeoPdfJniWrapper.ERROR_UNKNOWN) {
            throw ReaderException.cannotOpen();
        }
        throw ReaderException.cannotOpen();
    }

    public boolean supportDrm() {
        return true;
    }

    public ReaderDrmManager createDrmManager() {
        return this;
    }

    public boolean readMetadata(final ReaderDocumentMetadata metadata) {
        metadata.setTitle(getPluginImpl().metadataString("Title"));
        metadata.getAuthors().add(getPluginImpl().metadataString("Author"));
        metadata.setOptions(getPluginImpl().metadataString("OnyxOptions"));
        return true;
    }

    public boolean readCover(final Bitmap bitmap) {
        return getPluginImpl().drawPage(0, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                0, bitmap);
    }

    public RectF getPageOriginSize(final String position) {
        float size [] = {0, 0};
        getPluginImpl().pageSize(Integer.parseInt(position), size);
        return new RectF(0, 0, size[0], size[1]);
    }

    @Override
    public boolean supportTextPage() {
        return true;
    }

    @Override
    public boolean isTextPage(String position) {
        return getPluginImpl().isTextPage(Integer.parseInt(position));
    }

    @Override
    public String getPageText(String position) {
        return getPluginImpl().getPageText(PagePositionUtils.getPageNumber(position));
    }

    @Override
    public ReaderSentence getSentence(String position, String sentenceStartPosition) {
        int page = PagePositionUtils.getPageNumber(position);
        int startIndex = StringUtils.isNullOrEmpty(sentenceStartPosition) ? 0 :
                Integer.parseInt(sentenceStartPosition);
        return getPluginImpl().getSentence(page, startIndex);
    }

    public void abortCurrentJob() {

    }

    public void clearAbortFlag() {

    }

    @Override
    public void abortBookLoadingJob() {

    }

    public boolean readTableOfContent(final ReaderDocumentTableOfContent toc) {
        return getPluginImpl().getTableOfContent(toc.getRootEntry());
    }

    public ReaderView getView(final ReaderViewOptions viewOptions) {
        readerViewOptions = viewOptions;
        return this;
    }

    @Override
    public boolean readBuiltinOptions(BaseOptions options) {
        ReaderDocumentMetadata metadata = new ReaderDocumentMetadataImpl();
        if (!readMetadata(metadata)) {
            return false;
        }
        String optionsJson = metadata.getOptions();
        if (StringUtils.isBlank(optionsJson)) {
            return false;
        }
        try {
            JSONObject object = JSON.parseObject(optionsJson);
            if (object.containsKey("textGamma")) {
                options.setTextGamma(object.getIntValue("textGamma"));
            }
            if (object.containsKey("documentCategory")) {
                ReaderDocumentCategory category = ReaderDocumentCategory.valueOf(object.getString("documentCategory").toUpperCase());
                options.setDocumentCategory(category);
            }
            return true;
        } catch (Throwable tr) {
            return false;
        }
    }

    @Override
    public boolean saveOptions() {
        return true;
    }

    public ReaderRendererFeatures getRendererFeatures() {
        return this;
    }

    @Override
    public void setChineseConvertType(ReaderChineseConvertType convertType) {

    }

    @Override
    public void setTextGamma(float gamma) {
        getPluginImpl().setTextGamma(gamma);
    }

    public void close() {
        getPluginImpl().closeDocument();
    }

    @Override
    public void updateDocumentOptions(ReaderDocumentOptions documentOptions, ReaderPluginOptions pluginOptions) {

    }

    public ReaderViewOptions getViewOptions() {
        return readerViewOptions;
    }

    /**
     * Retrieve renderer.
     * @return the renderer.
     */
    public ReaderRenderer getRenderer() {
        return this;
    }

    /**
     * Retrieve the navigator.
     * @return
     */
    public ReaderNavigator getNavigator() {
        return this;
    }

    /**
     * Retrieve text style interface.
     */
    public ReaderTextStyleManager getTextStyleManager() {
        return this;
    }

    @Override
    public ReaderTextStyle getStyle() {
        return null;
    }

    /**
     * set stream document style. ignore.
     * @param style
     */
    public void setStyle(final ReaderTextStyle style) {}

    /**
     * Retrieve reader hit test.
     */
    public ReaderHitTestManager getReaderHitTestManager() {
        return this;
    }

    /**
     * Retrieve current visible links.
     * @return
     */
    public List<ReaderSelection> getLinks(final String position) {
        List<ReaderSelection> list = pageLinks.get(position);
        if (list == null) {
            list = new ArrayList<>();
            int page = PagePositionUtils.getPageNumber(position);
            if (!getPluginImpl().getPageLinks(page, list)) {
                return list;
            }
            pageLinks.put(position, list);
        }
        ArrayList<ReaderSelection> copy = new ArrayList<>();
        for (ReaderSelection link : list) {
            copy.add(((NeoPdfSelection)link).clone());
        }
        return copy;
    }

    @Override
    public List<ReaderImage> getImages(String position) {
        return null;
    }

    @Override
    public List<ReaderRichMedia> getRichMedias(String position) {
        int page = PagePositionUtils.getPageNumber(position);

        List<ReaderRichMedia> list = new ArrayList<>();
        if (!getPluginImpl().getPageRichMedias(page, list)) {
            return null;
        }

        return list;
    }

    /**
     * Retrieve search interface.
     * @return
     */
    public ReaderSearchManager getSearchManager() {
        return this;
    }

    @Override
    public ReaderFormManager getFormManager() {
        return this;
    }

    public boolean draw(final String pagePosition, final float scale, final int rotation, final RectF displayRect, final RectF pageRect, final RectF visibleRect, final Bitmap bitmap) {
        benchmark.restart();
        boolean ret = getPluginImpl().drawPage(PagePositionUtils.getPageNumber(pagePosition),
                (int)displayRect.left,
                (int)displayRect.top,
                (int)displayRect.width(),
                (int)displayRect.height(),
                rotation, bitmap);
        Debug.d(TAG, "rendering takes: " + benchmark.duration());
        return ret;
    }

    /**
     * Retrieve the default init position.
     * @return
     */
    public String getInitPosition() {
        return firstPage();
    }

    /**
     * Get position from page number
     * @param pageNumber The 0 based page number.
     * @return
     */
    public String getPositionByPageNumber(int pageNumber) {
        return PagePositionUtils.fromPageNumber(pageNumber);
    }

    public String getPositionByPageName(final String pageName) {
        return pageName;
    }

    @Override
    public int getPageNumberByPosition(String position) {
        return PagePositionUtils.getPageNumber(position);
    }

    /**
     * Return total page number.
     * @return 1 based total page number.
     */
    public int getTotalPage() {
        return getPluginImpl().pageCount();
    }

    @Override
    public int getScreenStartPageNumber() {
        return 0;
    }

    @Override
    public int getScreenEndPageNumber() {
        return 0;
    }

    @Override
    public String getScreenStartPosition() {
        return null;
    }

    @Override
    public String getScreenEndPosition() {
        return null;
    }

    @Override
    public int comparePosition(String pos1, String pos2) {
        return 0;
    }

    /**
     * Navigate to specified position.
     * @return
     */
    public boolean gotoPosition(final String position) {
        return false;
    }

    @Override
    public boolean gotoPage(int page) {
        return false;
    }

    /**
     * Navigate to next screen.
     */
    public String nextScreen(final String position) {
        return nextPage(position);
    }

    /**
     * Navigate to previous screen.
     */
    public String prevScreen(final String position) {
        return prevPage(position);
    }

    /**
     * Navigate to next page.
     * @return
     */
    public String nextPage(final String position) {
        int pn = PagePositionUtils.getPageNumber(position);
        if (pn + 1 < getTotalPage()) {
            return PagePositionUtils.fromPageNumber(pn + 1);
        }
        return null;
    }

    /**
     * Navigate to previous page.
     * @return
     */
    public String prevPage(final String position) {
        int pn = PagePositionUtils.getPageNumber(position);
        if (pn > 0) {
            return PagePositionUtils.fromPageNumber(pn - 1);
        }
        return null;

    }

    @Override
    public boolean isFirstPage() {
        return false;
    }

    /**
     * Navigate to first page.
     * @return
     */
    public String firstPage() {
        return PagePositionUtils.fromPageNumber(0);
    }

    @Override
    public boolean isLastPage() {
        return false;
    }

    /**
     * Navigate to last page.
     * @return
     */
    public String lastPage() {
        return PagePositionUtils.fromPageNumber(getTotalPage() - 1);
    }

    public boolean searchPrevious(final ReaderSearchOptions options) {
        searchResults.clear();
        int page = Integer.parseInt(options.fromPage());
        for (int i = page; i >= 0; i--) {
            if (searchInPage(i, options,false)) {
                return true;
            }
        }
        return false;
    }

    public boolean searchNext(final ReaderSearchOptions options) {
        searchResults.clear();
        int page = Integer.parseInt(options.fromPage());
        for (int i = page; i < getTotalPage(); i++) {
            if (searchInPage(i, options,false)) {
                return true;
            }
        }
        return false;
    }

    public boolean searchInPage(final int currentPage, final ReaderSearchOptions options, boolean clear) {
        if (clear){
            searchResults.clear();
        }
        getPluginImpl().searchInPage(currentPage, 0, 0,
                getViewOptions().getViewWidth(),
                getViewOptions().getViewHeight(),
                0, options.pattern(), options.isCaseSensitive(),
                options.isMatchWholeWord(), options.contextLength(),
                searchResults);
        return searchResults.size() > 0;
    }

    public List<ReaderSelection> searchResults() {
        return searchResults;
    }


    public boolean activateDeviceDRM(String deviceId, String certificate) {
        return getPluginImpl().activateDeviceDRM(deviceId, certificate);
    }

    public ReaderSelection selectWordOnScreen(final ReaderHitTestArgs hitTest, final ReaderTextSplitter splitter) {

        return null;
    }

    public String position(final ReaderHitTestArgs hitTest) {
        return null;
    }

    public ReaderSelection selectOnScreen(final ReaderHitTestArgs start, final ReaderHitTestArgs end, ReaderHitTestOptions hitTestOptions) {
        NeoPdfSelection selection = new NeoPdfSelection(start.pageName);
        getPluginImpl().hitTest(PagePositionUtils.getPageNumber(start.pageName),
                (int) start.pageDisplayRect.left,
                (int) start.pageDisplayRect.top,
                (int) start.pageDisplayRect.width(),
                (int) start.pageDisplayRect.height(),
                start.pageDisplayOrientation,
                (int) start.point.x,
                (int) start.point.y,
                (int) end.point.x,
                (int) end.point.y,
                hitTestOptions.isSelectingWord(),
                selection);
        return selection;
    }

    @Override
    public ReaderSelection selectOnScreen(String pagePosition, String startPosition, String endPosition) {
        int page = PagePositionUtils.getPageNumber(pagePosition);
        int start = PagePositionUtils.getPosition(startPosition);
        int end = PagePositionUtils.getPosition(endPosition);

        float[] size = new float[2];
        getPluginImpl().pageSize(page, size);

        NeoPdfSelection selection = new NeoPdfSelection(pagePosition);
        getPluginImpl().selection(page, 0, 0, (int)size[0], (int)size[1], 0, start, end, selection);
        return selection;
    }

    @Override
    public List<ReaderSelection> allText(final String pagePosition) {
        int page = PagePositionUtils.getPageNumber(pagePosition);
        final List<ReaderSelection> list = new ArrayList<>();
        getPluginImpl().getPageTextRegions(page, list);
        return list;
    }

    public boolean supportScale() {
        if (StringUtils.isNullOrEmpty(documentPath)) {
            return false;
        }
        if (documentPath.toLowerCase().endsWith("pdf")) {
            return true;
        }
        if (isJDPDF(documentPath)) {
            return true;
        }
        return false;
    }

    public boolean supportFontSizeAdjustment() {
        return false;
    }

    @Override
    public boolean supportFontGammaAdjustment() {
        return true;
    }

    public boolean supportTypefaceAdjustment() {
        return false;
    }

    @Override
    public boolean supportConvertBetweenSimplifiedAndTraditionalChineseText() {
        return false;
    }

    public boolean isCustomFormEnabled() {
        return customFormEnabled;
    }

    @Override
    public boolean loadFormFields(int page, List<ReaderFormField> fields) {
        return getPluginImpl().loadFormFields(page, fields);
    }
}
