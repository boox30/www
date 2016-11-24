package com.onyx.android.sdk.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.data.request.cloud.CloudFileDownloadRequest;
import com.onyx.android.sdk.data.utils.DownloadListener;
import com.onyx.android.sdk.data.utils.NotificationItem;

import java.util.LinkedHashMap;

/**
 * Created by suicheng on 2016/8/15.
 */
public class OnyxDownloadManager {

    private static int DEFAULT_PROGRESS_MIN_INTERVAL = 1200;
    private static OnyxDownloadManager instance;
    private FileDownloadNotificationHelper<NotificationItem> helper = new FileDownloadNotificationHelper<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    private LinkedHashMap<Object, BaseDownloadTask> taskMap = new LinkedHashMap<>();

    private OnyxDownloadManager(Context context) {
        FileDownloader.init(context);
        FileDownloader.getImpl().setMaxNetworkThreadCount(5);
    }

    /**
     * must init in Application
     */
    public static synchronized OnyxDownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new OnyxDownloadManager(context);
        }
        return instance;
    }

    public static synchronized OnyxDownloadManager getInstance() {
        return getInstance(getContext());
    }

    public static synchronized Context getContext() {
        return FileDownloadHelper.getAppContext();
    }

    public CloudFileDownloadRequest createDownloadRequest(final String url, final String path, final Object tag) {
        CloudFileDownloadRequest request = new CloudFileDownloadRequest(url, path, tag);
        request.setTaskId(FileDownloadUtils.generateId(url, path));
        return request;
    }

    public DownloadListener createDownloadListener(final CloudFileDownloadRequest request, BaseCallback baseCallback) {
        return new DownloadListener(request, baseCallback, helper);
    }

    public DownloadListener createDownloadListener(final NotificationItem.NotificationBean bean, final CloudFileDownloadRequest request,
                                                   BaseCallback baseCallback) {
        DownloadListener listener = createDownloadListener(request, baseCallback);
        listener.setNotificationBean(bean);
        return listener;
    }

    public BaseDownloadTask download(final String url, final String path, final Object tag, final BaseCallback baseCallback) {
        CloudFileDownloadRequest request = createDownloadRequest(url, path, tag);
        return download(request, baseCallback);
    }

    public BaseDownloadTask download(final CloudFileDownloadRequest request, final BaseCallback baseCallback) {
        request.setTaskId(FileDownloadUtils.generateId(request.getUrl(), request.getPath()));
        BaseDownloadTask task = FileDownloader.getImpl().create(request.getUrl()).setPath(request.getPath()).setTag(request.getTag());
        task.setListener(createDownloadListener(request, baseCallback));
        return task;
    }

    public BaseDownloadTask downloadWithNotify(final String url, final String path, final Object tag, NotificationItem.NotificationBean bean,
                                               BaseCallback callback) {
        CloudFileDownloadRequest request = createDownloadRequest(url, path, tag);
        DownloadListener listener = createDownloadListener(bean, request, callback);
        BaseDownloadTask task = FileDownloader.getImpl().create(request.getUrl()).setPath(request.getPath()).setTag(request.getTag());
        task.setListener(listener);
        return task;
    }

    public BaseDownloadTask downloadWithNotify(final CloudFileDownloadRequest request, NotificationItem.NotificationBean bean,
                                               BaseCallback callback) {
        BaseDownloadTask task = FileDownloader.getImpl().create(request.getUrl()).setPath(request.getPath()).setTag(request.getTag());
        request.setTaskId(FileDownloadUtils.generateId(request.getUrl(), request.getPath()));
        DownloadListener listener = createDownloadListener(bean, request, callback);
        task.setListener(listener);
        return task;
    }

    public int downloadDirectly(final String url, final String path, final Object tag, final BaseCallback baseCallback) {
        return startDownload(download(url, path, tag, baseCallback));
    }

    public int downloadDirectly(final CloudFileDownloadRequest request, final BaseCallback baseCallback) {
        return startDownload(download(request, baseCallback));
    }

    public int startDownload(BaseDownloadTask task) {
        task.setCallbackProgressMinInterval(DEFAULT_PROGRESS_MIN_INTERVAL);
        return task.start();
    }

    public int startDownload(BaseDownloadTask task, int progressMinInterval) {
        task.setCallbackProgressMinInterval(progressMinInterval);
        return task.start();
    }

    public void removeTask(Object key) {
        taskMap.remove(key);
    }

    public void addTask(Object key, BaseDownloadTask task) {
        taskMap.put(key, task);
    }

    public BaseDownloadTask getTask(Object key) {
        return taskMap.get(key);
    }

    public void clearTaskQueue() {
        taskMap.clear();
    }

    public int getTaskProgress(int taskId) {
        return (int) (FileDownloader.getImpl().getSoFar(taskId) * 1.0f / FileDownloader.getImpl().getTotal(taskId) * 100);
    }

    public int getTaskStatus(int taskId, String path) {
        return FileDownloader.getImpl().getStatus(taskId, path);
    }

}
