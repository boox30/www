package com.onyx.android.sdk.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.data.request.cloud.BaseCloudRequest;
import com.onyx.android.sdk.data.utils.CloudConf;
import com.onyx.android.sdk.dataprovider.BuildConfig;
import com.onyx.android.sdk.utils.LocaleUtils;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Created by zhuzeng on 8/10/16.
 */
public class StatisticsCloudManager {
    private CloudConf cloudConf;
    private com.onyx.android.sdk.common.request.RequestManager requestManager;
    private CloudConf chinaCloudConf;
    private CloudConf globalCloudConf;

    public StatisticsCloudManager() {
        requestManager = new com.onyx.android.sdk.common.request.RequestManager(Thread.NORM_PRIORITY);
        initCloudConf();
    }

    private void initCloudConf() {
        chinaCloudConf = new CloudConf(Constant.CN_HOST_BASE, Constant.CN_API_BASE, Constant.DEFAULT_CLOUD_STORAGE);
        globalCloudConf = new CloudConf(Constant.CN_HOST_BASE, Constant.CN_API_BASE, Constant.DEFAULT_CLOUD_STORAGE);
    }

    private CloudConf useCloudConf() {
        if (LocaleUtils.isChinese()) {
            cloudConf = chinaCloudConf;
        } else {
            cloudConf = globalCloudConf;
        }
        return cloudConf;
    }

    public void acquireWakeLock(final Context context, final String tag) {
        requestManager.acquireWakeLock(context, tag);
    }

    public void releaseWakeLock() {
        requestManager.releaseWakeLock();
    }

    private final Runnable generateRunnable(final BaseCloudRequest request) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    request.beforeExecute(StatisticsCloudManager.this);
                    request.execute(StatisticsCloudManager.this);
                } catch (Throwable tr) {
                    request.setException(tr);
                } finally {
                    request.afterExecute(StatisticsCloudManager.this);
                    requestManager.dumpWakelocks();
                    requestManager.removeRequest(request);
                }
            }
        };
        return runnable;
    }

    public boolean submitRequest(final Context context, final BaseCloudRequest request, final BaseCallback callback) {
        final Runnable runnable = generateRunnable(request);
        return requestManager.submitRequestToMultiThreadPool(context, request, runnable, callback);
    }

    public Handler getLooperHandler() {
        return requestManager.getLooperHandler();
    }

    public static boolean isWifiConnected(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi == null) {
            return false;
        }
        return wifi.isConnected();
    }

    public final CloudConf getCloudConf() {
        return useCloudConf();
    }

    static public void initDatabase(final Context context) {
        try {
            FlowConfig.Builder builder = new FlowConfig.Builder(context);
            FlowManager.init(builder.build());
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    static public void terminateCloudDatabase() {
        FlowManager.destroy();
    }
}
