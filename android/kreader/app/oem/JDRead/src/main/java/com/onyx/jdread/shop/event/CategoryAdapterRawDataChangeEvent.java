package com.onyx.jdread.shop.event;

import com.onyx.jdread.shop.cloud.entity.jdbean.CategoryListResultBean;

import java.util.List;

/**
 * Created by jackdeng on 2018/1/2.
 */

public class CategoryAdapterRawDataChangeEvent {
    private List<CategoryListResultBean.CatListBean> data;

    public CategoryAdapterRawDataChangeEvent(List<CategoryListResultBean.CatListBean> data) {
        this.data = data;
    }

    public List<CategoryListResultBean.CatListBean> getData() {
        return data;
    }
}