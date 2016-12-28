package com.onyx.kreader.plugins.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import com.onyx.android.sdk.data.model.Annotation;
import com.onyx.android.sdk.scribble.shape.Shape;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.kreader.api.*;
import com.onyx.android.sdk.utils.Benchmark;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.kreader.utils.PagePositionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhuzeng on 10/5/15.
 */
public class ImagesReaderPlugin implements ReaderPlugin,
        ReaderDocument,
        ReaderView,
        ReaderRenderer,
        ReaderNavigator,
        ReaderSearchManager,
        ReaderDrmManager,
        ReaderHitTestManager,
        ReaderRendererFeatures {

    private static final String TAG = ImagesReaderPlugin.class.getSimpleName();
    private Benchmark benchmark = new Benchmark();

    private ImagesWrapper impl;
    private String documentPath;
    private List<String> pageList = new ArrayList<String>();
    static private Set<String> extensionFilters = new HashSet<String>();

    public ImagesReaderPlugin(final Context context, final ReaderPluginOptions pluginOptions) {
    }

    public ImagesWrapper getPluginImpl() {
        if (impl == null) {
            impl = new ImagesAndroidWrapper();
        }
        return impl;
    }

    public String displayName() {
        return ImagesReaderPlugin.class.getSimpleName();
    }

    static public Set<String> getExtensionFilters() {
        if (extensionFilters.size() <= 0) {
            extensionFilters.add("png");
            extensionFilters.add("jpg");
            extensionFilters.add("jpeg");
            extensionFilters.add("bmp");
        }
        return extensionFilters;
    }

    static public boolean accept(final String path) {
        String extension = FileUtils.getFileExtension(path);
        return getExtensionFilters().contains(extension);
    }

    public ReaderDocument open(final String path, final ReaderDocumentOptions documentOptions, final ReaderPluginOptions pluginOptions) throws ReaderException {
        documentPath = path;
        final String baseDir = FileUtils.getParent(path);
        FileUtils.collectFiles(baseDir, getExtensionFilters(), false, pageList);
        return this;
    }

    public boolean supportDrm() {
        return false;
    }

    public ReaderDrmManager createDrmManager() {
        return null;
    }

    public boolean readMetadata(final ReaderDocumentMetadata metadata) {
        return false;
    }

    public boolean readCover(final Bitmap bitmap) {
        return false;
    }

    public RectF getPageOriginSize(final String position) {
        ImagesWrapper.ImageInformation imageInformation = getPluginImpl().imageInfo(pageList.get(Integer.parseInt(position)));
        if (imageInformation == null) {
            return null;
        }
        return new RectF(0, 0, imageInformation.getWidth(), imageInformation.getHeight());
    }

    @Override
    public boolean supportTextPage() {
        return false;
    }

    @Override
    public boolean isTextPage(String position) {
        return false;
    }

    @Override
    public String getPageText(String position) {
        return "";
    }

    @Override
    public ReaderSentence getSentence(String position, String sentenceStartPosition) {
        return null;
    }

    public void abortCurrentJob() {

    }

    public void clearAbortFlag() {

    }

    public boolean readTableOfContent(final ReaderDocumentTableOfContent toc) {
        return false;
    }

    @Override
    public boolean exportNotes(String sourceDocPath, String targetDocPath, List<Annotation> annotations, List<Shape> scribbles) {
        return false;
    }

    public ReaderView getView(final ReaderViewOptions viewOptions) {
        return this;
    }

    @Override
    public boolean saveOptions() {
        return false;
    }

    public ReaderRendererFeatures getRendererFeatures() {
        return this;
    }

    public void close() {
        getPluginImpl().closeAll();
        pageList.clear();
    }

    @Override
    public void updateDocumentOptions(ReaderDocumentOptions documentOptions, ReaderPluginOptions pluginOptions) {

    }

    public ReaderViewOptions getViewOptions() {
        return null;
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
        return null;
    }

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
        return null;
    }

    /**
     * Retrieve search interface.
     * @return
     */
    public ReaderSearchManager getSearchManager() {
        return this;
    }

    public boolean draw(final String pagePosition, final float scale, final int rotation, final Bitmap bitmap, final RectF displayRect, final RectF pageRect, final RectF visibleRect) {
        final String path = getImagePath(pagePosition);
        if (StringUtils.isNullOrEmpty(path)) {
            return false;
        }
        benchmark.restart();
        // getById doc position
        boolean ret = getPluginImpl().drawImage(path, scale, rotation, displayRect, pageRect, visibleRect, bitmap);
        Log.e(TAG, "rendering png:　"+ benchmark.duration());
        return ret;
    }

    /**
     * Retrieve the default init position.
     * @return
     */
    public String getInitPosition() {
        return getPositionByPageName(documentPath);
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
        int index = pageList.indexOf(pageName);
        if (index < 0 || index >= pageList.size()) {
            return null;
        }
        return PagePositionUtils.fromPageNumber(index);
    }

    @Override
    public int getPageNumberByPosition(String position) {
        return -1;
    }

    private String getImagePath(final String position) {
        int index = PagePositionUtils.getPageNumber(position);
        if (index < 0 || index >= pageList.size()) {
            return null;
        }
        return pageList.get(index);
    }

    /**
     * Return total page number.
     * @return 1 based total page number.
     */
    public int getTotalPage() {
        return pageList.size();
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

    @Override
    public boolean gotoPosition(String position) {
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
        return false;
    }

    public boolean searchNext(final ReaderSearchOptions options) {
        return false;
    }

    @Override
    public boolean searchInPage(int currentPage, ReaderSearchOptions options, boolean clear) {
        return false;
    }

    public List<ReaderSelection> searchResults() {
        return null;
    }


    public boolean acceptDRMFile(final String path) {
        return false;
    }

    public boolean registerDRMCallback(final ReaderDRMCallback callback) {
        return false;
    }

    public boolean activateDeviceDRM(String user, String password) {
        return false;
    }

    public boolean deactivateDeviceDRM() {
        return false;
    }

    public String getDeviceDRMAccount() {
        return "";
    }
    public boolean fulfillDRMFile(String path) {
        return false;
    }

    public ReaderSelection selectWordOnScreen(final ReaderHitTestArgs hitTest, final ReaderTextSplitter splitter) {

        return null;
    }

    public String position(final ReaderHitTestArgs hitTest) {
        return null;
    }

    public ReaderSelection selectOnScreen(final ReaderHitTestArgs start, final ReaderHitTestArgs end, ReaderHitTestOptions hitTestOptions) {
        return null;
    }

    @Override
    public ReaderSelection selectOnScreen(String pagePosition, String startPosition, String endPosition) {
        return null;
    }

    public boolean supportScale() {
        return true;
    }

    public boolean supportFontSizeAdjustment() {
        return false;
    }

    public boolean supportTypefaceAdjustment() {
        return false;
    }


}
