package com.onyx.android.eschool.request;

import com.alibaba.fastjson.JSON;
import com.onyx.android.eschool.model.StudentAccount;
import com.onyx.android.sdk.data.CloudManager;
import com.onyx.android.sdk.data.model.v2.ContentAccount;
import com.onyx.android.sdk.data.request.cloud.BaseCloudRequest;
import com.onyx.android.sdk.utils.StringUtils;

/**
 * Created by suicheng on 2017/5/27.
 */

public class SaveAccountToLocalRequest extends BaseCloudRequest {

    private ContentAccount contentAccount;

    public SaveAccountToLocalRequest(ContentAccount contentAccount) {
        this.contentAccount = contentAccount;
    }

    public void setContentAccount(ContentAccount contentAccount) {
        this.contentAccount = contentAccount;
    }

    @Override
    public void execute(CloudManager parent) throws Exception {
        if (contentAccount == null || StringUtils.isNullOrEmpty(contentAccount.info)) {
            return;
        }
        StudentAccount parseAccount = JSON.parseObject(contentAccount.info, StudentAccount.class);
        if (parseAccount != null) {
            parseAccount.token = parent.getToken();
            parseAccount.accountInfo = contentAccount;
            StudentAccount.saveAccount(getContext(), parseAccount);
        }
    }
}
