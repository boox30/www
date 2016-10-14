package com.onyx.android.sdk.data.request.cloud;

import com.onyx.android.sdk.data.CloudManager;
import com.onyx.android.sdk.data.model.Device;
import com.onyx.android.sdk.data.v1.ServiceFactory;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by suicheng on 2016/9/28.
 */
public class DeviceRequest extends BaseCloudRequest {

    private Device boundDevice;
    private String deviceId;
    private String sessionToken;

    public DeviceRequest(final String deviceId, final String sessionToken) {
        this.deviceId = deviceId;
        this.sessionToken = sessionToken;
    }

    public final Device getBoundDevice() {
        return boundDevice;
    }

    public void execute(final CloudManager parent) throws Exception {
        Response<Device> response = ServiceFactory.getAccountService(parent.getCloudConf().getApiBase())
                .getBoundDevice(deviceId, sessionToken).execute();
        if (response.isSuccessful()) {
            boundDevice = response.body();
        }
    }
}