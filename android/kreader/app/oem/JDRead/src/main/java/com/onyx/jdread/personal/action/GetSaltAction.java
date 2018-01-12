package com.onyx.jdread.personal.action;

import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.jdread.personal.cloud.entity.jdbean.SaltResultBean;
import com.onyx.jdread.personal.model.PersonalDataBundle;
import com.onyx.jdread.personal.request.cloud.RxGetSaltRequest;
import com.onyx.jdread.shop.common.JDAppBaseInfo;

/**
 * Created by li on 2018/1/11.
 */

public class GetSaltAction extends BaseAction {
    @Override
    public void execute(final PersonalDataBundle dataBundle, final RxCallback rxCallback) {
        JDAppBaseInfo baseInfo = new JDAppBaseInfo();
        baseInfo.addApp();
        final RxGetSaltRequest rq = new RxGetSaltRequest(baseInfo);
        rq.execute(new RxCallback() {
            @Override
            public void onNext(Object o) {
                SaltResultBean saltResultBean = rq.getSaltResultBean();
                if (saltResultBean != null && saltResultBean.data != null) {
                    dataBundle.setSalt(saltResultBean.data.salt);
                }
                if (rxCallback != null) {
                    rxCallback.onNext(GetSaltAction.class);
                }
            }
        });
    }
}
