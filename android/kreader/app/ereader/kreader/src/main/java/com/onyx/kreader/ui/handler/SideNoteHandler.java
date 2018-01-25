package com.onyx.kreader.ui.handler;

import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.ReaderMenuAction;
import com.onyx.android.sdk.reader.host.request.ScaleToPageCropRequest;
import com.onyx.android.sdk.ui.data.MenuClickEvent;
import com.onyx.android.sdk.ui.data.MenuManager;
import com.onyx.kreader.note.actions.FlushNoteAction;
import com.onyx.kreader.note.actions.ResumeDrawingAction;
import com.onyx.kreader.note.actions.StopNoteAction;
import com.onyx.kreader.note.actions.StopNoteActionChain;
import com.onyx.kreader.note.request.StartNoteRequest;
import com.onyx.kreader.ui.ReaderActivity;
import com.onyx.kreader.ui.actions.ChangeScaleWithDeltaAction;
import com.onyx.kreader.ui.actions.ShowDialogGoToPageAction;
import com.onyx.kreader.ui.actions.ToggleSideNoteMenuAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.events.ChangeOrientationEvent;
import com.onyx.kreader.ui.events.HideTabWidgetEvent;
import com.onyx.kreader.ui.events.ShowTabWidgetEvent;

import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Joy on 2014/3/26.
 */
public class SideNoteHandler extends BaseHandler {
    private static final String TAG = SideNoteHandler.class.getSimpleName();
    private MenuManager menuManager;
    private BaseCallback resumeDrawingCallBack;

    public SideNoteHandler(HandlerManager p) {
        super(p);
        menuManager = new MenuManager();
        resumeDrawingCallBack = new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                new ResumeDrawingAction(getParent().getReaderDataHolder().getVisiblePages()).execute(getParent().getReaderDataHolder(), null);
            }
        };
    }

    private boolean isEnableBigPen() {
        return true;
    }

    public void onActivate(final ReaderDataHolder readerDataHolder, final HandlerInitialState initialState) {
        final StartNoteRequest request = new StartNoteRequest(readerDataHolder.getVisiblePages(),
                true, readerDataHolder.getSideNoteStartSubPageIndex());
        readerDataHolder.getNoteManager().submit(readerDataHolder.getContext(), request, null);
        readerDataHolder.getEventBus().post(new HideTabWidgetEvent());

        readerDataHolder.getEventBus().register(this);
    }

    public void onDeactivate(final ReaderDataHolder readerDataHolder) {
        readerDataHolder.getEventBus().unregister(this);

        StopNoteActionChain stopNoteActionChain = new StopNoteActionChain(true, true, true, false, false, true);
        stopNoteActionChain.execute(readerDataHolder, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                readerDataHolder.getEventBus().post(new ShowTabWidgetEvent());
                if (readerDataHolder.getOrientationBeforeSideNote() > 0) {
                    readerDataHolder.getEventBus().post(new ChangeOrientationEvent(readerDataHolder.getOrientationBeforeSideNote()));
                    readerDataHolder.setOrientationBeforeSideNote(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
                if (menuManager.getMainMenu() != null && menuManager.getMainMenu().isShowing()) {
                    toggleSideNoteMenu(readerDataHolder);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(ReaderDataHolder readerDataHolder, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return false;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return false;
            case KeyEvent.KEYCODE_DPAD_UP:
                return false;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return false;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                return false;
            case KeyEvent.KEYCODE_MENU:
                return true;
            case KEYCDOE_ERASE:
            case KEYCDOE_ERASE_KK:
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                if (isEnableBigPen()) {
                    getParent().setActiveProvider(HandlerManager.ERASER_PROVIDER);
                }
                return false;
            case KEYCDOE_SCRIBE:
            case KEYCDOE_SCRIBE_KK:
                return false;
            default:
                return super.onKeyDown(readerDataHolder, keyCode, event);
        }
    }

    @Override
    public boolean onTouchEvent(ReaderDataHolder readerDataHolder, MotionEvent e) {
        if (e.getPointerCount() > 1) {
            return true;
        }
        if (inDocRegion(e)) {
            return true;
        }

        return readerDataHolder.getNoteManager().getTouchHelper().onTouchEvent(e);
    }

    public boolean onScroll(ReaderDataHolder readerDataHolder, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (inDocRegion(e1) && inDocRegion(e2)) {
            return super.onScroll(readerDataHolder, e1, e2, distanceX, distanceY);
        }
        return true;
    }

    @Override
    public boolean onSingleTapUp(ReaderDataHolder readerDataHolder, MotionEvent e) {
        if (inDocRegion(e)) {
            int left = readerDataHolder.getDocPageLeft();
            int width = readerDataHolder.getDisplayWidth() / 2;
            if (left <= e.getX() && e.getX() < left + width / 3) {
                prevScreen(readerDataHolder);
            } else if (e.getX() > left + ((width * 2) / 3) && e.getX() < left + width) {
                nextScreen(readerDataHolder);
            } else {
                toggleSideNoteMenu(readerDataHolder);
            }
        }
        return true;
    }

    public boolean onScaleEnd(ReaderDataHolder readerDataHolder, ScaleGestureDetector detector) {
        return true;
    }

    public boolean onScaleBegin(ReaderDataHolder readerDataHolder, ScaleGestureDetector detector) {
        setPinchZooming(true);
        return true;
    }

    public boolean onScale(ReaderDataHolder readerDataHolder, ScaleGestureDetector detector) {
        return true;
    }

    public boolean onActionUp(ReaderDataHolder readerDataHolder, final float startX, final float startY, final float endX, final float endY) {
        if (inDocRegion(startX, startY) && inDocRegion(endX, endY)) {
            return super.onActionUp(readerDataHolder, startX, startY, endX, endY);
        }
        return true;
    }

    public void onLongPress(ReaderDataHolder readerDataHolder, final float x1, final float y1, final float x2, final float y2) {
    }

    public void beforeProcessKeyDown(final ReaderDataHolder readerDataHolder, final String action, final String args) {
        final FlushNoteAction flushNoteAction = new FlushNoteAction(readerDataHolder.getVisiblePages(), true, true, false, false);
        flushNoteAction.execute(getParent().getReaderDataHolder(), null);
    }

    public void beforeChangePosition(ReaderDataHolder readerDataHolder) {
        new StopNoteAction(false).execute(readerDataHolder, null);
    }

    public void afterChangePosition(final ReaderDataHolder readerDataHolder) {
        new ResumeDrawingAction(readerDataHolder.getVisiblePages()).execute(readerDataHolder, null);
    }

    public void close(final ReaderDataHolder readerDataHolder) {
        StopNoteActionChain stopNoteActionChain = new StopNoteActionChain(true, true, true, false, false, true);
        stopNoteActionChain.execute(readerDataHolder, null);
    }

    private boolean inDocRegion(MotionEvent e) {
        return inDocRegion(e.getX(), e.getY());
    }

    private boolean inDocRegion(float x, float y) {
        return getParent().getReaderDataHolder().isInDocPageRegion((int)x, (int)y);
    }

    private void toggleSideNoteMenu(final ReaderDataHolder readerDataHolder) {
        FlushNoteAction flushNoteAction = FlushNoteAction.pauseAfterFlush(getParent().getReaderDataHolder().getVisiblePages());
        flushNoteAction.execute(getParent().getReaderDataHolder(), new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                new ToggleSideNoteMenuAction(menuManager,
                        ((ReaderActivity) readerDataHolder.getContext()).getExtraView(), readerDataHolder.supportScalable()).
                        execute(readerDataHolder, resumeDrawingCallBack);
            }
        });
    }

    @Subscribe
    public void onMenuClickEvent(MenuClickEvent event) {
        ReaderMenuAction menuAction = ReaderMenuAction.valueOf(event.getMenuId());
        FlushNoteAction flushNoteAction = FlushNoteAction.pauseAfterFlush(getParent().getReaderDataHolder().getVisiblePages());
        BaseCallback menuActionCallback = null;
        switch (menuAction) {
            case ZOOM_IN:
                menuActionCallback = new BaseCallback() {
                    @Override
                    public void done(BaseRequest request, Throwable e) {
                        final ChangeScaleWithDeltaAction scaleUpAction = new ChangeScaleWithDeltaAction(0.1f);
                        scaleUpAction.execute(getParent().getReaderDataHolder(), resumeDrawingCallBack);
                    }
                };
                break;
            case ZOOM_OUT:
                menuActionCallback = new BaseCallback() {
                    @Override
                    public void done(BaseRequest request, Throwable e) {
                        final ChangeScaleWithDeltaAction scaleDownAction = new ChangeScaleWithDeltaAction(-0.1f);
                        scaleDownAction.execute(getParent().getReaderDataHolder(), resumeDrawingCallBack);
                    }
                };
                break;
            case ZOOM_BY_CROP_PAGE:
                menuActionCallback = new BaseCallback() {
                    @Override
                    public void done(BaseRequest request, Throwable e) {
                        final ScaleToPageCropRequest scaleToPageCropRequest = new ScaleToPageCropRequest(getParent().getReaderDataHolder().getCurrentPageName());
                        getParent().getReaderDataHolder().submitRenderRequest(scaleToPageCropRequest, resumeDrawingCallBack);
                    }
                };
                break;
            case GOTO_PAGE:
                menuActionCallback = new BaseCallback() {
                    @Override
                    public void done(BaseRequest request, Throwable e) {
                        showDialogGoToPage();
                    }
                };
        }
        flushNoteAction.execute(getParent().getReaderDataHolder(), menuActionCallback);
    }

    private void showDialogGoToPage() {
        new ShowDialogGoToPageAction().execute(getParent().getReaderDataHolder(), null);
    }
}
