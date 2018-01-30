package com.onyx.jdread.shop.utils;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.onyx.android.sdk.data.OnyxDownloadManager;

/**
 * Created by jackdeng on 2017/12/21.
 */

public class DownLoadHelper {

    public static final int DOWNLOAD_PERCENT_FINISH = 100;

    public static boolean isDownloaded (int status) {
        return status == FileDownloadStatus.completed;
    }

    public static boolean isDownloading (int status) {
        return status == FileDownloadStatus.progress;
    }

    public static boolean isPause (int status) {
        return status == FileDownloadStatus.paused;
    }

    public static boolean isError (int status) {
        return status == FileDownloadStatus.error;
    }

    public static boolean isStarted (int status) {
        return status == FileDownloadStatus.started;
    }

    public static boolean isConnected (int status) {
        return status == FileDownloadStatus.connected;
    }

    public static byte getPausedState() {
        return FileDownloadStatus.paused;
    }

    public static boolean canInsertBookDetail (int status){
        return isDownloaded(status) || isPause(status) || isError(status) || isStarted(status) || isDownloading(status);
    }

    public static void stopDownloadingTask(Object tag) {
        BaseDownloadTask task = OnyxDownloadManager.getInstance().getTask(tag);
        removeDownloadingTask(tag);
        if (task != null) {
            task.pause();
        }
    }

    public static void removeDownloadingTask(Object tag) {
        OnyxDownloadManager.getInstance().removeTask(tag);
    }
}