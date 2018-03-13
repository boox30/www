package com.onyx.android.sdk.ui.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ming on 2017/1/7.
 */

public class ToastUtils {

    private static String oldMsg;
    protected static Toast toast = null;
    private static long oneTime = 0;
    private static long twoTime = 0;

    public static void showToast(Context appContext, String s) {
        if (toast == null) {
            toast = Toast.makeText(appContext.getApplicationContext(), s, Toast.LENGTH_SHORT);
            View view = toast.getView();
            TextView textView = (TextView) view.findViewById(android.R.id.message);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setMaxLines(20);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (s.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = s;
                toast.setText(s);
                toast.show();
            }
        }
        oneTime = twoTime;
    }


    public static void showToast(Context appContext, int resId) {
        showToast(appContext, appContext.getString(resId));
    }
}
