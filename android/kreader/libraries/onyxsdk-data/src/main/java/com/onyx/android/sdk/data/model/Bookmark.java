package com.onyx.android.sdk.data.model;

import com.onyx.android.sdk.data.db.ContentDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Index;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * Created by zhuzeng on 6/3/16.
 */
@Table(database = ContentDatabase.class)
public class Bookmark extends BaseData {

    @Column
    private String quote = null;

    @Column
    private String application = null;

    @Column
    @Index
    private String position = null;

    @Column
    private int pageNumber = -1;

    public void setQuote(final String q) {
        quote = q;
    }

    public String getQuote() {
        return quote;
    }

    public void setApplication(final String app) {
        application = app;
    }

    public String getApplication() {
        return application;
    }

    public void setPosition(final String p) {
        position = p;
    }

    public String getPosition() {
        return position;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
