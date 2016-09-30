package com.onyx.kreader.note.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.scribble.data.TouchPointList;
import com.onyx.kreader.note.request.ClearPageRequest;
import com.onyx.kreader.note.request.RemoveShapesByTouchPointListRequest;
import com.onyx.kreader.ui.actions.BaseAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.events.RequestFinishEvent;

import java.util.List;

/**
 * Created by zhuzeng on 9/30/16.
 */

public class RemoveShapesByTouchPointListAction extends BaseAction {

    private List<PageInfo> visiblePages;
    private TouchPointList touchPointList;

    public RemoveShapesByTouchPointListAction(final List<PageInfo> pageList, final TouchPointList list) {
        visiblePages = pageList;
        touchPointList = list;
    }

    @Override
    public void execute(final ReaderDataHolder readerDataHolder, final BaseCallback baseCallback) {
        final RemoveShapesByTouchPointListRequest request = new RemoveShapesByTouchPointListRequest(visiblePages, touchPointList);
        readerDataHolder.getNoteManager().submit(readerDataHolder.getContext(), request, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                readerDataHolder.getEventBus().post(RequestFinishEvent.shapeReadyEvent());
                BaseCallback.invoke(baseCallback, request, e);
            }
        });
    }
}
