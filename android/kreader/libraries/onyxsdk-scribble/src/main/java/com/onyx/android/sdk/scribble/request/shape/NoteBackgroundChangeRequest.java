package com.onyx.android.sdk.scribble.request.shape;

import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;

/**
 * Created by solskjaer49 on 16/7/5 14:50.
 */

public class NoteBackgroundChangeRequest extends BaseNoteRequest {
    private int bgType;

    public NoteBackgroundChangeRequest(int background , boolean resume) {
        bgType = background;
        setPauseInputProcessor(true);
        setResumeInputProcessor(resume);
    }

    public void execute(final NoteViewHelper parent) throws Exception {
        parent.setBackground(bgType);
        renderCurrentPage(parent);
        updateShapeDataInfo(parent);
    }

}
