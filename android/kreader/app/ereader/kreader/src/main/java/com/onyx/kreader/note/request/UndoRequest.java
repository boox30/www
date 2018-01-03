package com.onyx.kreader.note.request;

import com.onyx.android.sdk.data.PageInfo;
import com.onyx.kreader.note.NoteManager;

/**
 * Created by zhuzeng on 9/23/16.
 */
public class UndoRequest extends ReaderBaseNoteRequest {

    public UndoRequest(final PageInfo p) {
        setVisiblePage(p);
    }

    public void execute(final NoteManager noteManager) throws Exception {
        noteManager.undo(getContext(), getVisiblePages().get(0).getRange(),
                getVisiblePages().get(0).getSubPage());
        noteManager.setRenderBitmapDirty(true);
        getNoteDataInfo().setContentRendered(renderVisiblePages(noteManager));
        setResumeRawInputProcessor(noteManager.isDFBForCurrentShape());
    }

}
