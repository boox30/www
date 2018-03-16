package com.onyx.jdread.setting.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.jdread.R;
import com.onyx.jdread.databinding.FragmentPasswordSettingsBinding;
import com.onyx.jdread.main.common.BaseFragment;
import com.onyx.jdread.main.common.ResManager;
import com.onyx.jdread.main.common.ToastUtil;
import com.onyx.jdread.setting.event.BackToDeviceConfigEvent;
import com.onyx.jdread.setting.event.BackToDeviceConfigFragment;
import com.onyx.jdread.setting.event.PasswordSettingEvent;
import com.onyx.jdread.setting.model.PswSettingModel;
import com.onyx.jdread.setting.model.SettingBundle;
import com.onyx.jdread.setting.request.RxLockPswSettingRequest;
import com.onyx.jdread.setting.view.NumberKeyboardPopWindow;
import com.onyx.jdread.setting.view.NumberKeyboardView;
import com.onyx.jdread.util.Utils;
import com.onyx.jdread.util.ViewCompatUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by hehai on 17-12-28.
 */

public class PasswordSettingFragment extends BaseFragment {

    private FragmentPasswordSettingsBinding passwordSettingBinding;
    private NumberKeyboardPopWindow keyboardPopupWindow;
    private TextView[] textViews;
    private PswSettingModel pswSettingModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBinding(inflater, container);
        return passwordSettingBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showInputKeyboard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideLoadingDialog();
        dismissKeyboardPopupWindow();
    }


    private void showInputKeyboard() {
        final boolean encrypted = passwordSettingBinding.getPswSettingModel().encrypted.get();
        textViews = new TextView[]{passwordSettingBinding.unlockPasswordOne, passwordSettingBinding.unlockPasswordTwo
                , passwordSettingBinding.unlockPasswordThree, passwordSettingBinding.unlockPasswordFour};
        final EditText focusView = passwordSettingBinding.passwordEncryptEdit;
        focusView.requestFocus();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        showPopupWindow(focusView, encrypted, getKeyboardListener());
    }

    private NumberKeyboardView.OnKeyboardListener getKeyboardListener() {
        return new NumberKeyboardView.OnKeyboardListener() {
            @Override
            public void onInsertKeyEvent(String text) {
            }

            @Override
            public void onDeleteKeyEvent() {
            }

            @Override
            public void onCustomKeyEvent() {
                processKeyboardCustomKey();
            }

            @Override
            public void onFinishEvent(String password) {
                pswSettingModel.unLockPassword(password);
            }
        };
    }

    private void processKeyboardCustomKey() {
        boolean encrypted = passwordSettingBinding.getPswSettingModel().encrypted.get();
        if (encrypted) {
            processForgotPassword();
        } else {
            processNextEncrypt();
        }
    }

    private void processForgotPassword() {
        getViewEventCallBack().gotoView(PasswordFindFragment.class.getName());
    }

    private void processNextEncrypt() {
        int imeOptions = keyboardPopupWindow.getEditText().getImeOptions();
        if (imeOptions == EditorInfo.IME_ACTION_NEXT) {
            passwordSettingBinding.phoneEdit.requestFocus();
            passwordSettingBinding.phoneEdit.setSelection(passwordSettingBinding.phoneEdit.length());
        } else {
            passwordSettingBinding.getPswSettingModel().confirmPassword();
        }
    }

    private void initBinding(LayoutInflater inflater, @Nullable ViewGroup container) {
        passwordSettingBinding = FragmentPasswordSettingsBinding.inflate(inflater, container, false);
        pswSettingModel = new PswSettingModel(SettingBundle.getInstance().getEventBus());
        passwordSettingBinding.passwordSettingsTitle.setTitleModel(pswSettingModel.titleBarModel);
        passwordSettingBinding.setPswSettingModel(pswSettingModel);
        initView();
    }

    private void initView() {
        ViewCompatUtil.disableEditShowSoftInput(passwordSettingBinding.passwordEncryptEdit, passwordSettingBinding.phoneEdit);
        setEditOnFocusChangeListener(passwordSettingBinding.passwordEncryptEdit);
        setEditOnFocusChangeListener(passwordSettingBinding.phoneEdit);
    }

    private void setEditOnFocusChangeListener(EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showPopupWindow((EditText) v);
                }
            }
        });
    }

    private NumberKeyboardPopWindow showPopupWindow(EditText focusView) {
        return showPopupWindow(focusView,
                passwordSettingBinding.getPswSettingModel().encrypted.get(),
                getKeyboardListener());
    }

    private NumberKeyboardPopWindow showPopupWindow(EditText focusView, boolean encrypted, NumberKeyboardView.OnKeyboardListener listener) {
        if (keyboardPopupWindow == null) {
            keyboardPopupWindow = new NumberKeyboardPopWindow(getContext(), focusView, listener);
        }
        if (encrypted) {
            keyboardPopupWindow.getKeyboardView().setCustomText(getString(R.string.forgot_psw));
            keyboardPopupWindow.setTextViews(textViews);
            keyboardPopupWindow.bindTextViews(textViews, listener);
        } else {
            keyboardPopupWindow.getKeyboardView().setCustomDrawable(getResources().getDrawable(R.drawable.enter_icon));
            keyboardPopupWindow.bindEdit(focusView, listener);
        }

        keyboardPopupWindow.showAtBottomCenter(passwordSettingBinding.getRoot());
        return keyboardPopupWindow;
    }

    private void dismissKeyboardPopupWindow() {
        if (keyboardPopupWindow == null) {
            return;
        }
        keyboardPopupWindow.dismiss();
        keyboardPopupWindow = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.ensureRegister(SettingBundle.getInstance().getEventBus(), this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.ensureUnregister(SettingBundle.getInstance().getEventBus(), this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBackToDeviceConfigFragment(BackToDeviceConfigFragment event) {
        Utils.hideSoftWindow(getActivity());
        viewEventCallBack.viewBack();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBackToDeviceConfigEvent(BackToDeviceConfigEvent event) {
        ToastUtil.showToast(ResManager.getString(R.string.encryption_success));
        Utils.hideSoftWindow(getActivity());
        viewEventCallBack.viewBack();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLockPasswordSettingEvent(PasswordSettingEvent event) {
        final RxLockPswSettingRequest request = new RxLockPswSettingRequest(event.data);
        request.execute(new RxCallback() {
            @Override
            public void onNext(Object o) {
                if (request.isSucceed()) {
                    onBackToDeviceConfigEvent(new BackToDeviceConfigEvent());
                } else {
                    ToastUtil.showToast(R.string.network_or_server_error);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                ToastUtil.showToast(R.string.network_or_server_error);
            }

            @Override
            public void onFinally() {
                hideLoadingDialog();
            }
        });
        showLoadingDialog(getString(R.string.submitting));
    }
}
