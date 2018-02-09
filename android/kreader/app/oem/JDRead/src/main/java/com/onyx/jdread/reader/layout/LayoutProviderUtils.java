package com.onyx.jdread.reader.layout;

import android.graphics.RectF;
import android.util.Log;


import com.onyx.android.sdk.api.ReaderBitmap;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.ReaderTextStyle;
import com.onyx.android.sdk.reader.api.ReaderDocumentTableOfContent;
import com.onyx.android.sdk.reader.api.ReaderException;
import com.onyx.android.sdk.reader.api.ReaderRenderer;
import com.onyx.android.sdk.reader.cache.BitmapReferenceLruCache;
import com.onyx.android.sdk.reader.cache.ReaderBitmapReferenceImpl;
import com.onyx.android.sdk.reader.common.ReaderDrawContext;
import com.onyx.android.sdk.reader.common.ReaderViewInfo;
import com.onyx.android.sdk.reader.host.math.PageManager;
import com.onyx.android.sdk.reader.host.math.PageUtils;
import com.onyx.android.sdk.reader.host.math.PositionSnapshot;
import com.onyx.android.sdk.reader.host.navigation.NavigationList;
import com.onyx.android.sdk.reader.utils.ChapterInfo;
import com.onyx.android.sdk.reader.utils.PagePositionUtils;
import com.onyx.android.sdk.reader.utils.TocUtils;
import com.onyx.android.sdk.utils.BitmapUtils;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.reader.data.Reader;

import java.util.List;

/**
 * Created by zhuzeng on 10/14/15.
 */
public class LayoutProviderUtils {

    private static final Class TAG = com.onyx.android.sdk.reader.host.layout.LayoutProviderUtils.class;
    static boolean ENABLE_CACHE = true;

    /**
     * draw all visible pages. For each page:render the visible part of page. in screen coordinates system.
     * Before draw, make sure all visible pages have been calculated correctly.
     *
     * @param reader
     * @param layoutManager
     * @param drawContext
     * @param readerViewInfo
     */
    static public void drawVisiblePages(final Reader reader,
                                        final ReaderLayoutManager layoutManager,
                                        final ReaderDrawContext drawContext,
                                        final ReaderViewInfo readerViewInfo) throws ReaderException {
        drawVisiblePages(reader, layoutManager, drawContext, readerViewInfo, true);
    }

    static public void drawVisiblePages(final Reader reader,
                                        final ReaderLayoutManager layoutManager,
                                        final ReaderDrawContext drawContext,
                                        final ReaderViewInfo readerViewInfo,
                                        final boolean enableCache) throws ReaderException {
        // step1: prepare.
        final ReaderRenderer renderer = reader.getReaderHelper().getRenderer();
        final BitmapReferenceLruCache cache = reader.getReaderHelper().getBitmapCache();
        List<PageInfo> visiblePages = layoutManager.getPageManager().collectVisiblePages();

        // step2: check cache.
        final String key = PositionSnapshot.cacheKey(visiblePages);
        boolean hitCache = false;
        if (ENABLE_CACHE && enableCache && checkCache(cache, key, drawContext)) {
            hitCache = true;
        }
        Debug.d(TAG, "hit cache: " + hitCache + ", " + key);
        if (!hitCache) {
            ReaderBitmapReferenceImpl freeBitmap = cache.getFreeBitmap(reader.getReaderViewHelper().getContentWidth(),
                    reader.getReaderViewHelper().getContentHeight(), ReaderBitmapReferenceImpl.DEFAULT_CONFIG);
            try {
                drawContext.renderingBitmap = new ReaderBitmapReferenceImpl();
                drawContext.renderingBitmap.attachWith(key, freeBitmap.getBitmapReference());
                drawContext.renderingBitmap.clear();
            } finally {
                freeBitmap.close();
            }
        }
        Debug.d(TAG, "rendering bitmap reference count: " + drawContext.renderingBitmap.getBitmapReference().getUnderlyingReferenceTestOnly().getRefCountTestOnly());

        // step3: render
        drawVisiblePagesImpl(renderer, layoutManager, drawContext, drawContext.renderingBitmap, visiblePages, hitCache);

        // final step: update view info.
        updateReaderViewInfo(reader, readerViewInfo, layoutManager);
    }

    static private void drawVisiblePagesImpl(final ReaderRenderer renderer,
                                             final ReaderLayoutManager layoutManager,
                                             final ReaderDrawContext drawContext,
                                             final ReaderBitmapReferenceImpl bitmap,
                                             final List<PageInfo> visiblePages,
                                             boolean hitCache) {

        final RectF pageRect = new RectF();
        final RectF visibleRect = new RectF();
        for (PageInfo pageInfo : visiblePages) {
            final String documentPosition = pageInfo.getName();
            final RectF displayRect = pageInfo.getDisplayRect();
            final RectF positionRect = pageInfo.getPositionRect();
            pageRect.set(positionRect);
            pageRect.offset(0, -positionRect.top);
            visibleRect.set(positionRect);
            visibleRect.intersect(layoutManager.getPageManager().getViewportRect());
            PageUtils.translateCoordinates(visibleRect, positionRect);
            if (!hitCache) {
                Debug.d(TAG, "draw visible page: " + documentPosition + ", scale: " + pageInfo.getActualScale() +
                        ", bitmap: " + bitmap.getBitmap().getWidth() + ", " + bitmap.getBitmap().getHeight() +
                        ", display rect: " + displayRect +
                        ", position rect: " + positionRect +
                        ", viewport: " + layoutManager.getPageManager().getViewportRect() +
                        ", page rect: " + pageRect +
                        ", visible rect: " + visibleRect);
                renderer.draw(documentPosition, pageInfo.getActualScale(),
                        pageInfo.getPageDisplayOrientation(), displayRect, pageRect, visibleRect, bitmap.getBitmap()
                );
            }
        }
    }

    static public void updateReaderViewInfo(final Reader reader,
                                            final ReaderViewInfo readerViewInfo,
                                            final ReaderLayoutManager layoutManager) throws ReaderException {
        if (!reader.getReaderHelper().getRendererFeatures().supportScale()) {
            updateVisiblePagesForFlowDocument(reader, readerViewInfo, layoutManager);
        }
        String pagePosition = layoutManager.getCurrentLayoutProvider().getCurrentPagePosition();
        if(readerViewInfo.isLoadToc()) {
            setChapterName(reader, readerViewInfo, pagePosition);
        }else {
            readerViewInfo.chapterName = reader.getDocumentInfo().getBookName();
        }

        final List<PageInfo> visiblePages = layoutManager.getPageManager().collectVisiblePages();
        for (PageInfo pageInfo : visiblePages) {
            pageInfo.setChapterName(readerViewInfo.chapterName);
            readerViewInfo.copyPageInfo(pageInfo);
        }
        readerViewInfo.isFixedDocument = layoutManager.getReaderRendererFeatures().supportScale();
        readerViewInfo.canPrevScreen = layoutManager.getCurrentLayoutProvider().canPrevScreen();
        readerViewInfo.canNextScreen = layoutManager.getCurrentLayoutProvider().canNextScreen();
        readerViewInfo.supportTextPage = layoutManager.getReaderDocument().supportTextPage();
        readerViewInfo.supportReflow = layoutManager.isSupportReflow();
        readerViewInfo.supportScalable = layoutManager.getCurrentLayoutProvider().supportScale();
        readerViewInfo.supportFontSizeAdjustment = layoutManager.getReaderRendererFeatures().supportFontSizeAdjustment();
        readerViewInfo.supportTypefaceAdjustment = layoutManager.getReaderRendererFeatures().supportTypefaceAdjustment();
        readerViewInfo.canGoBack = layoutManager.canGoBack();
        readerViewInfo.canGoForward = layoutManager.canGoForward();
        readerViewInfo.viewportInDoc.set(layoutManager.getViewportRect());
        if (layoutManager.getCropRect() != null) {
            RectF rect = new RectF(layoutManager.getCropRect());
            PageUtils.translateCoordinates(rect, readerViewInfo.viewportInDoc);
            readerViewInfo.cropRegionInViewport.set(rect);
        }
        readerViewInfo.pagesBoundingRect.set(layoutManager.getPageBoundingRect());
        readerViewInfo.scale = layoutManager.getSpecialScale();
        if (layoutManager.getTextStyleManager() != null && layoutManager.getTextStyleManager().getStyle() != null) {
            readerViewInfo.readerTextStyle = ReaderTextStyle.copy(layoutManager.getTextStyleManager().getStyle());
        }
        if (layoutManager.getCurrentLayoutProvider().getNavigationArgs() != null &&
                layoutManager.getCurrentLayoutProvider().getNavigationArgs().getList() != null) {
            readerViewInfo.subScreenCount = layoutManager.getCurrentLayoutProvider().getNavigationArgs().getList().getSubScreenCount();
            readerViewInfo.autoCropForEachBlock = layoutManager.getCurrentLayoutProvider().getNavigationArgs().isAutoCropForEachBlock();
        }
        readerViewInfo.layoutChanged = layoutManager.isLayoutChanged();
        readerViewInfo.setTotalPage(reader.getReaderHelper().getNavigator().getTotalPage());
    }

    private static void setChapterName(final Reader reader, final ReaderViewInfo readerViewInfo, String pagePosition) {
        ReaderDocumentTableOfContent toc = new ReaderDocumentTableOfContent();
        reader.getReaderHelper().getDocument().readTableOfContent(toc);
        boolean hasToc = toc != null && !toc.isEmpty();
        if (!hasToc) {
            readerViewInfo.setChapterName(reader.getDocumentInfo().getBookName());
            return;
        }

        List<ChapterInfo> readTocChapterNodeList = TocUtils.buildChapterNodeList(toc);
        int position = PagePositionUtils.getPosition(pagePosition);
        ChapterInfo chapterInfo = getChapterInfoByPage(position, readTocChapterNodeList);
        if (chapterInfo != null && StringUtils.isNotBlank(chapterInfo.getTitle())) {
            readerViewInfo.setChapterName(chapterInfo.getTitle());
        } else {
            readerViewInfo.setChapterName(reader.getDocumentInfo().getBookName());
        }
        readerViewInfo.setReadTocChapterNodeList(readTocChapterNodeList);
    }

    public static ChapterInfo getChapterInfoByPage(int pagePosition, List<ChapterInfo> tocChapterNodeList) {
        if (CollectionUtils.isNullOrEmpty(tocChapterNodeList)) {
            return null;
        }

        int size = tocChapterNodeList.size();
        int start = 0;
        ChapterInfo prevChapterInfo = null;
        prevChapterInfo = tocChapterNodeList.get(0);
        int end = prevChapterInfo.getPosition();
        if(pagePosition >= start && pagePosition <= end){
            return prevChapterInfo;
        }
        start = prevChapterInfo.getPosition();
        ChapterInfo chapterInfo = null;
        for (int i = 1; i < size; i++) {
            chapterInfo = tocChapterNodeList.get(i);
            if(pagePosition >= start && pagePosition < chapterInfo.getPosition()){
                return prevChapterInfo;
            }
            if(pagePosition == chapterInfo.getPosition()){
                return chapterInfo;
            }
            start = chapterInfo.getPosition() + 1;
            prevChapterInfo = chapterInfo;
        }
        return chapterInfo;
    }

    static private void updateVisiblePagesForFlowDocument(final Reader reader,
                                                          final ReaderViewInfo readerViewInfo,
                                                          final ReaderLayoutManager layoutManager) {
        clear(layoutManager);
        String startPage = PagePositionUtils.fromPageNumber(reader.getReaderHelper().getNavigator().getScreenStartPageNumber());
        addPage(layoutManager, startPage, reader.getReaderHelper().getNavigator().getScreenStartPosition(),
                reader.getReaderHelper().getNavigator().getScreenEndPosition());
        layoutManager.getPageManager().gotoPage(reader.getReaderHelper().getNavigator().getScreenStartPosition());
        for (int i = reader.getReaderHelper().getNavigator().getScreenStartPageNumber() + 1;
             i <= reader.getReaderHelper().getNavigator().getScreenEndPageNumber();
             i++) {
            String page = PagePositionUtils.fromPageNumber(i);
            String pos = reader.getReaderHelper().getNavigator().getPositionByPageNumber(i);
            addPage(layoutManager, page, pos, reader.getReaderHelper().getNavigator().getScreenEndPosition());
        }
        layoutManager.getPageManager().getVisiblePages().addAll(layoutManager.getPageManager().getPageInfoList());
    }

    static public PageInfo drawReflowablePage(final PageInfo pageInfo, final ReaderBitmap bitmap, final ReaderRenderer readerRenderer) {
        final RectF viewport = new RectF(0, 0, bitmap.getBitmap().getWidth(), bitmap.getBitmap().getHeight());
        if (!readerRenderer.draw(pageInfo.getPositionSafely(), 1.0f, 0, viewport, viewport, viewport, bitmap.getBitmap())) {
            return null;
        }
        return pageInfo;
    }

    /**
     * draw page with scale to page on specified bitmap.
     *
     * @param pageInfo
     * @param bitmap
     * @param readerRenderer
     */
    static public PageInfo drawPageWithScaleToPage(final PageInfo pageInfo, final ReaderBitmap bitmap, final ReaderRenderer readerRenderer) {
        final RectF viewport = new RectF(0, 0, bitmap.getBitmap().getWidth(), bitmap.getBitmap().getHeight());
        final float scale = PageUtils.scaleToPage(pageInfo.getOriginWidth(), pageInfo.getOriginHeight(), viewport.width(), viewport.height());

        final PageManager internalPageManager = new PageManager();
        internalPageManager.add(pageInfo);
        internalPageManager.setViewportRect(viewport);
        internalPageManager.scaleToPage(pageInfo.getPositionSafely());
        final PageInfo visiblePage = internalPageManager.getFirstVisiblePage();
        final RectF visibleRect = new RectF(visiblePage.getPositionRect());
        visibleRect.intersect(viewport);

        BitmapUtils.clear(bitmap.getBitmap());
        readerRenderer.draw(pageInfo.getPositionSafely(),
                scale,
                pageInfo.getPageDisplayOrientation(),
                visiblePage.getDisplayRect(), visiblePage.getPositionRect(), visibleRect, bitmap.getBitmap()
        );

        List<PageInfo> pageInfos = internalPageManager.collectVisiblePages();
        if (pageInfos.size() > 0) {
            return pageInfos.get(0);
        }
        return null;
    }

    static private boolean canReuse(ReaderBitmapReferenceImpl cache, ReaderDrawContext context) {
        boolean ignoreTextGamma = true;
        return (cache.isGammaIgnored() || cache.isGammaApplied(context.targetGammaCorrection)) &&
                cache.isEmboldenApplied(context.targetEmboldenLevel) &&
                (ignoreTextGamma || cache.isTextGammaApplied(context.targetTextGammaCorrection));
    }

    static public boolean checkCache(final BitmapReferenceLruCache cache, final String key, final ReaderDrawContext context) {
        ReaderBitmapReferenceImpl result = cache.get(key);
        if (result == null || !canReuse(result, context)) {
            return false;
        }

        result = result.clone();
        cache.remove(key);
        context.renderingBitmap = result;
        return true;
    }

    static public void clear(final ReaderLayoutManager layoutManager) {
        layoutManager.getPageManager().clear();
    }

    static public void addPage(final ReaderLayoutManager layoutManager, final String name) {
        addPage(layoutManager, name, name, name);
    }

    static public void addPage(final ReaderLayoutManager layoutManager, final String name, final String startPosition, String endPosition) {
        PageInfo pageInfo = layoutManager.getPageManager().getPageInfo(startPosition);
        if (pageInfo == null) {
            RectF size = layoutManager.getReaderDocument().getPageOriginSize(startPosition);
            pageInfo = new PageInfo(name, startPosition, endPosition, size.width(), size.height());
            pageInfo.setIsTextPage(layoutManager.getReaderDocument().isTextPage(startPosition));
        }
        layoutManager.getPageManager().add(pageInfo);
    }

    static public void addAllPage(final ReaderLayoutManager layoutManager) {
        int total = layoutManager.getNavigator().getTotalPage();
        LayoutProviderUtils.clear(layoutManager);
        for (int i = 0; i < total; ++i) {
            final String position = layoutManager.getNavigator().getPositionByPageNumber(i);
            LayoutProviderUtils.addPage(layoutManager, position);
        }
        LayoutProviderUtils.updatePageBoundingRect(layoutManager);
    }

    static public void addSinglePage(final ReaderLayoutManager layoutManager, final String name) {
        addSinglePage(layoutManager, name, name, name);
    }

    static public void addSinglePage(final ReaderLayoutManager layoutManager, final String name,
                                     final String startPosition, String endPosition) {
        LayoutProviderUtils.clear(layoutManager);
        LayoutProviderUtils.addPage(layoutManager, name, startPosition, endPosition);
        LayoutProviderUtils.updatePageBoundingRect(layoutManager);
        LayoutProviderUtils.resetViewportPosition(layoutManager);
    }

    static public void updatePageBoundingRect(final ReaderLayoutManager layoutManager) {
        layoutManager.getPageManager().updatePagesBoundingRect();
    }

    static public void updateVisiblePages(final ReaderLayoutManager layoutManager) {
        layoutManager.getPageManager().collectVisiblePages();
    }

    static public void resetViewportPosition(final ReaderLayoutManager layoutManager) {
        layoutManager.getPageManager().panViewportPosition(null, 0, 0);
    }

    static public boolean moveViewportByPosition(final ReaderLayoutManager layoutManager, final String location) {
        return layoutManager.getPageManager().gotoPage(location);
    }

    static public void pan(final ReaderLayoutManager layoutManager, final float dx, final float dy) {
        layoutManager.getPageManager().panViewportPosition(dx, dy);
    }

    static public boolean firstSubScreen(final ReaderLayoutManager layoutManager, final String pageName, final NavigationList navigationList) {
        RectF subScreen = navigationList.first();
        if (subScreen == null) {
            return false;
        }
        layoutManager.getPageManager().scaleByRatioRect(pageName, subScreen);
        return true;
    }

    static public boolean lastSubScreen(final ReaderLayoutManager layoutManager, final String pageName, final NavigationList navigationList) {
        RectF subScreen = navigationList.last();
        if (subScreen == null) {
            return false;
        }
        layoutManager.getPageManager().scaleByRatioRect(pageName, subScreen);
        return true;
    }

    static public String firstPage(final ReaderLayoutManager layoutManager) {
        return layoutManager.getNavigator().getPositionByPageNumber(0);
    }

    static public String lastPage(final ReaderLayoutManager layoutManager) {
        int total = layoutManager.getNavigator().getTotalPage();
        return layoutManager.getNavigator().getPositionByPageNumber(total - 1);
    }

    static public String nextPage(final ReaderLayoutManager layoutManager) {
        String currentPagePosition = layoutManager.getCurrentPagePosition();
        return layoutManager.getNavigator().nextPage(currentPagePosition);
    }

    static public String prevPage(final ReaderLayoutManager layoutManager) {
        String currentPagePosition = layoutManager.getCurrentPagePosition();
        return layoutManager.getNavigator().prevPage(currentPagePosition);
    }

    static public String nextScreen(final ReaderLayoutManager layoutManager) {
        PageInfo pageInfo = layoutManager.getPageManager().getFirstVisiblePage();
        if (pageInfo == null) {
            return null;
        }

        return layoutManager.getNavigator().nextScreen(pageInfo.getName());
    }

    static public String prevScreen(final ReaderLayoutManager layoutManager) {
        PageInfo pageInfo = layoutManager.getPageManager().getFirstVisiblePage();
        if (pageInfo == null) {
            return null;
        }

        return layoutManager.getNavigator().prevScreen(pageInfo.getName());
    }
}
