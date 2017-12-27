package com.onyx.android.plato.requests.cloud;

import android.util.Log;

import com.onyx.android.plato.cloud.bean.TaskBean;
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

public class TaskDetailRequest extends BaseCloudRequest {
    private final static String TAG = TaskDetailRequest.class.getSimpleName();
    private int id;
    private TaskBean taskBean;

    public TaskDetailRequest(int id) {
        this.id = id;
    }

    public TaskBean getTaskBean() {
        return taskBean;
    }

    @Override
    public void execute(SunRequestManager helper) throws Exception {
        try {
            ContentService service = CloudApiContext.getService(CloudApiContext.BASE_URL);
            Call<TaskBean> call = getCall(service);
            Response<TaskBean> response = call.clone().execute();
            if (response.isSuccessful()) {
                taskBean = response.body();
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

    private Call<TaskBean> getCall(ContentService service) {
        return service.getTaskDetail(id);
    }
}
