package com.onyx.edu.homework.action.note;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.request.note.NoteDocumentOpenRequest;
import com.onyx.edu.homework.base.BaseNoteAction;
import com.onyx.edu.homework.event.RequestFinishedEvent;

/**
 * Created by zhuzeng on 6/27/16.
 */
public class DocumentOpenAction extends BaseNoteAction {

    private volatile String uniqueId;
    private volatile String parentUniqueId;
    private boolean create = false;

    public DocumentOpenAction(final String id, final String parent, boolean create) {
        uniqueId = id;
        parentUniqueId = parent;
        this.create = create;
    }

    @Override
    public void execute(final NoteViewHelper noteViewHelper, final BaseCallback baseCallback) {
        final NoteDocumentOpenRequest openRequest = new NoteDocumentOpenRequest(uniqueId, parentUniqueId, create);
        noteViewHelper.submit(getAppContext(), openRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (!create) {
                    noteViewHelper.post(RequestFinishedEvent.create(openRequest, e, true));
                }
                BaseCallback.invoke(baseCallback, request, e);
            }
        });
    }


}
