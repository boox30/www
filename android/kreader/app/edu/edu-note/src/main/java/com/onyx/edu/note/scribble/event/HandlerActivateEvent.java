package com.onyx.edu.note.scribble.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by solskjaer49 on 2017/7/20 15:18.
 */

public class HandlerActivateEvent {
    public List<Integer> getFunctionBarMenuFunctionIDList() {
        return mFunctionBarMenuFunctionIDList;
    }

    public List<Integer> getToolBarMenuFunctionIDList() {
        return mToolBarMenuFunctionIDList;
    }

    public HandlerActivateEvent(List<Integer> functionBarMenuFunctionIDList, List<Integer> toolBarMenuFunctionIDList) {
        mFunctionBarMenuFunctionIDList = functionBarMenuFunctionIDList;
        mToolBarMenuFunctionIDList = toolBarMenuFunctionIDList;
    }

    private List<Integer> mFunctionBarMenuFunctionIDList = new ArrayList<>();
    private List<Integer> mToolBarMenuFunctionIDList = new ArrayList<>();
}
