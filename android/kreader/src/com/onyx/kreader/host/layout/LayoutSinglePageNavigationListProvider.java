package com.onyx.kreader.host.layout;

import android.graphics.RectF;
import com.onyx.kreader.api.ReaderBitmap;
import com.onyx.kreader.api.ReaderException;
import com.onyx.kreader.host.impl.ReaderBitmapImpl;
import com.onyx.kreader.host.math.PositionSnapshot;
import com.onyx.kreader.host.navigation.NavigationArgs;
import com.onyx.kreader.host.navigation.NavigationList;
import com.onyx.kreader.host.options.ReaderStyle;
import com.onyx.kreader.utils.StringUtils;

/**
 * Created by zhuzeng on 10/19/15.
 * Single hard page with sub screen navigation list support.
 */
public class LayoutSinglePageNavigationListProvider extends LayoutProvider {

    private NavigationArgs navigationArgs;

    public LayoutSinglePageNavigationListProvider(final ReaderLayoutManager lm) {
        super(lm);
    }

    public void activate()  {

        //LayoutProviderUtils.addPage(layoutManager, layoutManager.getPositionManager().getCurrentPosition());
    }

    private NavigationArgs getNavigationArgs() {
        if (navigationArgs == null) {
            navigationArgs = new NavigationArgs(null, null);
        }
        return navigationArgs;
    }

    private NavigationList getNavigationList() {
        return getNavigationArgs().getList();
    }

    public boolean setNavigationArgs(final NavigationArgs args) throws ReaderException {
        navigationArgs = args;
        RectF subScreen = getNavigationList().first();
        getPageManager().scaleByRect(null, subScreen);
        return true;
    }

    public boolean prevScreen() throws ReaderException {
        if (getNavigationList().hasPrevious()) {
            RectF subScreen = getNavigationList().previous();
            getPageManager().scaleByRect(null, subScreen);
            return true;
        }
        return false;
    }

    public boolean nextScreen() throws ReaderException {
        if (getNavigationList().hasNext()) {
            RectF subScreen = getNavigationList().next();
            getPageManager().scaleByRect(null, subScreen);
            return true;
        }
        return false;
    }

    public boolean prevPage() throws ReaderException {
        return gotoPosition(LayoutProviderUtils.prevPage(getLayoutManager()));
    }

    public boolean nextPage() throws ReaderException {
        return gotoPosition(LayoutProviderUtils.nextPage(getLayoutManager()));
    }

    public boolean firstPage() throws ReaderException {
        return gotoPosition(LayoutProviderUtils.firstPage(getLayoutManager()));
    }

    public boolean lastPage() throws ReaderException {
        return gotoPosition(LayoutProviderUtils.lastPage(getLayoutManager()));
    }

    public boolean drawVisiblePages(ReaderBitmapImpl bitmap) throws ReaderException {
        LayoutProviderUtils.drawVisiblePages(getLayoutManager(), bitmap);
        return true;
    }

    public boolean setScale(final String pageName, float scale, float left, float top) throws ReaderException {
        getPageManager().setScale(pageName, scale);
        getPageManager().setViewportPosition(pageName, left, top);
        return true;
    }

    public void scaleToPage(final String pageName) throws ReaderException {
        getPageManager().scaleToPage(pageName);
    }

    public void scaleToWidth(final String pageName) throws ReaderException {
        getPageManager().scaleToWidth(pageName);
    }

    public boolean changeScaleWithDelta(float delta) throws ReaderException {
        return false;
    }

    public boolean changeScaleByRect(final String position, final RectF rect) throws ReaderException  {
        return false;
    }

    public boolean gotoPosition(final String location) throws ReaderException {
        if (StringUtils.isNullOrEmpty(location)) {
            return false;
        }
        LayoutProviderUtils.addNewSinglePage(getLayoutManager(), location);
        return true;
    }

    public boolean pan(int dx, int dy) throws ReaderException {
        LayoutProviderUtils.pan(getLayoutManager(), dx, dy);
        return true;
    }

    public boolean supportPreRender() throws ReaderException {
        return false;
    }
    public boolean supportSubScreenNavigation() {
        return false;
    }


    public boolean setStyle(final ReaderStyle style) throws ReaderException {
        return false;
    }

    public PositionSnapshot saveSnapshot() throws ReaderException {
        return null;
    }

    public void restoreBySnapshot(final PositionSnapshot snapshot) throws ReaderException {
    }

    public String renderingString() throws ReaderException {
        return null;
    }

    public RectF getPageRectOnViewport(final String position) throws ReaderException {
        return null;
    }

    public float getActualScale() throws ReaderException {
        return getPageManager().getActualScale();
    }

    public RectF getPageBoundingRect() throws ReaderException {
        return getPageManager().getPagesBoundingRect();
    }

    public RectF getViewportRect() throws ReaderException {
        return getPageManager().getViewportRect();
    }

    public void scaleByRect(final String pageName, final RectF child) throws ReaderException {
        getPageManager().scaleToViewport(pageName, child);
    }
}
