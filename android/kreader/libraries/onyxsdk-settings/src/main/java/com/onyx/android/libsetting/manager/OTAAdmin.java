package com.onyx.android.libsetting.manager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.onyx.android.libsetting.util.OTAUtil;
import com.onyx.android.sdk.device.Device;

import java.io.File;

/**
 * Created by solskjaer49 on 2017/2/11 16:15.
 */

public class OTAAdmin {
    private static final String TAG = OTAAdmin.class.getSimpleName();
    private static OTAAdmin instance;

    private static final String OTA_SERVICE_PACKAGE = "com.onyx.android.onyxotaservice";
    private static final String OTA_SERVICE_ACTIVITY = "com.onyx.android.onyxotaservice.OtaInfoActivity";
    private static final String OTA_SERVICE_PACKAGE_PATH_KEY = "updatePath";

    private static final String UPDATE_FILE_NAME = "update.zip";
    private static final String LOCAL_PATH_SDCARD = Device.currentDevice.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + UPDATE_FILE_NAME;
    private static final String LOCAL_PATH_EXTSD = Device.currentDevice.getRemovableSDCardDirectory()
            .getAbsolutePath() + File.separator + UPDATE_FILE_NAME;

    public interface FirmwareCheckCallback {
        void preCheck();

        void stateChanged(int state, long finished, long total, long percentage);

        /**
         * @param targetPath If not found any zip(success return false),targetPath is null.
         * @param success
         */
        void onPostCheck(final String targetPath, boolean success);
    }


    /**
     * TODO:should clean up all test resource before update,but wait further design with both new setting/launcher coop.
     * Temp empty implement here.
    */
    private void preFirmwareUpdate() {
//        ContentBrowserUtils.clearAllTestResource(OnyxOTAActivity.this, DeviceConfig.sharedInstance(this));
    }

    //TODO:maybe ota service pkg/activity should be custom?
    public void startFirmwareUpdate(Context context, String path) {
        preFirmwareUpdate();
        Intent i = new Intent();
        i.putExtra(OTA_SERVICE_PACKAGE_PATH_KEY, path);
        i.setClassName(OTA_SERVICE_PACKAGE, OTA_SERVICE_ACTIVITY);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    static public OTAAdmin sharedInstance() {
        if (instance == null) {
            instance = new OTAAdmin();
        }
        return instance;
    }

    public void checkLocalFirmware(final FirmwareCheckCallback callback) {
        CheckLocalFirmWareTask task = new CheckLocalFirmWareTask(callback);
        task.execute();
    }

    private static class CheckLocalFirmWareTask extends AsyncTask<Void, Integer, Boolean> {
        FirmwareCheckCallback callback;
        String[] pathArray = new String[]{LOCAL_PATH_SDCARD, LOCAL_PATH_EXTSD};
        String targetPath = null;

        CheckLocalFirmWareTask(FirmwareCheckCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (String path : pathArray) {
                if (OTAUtil.checkLocalUpdateZipLegality(path)) {
                    targetPath = path;
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            callback.onPostCheck(targetPath, aBoolean);
        }
    }
}
