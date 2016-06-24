package com.onyx.android.sdk.data;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by zhuzeng on 10/8/15.
 * Represent each page, could be scalable and reflowable.
 */
public class PageInfo {

    private String name;

    private int pageOrientation;      // degree 0, 90, 180, 270.
    private int pageDisplayOrientation = 0;

    private float originWidth;
    private float originHeight;

    private RectF autoCropContentRegion;    // content region with page origin size

    private RectF positionRect = new RectF(); // page position rect in document coordinates system with actual scale.
    private RectF displayRect = new RectF(); // page display rect in viewport(screen) coordinates system with actual scale.
    private float actualScale = 1.0f;
    private int specialScale = PageConstants.SCALE_INVALID;

    public PageInfo(final String string, final float nw, final float nh) {
        name = string;
        originWidth = nw;
        originHeight = nh;
        positionRect.set(0, 0, nw, nh);
    }

    public PageInfo(final PageInfo pageInfo) {
        name = pageInfo.getName();
        originWidth = pageInfo.getOriginWidth();
        originHeight = pageInfo.getOriginHeight();
        positionRect.set(pageInfo.positionRect);
        displayRect.set(pageInfo.displayRect);
        actualScale = pageInfo.actualScale;
        specialScale = pageInfo.specialScale;
    }

    public final float getOriginWidth() {
        return originWidth;
    }

    public final float getOriginHeight() {
        return originHeight;
    }

    public final RectF getAutoCropContentRegion() {
        return autoCropContentRegion;
    }

    public void setAutoCropContentRegion(final RectF region) {
        autoCropContentRegion = region;
    }

    public final RectF getPositionRect() {
        return positionRect;
    }

    public final float getScaledHeight() {
        return positionRect.height();
    }

    public final float getScaledWidth() {
        return positionRect.width();
    }

    public final float getActualScale() {
        return actualScale;
    }

    public void update(final float newScale, final float x, final float y) {
        setScale(newScale);
        setPosition(x, y);
    }

    public void setPosition(final float x, final float y) {
        positionRect.offsetTo(x, y);
    }

    public float getX() {
        return positionRect.left;
    }

    public float getY() {
        return positionRect.top;
    }

    public void setX(final float x) {
        positionRect.offsetTo(x, positionRect.top);
    }

    public void setY(final float y) {
        positionRect.offsetTo(positionRect.left, y);
    }

    public void setScale(final float newScale) {
        actualScale = newScale;
        positionRect.set(positionRect.left,
                positionRect.top,
                positionRect.left + originWidth * actualScale,
                positionRect.top + originHeight * actualScale);
    }

    public RectF updateDisplayRect(final RectF rect) {
        displayRect = new RectF(rect);
        return displayRect;
    }

    public RectF getDisplayRect() {
        return displayRect;
    }

    public int getPageDisplayOrientation() {
        return pageDisplayOrientation;
    }

    public void setName(final String n) {
        name = n;
    }

    public final String getName() {
        return name;
    }

    /**
     * To normalize point within display rect.
     * @return
     */
    public Matrix normalizeMatrix() {
        Matrix matrix = new Matrix();
        final float xScale = 1.0f / getDisplayRect().width() / getActualScale();
        final float yScale = 1.0f / getDisplayRect().height() / getActualScale();
        matrix.postTranslate(-getDisplayRect().left, -getDisplayRect().top);
        matrix.postScale(xScale, yScale);
        return matrix;
    }


}
