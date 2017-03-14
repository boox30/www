package com.onyx.android.sdk.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.onyx.android.sdk.ui.R;

/**
 * Created by ming on 2017/3/14.
 */

public class OnyxCustomDialog extends Dialog implements DialogInterface{

    private TextView title;
    private Button btnCancel;
    private Button btnOk;
    private EditText inputEditText;

    public static OnyxCustomDialog getConfirmDialog(Context context, String message, DialogInterface.OnClickListener onClickListener) {
        OnyxCustomDialog dialog = new OnyxCustomDialog(context);

        dialog.setTitle(message);
        dialog.setPositiveButton(context.getString(R.string.ok), onClickListener);
        dialog.setNegativeButton(context.getString(R.string.cancel), null);
        dialog.setCancelable(false);
        return dialog;
    }

    public static OnyxCustomDialog getInputDialog(Context context, String message, DialogInterface.OnClickListener onClickListener) {
        OnyxCustomDialog dialog = new OnyxCustomDialog(context);

        dialog.enableInputEditText();
        dialog.setTitle(message);
        dialog.setPositiveButton(context.getString(R.string.ok), onClickListener);
        dialog.setNegativeButton(context.getString(R.string.cancel), null);
        return dialog;
    }

    public OnyxCustomDialog(Context context) {
        super(context, R.style.CustomDialog);
        setContentView(R.layout.dialog_custom);
        initView();
    }

    private void initView() {
        title = (TextView) findViewById(R.id.title);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnOk = (Button) findViewById(R.id.btn_ok);
        inputEditText = (EditText) findViewById(R.id.input);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setTitle(CharSequence message) {
        title.setText(message);
    }

    public void setTitle(int resId) {
        title.setText(resId);
    }

    public void setPositiveButton(CharSequence text, final OnClickListener listener) {
        btnOk.setText(text);
        if (listener != null) {
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(OnyxCustomDialog.this, BUTTON_POSITIVE);
                    dismiss();
                }
            });
        }

    }

    public void setNegativeButton(CharSequence text, final OnClickListener listener) {
        btnCancel.setText(text);
        if (listener != null) {
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(OnyxCustomDialog.this, BUTTON_NEGATIVE);
                    dismiss();
                }
            });
        }
    }

    public void setCancelable(boolean cancel) {
        setCanceledOnTouchOutside(cancel);
    }


    public EditText getInputEditText() {
        return inputEditText;
    }

    public Editable getInputValue() {
        return inputEditText.getText();
    }

    public void enableInputEditText() {
        inputEditText.setVisibility(View.VISIBLE);
    }
}
