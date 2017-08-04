package com.onyx.edu.reader.note.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.scribble.shape.Shape;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.edu.reader.R;
import com.onyx.edu.reader.note.NoteManager;
import com.onyx.edu.reader.note.request.FlushShapeListRequest;
import com.onyx.edu.reader.note.request.FlushSignatureShapesRequest;
import com.onyx.edu.reader.ui.actions.BaseAction;
import com.onyx.edu.reader.ui.data.ReaderDataHolder;
import com.onyx.edu.reader.ui.events.ShapeRenderFinishEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuzeng on 9/22/16.
 */
public class FlushNoteAction extends BaseAction {

    private List<PageInfo> visiblePages = new ArrayList<>();
    private boolean render;
    private boolean save;
    private boolean showDialog;
    private boolean transfer;
    private boolean pauseNote = false;

    public FlushNoteAction(List<PageInfo> list, boolean renderShapes, boolean transferBitmap, boolean saveToDatabase, boolean show) {
        render = renderShapes;
        save = saveToDatabase;
        if (list != null) {
            visiblePages.addAll(list);
        }
        showDialog = show;
        transfer = transferBitmap;
    }

    public static FlushNoteAction pauseAfterFlush(List<PageInfo> list) {
        final FlushNoteAction flushNoteAction = new FlushNoteAction(list, true, true, false, false);
        flushNoteAction.pauseNote = true;
        return flushNoteAction;
    }


    public void execute(final ReaderDataHolder readerDataHolder, final BaseCallback callback) {
        final NoteManager noteManager = readerDataHolder.getNoteManager();
        final List<Shape> stash = noteManager.detachShapeStash();
        if (!readerDataHolder.isDocumentOpened() && !save && CollectionUtils.isNullOrEmpty(stash)) {
            BaseCallback.invoke(callback, null, null);
            return;
        }

        if (showDialog) {
            showLoadingDialog(readerDataHolder, R.string.saving);
        }

        final FlushShapeListRequest flushRequest = new FlushShapeListRequest(visiblePages, stash, 0, render, transfer, save);
        flushRequest.setPause(isPauseNote());
        final int id = readerDataHolder.getLastRequestSequence();
        noteManager.submitWithUniqueId(readerDataHolder.getContext(), id, flushRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (showDialog) {
                    hideLoadingDialog();
                }
                readerDataHolder.getEventBus().post(ShapeRenderFinishEvent.shapeReadyEventWithUniqueId(id));
                BaseCallback.invoke(callback, request, e);
            }
        });
    }

    public FlushNoteAction setPauseNote(boolean pauseNote) {
        this.pauseNote = pauseNote;
        return this;
    }

    public boolean isPauseNote() {
        return pauseNote;
    }

    public static void flush(final ReaderDataHolder readerDataHolder,
                                        boolean renderShapes,
                                        boolean transferBitmap,
                                        boolean saveToDatabase,
                                        boolean show,
                                        boolean pauseNote,
                                        BaseCallback callback) {
        final List<PageInfo> pages = readerDataHolder.getVisiblePages();
        new FlushNoteAction(pages, renderShapes, transferBitmap, saveToDatabase, show)
                .setPauseNote(pauseNote)
                .execute(readerDataHolder, callback);
    }
}
