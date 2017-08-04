package com.onyx.android.sdk.scribble.asyncrequest.event;

import com.onyx.android.sdk.scribble.data.TouchPointList;

/**
 * Created by solskjaer49 on 2017/8/2 18:18.
 */

public class ShapeSelectTouchPointListReceivedEvent {
    public ShapeSelectTouchPointListReceivedEvent(TouchPointList touchPointList) {
        this.touchPointList = touchPointList;
    }

    public TouchPointList getTouchPointList() {
        return touchPointList;
    }

    private TouchPointList touchPointList;

}