package com.onyx.cloud.store.request;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.cloud.CloudManager;

import org.json.JSONObject;

import android.util.Log;

import com.onyx.cloud.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * Created by zhuzeng on 11/21/15.
 */
public abstract class BaseCloudRequest extends BaseRequest {

    private static final String TAG = BaseCloudRequest.class.getSimpleName();
    public static final String RESPONSE_KEY_ERROR = "error";
    private boolean saveToLocal = true;

    public BaseCloudRequest() {
        super();
    }

    public boolean isSaveToLocal() {
        return saveToLocal;
    }

    public void setSaveToLocal(boolean save) {
        saveToLocal = save;
    }

    public void beforeExecute(final CloudManager parent) {
        parent.acquireWakeLock(getContext());
        benchmarkStart();
        if (isAbort()) {
        }
        if (getCallback() == null) {
            return;
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getCallback().start(BaseCloudRequest.this);
            }
        };
        if (isRunInBackground()) {
            parent.getLooperHandler().post(runnable);
        } else {
            runnable.run();
        }
    }

    public abstract void execute(final CloudManager parent) throws Exception;

    public void doComplexWork() {
    }

    /**
     * must not throw out exception from the method
     *
     * @param parent
     */
    public void afterExecute(final CloudManager parent) {
        try {
            afterExecuteImpl(parent);
        } catch (Throwable tr) {
            Log.w(TAG, tr);
        } finally {
            invokeCallback(parent);
        }
    }

    private void afterExecuteImpl(final CloudManager parent) throws Throwable {
        dumpException();
        benchmarkEnd();
    }

    private void invokeCallback(final CloudManager parent) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                BaseCallback.invoke(getCallback(), BaseCloudRequest.this, getException());
                parent.releaseWakeLock();
            }
        };

        if (isRunInBackground()) {
            parent.getLooperHandler().post(runnable);
        } else {
            runnable.run();
        }
    }

    private void invokeCallBackProgress(final CloudManager parent, final BaseCallback.ProgressInfo progressInfo) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getCallback().progress(BaseCloudRequest.this, progressInfo);
            }
        };

        if (isRunInBackground()) {
            parent.getLooperHandler().post(runnable);
        } else {
            runnable.run();
        }
    }



    protected boolean writeFileToDisk(CloudManager parent, ResponseBody body, File localFile, BaseCallback callback) {
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(localFile);
                BaseCallback.ProgressInfo progressInfo = new BaseCallback.ProgressInfo();
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    if (callback != null) {
                        if (fileSize > 0) {
                            progressInfo.progress = fileSizeDownloaded * 1.0 / fileSize * 100;
                            invokeCallBackProgress(parent, progressInfo);
                        }
                    }
                    dumpMessage(TAG, "file download: " + (fileSizeDownloaded * 1.0 / fileSize * 100) + "%");
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void dumpException() {
        if (hasException()) {
            Log.w(TAG, getException());
        }
    }

    public void dumpMessage(final String tag, final String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    public void dumpMessage(final String tag, Throwable throwable, JSONObject errorResponse) {
        if (throwable != null && throwable.getMessage() != null) {
            dumpMessage(tag, throwable.getMessage());
        }
        if (errorResponse != null) {
            dumpMessage(tag, errorResponse.toString());
        }
    }
}
