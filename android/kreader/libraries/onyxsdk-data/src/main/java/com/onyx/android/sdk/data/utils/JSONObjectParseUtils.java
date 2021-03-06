package com.onyx.android.sdk.data.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.onyx.android.sdk.data.model.OnyxAccount;

public class JSONObjectParseUtils {
    public static final String TAG_UPDATEDAT = "updatedAt";
    public static final String TAG_CREATEDAT = "createdAt";
    public static final String TAG_SESSIONTOKEN = "sessionToken";
    public static final String TAG_MAIN = "main";
    public static final String TAG_EMAIL = "email";
    public static final String TAG_DEVICE_IDS = "deviceIds";
    public static final String TAG_CODE = "code";
    public static final String TAG_MESSAGE = "message";

    public static final int STATUS_CODE_SUCCESS_RESPONSE = 200;
    public static final int STATUS_CODE_ERROR_SERVER_NO_ACTION = 0;
    public static final int STATUS_CODE_ERROR_BAD_REQUEST = 400;
    public static final int STATUS_CODE_ERROR_UNAUTHORIZED = 401;
    public static final int ACCOUNT_ERROR_EXIST = 404;
    public static final int ACCOUNT_ERROR_INCORRECT_PASSWORD = 405;
    public static final int ACCOUNT_ERROR_NOT_EXIST = 406;

    public static class deviceId {
        public static final String machineIdentifier = "machineIdentifier";
        public static final String timestamp = "timestamp";
        public static final String timeSecond = "timeSecond";
        public static final String time = "time";
        public static final String date = "date";
    }

    public static OnyxAccount parseOnyxAccount(String jsonString) {
        OnyxAccount onyxAccount = null;
        try {
            onyxAccount = JSON.parseObject(jsonString, OnyxAccount.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return onyxAccount;
    }

    public static String httpStatus(int statusCode, JSONObject errorResponse) throws JSONException {
        return errorResponse.getInt(TAG_CODE) + "," + errorResponse.getInt(TAG_MESSAGE);
    }

}
