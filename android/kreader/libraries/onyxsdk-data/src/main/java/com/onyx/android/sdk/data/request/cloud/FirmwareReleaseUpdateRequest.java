package com.onyx.android.sdk.data.request.cloud;

import com.alibaba.fastjson.JSON;
import com.onyx.android.sdk.data.CloudManager;
import com.onyx.android.sdk.data.model.Firmware;
import com.onyx.android.sdk.data.v1.ServiceFactory;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by zhuzeng on 11/21/15.
 */
public class FirmwareReleaseUpdateRequest extends BaseCloudRequest {
    private Firmware fw;
    private Firmware resultFirmware;

    public FirmwareReleaseUpdateRequest(final Firmware current) {
        fw = current;
    }

    public final Firmware getResultFirmware() {
        return resultFirmware;
    }

    @Override
    public void execute(final CloudManager parent) throws Exception {
        Call<Firmware> call = ServiceFactory.getOTAService(parent.getCloudConf().getApiBase())
                .ReleaseFirmwareUpdate(JSON.toJSONString(fw));
        Response<Firmware> response = call.execute();
        if (response.isSuccessful()) {
            resultFirmware = response.body();
        }
    }

}
