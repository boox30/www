package com.onyx.kreader.ui.events;

import android.content.Context;

import com.onyx.kreader.ui.data.ReaderDataHolder;

/**
 * Created by zhuzeng on 08/01/2017.
 */

public class PageChangedEvent {

    private String lastPage;
    private String currentPage;
    private int duration;
    private Context context;

    public PageChangedEvent() {
    }

    public static final PageChangedEvent beforePageChange(final ReaderDataHolder readerDataHolder) {
        final PageChangedEvent pageChangedEvent = new PageChangedEvent();
        pageChangedEvent.setLastPage(readerDataHolder.getReaderViewInfo().getFirstVisiblePageName());
        pageChangedEvent.setContext(readerDataHolder.getContext());
        return pageChangedEvent;
    }

    public void afterPageChange(final ReaderDataHolder readerDataHolder) {
        setCurrentPage(readerDataHolder.getReaderViewInfo().getFirstVisiblePageName());
    }

    public String getLastPage() {
        return lastPage;
    }

    public void setLastPage(String lastPage) {
        this.lastPage = lastPage;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
