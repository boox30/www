package com.onyx.android.dr;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.onyx.android.dr.device.DeviceConfig;
import com.onyx.android.dr.holder.LibraryDataHolder;
import com.onyx.android.dr.manager.LeanCloudManager;
import com.onyx.android.dr.reader.data.SingletonSharedPreference;
import com.onyx.android.dr.util.DRPreferenceManager;
import com.onyx.android.dr.util.DictPreference;
import com.onyx.android.sdk.data.CloudStore;
import com.onyx.android.sdk.data.Constant;
import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.data.OnyxDownloadManager;
import com.onyx.android.sdk.data.utils.CloudConf;
import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.dict.DictDatasInit;
import com.onyx.android.sdk.dict.data.DictionaryManager;
import com.raizlabs.android.dbflow.config.DRGeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.DatabaseHolder;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.util.ArrayList;
import java.util.List;

import static com.onyx.android.dr.util.Utils.getAllFolderPathList;

/**
 * Created by hehai on 17-6-26.
 */
public class DRApplication extends MultiDexApplication {
    private static DRApplication sInstance;
    private static CloudStore cloudStore;
    private static LibraryDataHolder libraryDataHolder;
    private int cartCount;
    private DictionaryManager dictionaryManager;
    private int apkDownloadReference;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(DRApplication.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DictPreference.init(this);
        initConfig();
    }

    private void initConfig() {
        try {
            sInstance = this;
            DRPreferenceManager.init(this);
            initDownloadManager();
            initCloudStore();
            initLeanCloud();
            initFrescoLoader();
            initDatabases(this, databaseHolderList());
            SingletonSharedPreference.init(this);
            turnOffLed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDownloadManager() {
        OnyxDownloadManager.init(sInstance.getApplicationContext());
        OnyxDownloadManager.getInstance();
    }

    private void initCloudStore() {
        CloudStore.init(sInstance.getApplicationContext());
    }

    private void initLeanCloud() {
        LeanCloudManager.initialize(this, DeviceConfig.sharedInstance(this).getLeanCloudApplicationId(),
                DeviceConfig.sharedInstance(this).getLeanCloudClientKey());
    }

    private void initFrescoLoader() {
        Fresco.initialize(sInstance.getApplicationContext());
    }

    public static CloudStore getCloudStore() {
        if (cloudStore == null) {
            cloudStore = new CloudStore();
            cloudStore.setCloudConf(getCloudConf());
        }
        return cloudStore;
    }

    public static DRApplication getInstance() {
        return sInstance;
    }

    private List<Class<? extends DatabaseHolder>> databaseHolderList() {
        List<Class<? extends DatabaseHolder>> list = new ArrayList<>();
        list.add(DRGeneratedDatabaseHolder.class);
        return list;
    }

    private static CloudConf getCloudConf() {
        String host = DeviceConfig.sharedInstance(sInstance).getCloudContentHost();
        String api = DeviceConfig.sharedInstance(sInstance).getCloudContentApi();
        CloudConf cloudConf = new CloudConf(host, api, Constant.DEFAULT_CLOUD_STORAGE);
        return cloudConf;
    }

    public static LibraryDataHolder getLibraryDataHolder() {
        if (libraryDataHolder == null) {
            libraryDataHolder = new LibraryDataHolder(sInstance);
            libraryDataHolder.setCloudManager(getCloudStore().getCloudManager());
        }
        return libraryDataHolder;
    }

    public void initDatabases(final Context context, final List<Class<? extends DatabaseHolder>> list) {
        FlowConfig.Builder builder = new FlowConfig.Builder(context);
        if (list != null) {
            for (Class tClass : list) {
                builder.addDatabaseHolder(tClass);
            }
        }
        FlowManager.init(builder.build());
    }

    public void initDictDatas() {
        List<String> allFolderPathList = getAllFolderPathList();
        DictDatasInit dictDatasInit = new DictDatasInit(DRApplication.getInstance(), allFolderPathList);
        dictionaryManager = dictDatasInit.dictionaryManager;
    }

    public void setApkDownloadReference(int apkDownloadReference) {
        this.apkDownloadReference = apkDownloadReference;
    }

    public DictionaryManager getDictionaryManager() {
        return dictionaryManager;
    }

    public int getCustomFontSize() {
        return DictPreference.getCustomTextSize(this);
    }

    public static DataManager getDataManager() {
        return getLibraryDataHolder().getDataManager();
    }

    public int getCartCount() {
        return cartCount;
    }

    public void setCartCount(int cartCount) {
        this.cartCount = cartCount;
    }

    private void turnOffLed() {
        Device.currentDevice.led(this, false);
    }
}
