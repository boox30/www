package com.onyx.jdread.shop.event;

import com.onyx.jdread.shop.cloud.entity.jdbean.ResultBookBean;

/**
 * Created by jackdeng on 2017/12/16.
 */

public class RecommendItemClickEvent {

    private ResultBookBean bookBean;

    public RecommendItemClickEvent(ResultBookBean bookBean) {
        this.bookBean = bookBean;
    }

    public ResultBookBean getBookBean() {
        return bookBean;
    }
}