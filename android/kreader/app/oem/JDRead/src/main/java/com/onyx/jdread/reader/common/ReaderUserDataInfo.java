package com.onyx.jdread.reader.common;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;

import com.onyx.android.sdk.data.model.*;

import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.model.Annotation;
import com.onyx.android.sdk.data.model.Bookmark;
import com.onyx.android.sdk.data.model.SearchHistory;
import com.onyx.android.sdk.data.provider.SearchHistoryProvider;
import com.onyx.android.sdk.reader.api.ReaderChineseConvertType;
import com.onyx.android.sdk.reader.api.ReaderDocument;
import com.onyx.android.sdk.reader.api.ReaderDocumentCategory;
import com.onyx.android.sdk.reader.api.ReaderDocumentMetadata;
import com.onyx.android.sdk.reader.api.ReaderDocumentTableOfContent;
import com.onyx.android.sdk.reader.api.ReaderFormField;
import com.onyx.android.sdk.reader.api.ReaderFormRadioButton;
import com.onyx.android.sdk.reader.api.ReaderFormRadioGroup;
import com.onyx.android.sdk.reader.api.ReaderFormScribble;
import com.onyx.android.sdk.reader.api.ReaderImage;
import com.onyx.android.sdk.reader.api.ReaderNavigator;
import com.onyx.android.sdk.reader.api.ReaderSelection;
import com.onyx.android.sdk.reader.common.PageAnnotation;
import com.onyx.android.sdk.reader.dataprovider.ContentSdkDataUtils;
import com.onyx.android.sdk.reader.host.math.PageUtils;
import com.onyx.android.sdk.reader.host.wrapper.Reader;
import com.onyx.android.sdk.reader.utils.PagePositionUtils;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.android.sdk.utils.Debug;
import com.raizlabs.android.dbflow.sql.language.OrderBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuzeng on 6/7/16.
 */
public class ReaderUserDataInfo {
    private List<ReaderSelection> searchResult = new ArrayList<>();
    private ReaderSelection highlightResult = null;
    private boolean wordSelected = false;
    private PointF touchPoint;

    private String documentPath;
    private ReaderDocumentCategory documentCategory;
    private int documentCodePage;
    public ReaderChineseConvertType chineseConvertType = ReaderChineseConvertType.NONE;
    private ReaderDocumentMetadata documentMetadata;

    private ReaderDocumentTableOfContent toc;
    private Map<String, Bookmark> bookmarkMap = new LinkedHashMap<>();
    private List<Bookmark> bookmarks;
    private Map<String, Bookmark> pageBookmarkMap = new LinkedHashMap<>();
    private Map<String, List<Annotation>> annotationMap = new LinkedHashMap<>();
    private List<Annotation> annotationList = new ArrayList<>();
    private Map<String, List<PageAnnotation>> pageAnnotationMap = new HashMap<>();
    private List<SearchHistory> searchHistoryList = new ArrayList<>();
    private Map<String, List<ReaderSelection>> pageLinkMap = new HashMap<>();
    private Map<String, List<ReaderImage>> pageImageMap = new HashMap<>();
    private Map<String, List<ReaderFormField>> formFieldMap = new HashMap<>();

    public void setDocumentPath(final String path) {
        documentPath = path;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentCategory(ReaderDocumentCategory documentType) {
        this.documentCategory = documentType;
    }

    public ReaderDocumentCategory getDocumentCategory() {
        return documentCategory;
    }

    public void setDocumentCodePage(int documentCodePage) {
        this.documentCodePage = documentCodePage;
    }

    public int getDocumentCodePage() {
        return documentCodePage;
    }

    public void setChineseConvertType(ReaderChineseConvertType chineseConvertType) {
        this.chineseConvertType = chineseConvertType;
    }

    public ReaderChineseConvertType getChineseConvertType() {
        return chineseConvertType;
    }

    public void setDocumentMetadata(ReaderDocumentMetadata documentMetadata) {
        this.documentMetadata = documentMetadata;
    }

    public ReaderDocumentMetadata getDocumentMetadata() {
        return documentMetadata;
    }

    public boolean hasSearchResults() {
        List<ReaderSelection> list = getSearchResults();
        return list != null && list.size() > 0;
    }

    public List<ReaderSelection> getSearchResults() {
        return searchResult;
    }

    public void saveSearchResults(List<ReaderSelection> list) {
        searchResult = list;
    }

    public boolean hasHighlightResult() {
        return getHighlightResult() != null;
    }

    public ReaderSelection getHighlightResult() {
        return highlightResult;
    }

    public void saveHighlightResult(ReaderSelection selection) {
        highlightResult = selection;
    }

    public void setTableOfContent(ReaderDocumentTableOfContent toc) {
        this.toc = toc;
    }

    public ReaderDocumentTableOfContent getTableOfContent() {
        return toc;
    }

    public List<Annotation> getAnnotations() {
        ArrayList<Annotation> list = new ArrayList<>();
        Collection<List<Annotation>> values = annotationMap.values();
        for (List<Annotation> l : values) {
            list.addAll(l);
        }
        return list;
    }

    public List<Annotation> getAnnotationList() {
        return annotationList;
    }

    public boolean loadDocumentTableOfContent(final Context context, final ReaderDocument document) {
        toc = new ReaderDocumentTableOfContent();
        return document.readTableOfContent(toc);
    }

    public boolean loadDocumentAnnotations(final Context context, final String displayName,final String md5) {
        annotationList = ContentSdkDataUtils.getDataProvider().loadAnnotations(
                displayName,
                md5,
                OrderBy.fromProperty(Annotation_Table.updatedAt).descending());
        if (annotationList != null && annotationList.size() > 0) {
            for (Annotation annotation : annotationList) {
                if (annotationMap.get(annotation.getPosition()) == null) {
                    annotationMap.put(annotation.getPosition(), new ArrayList<Annotation>());
                }
                annotationMap.get(annotation.getPosition()).add(annotation);
            }
        }
        return true;
    }

    public boolean hasPageAnnotations(final PageInfo pageInfo) {
        List<PageAnnotation> list = getPageAnnotations(pageInfo);
        return list != null && list.size() > 0;
    }

    public List<PageAnnotation> getPageAnnotations(final PageInfo pageInfo) {
        return pageAnnotationMap.get(pageInfo.getName());
    }

    public boolean loadPageAnnotations(final Context context,final boolean isSupportScale, final String displayName,final String md5, final ReaderNavigator navigator,final List<PageInfo> visiblePages) {
        if (isSupportScale) {
            return loadPageAnnotationsForFixedDocument(context, displayName,md5, visiblePages);
        } else {
            return loadPageAnnotationsForFlowDocument(context, displayName,md5,navigator, visiblePages);
        }
    }

    public boolean loadPageAnnotationsForFixedDocument(final Context context, final String displayName,final String md5, final List<PageInfo> visiblePages) {
        for (PageInfo pageInfo : visiblePages) {
            final List<Annotation> annotations = ContentSdkDataUtils.getDataProvider().loadAnnotations(
                    displayName,
                    md5,
                    PagePositionUtils.getPageNumber(pageInfo.getName()),
                    OrderBy.fromProperty(Annotation_Table.pageNumber).ascending());
            if (annotations != null && annotations.size() > 0) {
                List<PageAnnotation> list = new ArrayList<>();
                for (Annotation annotation : annotations) {
                    list.add(new PageAnnotation(pageInfo, annotation));
                }
                pageAnnotationMap.put(pageInfo.getName(), list);
            }
        }
        return true;
    }

    public boolean loadPageAnnotationsForFlowDocument(final Context context, final String displayName,final String md5,final ReaderNavigator navigator,final List<PageInfo> visiblePages) {
        final List<Annotation> annotations = ContentSdkDataUtils.getDataProvider().loadAnnotations(
                displayName,
                md5,
                OrderBy.fromProperty(Annotation_Table.pageNumber).ascending());
        if (annotations != null && annotations.size() > 0) {
            String startPos = navigator.getScreenStartPosition();
            String endPos = navigator.getScreenEndPosition();
            for (Annotation annotation : annotations) {
                if (navigator.comparePosition(annotation.getLocationEnd(), startPos) < 0 ||
                        navigator.comparePosition(annotation.getLocationBegin(), endPos) > 0) {
                    continue;
                }
                if (navigator.comparePosition(annotation.getLocationBegin(), startPos) < 0) {
                    annotation.setPageNumber(navigator.getPageNumberByPosition(startPos));
                } else {
                    annotation.setPageNumber(navigator.getPageNumberByPosition(annotation.getLocationBegin()));
                }
                String pageName = PagePositionUtils.fromPageNumber(annotation.getPageNumber());
                PageInfo pageInfo = findPageInfo(visiblePages, pageName);
                if (pageInfo == null) {
                    continue;
                }
                if (pageAnnotationMap.get(pageName) == null) {
                    pageAnnotationMap.put(pageName, new ArrayList<PageAnnotation>());
                }
                pageAnnotationMap.get(pageName).add(new PageAnnotation(pageInfo, annotation));
            }
        }
        return true;
    }

    public boolean loadNoEmptySideNotePageList(final Reader reader) {
        return false;
    }

    private PageInfo findPageInfo(final List<PageInfo> visiblePages, String pageName) {
        for (PageInfo page : visiblePages) {
            if (page.getName().equals(pageName)) {
                return page;
            }
        }
        return null;
    }

    public boolean hasBookmark(final PageInfo pageInfo) {
        return pageBookmarkMap.containsKey(pageInfo.getName());
    }

    public Bookmark getBookmark(final PageInfo pageInfo) {
        return pageBookmarkMap.get(pageInfo.getName());
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public boolean loadPageBookmarks(final Context context, final boolean isSupportScale,final String displayName,final String md5,final ReaderNavigator navigator, final List<PageInfo> visiblePages) {
        if (isSupportScale) {
            return loadBookmarksForFixedDocument(context, displayName,md5, visiblePages);
        } else {
            return loadBookmarksForFlowDocument(context, displayName,md5,navigator, visiblePages);
        }
    }

    private boolean loadBookmarksForFixedDocument(final Context context, final String displayName,final String md5, final List<PageInfo> visiblePages) {
        for (PageInfo pageInfo : visiblePages) {
            final Bookmark bookmark = ContentSdkDataUtils.getDataProvider().loadBookmark(displayName,
                    md5, PagePositionUtils.getPageNumber(pageInfo.getName()));
            if (bookmark != null) {
                pageBookmarkMap.put(pageInfo.getName(), bookmark);
            }
        }
        return true;
    }

    private boolean loadBookmarksForFlowDocument(final Context context, final String displayName,final String md5, final ReaderNavigator navigator,final List<PageInfo> visiblePages) {
        List<Bookmark> bookmarks = ContentSdkDataUtils.getDataProvider().loadBookmarks(
                displayName,
                md5,
                OrderBy.fromProperty(Bookmark_Table.createdAt).descending());
        String startPos = navigator.getScreenStartPosition();
        String endPos = navigator.getScreenEndPosition();
        if (bookmarks != null) {
            for (Bookmark bookmark : bookmarks) {
                if (navigator.comparePosition(bookmark.getPosition(), startPos) >= 0 &&
                        navigator.comparePosition(bookmark.getPosition(), endPos) <=0) {
                    int page = navigator.getPageNumberByPosition(bookmark.getPosition());
                    bookmark.setPageNumber(page);
                    pageBookmarkMap.put(PagePositionUtils.fromPageNumber(page), bookmark);
                }
            }
        }
        return true;
    }

    public boolean loadDocumentBookmarks(final Context context, final String displayName,final String md5) {
        bookmarks = ContentSdkDataUtils.getDataProvider().loadBookmarks(
                displayName,
                md5,
                OrderBy.fromProperty(Bookmark_Table.createdAt).ascending());
        if (bookmarks != null) {
            for (Bookmark bookmark : bookmarks) {
                bookmarkMap.put(bookmark.getPosition(), bookmark);
            }
        }
        return true;
    }

    public boolean loadSearchHistory(final Context context, final String md5, int count){
        searchHistoryList = SearchHistoryProvider.getLatestSearchHistory(md5, count);
        return true;
    }

    public List<SearchHistory> getSearchHistoryList() {
        return searchHistoryList;
    }

    public boolean hasPageLinks(final PageInfo pageInfo) {
        return pageLinkMap.containsKey(pageInfo.getName());
    }

    public List<ReaderSelection> getPageLinks(final PageInfo pageInfo) {
        return pageLinkMap.get(pageInfo.getName());
    }

    public boolean loadPageLinks(final Context context, final ReaderNavigator navigator, final List<PageInfo> visiblePages) {
        for (PageInfo pageInfo : visiblePages) {
            List<ReaderSelection> list = navigator.getLinks(pageInfo.getName());
            if (!CollectionUtils.isNullOrEmpty(list)) {
                for (ReaderSelection link : list) {
                    translateToScreen(pageInfo, link.getRectangles());
                }
                pageLinkMap.put(pageInfo.getName(), list);
            }
        }
        return true;
    }

    public boolean hasPageImages(final PageInfo pageInfo) {
        return pageImageMap.containsKey(pageInfo.getName());
    }

    public List<ReaderImage> getPageImages(final PageInfo pageInfo) {
        return pageImageMap.get(pageInfo.getName());
    }

    public boolean loadPageImages(final Context context, final ReaderNavigator navigator, final List<PageInfo> visiblePages) {
        for (PageInfo pageInfo : visiblePages) {
            List<ReaderImage> list = navigator.getImages(pageInfo.getName());
            if (!CollectionUtils.isNullOrEmpty(list)) {
                for (ReaderImage image : list) {
                    translateToScreen(pageInfo, image.getRectangle());
                }
                pageImageMap.put(pageInfo.getName(), list);
            }
        }
        return true;
    }

    public boolean hasFormFields(final PageInfo pageInfo) {
        return formFieldMap.containsKey(pageInfo.getName());
    }

    public boolean hasScribbleFormFields(final PageInfo pageInfo) {
        if (hasFormFields(pageInfo)) {
            List<ReaderFormField> formFields = formFieldMap.get(pageInfo.getName());
            for (ReaderFormField formField : formFields) {
                if (formField instanceof ReaderFormScribble) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ReaderFormScribble> getReaderFormScribbles(final PageInfo pageInfo) {
        List<ReaderFormScribble> readerFormScribbles = new ArrayList<>();
        if (hasFormFields(pageInfo)) {
            List<ReaderFormField> formFields = formFieldMap.get(pageInfo.getName());
            for (ReaderFormField formField : formFields) {
                if (formField instanceof ReaderFormScribble) {
                    readerFormScribbles.add((ReaderFormScribble)formField);
                }
            }
        }
        return readerFormScribbles;
    }

    public List<ReaderFormField> getFormFields(final PageInfo pageInfo) {
        return formFieldMap.get(pageInfo.getName());
    }

    public boolean loadFormFields(final Context context, final Reader reader, final List<PageInfo> visiblePages) {

        return true;
    }

    private void translateToScreen(PageInfo pageInfo, RectF rect) {
        PageUtils.translate(pageInfo.getDisplayRect().left,
                pageInfo.getDisplayRect().top,
                pageInfo.getActualScale(),
                rect);
    }

    private void translateToScreen(PageInfo pageInfo, List<RectF> list) {
        for (RectF rect : list) {
            PageUtils.translate(pageInfo.getDisplayRect().left,
                    pageInfo.getDisplayRect().top,
                    pageInfo.getActualScale(),
                    rect);
        }
    }

    public PointF getTouchPoint() {
        return touchPoint;
    }

    public void setTouchPoint(PointF touchPoint) {
        this.touchPoint = touchPoint;
    }

    public boolean isWordSelected() {
        return wordSelected;
    }

    private void setWordSelected(boolean wordSelected) {
        this.wordSelected = wordSelected;
    }
}
