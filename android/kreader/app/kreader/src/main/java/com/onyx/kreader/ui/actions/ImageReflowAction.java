package com.onyx.kreader.ui.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageConstants;
import com.onyx.kreader.R;
import com.onyx.kreader.common.BaseReaderRequest;
import com.onyx.kreader.host.navigation.NavigationArgs;
import com.onyx.kreader.host.request.ChangeLayoutRequest;
import com.onyx.kreader.reflow.ImageReflowSettings;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.dialog.DialogLoading;
import com.onyx.kreader.ui.dialog.DialogReflowSettings;

/**
 * Created by zhuzeng on 5/18/16.
 */
public class ImageReflowAction extends BaseAction {
    private DialogReflowSettings reflowSettingsDialog;
    private DialogLoading dialogLoading;

    public void execute(final ReaderDataHolder readerDataHolder) {
        showReflowSettingsDialog(readerDataHolder);
    }

    private void showReflowSettingsDialog(final ReaderDataHolder readerDataHolder) {
        if (reflowSettingsDialog == null) {
            ImageReflowSettings settings = readerDataHolder.getReader().getImageReflowSettings();
            settings.dev_width = readerDataHolder.getDisplayWidth();
            settings.dev_height = readerDataHolder.getDisplayHeight();
            reflowSettingsDialog = new DialogReflowSettings(readerDataHolder, settings, new DialogReflowSettings.ReflowCallback() {
                @Override
                public void onFinished(boolean confirm, ImageReflowSettings settings) {
                    if (confirm && settings != null) {
                        showLoadingDialog(readerDataHolder);
                        hideReflowSettingsDialog();
                        BaseReaderRequest request = new ChangeLayoutRequest(PageConstants.IMAGE_REFLOW_PAGE, new NavigationArgs());
                        readerDataHolder.submitRenderRequest(request, new BaseCallback() {
                            @Override
                            public void done(BaseRequest request, Throwable e) {
                                hideLoadingDialog();
                            }
                        });
                    }
                }
            });
            reflowSettingsDialog.show();
            ShowReaderMenuAction.hideReaderMenu();
        }
    }

    private void hideReflowSettingsDialog() {
        if (reflowSettingsDialog != null) {
            reflowSettingsDialog.dismiss();
            reflowSettingsDialog = null;
        }
    }

    private DialogLoading showLoadingDialog(final ReaderDataHolder holder) {
        if (dialogLoading == null) {
            dialogLoading = new DialogLoading(reflowSettingsDialog.getContext(),
                    holder.getContext().getResources().getString(R.string.reflowing),
                    true, new DialogLoading.Callback() {
                @Override
                public void onCanceled() {
                }
            });
        }
        dialogLoading.show();
        return dialogLoading;
    }

    private void hideLoadingDialog() {
        if (dialogLoading != null) {
            dialogLoading.dismiss();
            dialogLoading = null;
        }
    }
}
