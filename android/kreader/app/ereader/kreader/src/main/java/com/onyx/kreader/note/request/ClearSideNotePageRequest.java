package com.onyx.kreader.note.request;

import com.onyx.android.sdk.data.PageInfo;
import com.onyx.kreader.note.NoteManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuzeng on 9/28/16.
 */

public class ClearSideNotePageRequest extends ReaderBaseNoteRequest {

    private volatile List<PageInfo> pageInfo = new ArrayList<>();
    public ClearSideNotePageRequest(final List<PageInfo> p) {
        pageInfo.addAll(p);
        setVisiblePages(pageInfo);
    }

    public void execute(final NoteManager noteManager) throws Exception {
        for (PageInfo p : pageInfo) {
            if (noteManager.isSidePage(p)) {
                noteManager.getNoteDocument().clearPage(getContext(), p.getRange(),
                        p.getSubPage());
            }
        }
        noteManager.setRenderBitmapDirty(true);
        renderVisiblePages(noteManager);
        getNoteDataInfo().setContentRendered(true);
        setResumeRawInputProcessor(noteManager.isDFBForCurrentShape());
    }
}