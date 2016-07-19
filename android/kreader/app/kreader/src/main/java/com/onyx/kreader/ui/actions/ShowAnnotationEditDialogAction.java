package com.onyx.kreader.ui.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.kreader.dataprovider.Annotation;
import com.onyx.kreader.ui.ReaderActivity;
import com.onyx.kreader.ui.dialog.DialogAnnotation;

/**
 * Created by joy on 7/11/16.
 */
public class ShowAnnotationEditDialogAction extends BaseAction {

    public interface OnEditListener{
        void onUpdateFinished(Annotation annotation);
        void onDeleteFinished();
    }

    private Annotation annotation;

    public ShowAnnotationEditDialogAction(final Annotation annotation) {
        this.annotation = annotation;
    }

    private OnEditListener mOnEditListener;

    @Override
    public void execute(ReaderActivity readerActivity) {
        editAnnotationWithDialog(readerActivity, annotation);
    }

    private void editAnnotationWithDialog(final ReaderActivity readerActivity, final Annotation annotation) {
        DialogAnnotation dlg = new DialogAnnotation(readerActivity, DialogAnnotation.AnnotationAction.update, annotation.getNote(), new DialogAnnotation.Callback() {
            @Override
            public void onAddAnnotation(String note) {
            }

            @Override
            public void onUpdateAnnotation(String note) {
                updateAnnotation(readerActivity, annotation, note);
            }

            @Override
            public void onRemoveAnnotation() {
                deleteAnnotation(readerActivity, annotation);
            }
        });
        dlg.show();
    }

    private void updateAnnotation(final ReaderActivity readerActivity, final Annotation annotation, final String note) {
        new UpdateAnnotationAction(annotation, note).execute(readerActivity, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (mOnEditListener != null){
                    annotation.setNote(note);
                    mOnEditListener.onUpdateFinished(annotation);
                }
            }
        });
    }

    private void deleteAnnotation(final ReaderActivity readerActivity, final Annotation annotation) {
        new DeleteAnnotationAction(annotation).execute(readerActivity, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (mOnEditListener != null){
                    mOnEditListener.onDeleteFinished();
                }
            }
        });
    }

    public void setOnEditListener(OnEditListener onEditListener) {
        mOnEditListener = onEditListener;
    }
}
