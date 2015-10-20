package com.onyx.reader.host.math;

import android.graphics.PointF;
import android.graphics.RectF;
import com.onyx.reader.api.ReaderDocumentPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuzeng on 10/8/15.
 */
public class EntryManager {

    public static final int SCALE_TO_PAGE = -1;
    public static final int SCALE_TO_WIDTH = -2;
    public static final int ZOOM_TO_HEIGHT = -3;
    public static final int ZOOM_TO_PAGE_AUTO_CONTENT = -4;
    public static final int ZOOM_TO_WIDTH_AUTO_CONTENT = -5;
    public static final int ZOOM_TO_SCAN_REFLOW = -6;
    public static final int ZOOM_TO_REFLOW = -7;
    public static final int ZOOM_TO_COMICE = -8;
    public static final int ZOOM_TO_PAPER = -9;

    private int specialScale = 0;
    private float actualScale = 1.0f;
    private float topMargin, leftMargin, rightMargin, bottomMargin;
    private float spacing;

    private RectF hostRect = new RectF();
    private RectF viewportRect = new RectF();

    private List<EntryInfo> visible = new ArrayList<EntryInfo>();
    private List<EntryInfo> entryInfoList = new ArrayList<EntryInfo>();
    private Map<String, EntryInfo> entryInfoMap = new HashMap();

    public void clear() {
        entryInfoList.clear();
        hostRect.set(0, 0, 0, 0);
    }

    public void setViewport(final float x, final float y) {
        viewportRect.offsetTo(x, y);
        reboundViewport();
    }

    public void setViewportRect(final float left, final float top, final float right, final float bottom) {
        viewportRect.set(left, top, right, bottom);
        reboundViewport();
    }

    public final RectF getViewportRect() {
        return viewportRect;
    }

    public final RectF getHostRect() {
        return hostRect;
    }

    public void panViewport(final float dx, final float dy) {
        viewportRect.offset(dx, dy);
        reboundViewport();
    }

    private void reboundViewport() {
        EntryUtils.rebound(viewportRect, hostRect);
    }

    public boolean contains(final String name) {
        return entryInfoMap.containsKey(name);
    }

    public void add(final String name, final EntryInfo entryInfo) {
        entryInfo.setName(name);
        entryInfoMap.put(name, entryInfo);
        entryInfoList.add(entryInfo);
    }

    public boolean moveViewportByPosition(final String name) {
        EntryInfo entryInfo = entryInfoMap.get(name);
        if (entryInfo == null) {
            return false;
        }
        setViewport(entryInfo.getDisplayRect().left, entryInfo.getDisplayRect().top);
        return true;
    }

    public final EntryInfo getEntryInfo(final ReaderDocumentPosition position) {
        return entryInfoMap.get(position);
    }

    public final List<EntryInfo> getEntryInfoList() {
        return entryInfoList;
    }

    public void setScale(final float scale) {
        actualScale = scale;
        updateHostRect();
    }

    public boolean isSpecialScale() {
        return specialScale < 0;
    }

    public final float getActualScale() {
        return actualScale;
    }

    public boolean scaleToPage() {
        specialScale = SCALE_TO_PAGE;
        updateVisiblePages();
        if (visible.size() <= 0) {
            return false;
        }
        if (viewportRect.width() <= 0 || viewportRect.height() <= 0) {
            return false;
        }
        EntryInfo current = visible.get(0);
        setScale(EntryUtils.scaleToPage(current.getDisplayRect(), viewportRect));
        reboundViewport();
        return true;
    }

    public boolean scaleToWidth() {
        specialScale = SCALE_TO_WIDTH;
        updateVisiblePages();
        if (visible.size() <= 0) {
            return false;
        }
        if (viewportRect.width() <= 0 || viewportRect.height() <= 0) {
            return false;
        }
        EntryInfo current = visible.get(0);
        setScale(EntryUtils.scaleToWidth(current.getDisplayRect(), viewportRect));
        reboundViewport();
        return true;
    }

    /**
     * Scale the child rect to viewport.
     * @param child The user selected rect in host coordinates system.
     * @return true if succeed.
     */
    public boolean scaleToViewport(final RectF child) {
        updateVisiblePages();
        if (visible.size() <= 0) {
            return false;
        }
        if (viewportRect.width() <= 0 || viewportRect.height() <= 0) {
            return false;
        }

        setScale(actualScale * EntryUtils.scaleByRect(child, viewportRect));
        reboundViewport();
        return true;
    }

    public boolean scaleWithDelta(final float delta) {
        updateVisiblePages();
        if (visible.size() <= 0) {
            return false;
        }
        if (viewportRect.width() <= 0 || viewportRect.height() <= 0) {
            return false;
        }

        actualScale += EntryUtils.scaleWithDelta(getFirstVisibleEntry().getDisplayRect(), getViewportRect(), delta);
        setScale(actualScale);
        reboundViewport();
        return true;
    }


    public boolean scaleByRatio(final RectF ratio) {
        EntryInfo entryInfo = getFirstVisibleEntry();
        if (entryInfo == null) {
            return false;
        }
        if (viewportRect.width() <= 0 || viewportRect.height() <= 0) {
            return false;
        }

        setScale(EntryUtils.scaleByRatio(ratio, entryInfo.getNaturalRect(), viewportRect));
        reboundViewport();
        return false;
    }

    public EntryInfo hitTest(final float x, final float y) {
        for(EntryInfo entryInfo : visible) {
            if (entryInfo.getDisplayRect().contains(x, y)) {
                return entryInfo;
            }
        }
        return null;
    }

    /**
     * viewport to point in entry coodinates system.
     * @param viewportPoint
     * @return
     */
    public PointF viewportToEntry(final PointF viewportPoint) {
        for(EntryInfo entryInfo : visible) {
            if (entryInfo.getDisplayRect().contains(viewportPoint.x, viewportPoint.y)) {
                viewportPoint.offset(-entryInfo.getDisplayRect().left, -entryInfo.getDisplayRect().top);
                return viewportPoint;
            }
        }
        return null;
    }

    /**
     * search in entry list to get
     */
    public List<EntryInfo> updateVisiblePages() {
        visible.clear();
        boolean found = false;
        for(EntryInfo entryInfo : entryInfoList) {
            if (RectF.intersects(viewportRect, entryInfo.getDisplayRect())) {
                visible.add(entryInfo);
                found = true;
            } else if (found) {
                break;
            }
        }
        return visible;
    }

    public EntryInfo getFirstVisibleEntry() {
        List<EntryInfo> list = updateVisiblePages();
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }


    /**
     * calculate the host rectangle
     */
    public void updateHostRect() {
        float y = topMargin, maxWidth = 0;
        for(EntryInfo entryInfo : entryInfoList) {
            entryInfo.update(actualScale, 0, y);
            y += entryInfo.getDisplayHeight();
            if (maxWidth < entryInfo.getDisplayWidth()) {
                maxWidth = entryInfo.getDisplayWidth();
            }
            y += spacing;
        }
        maxWidth += leftMargin + rightMargin;
        hostRect.set(0, 0, maxWidth, y);
        for(EntryInfo entryInfo : entryInfoList) {
            float x = (maxWidth - entryInfo.getDisplayWidth()) / 2;
            entryInfo.setX(x);
        }
    }

    public boolean nextViewport() {
        if (viewportRect.bottom >= hostRect.bottom) {
            return false;
        }
        viewportRect.offset(0, viewportRect.height());
        reboundViewport();
        return true;
    }

    public boolean prevViewport() {
        if (viewportRect.top <= hostRect.top) {
            return false;
        }
        viewportRect.offset(0, -viewportRect.height());
        reboundViewport();
        return true;
    }


}
