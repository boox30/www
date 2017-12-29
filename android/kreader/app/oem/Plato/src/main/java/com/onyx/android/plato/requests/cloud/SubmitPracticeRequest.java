package com.onyx.android.plato.requests.cloud;

import android.util.Log;

import com.onyx.android.plato.cloud.bean.SubmitPracticeRequestBean;
import com.onyx.android.plato.cloud.bean.SubmitPracticeResultBean;
import com.onyx.android.plato.cloud.service.ContentService;
import com.onyx.android.plato.common.CloudApiContext;
import com.onyx.android.plato.event.ExceptionEvent;
import com.onyx.android.plato.requests.requestTool.BaseCloudRequest;
import com.onyx.android.plato.requests.requestTool.SunRequestManager;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by jackdeng on 2017/10/21.
 */

public class SubmitPracticeRequest extends BaseCloudRequest {
    private final static String TAG = SubmitPracticeRequest.class.getSimpleName();
    private final SubmitPracticeRequestBean requestBean;
    private SubmitPracticeResultBean result;

    public SubmitPracticeRequest(SubmitPracticeRequestBean requestBean) {
        this.requestBean = requestBean;
    }

    public SubmitPracticeResultBean getResult() {
        return result;
    }

    @Override
    public void execute(SunRequestManager helper) throws Exception {
        try {
            ContentService service = CloudApiContext.getService(CloudApiContext.BASE_URL);
            Call<SubmitPracticeResultBean> call = getCall(service);
            Response<SubmitPracticeResultBean> response = call.clone().execute();

            if (response.isSuccessful()) {
                result = response.body();
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

    private Call<SubmitPracticeResultBean> getCall(ContentService service) {
        return service.submitPractice(requestBean.id, requestBean.answer);
    }
}
