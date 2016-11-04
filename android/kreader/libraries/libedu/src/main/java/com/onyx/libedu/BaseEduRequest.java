package com.onyx.libedu;import android.util.Log;import com.onyx.android.sdk.common.request.BaseCallback;import com.onyx.android.sdk.common.request.BaseRequest;import org.apache.commons.codec.digest.DigestUtils;import org.json.JSONObject;import java.util.Arrays;import java.util.HashMap;import java.util.Map;/** * Created by ming on 2016/11/1. */public abstract class BaseEduRequest extends BaseRequest{    private static final String TAG = BaseEduRequest.class.getSimpleName();    public static final String RESPONSE_KEY_ERROR = "error";    private volatile boolean saveToLocal = true;    public BaseEduRequest() {        super();    }    public boolean isSaveToLocal() {        return saveToLocal;    }    public void setSaveToLocal(boolean save) {        saveToLocal = save;    }    public void beforeExecute(final EduCloudManager parent) {//        parent.acquireWakeLock(getContext(), getClass().getSimpleName());        benchmarkStart();        if (isAbort()) {        }        if (getCallback() == null) {            return;        }        final Runnable runnable = new Runnable() {            @Override            public void run() {                getCallback().start(BaseEduRequest.this);            }        };        if (isRunInBackground()) {            parent.getLooperHandler().post(runnable);        } else {            runnable.run();        }    }    public abstract void execute(final EduCloudManager parent) throws Exception;    /**     * must not throw out exception from the method     *     * @param parent     */    public void afterExecute(final EduCloudManager parent) {        try {            afterExecuteImpl(parent);        } catch (Throwable tr) {            Log.w(TAG, tr);        } finally {            invokeCallback(parent);        }    }    private void afterExecuteImpl(final EduCloudManager parent) throws Throwable {        dumpException();        benchmarkEnd();    }    private void invokeCallback(final EduCloudManager parent) {        final Runnable runnable = new Runnable() {            @Override            public void run() {                BaseCallback.invoke(getCallback(), BaseEduRequest.this, getException());                parent.releaseWakeLock();            }        };        if (isRunInBackground()) {            parent.getLooperHandler().post(runnable);        } else {            runnable.run();        }    }    private void invokeCallBackProgress(final EduCloudManager parent, final BaseCallback.ProgressInfo progressInfo) {        final Runnable runnable = new Runnable() {            @Override            public void run() {                getCallback().progress(BaseEduRequest.this, progressInfo);            }        };        if (isRunInBackground()) {            parent.getLooperHandler().post(runnable);        } else {            runnable.run();        }    }    private void dumpException() {        if (hasException()) {            Log.w(TAG, getException());        }    }    public void dumpMessage(final String tag, final String message) {        if (com.onyx.android.sdk.dataprovider.BuildConfig.DEBUG) {            Log.d(tag, message);        }    }    public void dumpMessage(final String tag, Throwable throwable, JSONObject errorResponse) {        if (throwable != null && throwable.getMessage() != null) {            dumpMessage(tag, throwable.getMessage());        }        if (errorResponse != null) {            dumpMessage(tag, errorResponse.toString());        }    }}