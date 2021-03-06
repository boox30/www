package com.onyx.android.sdk.ui;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.onyx.android.sdk.data.ReaderMenuAction;
import com.onyx.android.sdk.data.ReaderMenuItem;
import com.onyx.android.sdk.ui.data.ReaderLayerMenuItem;
import com.onyx.android.sdk.ui.data.ReaderLayerMenuRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joy on 10/18/16.
 */
public class ReaderLayerMenuRepositoryTest extends ApplicationTestCase<Application> {
    public ReaderLayerMenuRepositoryTest() {
        super(Application.class);
    }

    private int getGroupCount(ReaderMenuItem[] items) {
        int count = 0;
        for (ReaderMenuItem item : items) {
            if (item.getItemType() == ReaderMenuItem.ItemType.Group) {
                count++;
            }
        }
        return count;
    }

    private boolean contains(List<? extends ReaderMenuItem> menuItemList, ReaderMenuAction action) {
        for (ReaderMenuItem item : menuItemList) {
            if (item.getAction() == action) {
                return true;
            }
            if (item.getItemType() == ReaderMenuItem.ItemType.Group) {
                if (contains(item.getChildren(), action)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void testCreateMenu() {
        List<ReaderLayerMenuItem> menuList = ReaderLayerMenuRepository.createFromArray(ReaderLayerMenuRepository.fixedPageMenuItems);
        assertEquals(menuList.size(), getGroupCount(ReaderLayerMenuRepository.fixedPageMenuItems));
    }

    public void testCreateMenuWithExcludingList() {
        List<ReaderMenuAction> menuActions = new ArrayList<>();
        menuActions.add(ReaderMenuAction.ZOOM);
        List<ReaderLayerMenuItem> menuList = ReaderLayerMenuRepository.createFromArray(ReaderLayerMenuRepository.fixedPageMenuItems,
                menuActions);
        assertEquals(menuList.size(), getGroupCount(ReaderLayerMenuRepository.fixedPageMenuItems) - 1);
        assertFalse(contains(menuList, ReaderMenuAction.ZOOM));

        menuActions.clear();
        menuActions.add(ReaderMenuAction.NOTE_EXPORT);
        menuList = ReaderLayerMenuRepository.createFromArray(ReaderLayerMenuRepository.fixedPageMenuItems,
                menuActions);
        assertEquals(menuList.size(), getGroupCount(ReaderLayerMenuRepository.fixedPageMenuItems));
        assertFalse(contains(menuList, ReaderMenuAction.NOTE_EXPORT));
        assertTrue(contains(menuList, ReaderMenuAction.DIRECTORY_SCRIBBLE));
    }
}
