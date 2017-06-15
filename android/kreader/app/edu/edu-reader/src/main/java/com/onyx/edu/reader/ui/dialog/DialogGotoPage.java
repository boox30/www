package com.onyx.edu.reader.ui.dialog;

import android.content.DialogInterface;
import android.text.InputType;
import android.widget.SeekBar;
import android.widget.Toast;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.reader.utils.PagePositionUtils;
import com.onyx.android.sdk.ui.dialog.OnyxCustomDialog;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.edu.reader.R;
import com.onyx.edu.reader.ui.actions.GotoPageAction;
import com.onyx.edu.reader.ui.data.ReaderDataHolder;

/**
 * Created by ming on 2017/6/14.
 */

public class DialogGotoPage {

    public static void show(final ReaderDataHolder readerDataHolder, boolean showProgress, final BaseCallback gotoPageCallback) {
        readerDataHolder.postDialogUiChangedEvent(true);
        final OnyxCustomDialog dlg = OnyxCustomDialog.getInputDialog(readerDataHolder.getContext(), readerDataHolder.getContext().getString(R.string.dialog_quick_view_enter_page_number), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                OnyxCustomDialog onyxCustomDialog = (OnyxCustomDialog) dialog;
                String page = onyxCustomDialog.getInputValue().toString();
                if (!StringUtils.isNullOrEmpty(page)) {
                    int pageNumber = PagePositionUtils.getPageNumber(page);
                    if (pageNumber >= 0 && pageNumber <= readerDataHolder.getPageCount()) {
                        pageNumber = readerDataHolder.isFlowDocument() ? pageNumber : pageNumber - 1;
                        new GotoPageAction(pageNumber, true).execute(readerDataHolder, new BaseCallback() {
                            @Override
                            public void done(BaseRequest request, Throwable e) {
                                dialog.dismiss();
                                if (gotoPageCallback != null) {
                                    gotoPageCallback.done(request, e);
                                }
                            }
                        });
                    } else {
                        Toast.makeText(readerDataHolder.getContext(), readerDataHolder.getContext().getString(R.string.dialog_quick_view_enter_page_number_out_of_range_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(readerDataHolder.getContext(), readerDataHolder.getContext().getString(R.string.dialog_quick_view_enter_page_number_empty_error), Toast.LENGTH_SHORT).show();
                }
            }
        }).setOnCloseListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                readerDataHolder.postDialogUiChangedEvent(false);
            }
        });
        dlg.getInputEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        dlg.getInputEditText().setHint("1-" + readerDataHolder.getPageCount());
        if (showProgress) {
            dlg.enableProgress(readerDataHolder.getPageCount(), readerDataHolder.getCurrentPage(), new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int pageNumber = readerDataHolder.isFlowDocument() ? progress : progress - 1;
                    pageNumber = Math.max(pageNumber, 0);
                    pageNumber = Math.min(pageNumber, readerDataHolder.getPageCount() - 1);
                    gotoPage(readerDataHolder, pageNumber, true);
                    dlg.getInputEditText().setText(String.valueOf(Math.max(progress, 1)));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        readerDataHolder.trackDialog(dlg);
        dlg.show();
    }

    private static void gotoPage(final ReaderDataHolder readerDataHolder, Object o, final boolean abortPendingTasks) {
        if (o == null) {
            return;
        }
        int page = (int) o;
        new GotoPageAction(page, abortPendingTasks).execute(readerDataHolder);
    }
}