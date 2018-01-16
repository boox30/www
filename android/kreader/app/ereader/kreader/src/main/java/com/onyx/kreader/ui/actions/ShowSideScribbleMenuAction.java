package com.onyx.kreader.ui.actions;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.ReaderMenuAction;
import com.onyx.android.sdk.scribble.data.NoteDrawingArgs;
import com.onyx.android.sdk.ui.data.Menu;
import com.onyx.android.sdk.ui.data.MenuClickEvent;
import com.onyx.android.sdk.ui.data.MenuItem;
import com.onyx.android.sdk.ui.data.MenuManager;
import com.onyx.android.sdk.ui.dialog.DialogCustomLineWidth;
import com.onyx.android.sdk.utils.DeviceInfoUtil;
import com.onyx.android.sdk.utils.TreeObserverUtils;
import com.onyx.kreader.BR;
import com.onyx.kreader.R;
import com.onyx.kreader.note.actions.FlushNoteAction;
import com.onyx.kreader.note.request.PauseDrawingRequest;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.data.SingletonSharedPreference;
import com.onyx.kreader.ui.events.CloseScribbleMenuEvent;
import com.onyx.kreader.ui.events.RequestFinishEvent;
import com.onyx.kreader.ui.events.ScribbleMenuChangedEvent;
import com.onyx.kreader.ui.handler.HandlerManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lxm on 2017/9/20.
 */

public class ShowSideScribbleMenuAction extends BaseAction {

    private ViewGroup parent;
    private ReaderMenuAction parentAction;
    private MenuManager sideMenu;
    private ReaderDataHolder readerDataHolder;
    private View dividerLine, readerStatusBar;
    private ShowScribbleMenuAction.ActionCallback actionCallback;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;

    public ShowSideScribbleMenuAction(ViewGroup parent,View readerStatusBar ,ShowScribbleMenuAction.ActionCallback actionCallback) {
        this.parent = parent;
        this.actionCallback = actionCallback;
        this.readerStatusBar = readerStatusBar;
    }

    @Override
    public void execute(ReaderDataHolder readerDataHolder, BaseCallback baseCallback) {
        this.readerDataHolder = readerDataHolder;
        readerDataHolder.getEventBus().register(this);
        show(readerDataHolder);
        readerDataHolder.getHandlerManager().setActiveProvider(HandlerManager.SIDE_NOTE_PROVIDER);
    }

    private void show(final ReaderDataHolder readerDataHolder) {
        parent.removeAllViews();
        sideMenu = new MenuManager();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        sideMenu.addMainMenu(parent,
                readerDataHolder.getEventBus(),
                R.layout.reader_side_scribble_menu,
                BR.item,
                lp,
                MenuItem.createVisibleMenus(getMainMenuActions()));
        updateSideNotePositionText(readerDataHolder);
        addDividerLine(parent);
        onMenuViewSizeChange(parent);
        updateMenuHeight(true);
    }

    private void updateMenuHeight(boolean isShow){
        if (SingletonSharedPreference.isReaderStatusBarEnabled(parent.getContext())){
            ViewGroup.LayoutParams params = parent.getLayoutParams();
            params.height = isShow ? DeviceInfoUtil.getScreenResolution(parent.getContext()).y - readerStatusBar.getHeight() -
                    (int) (4 * parent.getContext().getResources().getDisplayMetrics().density)
                    : ViewGroup.LayoutParams.MATCH_PARENT;
            parent.setLayoutParams(params);
        }
    }

    private void onMenuViewSizeChange(ViewGroup parent) {
        layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                List<RectF> excludeRectFs = new ArrayList<>();
                collectExcludeRectFs(excludeRectFs, sideMenu.getMainMenu());
                collectExcludeRectFs(excludeRectFs, sideMenu.getSubMenu());
                readerDataHolder.getEventBus().post(ScribbleMenuChangedEvent.create(excludeRectFs));
            }
        };
        parent.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }

    private void addDividerLine(ViewGroup parent) {
        dividerLine = new View(parent.getContext());
        dividerLine.setBackgroundColor(Color.GRAY);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int)
                parent.getContext().getResources().getDimension(R.dimen.divider_line_size),
                ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        parent.addView(dividerLine, lp);
    }

    private List<Integer> getExpandMenuActions() {
        List<Integer> menuActions = new ArrayList<>();
        menuActions.add(ReaderMenuAction.SCRIBBLE_WIDTH.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SHAPE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_ERASER.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_UNDO.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SAVE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_REDO.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SIDE_NOTE_ADD_PAGE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SIDE_NOTE_DELETE_PAGE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_MINIMIZE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_CLOSE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SIDE_NOTE_PREV_PAGE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SIDE_NOTE_POSITION.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SIDE_NOTE_NEXT_PAGE.ordinal());
        return menuActions;
    }

    private void updateMainMenuBg() {
        boolean subMenuShowed = sideMenu.getSubMenu() != null && sideMenu.getSubMenu().isShowing();
        sideMenu.getMainMenu().getRootView().setBackgroundResource(subMenuShowed ? R.drawable.sub_menu_show_shadow_bg : R.drawable.shadow_bg);
    }

    private List<Integer> getMainMenuActions() {
        List<Integer> menuActions = new ArrayList<>();
        menuActions.add(ReaderMenuAction.SCRIBBLE_MAXIMIZE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_CLOSE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SIDE_NOTE_PREV_PAGE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SIDE_NOTE_POSITION.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SIDE_NOTE_NEXT_PAGE.ordinal());
        return menuActions;
    }

    private List<Integer> getPenWidthMenuActions() {
        List<Integer> menuActions = new ArrayList<>();
        menuActions.add(ReaderMenuAction.SCRIBBLE_WIDTH1.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_WIDTH2.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_WIDTH3.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_WIDTH4.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_WIDTH5.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_CUSTOM_WIDTH.ordinal());
        return menuActions;
    }

    private List<Integer> getPenShapeMenuActions() {
        List<Integer> menuActions = new ArrayList<>();
        menuActions.add(ReaderMenuAction.SCRIBBLE_PENCIL.ordinal());
//        menuActions.add(ReaderMenuAction.SCRIBBLE_BRUSH.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_LINE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_TRIANGLE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_CIRCLE.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_SQUARE.ordinal());
        return menuActions;
    }

    private List<Integer> getEraserMenuActions() {
        List<Integer> menuActions = new ArrayList<>();
        menuActions.add(ReaderMenuAction.SCRIBBLE_ERASER_PART.ordinal());
        menuActions.add(ReaderMenuAction.SCRIBBLE_ERASER_ALL.ordinal());
        return menuActions;
    }

    private void expandMainMenu(final ReaderDataHolder readerDataHolder) {
        sideMenu.getMainMenu().updateItemMap(BR.item,
                readerDataHolder.getEventBus(),
                MenuItem.createVisibleMenus(getExpandMenuActions()));
        updateSideNotePositionText(readerDataHolder);
    }

    private void foldMainMenu(final ReaderDataHolder readerDataHolder) {
        closeSubMenu(parent);
        sideMenu.getMainMenu().updateItemMap(BR.item,
                readerDataHolder.getEventBus(),
                MenuItem.createVisibleMenus(getMainMenuActions()));
        updateSideNotePositionText(readerDataHolder);
    }

    private void updateSideNotePositionText(final ReaderDataHolder readerDataHolder) {
        if (sideMenu.getMainMenu() == null) {
            return;
        }
        String text = (readerDataHolder.getSideNotePage() + 1) + "/" + readerDataHolder.getSideNotePageCount();
        sideMenu.getMainMenu().setText(ReaderMenuAction.SCRIBBLE_SIDE_NOTE_POSITION.ordinal(), text);
    }

    @Subscribe
    public void onMenuClickEvent(MenuClickEvent event) {
        int menuId = event.getMenuId();
        ReaderMenuAction action = ReaderMenuAction.valueOf(menuId);
        actionCallback.onClicked(action);
        if (event.isSubMenu()) {
            closeSubMenu(parent);
        }
        switch (action) {
            case SCRIBBLE_MINIMIZE:
                foldMainMenu(readerDataHolder);
                break;
            case SCRIBBLE_MAXIMIZE:
                expandMainMenu(readerDataHolder);
                break;
            case SCRIBBLE_CUSTOM_WIDTH:
                showCustomLineWidthDialog();
                break;
            case SCRIBBLE_WIDTH:
                showSubMenu(ReaderMenuAction.SCRIBBLE_WIDTH,
                        getPenWidthMenuActions(),
                        R.layout.side_scribble_pen_width_menu,
                        ShowReaderMenuAction.getCurrentPenWidth(readerDataHolder));
                break;
            case SCRIBBLE_SHAPE:
                showSubMenu(ReaderMenuAction.SCRIBBLE_SHAPE,
                        getPenShapeMenuActions(),
                        R.layout.side_scribble_pen_style_menu,
                        ShowReaderMenuAction.getCurrentPenShape(readerDataHolder));
                break;
            case SCRIBBLE_ERASER:
                showSubMenu(ReaderMenuAction.SCRIBBLE_ERASER,
                        getEraserMenuActions(),
                        R.layout.side_scribble_eraser_menu,
                        null);
                break;
            case SCRIBBLE_CLOSE:
                close(null);
                break;
        }
    }

    @Subscribe
    public void onRequestFinished(final RequestFinishEvent event) {
        updateSideNotePositionText(readerDataHolder);
    }

    private void showSubMenu(final ReaderMenuAction parentAction, List<Integer> menuIds, int subLayoutId, ReaderMenuAction checkedMenuAction) {
        if (sideMenu.getSubMenu() != null && sideMenu.getSubMenu().getParentMenuId() == parentAction.ordinal() && sideMenu.getSubMenu().isShowing()) {
            closeSubMenu(parent);
            return;
        }
        int mainMenuViewId = sideMenu.getMainMenu().getRootViewId();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.LEFT_OF, mainMenuViewId);
        lp.addRule(RelativeLayout.ALIGN_TOP, mainMenuViewId);
        sideMenu.addSubMenu(parent,
                readerDataHolder.getEventBus(),
                subLayoutId,
                BR.item,
                lp,
                MenuItem.createSubVisibleMenus(menuIds, parentAction.ordinal()));
        if (checkedMenuAction != null) {
            sideMenu.getSubMenu().unCheckAll().check(checkedMenuAction.ordinal());
        }
        sideMenu.getSubMenu().setParentMenuId(parentAction.ordinal());
        updateMainMenuBg();

        this.parentAction = parentAction;
        actionCallback.onToggle(parentAction, true);

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSubMenu(parent);
            }
        });
    }


    private void removeMenu() {
        sideMenu.removeSubMenu(parent);
        sideMenu.removeMainMenu(parent);
    }

    @Subscribe
    public void close(CloseScribbleMenuEvent event) {
        TreeObserverUtils.removeGlobalOnLayoutListener(parent.getViewTreeObserver(), layoutListener);
        removeMenu();
        parent.removeView(dividerLine);
        readerDataHolder.getEventBus().unregister(this);
        readerDataHolder.resetHandlerManager();
        updateMenuHeight(false);
    }

    private void closeSubMenu(final ViewGroup parent) {
        if (sideMenu.getMainMenu() != null) {
            sideMenu.removeSubMenu(parent);
            updateMainMenuBg();
        }

        actionCallback.onToggle(parentAction, false);
        parent.setOnClickListener(null);
        parent.setClickable(false);
    }

    private void showCustomLineWidthDialog() {
        final PauseDrawingRequest pauseDrawingRequest = new PauseDrawingRequest(readerDataHolder.getVisiblePages());
        readerDataHolder.getNoteManager().submit(readerDataHolder.getContext(), pauseDrawingRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                DialogCustomLineWidth customLineWidth = new DialogCustomLineWidth(readerDataHolder.getContext(),
                        (int) readerDataHolder.getNoteManager().getNoteDataInfo().getStrokeWidth(),
                        NoteDrawingArgs.MAX_STROKE_WIDTH,
                        Color.BLACK, new DialogCustomLineWidth.Callback() {
                    @Override
                    public void done(int lineWidth) {
                        ShowReaderMenuAction.useStrokeWidth(readerDataHolder, lineWidth);
                    }
                });
                customLineWidth.show();
                customLineWidth.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        final List<PageInfo> pages = readerDataHolder.getVisiblePages();
                        new FlushNoteAction(pages, true, true, false, false).execute(readerDataHolder, null);
                    }
                });
            }
        });
    }

    private void collectExcludeRectFs(List<RectF> excludeRectFs, Menu menu) {
        if (menu == null) {
            return;
        }
        View menuView = menu.getRootView();
        if (menu.isShowing()) {
            excludeRectFs.add(new RectF(menuView.getLeft(), menuView.getTop(), menuView.getRight(), menuView.getBottom()));
        }
    }

}
