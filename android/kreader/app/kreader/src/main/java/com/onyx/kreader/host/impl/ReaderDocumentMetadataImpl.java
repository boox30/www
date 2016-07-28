package com.onyx.kreader.host.impl;

import android.support.annotation.NonNull;
import com.onyx.kreader.api.ReaderDocumentMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joy on 7/22/16.
 */
public class ReaderDocumentMetadataImpl implements ReaderDocumentMetadata {

    private String title;
    private String description;
    private ArrayList<String> authors = new ArrayList<>();
    private String publisher;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    @Override
    public List<String> getAuthors() {
        return authors;
    }

    @Override
    public String getPublisher() {
        return publisher;
    }

    @Override
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
