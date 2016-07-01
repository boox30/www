package com.onyx.android.note.actions;

import com.onyx.android.note.activity.ScribbleActivity;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.scribble.request.navigation.PageNextRequest;
import com.onyx.android.sdk.scribble.request.navigation.PagePrevRequest;

/**
 * Created by zhuzeng on 7/1/16.
 */
public class GotoPrevPageAction<T extends ScribbleActivity> extends BaseNoteAction<T> {

    public GotoPrevPageAction() {
    }

    public void execute(final T activity,  final BaseCallback callback) {
        final PagePrevRequest prevRequest = new PagePrevRequest();
        activity.getNoteViewHelper().submit(activity, prevRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                activity.onRequestFinished(prevRequest, true);
            }
        });
    }
}
