package com.onyx.jdread.shop.cloud.entity;

/**
 * Created by jackdeng on 2018/1/12.
 */

public class SearchBooksRequestBean extends BaseRequestInfo {
    public String search_type;
    public String key_word;
    public String cid;
    public String filter;
    public String sort;
    public String page;
    public String page_size;
}