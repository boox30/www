package com.onyx.android.dr.request.cloud;

import com.onyx.android.sdk.data.CloudManager;
import com.onyx.android.sdk.data.model.v2.CreateGroupCommonBean;
import com.onyx.android.sdk.data.request.cloud.BaseCloudRequest;

/**
 * Created by zhouzhiming on 2017/8/30.
 */
public class ExitGroupRequest extends AutoNetWorkConnectionBaseCloudRequest {
    private CreateGroupCommonBean createGroupResultBean;

    public ExitGroupRequest() {
    }

    public CreateGroupCommonBean getResult() {
        return createGroupResultBean;
    }

    @Override
    public void execute(CloudManager parent) throws Exception {
        getJoinGroupState(parent);
    }

    private void getJoinGroupState(CloudManager parent) {
        createGroupResultBean  = new CreateGroupCommonBean();
    }
}