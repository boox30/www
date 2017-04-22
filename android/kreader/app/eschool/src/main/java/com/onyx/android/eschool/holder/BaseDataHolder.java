package com.onyx.android.eschool.holder;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by suicheng on 2017/4/15.
 */

public class BaseDataHolder {

    private Context context;
    private EventBus eventBus = new EventBus();

    public BaseDataHolder(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}