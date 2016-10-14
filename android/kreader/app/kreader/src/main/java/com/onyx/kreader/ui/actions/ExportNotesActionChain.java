package com.onyx.kreader.ui.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.kreader.R;
import com.onyx.kreader.note.actions.GetAllShapesAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;

/**
 * Created by zhuzeng on 5/17/16.
 */
public class ExportNotesActionChain extends BaseAction {

    @Override
    public void execute(ReaderDataHolder readerDataHolder, final BaseCallback baseCallback) {
        showLoadingDialog(readerDataHolder, R.string.exporting);
        ActionChain chain = new ActionChain();
        GetDocumentInfoAction documentInfoAction = new GetDocumentInfoAction();
        GetAllShapesAction shapesAction = new GetAllShapesAction();
        ExportNotesAction exportNotesAction = new ExportNotesAction(documentInfoAction, shapesAction);
        chain.addAction(documentInfoAction);
        chain.addAction(shapesAction);
        chain.addAction(exportNotesAction);
        chain.execute(readerDataHolder, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                hideLoadingDialog();
                baseCallback.done(request, e);
            }
        });
    }
}
