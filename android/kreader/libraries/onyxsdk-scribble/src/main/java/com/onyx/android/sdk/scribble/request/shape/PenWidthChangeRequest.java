package com.onyx.android.sdk.scribble.request.shape;

import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;

/**
 * Created by solskjaer49 on 16/7/5 14:51.
 */

public class PenWidthChangeRequest extends BaseNoteRequest {
    private volatile float penWidth;

    public PenWidthChangeRequest(float pw) {
        penWidth = pw;
        setPauseInputProcessor(true);
        setResumeInputProcessor(true);
    }

    public void execute(final NoteViewHelper parent) throws Exception {
        parent.setStrokeWidth(penWidth);
        renderCurrentPage(parent);
        updateShapeDataInfo(parent);
    }
}
