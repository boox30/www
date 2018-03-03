package com.onyx.android.note.note.menu;

import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.onyx.android.note.NoteDataBundle;
import com.onyx.android.note.action.ClearAllFreeShapesAction;
import com.onyx.android.note.action.PenWidthChangeAction;
import com.onyx.android.note.action.RedoAction;
import com.onyx.android.note.action.UndoAction;
import com.onyx.android.note.action.menu.ToggleTopMenuAction;
import com.onyx.android.note.common.StrokeWidth;
import com.onyx.android.note.common.base.BaseViewModel;
import com.onyx.android.sdk.note.NoteManager;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lxm on 2018/2/2.
 */

public class NoteMenuModel extends BaseViewModel {

    private static final String TAG = "NoteMenuModel";

    public ObservableField<Boolean> menuExpanded = new ObservableField<>(true);
    
    public NoteMenuModel(@NonNull EventBus eventBus) {
        super(eventBus);
    }

    public void setMenuExpanded(boolean expand) {
        this.menuExpanded.set(expand);
    }

    public void toggle() {
        boolean expanded = !menuExpanded.get();
        setMenuExpanded(expanded);
    }

    public void onToggle(final View view) {
        new ToggleTopMenuAction(getNoteManager(), this).execute(null);
    }

    public void onPenWidth1(View view) {
        new PenWidthChangeAction(getNoteManager())
                .setPenWidth(StrokeWidth.LIGHT.getWidth())
                .execute(null);
    }

    public void onPenWidth2(View view) {
        new PenWidthChangeAction(getNoteManager())
                .setPenWidth(StrokeWidth.ULTRA_BOLD.getWidth())
                .execute(null);
    }

    public void onRedo(View view) {
        new RedoAction(getNoteManager(), false).execute(null);
    }

    public void onUndo(View view) {
        new UndoAction(getNoteManager(), false).execute(null);
    }

    public void onErase(View view) {
        new ClearAllFreeShapesAction(getNoteManager()).execute(null);
    }

    private NoteManager getNoteManager() {
        return NoteDataBundle.getInstance().getNoteManager();
    }
}