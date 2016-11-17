package com.onyx.android.sdk.data.request.data;

import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.data.model.Library;

/**
 * Created by suicheng on 2016/9/12.
 */
public class ModifyLibraryRequest extends BaseDataRequest {
    private Library library;
    private boolean modifyCriteria = false;

    public ModifyLibraryRequest(Library library, boolean modifyCriteria) {
        this.library = library;
        this.modifyCriteria = modifyCriteria;
    }

    @Override
    public void execute(DataManager dataManager) throws Exception {
        dataManager.getDataManagerHelper().modifyLibrary(getContext(), library, modifyCriteria);
    }
}
