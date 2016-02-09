package com.onyx.kreader.api;

/**
 * Created by zhuzeng on 10/2/15.
 * Defined in host and used by plugin.
 */
public interface ReaderViewOptions {

    /**
     * Retrive view width.
     * @return the view width.
     */
    public int getViewWidth();

    /**
     * Retrieve the view height.
     * @return view height in pixel.
     */
    public int getViewHeight();

    /**
     * Retrieve the top margin.
     * @return top margin in pixel.
     */
    public int getTopMargin();

    /**
     * Retrieve the left margin.
     * @return left margin in pixel.
     */
    public int getLeftMargin();

    /**
     * Retrieve the bottom margin.
     * @return bottom margin in pixel.
     */
    public int getBottomMargin();

    /**
     * Retrieve the right margin.
     * @return right margin in pixel.
     */
    public int getRightMargin();


}
