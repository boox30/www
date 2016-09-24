package com.onyx.kreader.note.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.kreader.note.NoteManager;
import com.onyx.kreader.note.request.GetNotePageListRequest;
import com.onyx.kreader.ui.actions.BaseAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;

import java.util.List;

/**
 * Created by ming on 16/9/23.
 */
public class GetNotePageListAction extends BaseAction{

    List<String> scribblePages;

    @Override
    public void execute(final ReaderDataHolder readerDataHolder, final BaseCallback callback) {
        final NoteManager noteManager = readerDataHolder.getNoteManager();
        final GetNotePageListRequest noteRequest = new GetNotePageListRequest();

        noteManager.submit(readerDataHolder.getContext(), noteRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                scribblePages = noteRequest.getPageList();
                callback.done(request, e);
            }
        });
    }

    public List<String> getScribblePages() {
        return scribblePages;
    }

}
