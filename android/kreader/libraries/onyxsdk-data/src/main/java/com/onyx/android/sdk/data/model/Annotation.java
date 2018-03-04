package com.onyx.android.sdk.data.model;

import android.graphics.RectF;
import com.onyx.android.sdk.data.db.ContentDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuzeng on 6/3/16.
 */
@Table(database = ContentDatabase.class)
public class Annotation extends BaseData {

    @Column
    String quote = null;

    @Column
    String locationBegin = null;

    @Column
    String locationEnd = null;

    @Column
    String note = null;

    @Column
    String application = null;

    @Column
    String position = null;

    @Column
    int pageNumber = -1;

    @Column
    String chapterName;

    @Column
    String key;

    @Column(typeConverter = DBFlowTypeConverters.RectangleListConverter.class)
    private List rectangles = new ArrayList();

    public void setQuote(final String q) {
        quote = q;
    }

    public String getQuote() {
        return quote;
    }

    public void setLocationBegin(final String l) {
        locationBegin = l;
    }

    public String getLocationBegin() {
        return locationBegin;
    }

    public void setLocationEnd(final String l) {
        locationEnd = l;
    }

    public String getLocationEnd() {
        return locationEnd;
    }

    public void setNote(final String n) {
        note = n;
    }

    public final String getNote() {
        return note;
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

    public void setRectangles(final List<RectF> rectList) {
        rectangles.addAll(rectList);
    }

    public List<RectF> getRectangles() {
        return rectangles;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

