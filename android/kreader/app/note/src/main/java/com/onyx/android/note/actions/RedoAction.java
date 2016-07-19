package com.onyx.android.note.actions;

import com.onyx.android.note.activity.ScribbleActivity;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.scribble.request.shape.RedoRequest;
import com.onyx.android.sdk.scribble.request.shape.UndoRequest;

/**
 * Created by zhuzeng on 7/19/16.
 */
public class RedoAction <T extends ScribbleActivity> extends BaseNoteAction<T> {

    public RedoAction() {
    }

    public void execute(final T activity,  final BaseCallback callback) {
        final RedoRequest redoRequest = new RedoRequest();
        activity.getNoteViewHelper().submit(activity, redoRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                activity.onRequestFinished(redoRequest, true);
            }
        });
    }

}
