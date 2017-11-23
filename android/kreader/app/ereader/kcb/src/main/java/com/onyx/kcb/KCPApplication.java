package com.onyx.kcb;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.device.EnvironmentUtil;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.ui.dialog.DialogLoading;
import com.onyx.android.sdk.utils.DeviceReceiver;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.kcb.action.ActionChain;
import com.onyx.kcb.action.RxFileSystemScanAction;
import com.onyx.kcb.device.DeviceConfig;
import com.onyx.kcb.holder.LibraryDataHolder;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by hehai on 17-11-13.
 */

public class KCPApplication extends MultiDexApplication {
    private boolean hasMetadataScanned = false;
    private static final String TAG = KCPApplication.class.getSimpleName();
    private static KCPApplication instance = null;
    private LibraryDataHolder libraryDataHolder;
    private DeviceReceiver deviceReceiver = new DeviceReceiver();

    public static KCPApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initConfig();
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(KCPApplication.this);
    }

    private void initConfig() {
        instance = this;
        DataManager.init(this, null);
        initFrescoLoader();
        initEventListener();
    }

    private void initEventListener() {
        deviceReceiver.setMediaStateListener(new DeviceReceiver.MediaStateListener() {

            @Override
            public void onMediaScanStarted(Intent intent) {
                if (DeviceConfig.sharedInstance(getApplicationContext()).supportMediaScan()) {
                    processRemovableSDCardScan();
                }
            }

            @Override
            public void onMediaMounted(Intent intent) {
                Log.w(TAG, "onMediaMounted " + intent.getData().toString());
                if (EnvironmentUtil.isRemovableSDDirectory(getApplicationContext(), intent)) {
                    processRemovableSDCardScan();
                    setHasMetadataScanned(true);
                }
            }

            @Override
            public void onMediaUnmounted(Intent intent) {
                if (EnvironmentUtil.isRemovableSDDirectory(getApplicationContext(), intent)) {
                }
            }

            @Override
            public void onMediaBadRemoval(Intent intent) {
                if (EnvironmentUtil.isRemovableSDDirectory(getApplicationContext(), intent)) {
                }
            }

            @Override
            public void onMediaRemoved(Intent intent) {
            }
        });
        deviceReceiver.setWifiStateListener(new DeviceReceiver.WifiStateListener() {
            @Override
            public void onWifiConnected(Intent intent) {

            }
        });
        deviceReceiver.enable(getApplicationContext(), true);
    }

    private void processRemovableSDCardScan() {
        ActionChain actionChain = new ActionChain();
        actionChain.addAction(new RxFileSystemScanAction(RxFileSystemScanAction.MMC_STORAGE_ID, true));
        String sdcardCid = EnvironmentUtil.getRemovableSDCardCid();
        if (StringUtils.isNotBlank(sdcardCid)) {
            actionChain.addAction(new RxFileSystemScanAction(sdcardCid, false));
        }
        actionChain.execute(getLibraryDataHolder(), null);
    }

    public LibraryDataHolder getLibraryDataHolder() {
        if (libraryDataHolder == null) {
            libraryDataHolder = new LibraryDataHolder(instance);
        }
        return libraryDataHolder;
    }

    public boolean isHasMetadataScanned() {
        return hasMetadataScanned;
    }

    public void setHasMetadataScanned(boolean hasMetadataScanned) {
        this.hasMetadataScanned = hasMetadataScanned;
    }

    private void initFrescoLoader() {
        Fresco.initialize(getInstance().getApplicationContext());
    }

}
