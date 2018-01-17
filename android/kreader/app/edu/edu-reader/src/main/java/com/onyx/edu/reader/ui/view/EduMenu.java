package com.onyx.edu.reader.ui.view;

import android.content.Context;
import android.view.View;

import com.onyx.android.sdk.data.ReaderMenu;
import com.onyx.android.sdk.data.ReaderMenuAction;
import com.onyx.android.sdk.data.ReaderMenuItem;
import com.onyx.android.sdk.data.ReaderMenuState;
import com.onyx.android.sdk.ui.data.ReaderLayerMenuItem;
import com.onyx.android.sdk.ui.data.ReaderLayerMenuViewFactory;
import com.onyx.android.sdk.utils.RunnableWithArgument;
import com.onyx.edu.reader.R;
import com.onyx.edu.reader.ui.dialog.DialogReaderEduMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ming on 2016/11/21.
 */

public class EduMenu extends ReaderMenu {

    private Context context;
    private DialogReaderEduMenu dialog;
    private ReaderMenuState state;
    private List<ReaderLayerMenuItem> menuItems = new ArrayList<>();
    private View mainMenuContainerView;

    private ReaderMenuCallback readerMenuCallback = new ReaderMenuCallback() {
        @Override
        public void onMenuItemClicked(ReaderMenuItem menuItem) {
            notifyMenuItemClicked(menuItem);
        }

        @Override
        public void onMenuItemValueChanged(ReaderMenuItem menuItem, Object oldValue, Object newValue) {
            notifyMenuItemValueChanged(menuItem, oldValue, newValue);
        }

        @Override
        public void onHideMenu() {
            handleHideMenu();
        }
    };

    public EduMenu(Context context) {
        this.context = context;
    }

    private void handleHideMenu() {
        hide();
    }

    @Override
    public boolean isShown() {
        return getDialog().isShowing();
    }

    @Override
    public void show(ReaderMenuState state) {
        this.state = state;
        updateMenuItemsWithState(state);
        updateMenuContent();
        getDialog().show(state);
    }

    @Override
    public void hide() {
        getDialog().dismiss();
    }

    @Override
    public void updateReaderMenuState(ReaderMenuState state) {

    }

    @Override
    public void fillItems(List<? extends ReaderMenuItem> items) {
        menuItems = (List<ReaderLayerMenuItem>)items;
    }

    private View createMainMenuContainerView(List<ReaderLayerMenuItem> items, ReaderMenuState state) {
        return ReaderLayerMenuViewFactory.createMainMenuContainerView(context, items, R.layout.edu_reader_layer_menu_button_item, state, readerMenuCallback, true);
    }

    private DialogReaderEduMenu getDialog() {
        if (dialog == null) {
            dialog = new DialogReaderEduMenu(context, readerMenuCallback);
        }
        return dialog;
    }

    private void updateMenuItemsWithState(final ReaderMenuState state) {
        traverseMenuItems(menuItems, new RunnableWithArgument<ReaderLayerMenuItem>() {
            @Override
            public void run(ReaderLayerMenuItem value) {
                updateMediaMenuVisible(value, state);
            }
        });
    }

    private void updateMediaMenuVisible(ReaderLayerMenuItem item, ReaderMenuState state) {
        if (item.getAction() != ReaderMenuAction.MEDIA_PLAY) {
            return;
        }
        item.setVisible(state.hasRichMedias());
    }

    private void traverseMenuItems(List<ReaderLayerMenuItem> items, RunnableWithArgument<ReaderLayerMenuItem> callback) {
        for (ReaderLayerMenuItem item : items) {
            callback.run(item);
            traverseMenuItems(item.getChildren(), callback);
        }
    }

    private void updateMenuContent() {
        mainMenuContainerView = createMainMenuContainerView(menuItems, state);
        getDialog().updateMenuView(mainMenuContainerView);
    }
}
