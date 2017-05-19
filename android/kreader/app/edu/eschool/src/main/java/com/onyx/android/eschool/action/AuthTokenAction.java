package com.onyx.android.eschool.action;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.onyx.android.eschool.holder.LibraryDataHolder;
import com.onyx.android.eschool.model.StudentAccount;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.CloudManager;
import com.onyx.android.sdk.data.model.ContentAccount;
import com.onyx.android.sdk.data.model.ContentAuthAccount;
import com.onyx.android.sdk.data.model.Device;
import com.onyx.android.sdk.data.request.cloud.ContentAccountGetRequest;
import com.onyx.android.sdk.data.request.cloud.ContentAccountTokenRequest;
import com.onyx.android.sdk.data.request.cloud.CloudRequestChain;
import com.onyx.android.sdk.ui.utils.ToastUtils;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.android.sdk.utils.StringUtils;

/**
 * Created by suicheng on 2017/5/18.
 */
public class AuthTokenAction extends BaseAction<LibraryDataHolder> {
    public static final String NAME_SECRET = "eefbb54a-ffd1-4e86-9513-f83e15b807c9";
    public static final String PASSWORD_SECRET = "807bb28a-623e-408c-97c5-61177091737b";

    @Override
    public void execute(LibraryDataHolder dataHolder, final BaseCallback baseCallback) {
        final ContentAuthAccount account = createContentAccount(dataHolder.getContext());
        if (account == null) {
            ToastUtils.showToast(dataHolder.getContext(), "当前wifi可能没有连接，获取不了mac地址");
            return;
        }
        final CloudManager cloudManager = dataHolder.getCloudManager();
        CloudRequestChain requestChain = new CloudRequestChain();
        final ContentAccountTokenRequest tokenRequest = new ContentAccountTokenRequest(account);
        final ContentAccountGetRequest applyRequest = new ContentAccountGetRequest();
        requestChain.addRequest(tokenRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (e != null) {
                    ToastUtils.showToast(request.getContext(), "token获取失败");
                }
            }
        });
        requestChain.addRequest(applyRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (e != null) {
                    ToastUtils.showToast(request.getContext(), "个人信息获取失败");
                    return;
                }
                ContentAccount accountInfo = applyRequest.getAccount();
                saveStudentAccount(request.getContext(), accountInfo, cloudManager.getToken());
                BaseCallback.invoke(baseCallback, request, e);
            }
        });
        requestChain.execute(dataHolder.getContext(), cloudManager);
    }

    private void saveStudentAccount(Context context, ContentAccount contentAccount, String token) {
        if (contentAccount == null || StringUtils.isNullOrEmpty(contentAccount.info)) {
            return;
        }
        StudentAccount parseAccount = JSON.parseObject(contentAccount.info, StudentAccount.class);
        if (parseAccount != null) {
            parseAccount.token = token;
            parseAccount.accountInfo = contentAccount;
            parseAccount.saveAccount(context);
        }
    }

    public static ContentAuthAccount createContentAccount(Context context) {
        Device device = Device.updateCurrentDeviceInfo(context);
        if (device == null || StringUtils.isNullOrEmpty(device.macAddress)) {
            return null;
        }
        return ContentAuthAccount.create(FileUtils.computeMD5(device.macAddress + NAME_SECRET),
                FileUtils.computeMD5(device.macAddress + PASSWORD_SECRET));
    }
}
