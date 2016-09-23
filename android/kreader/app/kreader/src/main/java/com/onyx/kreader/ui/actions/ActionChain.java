package com.onyx.kreader.ui.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.kreader.ui.data.ReaderDataHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuzeng on 9/22/16.
 */
public class ActionChain {

    private List<BaseAction> actionList = new ArrayList<>();

    public void addAction(final BaseAction action) {
        actionList.add(action);
    }

    public void execute(final ReaderDataHolder readerDataHolder, final BaseCallback callback) {
        if (isFinished(callback)) {
            return;
        }

        final BaseAction action = actionList.remove(0);
        executeAction(readerDataHolder, action, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                execute(readerDataHolder, callback);
            }
        });
    }

    private void executeAction(final ReaderDataHolder readerDataHolder, final BaseAction action, final BaseCallback callback) {
        action.execute(readerDataHolder, callback);
    }

    private boolean isFinished(final BaseCallback callback) {
        if (actionList.size() <= 0) {
            BaseCallback.invoke(callback, null, null);
            return true;
        }
        return false;
    }

}
