package com.onyx.edu.homework;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.onyx.android.sdk.ui.compat.AppCompatImageViewCollection;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.DeviceUtils;
import com.onyx.android.sdk.utils.PackageUtils;
import com.onyx.edu.homework.base.BaseNoteAction;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.HomeworkGeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.ShapeGeneratedDatabaseHolder;

import me.yokeyword.fragmentation.Fragmentation;

/**
 * Created by lxm on 2017/12/5.
 */

public class HomeworkApp extends MultiDexApplication {

    public static HomeworkApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        BaseNoteAction.setAppContext(this);
        initContentProvider(this);
        initDataProvider();
        initCompatColorImageConfig();
        Debug.setDebug(BuildConfig.DEBUG || DeviceUtils.isEngVersion() || PackageUtils.getAppType(this).equals(PackageUtils.APP_TYPE_DEBUG));

        // fragment
        Fragmentation.builder()
                     .stackViewMode(Fragmentation.BUBBLE)
                     .debug(BuildConfig.DEBUG)
                     .install();
    }

    static public void initContentProvider(final Context context) {
        try {
            FlowConfig.Builder builder = new FlowConfig.Builder(context);
            FlowManager.init(builder.build());
        } catch (Exception e) {
            if (com.onyx.android.sdk.dataprovider.BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public void initDataProvider() {
        FlowConfig.Builder builder = new FlowConfig.Builder(this);
        builder.addDatabaseHolder(ShapeGeneratedDatabaseHolder.class);
        builder.addDatabaseHolder(HomeworkGeneratedDatabaseHolder.class);
        FlowManager.init(builder.build());
    }

    private void initCompatColorImageConfig() {
        AppCompatImageViewCollection.setAlignView(true);
    }
}
