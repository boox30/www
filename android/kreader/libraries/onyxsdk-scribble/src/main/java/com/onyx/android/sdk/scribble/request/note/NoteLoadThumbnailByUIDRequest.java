package com.onyx.android.sdk.scribble.request.note;

import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.data.NoteDataProvider;
import com.onyx.android.sdk.scribble.data.NoteModel;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhuzeng on 6/20/16.
 */
public class NoteLoadThumbnailByUIDRequest extends BaseNoteRequest {
    private List<NoteModel> noteList = new ArrayList<>();
    private List<String> targetIdList;
    public int thumbnailLimit;

    public NoteLoadThumbnailByUIDRequest(final List<String> idList, int thumbLimit) {
        targetIdList = idList;
        thumbnailLimit = thumbLimit;
        setPauseInputProcessor(true);
        setResumeInputProcessor(false);
    }

    public NoteLoadThumbnailByUIDRequest(final List<String> idList) {
        this(idList, Integer.MAX_VALUE);
    }

    public void execute(final NoteViewHelper shapeManager) throws Exception {
        int i = 0;
        for (String id : targetIdList) {
            NoteModel noteModel = NoteDataProvider.load(getContext(), id);
            if (noteModel != null && noteModel.isDocument() && i < thumbnailLimit) {
                noteModel.setThumbnail(NoteDataProvider.loadThumbnail(getContext(), noteModel.getUniqueId()));
                i++;
            }
            noteList.add(noteModel);
        }
    }

    public List<NoteModel> getNoteList() {
        return noteList;
    }

}
