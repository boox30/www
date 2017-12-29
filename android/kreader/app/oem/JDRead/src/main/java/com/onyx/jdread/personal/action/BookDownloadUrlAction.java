package com.onyx.jdread.personal.action;

import com.alibaba.fastjson.JSONObject;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.utils.PreferenceManager;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.common.Constants;
import com.onyx.jdread.shop.action.BaseAction;
import com.onyx.jdread.shop.cloud.entity.BaseRequestBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookDetailResultBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookDownloadUrlResultBean;
import com.onyx.jdread.shop.common.CloudApiContext;
import com.onyx.jdread.shop.model.ShopDataBundle;
import com.onyx.jdread.shop.request.cloud.RxRequestBookDownloadUrl;

/**
 * Created by jackdeng on 2017/12/29.
 */

public class BookDownloadUrlAction extends BaseAction {

    private BookDetailResultBean.Detail entity;
    private BookDownloadUrlResultBean bookDownloadUrlResultBean;

    public BookDownloadUrlAction(BookDetailResultBean.Detail entity) {
        this.entity = entity;
    }

    @Override
    public void execute(ShopDataBundle dataBundle, RxCallback rxCallback) {
        BaseRequestBean baseRequestBean = new BaseRequestBean();
        baseRequestBean.setAppBaseInfo(dataBundle.getAppBaseInfo());
        JSONObject body = new JSONObject();
        body.put(CloudApiContext.BookDownloadUrl.ORDER_ID, entity.getOrderId());
        body.put(CloudApiContext.BookDownloadUrl.UUID, dataBundle.getAppBaseInfo().getUuid());
        body.put(CloudApiContext.BookDownloadUrl.EBOOK_ID, entity.getEbookId());
        body.put(CloudApiContext.BookDownloadUrl.USER_ID, PreferenceManager.getStringValue(JDReadApplication.getInstance(), Constants.SP_KEY_USER_NICK_NAME, ""));
        baseRequestBean.setBody(body.toString());
        final RxRequestBookDownloadUrl req = new RxRequestBookDownloadUrl();
        req.setBaseRequestBean(baseRequestBean);
        req.execute(new RxCallback() {
            @Override
            public void onNext(Object o) {
                bookDownloadUrlResultBean = req.getBookDownloadUrlResultBean();
                if (bookDownloadUrlResultBean != null && StringUtils.isNotBlank(bookDownloadUrlResultBean.ebookAddress)) {
                    entity.setDownLoadUrl( bookDownloadUrlResultBean.ebookAddress);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }

            @Override
            public void onComplete() {
                super.onComplete();
                if (bookDownloadUrlResultBean != null) {
                    if (Constants.CODE_STATE_THREE.equals(bookDownloadUrlResultBean.Code) || Constants.CODE_STATE_FOUR.equals(bookDownloadUrlResultBean.Code)) {
                        JDReadApplication.getInstance().setLogin(false);
                    }
                }
            }
        });
    }
}