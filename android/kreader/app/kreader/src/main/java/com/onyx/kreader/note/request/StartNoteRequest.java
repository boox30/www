package com.onyx.kreader.note.request;

import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.scribble.data.NoteDrawingArgs;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;
import com.onyx.android.sdk.scribble.shape.ShapeFactory;
import com.onyx.kreader.note.NoteManager;

import java.util.List;

/**
 * Created by zhuzeng on 10/6/16.
 */

public class StartNoteRequest extends ReaderBaseNoteRequest {

    public StartNoteRequest(final List<PageInfo> list) {
        setAbortPendingTasks(false);
        setVisiblePages(list);
        setResetNoteDataInfo(false);
    }

    public void execute(final NoteManager noteManager) throws Exception {
        ensureDocumentOpened(noteManager);
        setVisiblePages(getVisiblePages());
        noteManager.startRawEventProcessor();
        noteManager.enableRawEventProcessor(true);
        noteManager.setCurrentShapeType(NoteDrawingArgs.defaultShape());
        noteManager.setNoteDirty(true);
        getNoteDataInfo().setContentRendered(renderVisiblePages(noteManager));
        updateShapeDataInfo(noteManager);
    }

}
