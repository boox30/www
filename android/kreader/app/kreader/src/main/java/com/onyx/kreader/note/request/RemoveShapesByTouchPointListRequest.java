package com.onyx.kreader.note.request;

import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.scribble.data.TouchPointList;
import com.onyx.kreader.note.NoteManager;
import com.onyx.kreader.note.data.ReaderNotePage;

import java.util.List;

/**
 * Created by zhuzeng on 9/30/16.
 */

public class RemoveShapesByTouchPointListRequest extends ReaderBaseNoteRequest {

    private volatile TouchPointList touchPointList;
    public RemoveShapesByTouchPointListRequest(final List<PageInfo> pageInfoList, final TouchPointList pointList) {
        setVisiblePages(pageInfoList);
        touchPointList = pointList;
    }

    public void execute(final NoteManager noteManager) throws Exception {
        final PageInfo pageInfo = getVisiblePages().get(0);
        final float radius = noteManager.getNoteDrawingArgs().eraserRadius / pageInfo.getActualScale();
        final ReaderNotePage notePage = noteManager.getNoteDocument().loadPage(getContext(), pageInfo.getName(), 0);
        if (notePage != null) {
            notePage.removeShapesByTouchPointList(touchPointList, radius);
        }
        getNoteDataInfo().setContentRendered(renderVisiblePages(noteManager));
        setResumeRawInputProcessor(noteManager.isDFBForCurrentShape());
        updateShapeDataInfo(noteManager);
    }
}
