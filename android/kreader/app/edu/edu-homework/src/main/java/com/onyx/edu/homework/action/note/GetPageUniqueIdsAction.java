package com.onyx.edu.homework.action.note;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.edu.homework.base.BaseNoteAction;
import com.onyx.edu.homework.request.GetPageUniqueIdsRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lxm on 2017/12/8.
 */

public class GetPageUniqueIdsAction extends BaseNoteAction {

    private volatile List<String> docIds;
    private Map<String, List<String>> pageUniqueMap = new HashMap<>();
    private boolean filterUnRedoingDocId;

    public GetPageUniqueIdsAction(List<String> docIds) {
        this.docIds = docIds;
    }

    public GetPageUniqueIdsAction setFilterUnRedoingDocId(boolean filterUnRedoingDocId) {
        this.filterUnRedoingDocId = filterUnRedoingDocId;
        return this;
    }

    @Override
    public void execute(NoteViewHelper noteViewHelper, final BaseCallback baseCallback) {
        final GetPageUniqueIdsRequest pageUniqueIdsRequest = new GetPageUniqueIdsRequest(docIds);
        pageUniqueIdsRequest.setFilterUnRedoingDocId(filterUnRedoingDocId);
        noteViewHelper.submit(getAppContext(), pageUniqueIdsRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                pageUniqueMap.putAll(pageUniqueIdsRequest.getPageUniqueMap());
                BaseCallback.invoke(baseCallback, request, e);
            }
        });
    }

    public Map<String, List<String>> getPageUniqueMap() {
        return pageUniqueMap;
    }
}
