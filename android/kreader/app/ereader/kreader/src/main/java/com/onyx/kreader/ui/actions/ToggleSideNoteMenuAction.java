package com.onyx.kreader.ui.actions;

import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.data.ReaderMenuAction;
import com.onyx.android.sdk.ui.data.MenuItem;
import com.onyx.android.sdk.ui.data.MenuManager;
import com.onyx.kreader.BR;
import com.onyx.kreader.R;
import com.onyx.kreader.ui.data.ReaderDataHolder;

import java.util.ArrayList;
import java.util.List;


public class ToggleSideNoteMenuAction extends BaseAction {
    public static final String TAG = ToggleSideNoteMenuAction.class.getSimpleName();
    private boolean supportScale = false;
    private ViewGroup parent;
    private static List<Integer> menuIdList = new ArrayList<>();
    private MenuManager menuManager;

    public ToggleSideNoteMenuAction(MenuManager menuManager, ViewGroup parent, boolean supportScale) {
        this.parent = parent;
        this.menuManager = menuManager;
        this.supportScale = supportScale;
    }

    @Override
    public void execute(ReaderDataHolder readerDataHolder, final BaseCallback callback) {
        if (menuManager.isMainMenuShown()) {
            menuManager.removeMainMenu(parent);
        } else {
            initMenu(readerDataHolder);
        }
        BaseCallback.invoke(callback, null, null);
    }

    private List<Integer> getIDList() {
        if (menuIdList == null || menuIdList.size() == 0) {
            menuIdList = new ArrayList<>();
            if (supportScale) {
                menuIdList.add(ReaderMenuAction.ZOOM_IN.ordinal());
                menuIdList.add(ReaderMenuAction.ZOOM_OUT.ordinal());
                menuIdList.add(ReaderMenuAction.ZOOM_BY_CROP_PAGE.ordinal());
            }
            menuIdList.add(ReaderMenuAction.GOTO_PAGE.ordinal());
        }
        return menuIdList;
    }

    private void initMenu(ReaderDataHolder readerDataHolder) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (readerDataHolder.getDisplayWidth()) - 1, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(readerDataHolder.getSideNoteArea() == ReaderDataHolder.SideNoteArea.LEFT
                ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
        menuManager.addMainMenu(parent,
                readerDataHolder.getEventBus(),
                R.layout.side_note_menu,
                BR.item,
                params,
                MenuItem.createVisibleMenus(getIDList()));
        menuManager.getMainMenu().setColumns(4);
    }

}
