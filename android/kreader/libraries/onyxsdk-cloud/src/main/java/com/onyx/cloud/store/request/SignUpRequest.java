package com.onyx.cloud.store.request;

import com.onyx.cloud.CloudManager;
import com.onyx.cloud.model.OnyxAccount;
import com.onyx.cloud.service.v1.ServiceFactory;
import com.onyx.cloud.utils.JSONObjectParseUtils;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by zhuzeng on 11/21/15.
 */
public class SignUpRequest extends BaseCloudRequest {
    private static final String TAG = SignUpRequest.class.getSimpleName();
    private OnyxAccount account;
    private OnyxAccount accountSignUp;

    public SignUpRequest(final OnyxAccount value) {
        account = value;
    }

    public final OnyxAccount getAccountSignUp() {
        return accountSignUp;
    }

    public void execute(final CloudManager parent) throws Exception {
        Call<ResponseBody> call = ServiceFactory.getAccountService(parent.getCloudConf().getApiBase())
                .signup(account);
        Response<ResponseBody> response = call.execute();
        if (response.isSuccessful()) {
            accountSignUp = JSONObjectParseUtils.parseOnyxAccount(response.body().string());
        } else {
            String errorCode = JSONObjectParseUtils.httpStatus(response.code(), new JSONObject(response.errorBody().string()));
            throw new Exception(errorCode);
        }
    }

}
