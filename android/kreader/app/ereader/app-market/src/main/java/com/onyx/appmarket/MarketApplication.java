package com.onyx.appmarket;

import android.content.Context;

import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.onyx.android.sdk.data.CloudStore;
import com.onyx.android.sdk.data.OnyxDownloadManager;

/**
 * Created by suicheng on 2017/2/27.
 */
public class MarketApplication extends MultiDexApplication {
    static public MarketApplication sInstance;
    static private CloudStore cloudStore;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(MarketApplication.this);
    }    

    @Override
    public void onCreate() {
        super.onCreate();
        initConfig();
    }

    private void initConfig() {
        try {
            sInstance = this;
            initDownloadManager();
            initCloudStore();
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

    public static CloudStore getCloudStore() {
        if (cloudStore == null) {
            cloudStore = new CloudStore();
        }
        return cloudStore;
    }
}
