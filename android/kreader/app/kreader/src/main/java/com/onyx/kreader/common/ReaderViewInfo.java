package com.onyx.kreader.common;

import android.graphics.PointF;
import android.graphics.RectF;

import com.onyx.android.sdk.data.PageConstants;
import com.onyx.android.sdk.data.PageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengzhu on 2/22/16.
 */
public class ReaderViewInfo {

    private List<PageInfo> visiblePages = new ArrayList<PageInfo>();

    public boolean canPrevScreen;
    public boolean canNextScreen;
    public boolean canGoBack;
    public boolean canGoForward;
    public boolean supportReflow;
    public boolean supportScalable;
    public int scale;
    public RectF viewportInDoc = new RectF();
    public RectF pagesBoundingRect = new RectF();
    public PointF lastViewportOverlayPosition = null;
    public boolean layoutChanged = false;

    public final List<PageInfo> getVisiblePages() {
        return visiblePages;
    }

    public final PageInfo getFirstVisiblePage() {
        if (visiblePages.size() <= 0) {
            return null;
        }
        return visiblePages.get(0);
    }

    public final PageInfo getPageInfo(final String pageName) {
        for(PageInfo pageInfo : getVisiblePages()) {
            if (pageInfo.getName().equals(pageName)) {
                return pageInfo;
            }
        }
        return null;
    }

    public void copyPageInfo(final PageInfo pageInfo) {
        PageInfo copy = new PageInfo(pageInfo);
        visiblePages.add(copy);
    }


    public boolean canPan() {
        if (isSpecialScale()) {
            return false;
        }
        return pagesBoundingRect.width() > viewportInDoc.width() || pagesBoundingRect.height() > viewportInDoc.height();
    }

    /**
     * return null if there is no valid anchor point
     *
     * @return
     */
    public PointF getLastViewportOverlayPosition() {
        return lastViewportOverlayPosition;
    }

    public void setLastViewportOverlayPosition(PointF position) {
        lastViewportOverlayPosition = position;
    }

    public boolean isSpecialScale() {
        return PageConstants.isSpecialScale(scale);
    }

    public boolean isTextPages() {
        for (PageInfo pageInfo : visiblePages) {
            if (!pageInfo.isTextPage()) {
                return false;
            }
        }
        return true;
    }

}
