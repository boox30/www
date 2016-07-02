package com.onyx.android.sdk.common.request;

/**
 * Created by zhuzeng on 10/4/15.
 */
public abstract class BaseCallback {

    public static class ProgressInfo {

        public double progress;
    }

    public void start(final BaseRequest request) {
    }

    public void progress(final BaseRequest request, final ProgressInfo info) {
    }

    public abstract void done(final BaseRequest request, final Throwable e);

    public static void invoke(final BaseCallback callback, final BaseRequest request, final Throwable e) {
        if (callback != null) {
            callback.done(request, e);
        }
    }
}
