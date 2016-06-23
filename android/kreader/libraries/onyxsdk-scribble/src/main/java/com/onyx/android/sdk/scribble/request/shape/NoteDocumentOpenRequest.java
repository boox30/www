package com.onyx.android.sdk.scribble.request.shape;

import com.onyx.android.sdk.scribble.ShapeViewHelper;
import com.onyx.android.sdk.scribble.data.NoteDataProvider;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;

/**
 * Created by zhuzeng on 6/23/16.
 */
public class NoteDocumentOpenRequest extends BaseNoteRequest {

    private volatile String documentUniqueId;

    public NoteDocumentOpenRequest(final String id) {
        documentUniqueId = id;
    }

    public void execute(final ShapeViewHelper parent) throws Exception {
        parent.getNoteDocument().open(getContext(), documentUniqueId);
        getShapeDataInfo().updateShapePageMap(
                parent.getNoteDocument().getPageNameList(),
                parent.getNoteDocument().getCurrentPageIndex());
    }

}
