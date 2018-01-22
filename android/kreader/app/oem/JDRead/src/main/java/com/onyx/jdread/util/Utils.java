package com.onyx.jdread.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.onyx.android.sdk.data.model.Metadata;
import com.onyx.android.sdk.data.utils.JSONObjectParseUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.R;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookExtraInfoBean;

import java.text.DecimalFormat;

/**
 * Created by li on 2017/12/25.
 */

public class Utils {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            return activeNetworkInfo.isAvailable();
        }
        return false;
    }

    public static void showMessage(String message) {
        Toast.makeText(JDReadApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    public static void sendTimeChangeBroadcast() {
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        JDReadApplication.getInstance().sendBroadcast(timeChanged);
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public static int getScreenWidth(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    public static void hideSoftWindow(Activity activity) {
        InputMethodManager imm = (InputMethodManager) JDReadApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    public static void showSoftWindow(View view) {
        InputMethodManager imm = (InputMethodManager) JDReadApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        view.requestFocus();
    }

    public static String keepPoints(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return decimalFormat.format(value);
    }

    public static String bookSizeFormat(long size) {
        return keepPoints(size * 1f / 1000 / 1000) + "MB";
    }

    public static boolean hasDownload(String extraAttributes) {
        if (StringUtils.isNullOrEmpty(extraAttributes)) {
            return false;
        }
        BookExtraInfoBean bean = JSONObjectParseUtils.toBean(extraAttributes, BookExtraInfoBean.class);
        return bean.percentage != 0;
    }

    public static String updateState(String extraAttributes) {
        if (StringUtils.isNullOrEmpty(extraAttributes)) {
            return "";
        }
        BookExtraInfoBean bean = JSONObjectParseUtils.toBean(extraAttributes, BookExtraInfoBean.class);
        String state = null;
        switch (bean.downLoadState) {
            case FileDownloadStatus.completed:
                state = "";
                break;
            case FileDownloadStatus.paused:
                state = String.format(JDReadApplication.getInstance().getResources().getString(R.string.paused), bean.percentage) + "%";
                break;
            case FileDownloadStatus.progress:
                state = String.format(JDReadApplication.getInstance().getResources().getString(R.string.downloading_progress), bean.percentage) + "%";
                break;
            default:
                state = "";
                break;
        }
        return state;
    }

    public static float getValuesFloat(int res) {
        TypedValue outValue = new TypedValue();
        JDReadApplication.getInstance().getResources().getValue(res, outValue, true);
        return outValue.getFloat();
    }
}