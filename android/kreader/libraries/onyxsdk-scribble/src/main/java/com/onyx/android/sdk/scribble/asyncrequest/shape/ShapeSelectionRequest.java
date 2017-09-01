package com.onyx.android.sdk.scribble.asyncrequest.shape;

import com.onyx.android.sdk.scribble.asyncrequest.AsyncBaseNoteRequest;
import com.onyx.android.sdk.scribble.asyncrequest.NoteManager;
import com.onyx.android.sdk.scribble.asyncrequest.navigation.PageFlushRequest;
import com.onyx.android.sdk.scribble.data.TouchPoint;

/**
 * Created by john on 5/8/2017.
 */

public class ShapeSelectionRequest extends AsyncBaseNoteRequest {

    private static final String TAG = PageFlushRequest.class.getSimpleName();
    private volatile TouchPoint start;
    private volatile TouchPoint end;

    public ShapeSelectionRequest(final TouchPoint s, final TouchPoint e) {
        super();
        setAbortPendingTasks(false);
        setPauseInputProcessor(true);
        setResumeInputProcessor(false);
        start = new TouchPoint(s);
        end = new TouchPoint(e);
    }

    @Override
    public void execute(final NoteManager noteManager) throws Exception {
        if (!noteManager.getNoteDocument().isOpen()) {
            return;
        }
        benchmarkStart();
        setRender(true);
        renderCurrentPageInBitmap(noteManager);
        noteManager.renderSelectionRectangle(start, end);
    }
}
