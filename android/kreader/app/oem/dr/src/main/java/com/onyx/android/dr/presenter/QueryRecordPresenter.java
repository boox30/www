package com.onyx.android.dr.presenter;

import com.onyx.android.dr.DRApplication;
import com.onyx.android.dr.data.NewWordData;
import com.onyx.android.dr.data.QueryRecordData;
import com.onyx.android.dr.interfaces.QueryRecordView;
import com.onyx.android.dr.request.local.QueryRecordQueryAll;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;

/**
 * Created by zhouzhiming on 2017/7/12.
 */
public class QueryRecordPresenter {
    private final QueryRecordData queryRecordData;
    private QueryRecordView queryRecordView;
    private NewWordData newWordData;

    public QueryRecordPresenter(QueryRecordView queryRecordView) {
        this.queryRecordView = queryRecordView;
        queryRecordData = new QueryRecordData();
        newWordData = new NewWordData();
    }

    public void getAllQueryRecordData() {
        final QueryRecordQueryAll req = new QueryRecordQueryAll();
        queryRecordData.getAllQueryRecord(DRApplication.getInstance(), req, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                queryRecordView.setQueryRecordData(req.getList());
            }
        });
    }
}