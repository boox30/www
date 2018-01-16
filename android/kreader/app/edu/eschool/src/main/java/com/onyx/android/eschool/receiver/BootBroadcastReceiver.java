package com.onyx.android.eschool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.WriterException;
import com.onyx.android.eschool.device.DeviceConfig;
import com.onyx.android.eschool.utils.QRCodeUtil;
import com.onyx.android.sdk.data.model.common.DeviceInfoShowConfig;
import com.onyx.android.sdk.data.model.v2.DeviceBind;
import com.onyx.android.sdk.data.utils.JSONObjectParseUtils;
import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.im.push.LeanCloudManager;
import com.onyx.android.sdk.utils.BitmapUtils;
import com.onyx.android.sdk.utils.NetworkUtil;
import com.onyx.android.sdk.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final boolean DEBUG = false;
    static File cacheFile;
    DeviceBindQrCodeGenerateTask task;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        cacheFile = new File(QRCodeUtil.CFA_QR_CODE_FILE_PATH);
        if (!TextUtils.isEmpty(action)) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                updateQrCodePosition(context);
                if (!checkCacheQRFile()) {
                    buildTask(context);
                    task.execute();
                }
            }
        }
    }

    private void buildTask(Context context) {
        if (task != null) {
            return;
        }
        task = new DeviceBindQrCodeGenerateTask(context);
    }


    static class DeviceBindQrCodeGenerateTask extends AsyncTask<Void, Integer, DeviceBind> {
        WeakReference<Context> contextWeakReference;

        DeviceBindQrCodeGenerateTask(Context cxt) {
            contextWeakReference = new WeakReference<>(cxt);
        }

        @Override
        protected DeviceBind doInBackground(Void... params) {
            Context context;
            if (contextWeakReference.get() == null) {
                return null;
            }
            context = contextWeakReference.get();
            DeviceBind deviceBind = null;
            try {
                boolean success = detectMacAddress(context);
                if (!success) {
                    return null;
                }
                cacheFile.getParentFile().mkdirs();
                cacheFile.createNewFile();
                deviceBind = createDeviceBind(context);
                BitmapUtils.saveBitmap(QRCodeUtil.stringToImageEncode(context, JSONObjectParseUtils.toJson(deviceBind),
                        QRCodeUtil.DEFAULT_SIZE, true, QRCodeUtil.WHITE_MARGIN_SIZE,
                        context.getResources().getColor(android.R.color.black))
                        , cacheFile.getPath(),true);
                updateQrCodePosition(context);
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return deviceBind;
        }

        @Override
        protected void onPostExecute(DeviceBind result) {
            if (DEBUG) {
                Log.e("QrCodeGenerateTask", "get device bind:" + JSONObjectParseUtils.toJson(result));
                Log.e("QrCodeGenerateTask", "get Mac address:" + result == null ? null : result.mac);
            }
        }
    }

    static private void updateQrCodePosition(Context context) {
        DeviceInfoShowConfig qrShowConfig = DeviceConfig.sharedInstance(context)
                .getQrCodeShowConfig();
        if (qrShowConfig != null) {
            Device.currentDevice().setQRShowConfig(qrShowConfig.orientation,
                    qrShowConfig.startX, qrShowConfig.startY);
        } else {
            Log.e("QrCodeGenerateTask", "qrShowConfig detects null");
        }
    }

    static private DeviceBind createDeviceBind(Context context) {
        DeviceBind deviceBind = new DeviceBind();
        deviceBind.mac = NetworkUtil.getMacAddress(context);
        deviceBind.installationId = LeanCloudManager.getInstallationId();
        deviceBind.model = Build.MODEL;
        return deviceBind;
    }

    static public boolean detectMacAddress(Context context) {
        String mac = NetworkUtil.getMacAddress(context);
        return StringUtils.isNotBlank(mac);
    }

    private static boolean checkCacheQRFile() {
        return cacheFile.exists() && cacheFile.canRead();
    }
}

