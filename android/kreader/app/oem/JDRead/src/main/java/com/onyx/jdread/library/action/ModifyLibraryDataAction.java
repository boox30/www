package com.onyx.jdread.library.action;

import com.onyx.android.sdk.data.rxrequest.data.db.RxFileChangeRequest;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.jdread.R;
import com.onyx.jdread.library.model.LibraryDataBundle;
import com.onyx.jdread.main.action.BaseAction;

import java.util.List;

/**
 * Created by hehai on 17-12-14.
 */

public class ModifyLibraryDataAction extends BaseAction<LibraryDataBundle> {
    private List<String> pathList;

    public ModifyLibraryDataAction(List<String> pathList) {
        this.pathList = pathList;
    }

    @Override
    public void execute(final LibraryDataBundle libraryDataBundle, final RxCallback baseCallback) {
        if (CollectionUtils.isNullOrEmpty(pathList)) {
            return;
        }
        RxFileChangeRequest rxFileChangeRequest = new RxFileChangeRequest(libraryDataBundle.getDataManager(), pathList);
        rxFileChangeRequest.execute(new RxCallback<RxFileChangeRequest>() {
            @Override
            public void onNext(RxFileChangeRequest o) {
                if (baseCallback != null) {
                    baseCallback.onNext(o);
                }
            }

            @Override
            public void onFinally() {
                super.onFinally();
                hideLoadingDialog(libraryDataBundle);
            }
        });

        showLoadingDialog(libraryDataBundle, R.string.loading);
    }
}
