package com.onyx.kreader.ui.handler;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.kreader.R;
import com.onyx.kreader.api.ReaderSelection;
import com.onyx.kreader.common.BaseReaderRequest;
import com.onyx.kreader.host.request.AnalyzeWordAction;
import com.onyx.kreader.host.request.SelectWordRequest;
import com.onyx.kreader.ui.actions.SelectWordAction;
import com.onyx.kreader.ui.actions.ShowTextSelectionMenuAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.highlight.HighlightCursor;
import com.onyx.kreader.utils.MathUtils;
import com.onyx.kreader.utils.DeviceConfig;
import com.onyx.kreader.utils.RectUtils;

/**
 * Created with IntelliJ IDEA.
 * User: zhuzeng
 * Date: 3/23/14
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class WordSelectionHandler extends BaseHandler{

    private static final String TAG = "WordSelectionHandler";

    private int selectionMoveDistanceThreshold;
    private int moveRangeAfterLongPress = 10;

    private float movePointOffsetHeight;
    private Point lastMovedPoint = null;
    private int cursorSelected = -1;
    private boolean showSelectionCursor = true;
    private boolean movedAfterLongPress = false;
    private Point longPressPoint = new Point();
    private SelectWordRequest selectWordRequest;
    private PointF highLightBeginTop;
    private PointF highLightEndBottom;

    public WordSelectionHandler(HandlerManager parent, Context context) {
        super(parent);
        selectionMoveDistanceThreshold = DeviceConfig.sharedInstance(context).getSelectionMoveDistanceThreshold();
        movePointOffsetHeight = context.getResources().getDimension(R.dimen.move_point_offset_height);
    }

    public void onLongPress(ReaderDataHolder readerDataHolder, final float x1, final float y1, final float x2, final float y2) {
        longPressPoint.set((int) x2, (int) y2);
        lastMovedPoint = new Point((int) x2, (int) y2);
        cursorSelected = getCursorSelected(readerDataHolder, (int) x2, (int) y2);
        if (!hasSelectionWord(readerDataHolder)) {
            highLightBeginTop = new PointF(x2, y2);
            highLightEndBottom = new PointF(x2, y2);
            movedAfterLongPress = false;
            showSelectionCursor = false;
            super.onLongPress(readerDataHolder, x1, y1, x2, y2);
            selectWord(readerDataHolder, x1, y1, x2, y2);
        } else if (cursorSelected < 0) {
            quitWordSelection(readerDataHolder);
        }
        readerDataHolder.changeEpdUpdateMode(UpdateMode.DU);
    }

    @Override
    public boolean onDown(ReaderDataHolder readerDataHolder, MotionEvent e) {
        cursorSelected = getCursorSelected(readerDataHolder,(int)e.getX(), (int)e.getY());
        return super.onDown(readerDataHolder, e);
    }

    public boolean onScroll(ReaderDataHolder readerDataHolder, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    public boolean onSingleTapConfirmed(ReaderDataHolder readerDataHolder, MotionEvent e) {
        quitWordSelection(readerDataHolder);
        return true;
    }

    public boolean onSingleTapUp(ReaderDataHolder readerDataHolder, MotionEvent e) {
        setSingleTapUp(true);
        return true;
    }

    public boolean onActionUp(final ReaderDataHolder readerDataHolder, final float startX, final float startY, final float endX, final float endY) {
        if (readerDataHolder.getReaderUserDataInfo().hasHighlightResult() && !isSingleTapUp()) {
            String text = readerDataHolder.getReaderUserDataInfo().getHighlightResult().getText();
            if (!StringUtils.isNullOrEmpty(text)) {
                analyzeWord(readerDataHolder, text);
            }
        }

        updateHighLightRect(readerDataHolder);
        setSingleTapUp(false);
        setActionUp(true);
        return true;
    }

    private void updateHighLightRect(final ReaderDataHolder readerDataHolder) {
        if (readerDataHolder.getReaderUserDataInfo().hasHighlightResult()) {
            final ReaderSelection selection = readerDataHolder.getReaderUserDataInfo().getHighlightResult();
            if (cursorSelected == HighlightCursor.BEGIN_CURSOR_INDEX) {
                highLightBeginTop = RectUtils.getBeginTop(selection.getRectangles());
            } else {
                highLightEndBottom = RectUtils.getEndBottom(selection.getRectangles());
            }
        }
    }

    private void analyzeWord(final ReaderDataHolder readerDataHolder, String text) {
        final AnalyzeWordAction analyzeWordAction = new AnalyzeWordAction(text);
        analyzeWordAction.execute(readerDataHolder, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                boolean isWord = analyzeWordAction.isWord();
                showSelectionMenu(readerDataHolder, isWord);
            }
        });
    }

    private void showSelectionMenu(ReaderDataHolder readerDataHolder, boolean isWord) {
        ShowTextSelectionMenuAction.showTextSelectionPopupMenu(readerDataHolder, isWord);
        enableSelectionCursor(readerDataHolder, selectWordRequest);
        selectWordRequest = null;
    }

    public boolean onScrollAfterLongPress(ReaderDataHolder readerDataHolder, final float x1, final float y1, final float x2, final float y2) {
        return true;
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
                return false;
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
            case KEYCDOE_ERASE_KK:
            case KEYCDOE_SCRIBE_KK:
            case KEYCDOE_SCRIBE:
            case KEYCDOE_ERASE:
                return false;
            case KeyEvent.KEYCODE_BACK:
                quitWordSelection(readerDataHolder);
                return true;
            case KeyEvent.KEYCODE_PAGE_DOWN:
            case KeyEvent.KEYCODE_PAGE_UP:
                quitWordSelection(readerDataHolder);
                return super.onKeyDown(readerDataHolder,keyCode,event);
            default:
                return super.onKeyDown(readerDataHolder,keyCode,event);
        }
    }

    private boolean moveOutOfRange(int x, int y) {
        return MathUtils.distance(lastMovedPoint.x, lastMovedPoint.y, x, y) > moveRangeAfterLongPress;
    }

    @Override
    public boolean onTouchEvent(ReaderDataHolder readerDataHolder, MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (cursorSelected < 0 && showSelectionCursor){
                    return true;
                }

                if (filterMoveAfterLongPress(x, y)) {
                    return true;
                }

                highlightAlongTouchMoved(readerDataHolder,x, y, cursorSelected);
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return true;
    }

    private boolean filterMoveAfterLongPress(float x, float y) {
        if (!movedAfterLongPress) {
            if (!moveOutOfRange((int) x, (int) y)) {
                return true;
            }
        }
        movedAfterLongPress = true;
        return false;
    }

    @Override
    public void close(ReaderDataHolder readerDataHolder) {
        quitWordSelection(readerDataHolder);
    }

    public void highlightAlongTouchMoved(ReaderDataHolder readerDataHolder, float x, float y, int cursorSelected) {
        hideTextSelectionPopupWindow(readerDataHolder);
        selectText(readerDataHolder, longPressPoint.x, longPressPoint.y, x, y);
    }

    private PageInfo hitTestPage(ReaderDataHolder readerDataHolder, float x, float y) {
        if (readerDataHolder.getReaderViewInfo().getVisiblePages() == null) {
            return null;
        }
        for (PageInfo pageInfo : readerDataHolder.getReaderViewInfo().getVisiblePages()) {
            if (pageInfo.getDisplayRect().contains(x, y)) {
                return pageInfo;
            }
        }
        return null;
    }

    public void selectWord(final ReaderDataHolder readerDataHolder, final float x1, final float y1, final float x2, final float y2) {
        SelectWordAction.selectWord(readerDataHolder, readerDataHolder.getCurrentPageName(), new PointF(x1, y1), new PointF(x2, y2), new PointF(x2, y2),new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                onSelectWordFinished(readerDataHolder, (SelectWordRequest) request, e);
            }
        });
    }

    public void selectText(final ReaderDataHolder readerDataHolder, final float x1, final float y1, final float x2, final float y2) {
        PointF touchPoint = new PointF(x2, y2);
        final ReaderSelection selection = readerDataHolder.getReaderUserDataInfo().getHighlightResult();
        if (cursorSelected == HighlightCursor.BEGIN_CURSOR_INDEX) {
            highLightBeginTop = new PointF(x2, y2 - getMovePointOffsetHeight(readerDataHolder));
        } else {
            highLightEndBottom = new PointF(x2, y2 - getMovePointOffsetHeight(readerDataHolder));
        }

        SelectWordAction.selectText(readerDataHolder, readerDataHolder.getCurrentPageName(), highLightBeginTop, highLightEndBottom, touchPoint, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                onSelectWordFinished(readerDataHolder, (SelectWordRequest)request, e);
                updateCursorSelected(readerDataHolder, selection);
            }
        });
    }

    private float getMovePointOffsetHeight(ReaderDataHolder readerDataHolder){
        if (!readerDataHolder.getSelectionManager().isEnable()){
            return 0f;
        }
        return movePointOffsetHeight;
    }

    private void updateCursorSelected(ReaderDataHolder readerDataHolder, ReaderSelection selection){
        final ReaderSelection updatedSelection = readerDataHolder.getReaderUserDataInfo().getHighlightResult();
        if (updatedSelection == null || selection == null) {
            return;
        }

        if (cursorSelected == HighlightCursor.END_CURSOR_INDEX) {
            if (selection.getStartPosition().equals(updatedSelection.getEndPosition())) {
                highLightEndBottom.set(highLightBeginTop.x, highLightBeginTop.y);
                cursorSelected = HighlightCursor.BEGIN_CURSOR_INDEX;
            }
        }else if (cursorSelected == HighlightCursor.BEGIN_CURSOR_INDEX) {
            if (selection.getEndPosition().equals(updatedSelection.getStartPosition())) {
                highLightBeginTop.set(highLightEndBottom.x, highLightEndBottom.y);
                cursorSelected = HighlightCursor.END_CURSOR_INDEX;
            }
        }
    }

    public void hideTextSelectionPopupWindow(ReaderDataHolder readerDataHolder) {
        ShowTextSelectionMenuAction.hideTextSelectionPopupWindow(readerDataHolder, false);
    }

    public boolean hasSelectionWord(ReaderDataHolder readerDataHolder) {
        return readerDataHolder.getReaderUserDataInfo().hasHighlightResult();
    }

    public int getCursorSelected(ReaderDataHolder readerDataHolder, int x, int y) {
        HighlightCursor beginHighlightCursor = readerDataHolder.getSelectionManager().getHighlightCursor(HighlightCursor.BEGIN_CURSOR_INDEX);
        HighlightCursor endHighlightCursor = readerDataHolder.getSelectionManager().getHighlightCursor(HighlightCursor.END_CURSOR_INDEX);

        if (endHighlightCursor != null && endHighlightCursor.hitTest(x, y)) {
            return HighlightCursor.END_CURSOR_INDEX;
        }
        if (beginHighlightCursor != null && beginHighlightCursor.hitTest(x, y)) {
            return HighlightCursor.BEGIN_CURSOR_INDEX;
        }
        return -1;
    }

    public void quitWordSelection(ReaderDataHolder readerDataHolder) {
        readerDataHolder.resetEpdUpdateMode();
        ShowTextSelectionMenuAction.hideTextSelectionPopupWindow(readerDataHolder,true);
        getParent().resetToDefaultProvider();
        readerDataHolder.redrawPage();
        readerDataHolder.getSelectionManager().clear();
        readerDataHolder.getHandlerManager().setActiveProvider(HandlerManager.READING_PROVIDER);
    }

    public void onSelectWordFinished(ReaderDataHolder readerDataHolder, SelectWordRequest request, Throwable e) {
        if (e != null) {
            return;
        }

        if (request.getReaderUserDataInfo().hasHighlightResult() && !isActionUp()) {
            ReaderSelection selection = request.getReaderUserDataInfo().getHighlightResult();
            readerDataHolder.getSelectionManager().setCurrentSelection(selection);
            readerDataHolder.getSelectionManager().update(readerDataHolder.getContext());
            readerDataHolder.getSelectionManager().updateDisplayPosition();
            readerDataHolder.getSelectionManager().setEnable(showSelectionCursor);
            selectWordRequest = request;
            readerDataHolder.onRenderRequestFinished(request, e, false, false);
        }
    }

    private void enableSelectionCursor(ReaderDataHolder readerDataHolder, BaseReaderRequest request){
        if (request == null){
            return;
        }
        readerDataHolder.getSelectionManager().setEnable(true);
        readerDataHolder.onRenderRequestFinished(request, null, false, false);
        showSelectionCursor = true;
    }
}
