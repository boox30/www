package com.onyx.android.sdk.scribble.request.note;

import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.data.NoteDataProvider;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;

/**
 * Created by zhuzeng on 6/23/16.
 */
public class NoteDocumentSaveRequest extends BaseNoteRequest {

    private volatile String title;
    private volatile boolean close;
    private volatile boolean resume = true;
    private volatile boolean render = false;
    private volatile boolean saveThumbnail = false;

    public NoteDocumentSaveRequest(final String t, boolean c , boolean r, boolean render, boolean s) {
        title = t;
        close = c;
        resume = r;
        this.render = render;
        saveThumbnail = s;
        setPauseInputProcessor(true);
        setResumeInputProcessor(!close && resume);
        setRender(render);
    }

    public void execute(final NoteViewHelper parent) throws Exception {
        renderCurrentPage(parent);
        if (saveThumbnail) {
            NoteDataProvider.saveThumbnailWithSize(getContext(),
                    parent.getNoteDocument().getDocumentUniqueId(),
                    parent.getRenderBitmap(),
                    512,
                    512);
        }
        parent.save(getContext(), title, close);
    }

}
