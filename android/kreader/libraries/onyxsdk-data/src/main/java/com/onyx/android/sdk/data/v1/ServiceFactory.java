package com.onyx.android.sdk.data.v1;

import android.app.Application;

import com.onyx.android.sdk.data.v2.ContentService;
import com.onyx.android.sdk.data.v2.TokenHeaderInterceptor;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.Utils;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by zhuzeng on 8/10/16.
 */
public class ServiceFactory {
    private static ConcurrentHashMap<String, Retrofit> retrofitMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, OkHttpClient> clientMap = new ConcurrentHashMap<>();
    private static InetSocketAddress inetSocketAddress;
    private static boolean openProxy;
    private static final long cacheSize = 10 * 1024 * 1024; // 10 MB
    private static final String cacheDirName = "okhttp";

    private static Retrofit getRetrofit(String baseUrl) {
        if (!retrofitMap.containsKey(baseUrl)) {
            Retrofit retrofit = getBaseRetrofitBuilder(baseUrl).build();
            retrofitMap.put(baseUrl, retrofit);
        }
        return retrofitMap.get(baseUrl);
    }

    public static Retrofit.Builder getBaseRetrofitBuilder(final String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getBaseOkClientBuilder(baseUrl))
                .addConverterFactory(FastJsonConverterFactory.create());
    }

    public static OkHttpClient getBaseOkClientBuilder(final String baseUrl) {
        if (clientMap.containsKey(baseUrl)) {
            return clientMap.get(baseUrl);
        }
        OkHttpClient client =
                configOkhttp(new OkHttpClient().newBuilder())
                        .build();
        clientMap.put(baseUrl, client);
        return client;
    }

    public static final OnyxAccountService getAccountService(final String baseUrl) {
        return getSpecifyService(OnyxAccountService.class, baseUrl);
    }

    public static final OnyxBookStoreService getBookStoreService(final String baseUrl) {
        return getSpecifyService(OnyxBookStoreService.class, baseUrl);
    }

    public static final OnyxDictionaryService getDictionaryService(final String baseUrl) {
        return getSpecifyService(OnyxDictionaryService.class, baseUrl);
    }

    public static final OnyxHardwareService getHardwareService(final String baseUrl) {
        return getSpecifyService(OnyxHardwareService.class, baseUrl);
    }

    public static final OnyxOTAService getOTAService(final String baseUrl) {
        return getSpecifyService(OnyxOTAService.class, baseUrl);
    }

    public static final OnyxFileDownloadService getFileDownloadService(final String baseUrl) {
        return getSpecifyService(OnyxFileDownloadService.class, baseUrl);
    }

    public static final OnyxConsumerService getConsumerService(final String baseUrl) {
        return getSpecifyService(OnyxConsumerService.class, baseUrl);
    }

    public static final OnyxGroupService getGroupService(final String baseUrl) {
        return getSpecifyService(OnyxGroupService.class, baseUrl);
    }

    public static final OnyxPushService getPushService(final String baseUrl) {
        return getSpecifyService(OnyxPushService.class, baseUrl);
    }

    public static final OnyxStatisticsService getStatisticsService(final String baseUrl) {
        return getSpecifyService(OnyxStatisticsService.class, baseUrl);
    }

    public static final OnyxLogService getLogService(final String baseUrl) {
        return getSpecifyService(OnyxLogService.class, baseUrl);
    }

    public static final ContentService getContentService(final String baseUrl) {
        return getSpecifyService(ContentService.class, baseUrl);
    }

    public static final OnyxSyncService getSyncService(final String baseUrl) {
        return getSpecifyService(OnyxSyncService.class, baseUrl);
    }

    public static final OnyxBackupService getBackupService(final String baseUrl) {
        return getSpecifyService(OnyxBackupService.class, baseUrl);
    }

    public static final OnyxHomeworkService getHomeworkService(final String baseUrl) {
        return getSpecifyService(OnyxHomeworkService.class, baseUrl);
    }

    public static final <T> T getSpecifyService(final Class<T> service, final String baseUrl) {
        return getRetrofit(baseUrl).create(service);
    }

    private static void addInterceptors(OkHttpClient.Builder builder, List<Interceptor> interList) {
        if (CollectionUtils.isNullOrEmpty(interList)) {
            return;
        }
        for (Interceptor interceptor : interList) {
            builder.addInterceptor(interceptor);
        }
    }

    private static void addNetworkInterceptors(OkHttpClient.Builder builder, List<Interceptor> interList) {
        if (CollectionUtils.isNullOrEmpty(interList)) {
            return;
        }
        for (Interceptor interceptor : interList) {
            builder.addNetworkInterceptor(interceptor);
        }
    }

    private static void removeInterceptors(OkHttpClient.Builder builder, Class clazz) {
        if (!CollectionUtils.isNullOrEmpty(builder.interceptors())) {
            for (Interceptor interceptor : builder.interceptors()) {
                if (clazz.isInstance(interceptor)) {
                    builder.interceptors().remove(interceptor);
                    removeInterceptors(builder, clazz);
                }
            }
        }
    }

    private static void restoreBuilderFromClient(OkHttpClient okHttpClient, OkHttpClient.Builder builder) {
        if (okHttpClient == null || builder == null) {
            return;
        }
        addInterceptors(builder, okHttpClient.interceptors());
        addNetworkInterceptors(builder, okHttpClient.networkInterceptors());
        builder.authenticator(okHttpClient.authenticator());
    }

    public static Retrofit addRetrofitTokenHeader(final String baseUrl, final String tokenKey, final String token) {
        OkHttpClient okHttpClient = clientMap.get(baseUrl);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (okHttpClient != null) {
            restoreBuilderFromClient(okHttpClient, builder);
            removeInterceptors(builder, TokenHeaderInterceptor.class);
        }
        builder.addInterceptor(new TokenHeaderInterceptor(tokenKey, token));
        configOkhttp(builder);
        okHttpClient = builder.build();
        Retrofit retrofit = getBaseRetrofitBuilder(baseUrl).client(okHttpClient).build();
        retrofitMap.put(baseUrl, retrofit);
        clientMap.put(baseUrl, okHttpClient);
        return retrofit;
    }

    public static void addAuthenticator(final String baseUrl, Authenticator auth) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        restoreBuilderFromClient(clientMap.get(baseUrl), builder);
        builder.authenticator(auth);
        clientMap.put(baseUrl, builder.build());
    }

    public static void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        ServiceFactory.inetSocketAddress = inetSocketAddress;
    }

    public static void setOpenProxy(boolean openProxy) {
        ServiceFactory.openProxy = openProxy;
    }


    private static OkHttpClient.Builder setProxy(OkHttpClient.Builder builder){
        if (openProxy && null != inetSocketAddress) {
            return builder.proxy(new Proxy(Proxy.Type.HTTP, inetSocketAddress));
        } else {
            return builder;
        }
    }

    private static OkHttpClient.Builder setCache(OkHttpClient.Builder builder) {
        Application app = Utils.getApp();
        if (null != app) {
            File cacheDir = new File(app.getCacheDir(), cacheDirName);
            Cache cache = new Cache(cacheDir, cacheSize);
            return builder.cache(cache);
        } else {
            Debug.i("Utils don't init application, okhttp cannot set cache");
            return builder;
        }
    }

    private static OkHttpClient.Builder configOkhttp(OkHttpClient.Builder builder) {
        setProxy(builder);
        return setCache(builder);
    }
}
