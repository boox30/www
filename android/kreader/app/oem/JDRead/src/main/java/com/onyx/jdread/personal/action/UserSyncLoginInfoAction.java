package com.onyx.jdread.personal.action;

import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.jdread.personal.cloud.entity.jdbean.SyncLoginInfoBean;
import com.onyx.jdread.personal.model.PersonalDataBundle;
import com.onyx.jdread.personal.request.cloud.RxRequestSyncLoginInfo;
import com.onyx.jdread.shop.common.CloudApiContext;
import com.onyx.jdread.shop.common.JDAppBaseInfo;

/**
 * Created by jackdeng on 2017/12/26.
 */

public class UserSyncLoginInfoAction extends BaseAction {
    private SyncLoginInfoBean syncLoginInfoBean;

    @Override
    public void execute(PersonalDataBundle dataBundle, final RxCallback rxCallback) {
        JDAppBaseInfo baseInfo = new JDAppBaseInfo();
        final RxRequestSyncLoginInfo rq = new RxRequestSyncLoginInfo();
        String signValue = baseInfo.getSignValue(CloudApiContext.User.SYNC_INFO);
        baseInfo.setSign(signValue);
        rq.setRequestBean(baseInfo);
        rq.execute(new RxCallback() {
            @Override
            public void onNext(Object o) {
                syncLoginInfoBean = rq.getSyncLoginInfoBean();
                if (rxCallback != null) {
                    rxCallback.onNext(UserSyncLoginInfoAction.this);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (rxCallback != null) {
                    rxCallback.onError(throwable);
                }
            }
        });

    }

    public SyncLoginInfoBean getSyncLoginInfoBean() {
        return syncLoginInfoBean;
    }
}