package com.onyx.jdread.reader.actions;

import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.jdread.reader.data.ReaderDataHolder;
import com.onyx.jdread.reader.data.ReadingData;
import com.onyx.jdread.reader.data.ReadingDataResultBean;
import com.onyx.jdread.reader.request.RxSyncReadingDataRequest;
import com.onyx.jdread.shop.common.CloudApiContext;
import com.onyx.jdread.shop.common.JDAppBaseInfo;

import java.util.List;

/**
 * Created by li on 2018/3/1.
 */

public class SyncReadDataAction extends BaseReaderAction {
    private List<ReadingData> list;
    private ReadingDataResultBean resultBean;

    public SyncReadDataAction(List<ReadingData> list) {
        this.list = list;
    }

    @Override
    public void execute(ReaderDataHolder readerDataHolder, final RxCallback baseCallback) {
        JDAppBaseInfo baseInfo = new JDAppBaseInfo();
        String signValue = baseInfo.getSignValue(CloudApiContext.User.READING_DATA);
        baseInfo.setSign(signValue);
        final RxSyncReadingDataRequest rq = new RxSyncReadingDataRequest();
        rq.setRequestData(baseInfo, list);
        rq.execute(new RxCallback() {
            @Override
            public void onNext(Object o) {
                resultBean = rq.getResultBean();
                RxCallback.invokeNext(baseCallback, SyncReadDataAction.this);
            }

            @Override
            public void onError(Throwable throwable) {
                RxCallback.invokeError(baseCallback, throwable);
            }
        });
    }

    public ReadingDataResultBean getResultBean() {
        return resultBean;
    }
}