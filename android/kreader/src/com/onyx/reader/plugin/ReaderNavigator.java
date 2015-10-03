package com.onyx.reader.plugin;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Created by zhuzeng on 10/3/15.
 */
public interface ReaderNavigator {


    /**
     * Retrieve the default init position.
     * @return
     */
    public ReaderDocumentPosition getInitPosition();


    /**
     * Return total page number.
     * @return 1 based total page number.
     */
    public int getTotalPage();

    /**
     * Navigate to next screen.
     */
    public boolean nextScreen();

    /**
     * Navigate to prev screen.
     */
    public boolean prevScreen();

    /**
     * Navigate to next page.
     * @return
     */
    public boolean nextPage();

    /**
     * Navigate to prev page.
     * @return
     */
    public boolean prevPage();

    /**
     * Navigate to first page.
     * @return
     */
    public boolean firstPage();

    /**
     * Navigate to last page.
     * @return
     */
    public boolean lastPage();

    /**
     * Navigate to specified position.
     * @return
     */
    public boolean gotoPosition(final ReaderDocumentPosition position);

    /**
     * Set viewport.
     * @param viewport
     */
    public void setViewport(final RectF viewport);

    /**
     * Retrieve the viewport.
     * @return the current viewport.
     */
    public RectF getViewport();
}
