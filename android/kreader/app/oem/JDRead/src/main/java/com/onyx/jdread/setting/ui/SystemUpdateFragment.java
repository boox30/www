package com.onyx.jdread.setting.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.model.ApplicationUpdate;
import com.onyx.android.sdk.data.model.Firmware;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.R;
import com.onyx.jdread.databinding.SystemUpdateBinding;
import com.onyx.jdread.library.event.HideAllDialogEvent;
import com.onyx.jdread.library.event.LoadingDialogEvent;
import com.onyx.jdread.main.common.BaseFragment;
import com.onyx.jdread.main.common.JDPreferenceManager;
import com.onyx.jdread.main.common.ResManager;
import com.onyx.jdread.main.common.ToastUtil;
import com.onyx.jdread.main.event.TitleBarRightTitleEvent;
import com.onyx.jdread.main.model.TitleBarModel;
import com.onyx.jdread.setting.action.CheckApkUpdateAction;
import com.onyx.jdread.setting.action.DownloadPackageAction;
import com.onyx.jdread.setting.action.LocalUpdateSystemAction;
import com.onyx.jdread.setting.action.OnlineCheckSystemUpdateAction;
import com.onyx.jdread.setting.dialog.CheckUpdateLoadingDialog;
import com.onyx.jdread.setting.dialog.SystemUpdateDialog;
import com.onyx.jdread.setting.event.BackToDeviceConfigFragment;
import com.onyx.jdread.setting.event.DelayEvent;
import com.onyx.jdread.setting.event.ExecuteUpdateEvent;
import com.onyx.jdread.setting.model.DeviceConfigData;
import com.onyx.jdread.setting.model.SettingBundle;
import com.onyx.jdread.setting.model.SettingUpdateModel;
import com.onyx.jdread.setting.model.SystemUpdateData;
import com.onyx.jdread.setting.utils.UpdateUtil;
import com.onyx.jdread.util.TimeUtils;
import com.onyx.jdread.util.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by li on 2017/12/22.
 */

public class SystemUpdateFragment extends BaseFragment {
    private SystemUpdateBinding binding;
    private CheckUpdateLoadingDialog checkUpdateLoadingDialog;
    private SystemUpdateData systemUpdateData;
    private String downloadUrl;
    private String downloadPath;
    private String tag;
    private SettingUpdateModel settingUpdateModel;
    private CheckApkUpdateAction apkUpdateAction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = (SystemUpdateBinding) DataBindingUtil.inflate(inflater, R.layout.fragment_system_update, container, false);
        initData();
        initListener();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!SettingBundle.getInstance().getEventBus().isRegistered(this)) {
            SettingBundle.getInstance().getEventBus().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        SettingBundle.getInstance().getEventBus().unregister(this);
    }

    private void initListener() {
        binding.upgradeImmediately.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.isNetworkConnected(JDReadApplication.getInstance())) {
                    ToastUtil.showToast(ResManager.getString(R.string.wifi_no_connected));
                    return;
                }
                if (JDReadApplication.getInstance().getString(R.string.download_update_package).equals(systemUpdateData.getUpdateDes())) {
                    downloadUpdatePackage();
                } else if (JDReadApplication.getInstance().getString(R.string.upgrade_immediately).equals(systemUpdateData.getUpdateDes())) {
                    showDialog();
                } else {
                    checkSystemUpdate();
                }
            }
        });
    }

    private void downloadUpdatePackage() {
        systemUpdateData.setShowProgress(true);
        DownloadPackageAction downloadPackageAction = new DownloadPackageAction(downloadUrl, downloadPath, tag);
        downloadPackageAction.execute(new BaseCallback() {
            @Override
            public void start(BaseRequest request) {

            }

            @Override
            public void progress(BaseRequest request, ProgressInfo info) {
                systemUpdateData.setProgress((int) info.progress);
            }

            @Override
            public void done(BaseRequest request, Throwable e) {
                systemUpdateData.setProgress(100);
                systemUpdateData.setShowProgress(false);
                systemUpdateData.setShowDownloaded(true);
                systemUpdateData.setUpdateDes(JDReadApplication.getInstance().getResources().getString(R.string.upgrade_immediately));
                settingUpdateModel.saveDownloadVersion(systemUpdateData.getVersion());
            }
        });
    }

    private void initData() {
        TitleBarModel titleModel = new TitleBarModel(SettingBundle.getInstance().getEventBus());
        titleModel.title.set(JDReadApplication.getInstance().getResources().getString(R.string.system_update));
        titleModel.rightTitle.set(getString(R.string.view_history_version));
        titleModel.backEvent.set(new BackToDeviceConfigFragment());
        binding.systemUpdateSettingBar.setTitleModel(titleModel);

        settingUpdateModel = SettingBundle.getInstance().getSettingUpdateModel();
        systemUpdateData = settingUpdateModel.getSystemUpdateData();
        List<DeviceConfigData> deviceConfigDataList = SettingBundle.getInstance().getDeviceConfigModel().getDeviceConfigDataList();
        if (StringUtils.isNotBlank(deviceConfigDataList.get(deviceConfigDataList.size() - 1).getUpdateRecord())) {
            systemUpdateData.setShowDownloaded(true);
            systemUpdateData.setUpdateDes(JDReadApplication.getInstance().getResources().getString(R.string.upgrade_immediately));
            systemUpdateData.setVersionTitle(JDReadApplication.getInstance().getResources().getString(R.string.updatable_version));
        } else {
            systemUpdateData.setShowDownloaded(false);
            systemUpdateData.setUpdateDes(JDReadApplication.getInstance().getResources().getString(R.string.check_update));
            systemUpdateData.setVersionTitle(JDReadApplication.getInstance().getResources().getString(R.string.current_version));
        }
        systemUpdateData.setVersion(settingUpdateModel.getDownloadVersion());
        binding.setModel(settingUpdateModel);
    }

    private void checkSystemUpdate() {
        if (!Utils.isNetworkConnected(JDReadApplication.getInstance())) {
            Utils.showMessage(JDReadApplication.getInstance().getResources().getString(R.string.wifi_no_connected));
            return;
        }
        final OnlineCheckSystemUpdateAction checkAction = new OnlineCheckSystemUpdateAction();
        checkAction.showLoadingDialog(SettingBundle.getInstance(), R.string.new_version_detection);
        checkAction.execute(SettingBundle.getInstance(), new RxCallback() {
            @Override
            public void onNext(Object o) {
                if (SettingBundle.getInstance().getFirmwareValid()) {
                    Firmware resultFirmware = SettingBundle.getInstance().getResultFirmware();
                    String changeLog = resultFirmware.getChangeLog();
                    if (StringUtils.isNullOrEmpty(changeLog)) {
                        changeLog = resultFirmware.buildDisplayId;
                    }
                    downloadUrl = resultFirmware.getUrl();
                    downloadPath = UpdateUtil.getUpdateZipFile().getAbsolutePath();
                    tag = UpdateUtil.SYSTEM_UPDATE_TAG;
                    systemUpdateData.setUpdateDes(JDReadApplication.getInstance().getString(R.string.download_update_package));
                    systemUpdateData.setVersionTitle(JDReadApplication.getInstance().getResources().getString(R.string.updatable_version));
                    String fingerprint = resultFirmware.fingerprint;
                    String[] split = fingerprint.split("/");
                    String versionTime = getVersionTime(split);
                    systemUpdateData.setVersion(UpdateUtil.VERSION_SYSTEM + versionTime);
                    systemUpdateData.setNoticeMessage(changeLog);
                    checkAction.hideLoadingDialog(SettingBundle.getInstance());
                } else {
                    checkApkUpdate();
                }
            }
        });
    }

    private String getVersionTime(String[] split) {
        String versionTime = "";
        if (split.length > 3) {
            String time = split[3];
            if (time.contains("_")) {
                time = time.substring(0, time.lastIndexOf("_"));
                time = time.substring(0, time.lastIndexOf("_"));
            }
            versionTime = TimeUtils.getFormatTime(time);
        }
        return versionTime;
    }

    private void checkApkUpdate() {
        apkUpdateAction = new CheckApkUpdateAction();
        apkUpdateAction.execute(SettingBundle.getInstance(), new RxCallback() {
            @Override
            public void onNext(Object o) {
                ApplicationUpdate applicationUpdate = SettingBundle.getInstance().getApplicationUpdate();
                if (applicationUpdate != null) {
                    Map<String, List<String>> changeLogs = applicationUpdate.changeLogs;
                    String[] downloadUrlList = applicationUpdate.downloadUrlList;
                    String language = getResources().getConfiguration().locale.toString();
                    String message = "";
                    if (changeLogs != null && changeLogs.size() > 0) {
                        List<String> messageList = changeLogs.get(language);
                        for (int i = 0; i < messageList.size(); i++) {
                            message += messageList.get(i);
                            message += "\n";
                        }
                    }
                    if (downloadUrlList == null || downloadUrlList.length <= 0) {
                        apkUpdateAction.showLoadingDialog(SettingBundle.getInstance(), R.string.already_latest_version);
                        return;
                    }
                    downloadUrl = downloadUrlList[0];
                    downloadPath = UpdateUtil.getApkUpdateFile();
                    tag = UpdateUtil.APK_UPDATE_TAG;
                    systemUpdateData.setUpdateDes(JDReadApplication.getInstance().getString(R.string.download_update_package));
                    systemUpdateData.setVersionTitle(JDReadApplication.getInstance().getResources().getString(R.string.updatable_version));
                    String versionTime = applicationUpdate.versionName.substring(applicationUpdate.versionName.lastIndexOf("-") + 1, applicationUpdate.versionName.length());
                    systemUpdateData.setVersion(UpdateUtil.VERSION_LAUNCHER + versionTime);
                    systemUpdateData.setNoticeMessage(message);
                    apkUpdateAction.hideLoadingDialog(SettingBundle.getInstance());
                } else {
                    apkUpdateAction.showLoadingDialog(SettingBundle.getInstance(), R.string.already_latest_version);
                    DelayEvent delayEvent = new DelayEvent(2000);
                }
            }
        });
    }

    private void showDialog() {
        SystemUpdateDialog dialog = new SystemUpdateDialog();
        dialog.setEventBus(SettingBundle.getInstance().getEventBus());
        dialog.show(getActivity().getFragmentManager(), "");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExecuteUpdateEvent(ExecuteUpdateEvent event) {
        File apkUpdateFile = new File(UpdateUtil.getApkUpdateFile());
        if (apkUpdateFile.exists()) {
            UpdateUtil.startUpdateApkActivity(JDReadApplication.getInstance(), downloadPath);
            return;
        }
        LocalUpdateSystemAction action = new LocalUpdateSystemAction();
        action.execute(SettingBundle.getInstance(), new RxCallback() {
            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onComplete() {
                JDPreferenceManager.setIntValue(UpdateUtil.DOWNLOAD_UPDATE_CODE, UpdateUtil.UPDATE_CODE);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDelayEvent(DelayEvent event) {
        apkUpdateAction.hideLoadingDialog(SettingBundle.getInstance());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadingDialogEvent(LoadingDialogEvent event) {
        if (checkUpdateLoadingDialog == null) {
            checkUpdateLoadingDialog = new CheckUpdateLoadingDialog();
        }
        if (!checkUpdateLoadingDialog.isVisible()) {
            checkUpdateLoadingDialog.setTips(JDReadApplication.getInstance().getResources().getString(event.getResId()));
            checkUpdateLoadingDialog.show(getActivity().getFragmentManager(), "");
        } else {
            checkUpdateLoadingDialog.setTips(JDReadApplication.getInstance().getResources().getString(event.getResId()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHideAllDialogEvent(HideAllDialogEvent event) {
        checkUpdateLoadingDialog.dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBackToDeviceConfigFragment(BackToDeviceConfigFragment event) {
        viewEventCallBack.viewBack();
    }
    
    @Subscribe
    public void onTitleBarRightTitleEvent(TitleBarRightTitleEvent event){
        // TODO: 18-1-8
    }
}
