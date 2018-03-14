package com.onyx.android.sdk.ui.dialog;

import android.app.Dialog;
import android.content.Context;

import com.onyx.android.sdk.api.device.epd.EpdController;

/**
 * Created by wangxu on 17-3-16.
 */

public class OnyxBaseDialog extends Dialog {

    public volatile static boolean showing;

    public OnyxBaseDialog(Context context) {
        super(context);
    }

    public OnyxBaseDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected OnyxBaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void show() {
        EpdController.disableRegal();
        showing = true;
        super.show();
    }

    @Override
    public void dismiss() {
        EpdController.enableRegal();
        showing = false;
        super.dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showing = false;
    }

    @Override
    public void hide() {
        showing = false;
        EpdController.enableRegal();
        super.hide();
    }
}
