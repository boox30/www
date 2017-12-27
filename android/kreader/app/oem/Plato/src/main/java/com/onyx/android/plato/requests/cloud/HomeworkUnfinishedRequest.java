package com.onyx.android.plato.requests.cloud;


import android.util.Log;

import com.onyx.android.plato.cloud.bean.HomeworkRequestBean;
import com.onyx.android.plato.cloud.bean.HomeworkUnfinishedResultBean;
import com.onyx.android.plato.cloud.service.ContentService;
import com.onyx.android.plato.common.CloudApiContext;
import com.onyx.android.plato.event.ExceptionEvent;
import com.onyx.android.plato.requests.requestTool.BaseCloudRequest;
import com.onyx.android.plato.requests.requestTool.SunRequestManager;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by li on 2017/10/10.
 */

public class HomeworkUnfinishedRequest extends BaseCloudRequest {
    private static final String TAG = HomeworkUnfinishedRequest.class.getSimpleName();
    private HomeworkRequestBean requestBean;
    private HomeworkUnfinishedResultBean resultBean;

    public HomeworkUnfinishedRequest(HomeworkRequestBean requestBean) {
        this.requestBean = requestBean;
    }

    public HomeworkUnfinishedResultBean getResultBean() {
        return resultBean;
    }

    @Override
    public void execute(SunRequestManager helper) throws Exception {
        try {
            ContentService service = CloudApiContext.getService(CloudApiContext.BASE_URL);
            Call<HomeworkUnfinishedResultBean> call = getCall(service);
            Response<HomeworkUnfinishedResultBean> response = call.clone().execute();
            if (response.isSuccessful()) {
                resultBean = response.body();
            } else {
                String error = response.errorBody().string();
                EventBus.getDefault().post(new ExceptionEvent(error));
            }
        } catch (Exception e) {
            EventBus.getDefault().post(new ExceptionEvent(e.toString()));
            Log.i(TAG, e.toString());
            setException(e);
        }
    }

    private Call<HomeworkUnfinishedResultBean> getCall(ContentService service) {
        return service.getHomeworkUnfinished(requestBean.status,
                requestBean.page, requestBean.size,
                requestBean.course, requestBean.type,
                requestBean.starttime, requestBean.endtime);
    }
}
