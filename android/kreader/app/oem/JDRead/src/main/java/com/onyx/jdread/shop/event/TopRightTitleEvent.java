package com.onyx.jdread.shop.event;

/**
 * Created by jackdeng on 2017/12/16.
 */

public class TopRightTitleEvent {

    private int tag;

    public TopRightTitleEvent(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }
}