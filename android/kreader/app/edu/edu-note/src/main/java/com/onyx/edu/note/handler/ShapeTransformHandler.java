package com.onyx.edu.note.handler;

import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.scribble.asyncrequest.AsyncBaseNoteRequest;
import com.onyx.android.sdk.scribble.asyncrequest.NoteManager;
import com.onyx.android.sdk.scribble.asyncrequest.event.DrawingTouchEvent;
import com.onyx.android.sdk.scribble.asyncrequest.shape.GetSelectedShapeListRequest;
import com.onyx.android.sdk.scribble.data.ScribbleMode;
import com.onyx.android.sdk.scribble.data.TouchPoint;
import com.onyx.android.sdk.scribble.data.TouchPointList;
import com.onyx.edu.note.actions.scribble.ChangeSelectedShapePositionAction;
import com.onyx.edu.note.actions.scribble.ChangeSelectedShapeScaleAction;
import com.onyx.edu.note.actions.scribble.DocumentSaveAction;
import com.onyx.edu.note.actions.scribble.GetSelectedShapeListAction;
import com.onyx.edu.note.actions.scribble.GotoNextPageAction;
import com.onyx.edu.note.actions.scribble.GotoPrevPageAction;
import com.onyx.edu.note.actions.scribble.RedoAction;
import com.onyx.edu.note.actions.scribble.SelectShapeByPointListAction;
import com.onyx.edu.note.actions.scribble.ShapeSelectionAction;
import com.onyx.edu.note.actions.scribble.UndoAction;
import com.onyx.edu.note.data.ScribbleFunctionBarMenuID;
import com.onyx.edu.note.data.ScribbleToolBarMenuID;
import com.onyx.edu.note.scribble.event.ChangeScribbleModeEvent;
import com.onyx.edu.note.scribble.event.RequestInfoUpdateEvent;
import com.onyx.edu.note.scribble.event.ShowSubMenuEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.onyx.android.sdk.scribble.shape.ShapeFactory.SHAPE_PENCIL_SCRIBBLE;
import static com.onyx.android.sdk.scribble.shape.ShapeFactory.SHAPE_SELECTOR;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_CALENDAR;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_EMPTY;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_ENGLISH;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_GRID;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_GRID_5_5;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_GRID_POINT;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_LEFT_GRID;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_LINE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_LINE_1_6;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_LINE_2_0;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_LINE_COLUMN;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_MATS;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_MUSIC;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Background.BG_TABLE_GRID;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Eraser.ERASE_PARTIALLY;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Eraser.ERASE_TOTALLY;
import static com.onyx.edu.note.data.ScribbleSubMenuID.PenStyle.BRUSH_PEN_STYLE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.PenStyle.CIRCLE_STYLE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.PenStyle.LINE_STYLE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.PenStyle.NORMAL_PEN_STYLE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.PenStyle.RECT_STYLE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.PenStyle.TRIANGLE_45_STYLE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.PenStyle.TRIANGLE_60_STYLE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.PenStyle.TRIANGLE_90_STYLE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.PenStyle.TRIANGLE_STYLE;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Thickness.THICKNESS_BOLD;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Thickness.THICKNESS_CUSTOM_BOLD;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Thickness.THICKNESS_LIGHT;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Thickness.THICKNESS_NORMAL;
import static com.onyx.edu.note.data.ScribbleSubMenuID.Thickness.THICKNESS_ULTRA_LIGHT;

/**
 * Created by solskjaer49 on 2017/8/8 17:43.
 */

public class ShapeTransformHandler extends BaseHandler {

    private BaseCallback actionDoneCallback = new BaseCallback() {
        @Override
        public void done(BaseRequest request, Throwable e) {
            AsyncBaseNoteRequest noteRequest = (AsyncBaseNoteRequest)request;
            noteManager.post(new RequestInfoUpdateEvent(noteRequest.getShapeDataInfo(), request, e));
        }
    };

    private static final String TAG = ShapeTransformHandler.class.getSimpleName();
    private TouchPoint mShapeSelectStartPoint = null;
    private TouchPoint mShapeSelectPoint = null;
    private ControlMode currentControlMode = ControlMode.SelectMode;
    private TransformAction transformAction = TransformAction.Undefined;
    private TouchPointList shapeSelectPoints;
    private RectF selectedRectF;

    private enum ControlMode {SelectMode, OperatingMode}

    private enum TransformAction {Undefined, Zoom, Move}

    public ShapeTransformHandler(NoteManager noteManager) {
        super(noteManager);
    }

    @Override
    public void onActivate(HandlerArgs args) {
        super.onActivate(args);
        noteManager.registerEventBus(this);
        currentControlMode = ControlMode.SelectMode;
        noteManager.getShapeDataInfo().setCurrentShapeType(SHAPE_SELECTOR);
        noteManager.sync(true, false);
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        noteManager.unregisterEventBus(this);
    }

    @Override
    protected void buildFunctionBarMenuFunctionList() {
        functionBarMenuIDList = new ArrayList<>();
        functionBarMenuIDList.add(ScribbleFunctionBarMenuID.PEN_STYLE);
        functionBarMenuIDList.add(ScribbleFunctionBarMenuID.BG);
        functionBarMenuIDList.add(ScribbleFunctionBarMenuID.ERASER);
        functionBarMenuIDList.add(ScribbleFunctionBarMenuID.PEN_WIDTH);
        functionBarMenuIDList.add(ScribbleFunctionBarMenuID.SHAPE_SELECT);
    }

    @Override
    protected void buildToolBarMenuFunctionList() {
        toolBarMenuIDList = new ArrayList<>();
        toolBarMenuIDList.add(ScribbleToolBarMenuID.UNDO);
        toolBarMenuIDList.add(ScribbleToolBarMenuID.REDO);
    }

    @Override
    protected void buildFunctionBarMenuSubMenuIDListSparseArray() {
        functionBarSubMenuIDMap = new SparseArray<>();
        functionBarSubMenuIDMap.put(ScribbleFunctionBarMenuID.PEN_WIDTH, buildSubMenuThicknessIDList());
        functionBarSubMenuIDMap.put(ScribbleFunctionBarMenuID.BG, buildSubMenuBGIDList());
        functionBarSubMenuIDMap.put(ScribbleFunctionBarMenuID.ERASER, buildSubMenuEraserIDList());
        functionBarSubMenuIDMap.put(ScribbleFunctionBarMenuID.PEN_STYLE, buildSubMenuPenStyleIDList());
    }

    @Override
    public void handleFunctionBarMenuFunction(int functionBarMenuID) {
        //TODO:temp restore to normal scribble here.in shape select mode , may have different icon here.
        switch (functionBarMenuID) {
            case ScribbleFunctionBarMenuID.ADD_PAGE:
                addPage();
                break;
            case ScribbleFunctionBarMenuID.DELETE_PAGE:
                deletePage();
                break;
            case ScribbleFunctionBarMenuID.NEXT_PAGE:
                nextPage();
                break;
            case ScribbleFunctionBarMenuID.PREV_PAGE:
                prevPage();
                break;
            case ScribbleFunctionBarMenuID.SHAPE_SELECT:
                onSetShapeSelectModeChanged();
                break;
            default:
                noteManager.post(new ShowSubMenuEvent(functionBarMenuID));
                break;
        }
    }

    private void onSetShapeSelectModeChanged() {
        noteManager.getShapeDataInfo().setCurrentShapeType(SHAPE_PENCIL_SCRIBBLE);
        noteManager.post(new ChangeScribbleModeEvent(ScribbleMode.MODE_NORMAL_SCRIBBLE));
    }

    private void redo() {
        noteManager.syncWithCallback(true, false, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                RedoAction reDoAction = new RedoAction(false);
                reDoAction.execute(noteManager, actionDoneCallback);
            }
        });
    }

    private void undo() {
        noteManager.syncWithCallback(true, false, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                UndoAction unDoAction = new UndoAction(false);
                unDoAction.execute(noteManager, actionDoneCallback);
            }
        });
    }

    @Override
    public void handleSubMenuFunction(int subMenuID) {

    }

    @Override
    public void handleToolBarMenuFunction(String uniqueID, String title, int toolBarMenuID) {
        Log.e(TAG, "handleToolBarMenuFunction: ");
        switch (toolBarMenuID) {
            case ScribbleToolBarMenuID.UNDO:
                undo();
                break;
            case ScribbleToolBarMenuID.REDO:
                redo();
                break;
        }
    }

    @Override
    public void saveDocument(String uniqueID, String title, boolean closeAfterSave, BaseCallback callback) {
        DocumentSaveAction documentSaveAction = new DocumentSaveAction(uniqueID,
                title, closeAfterSave);
        documentSaveAction.execute(noteManager, callback);
    }

    @Override
    public void prevPage() {
        noteManager.syncWithCallback(true, false, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                GotoPrevPageAction prevPageAction = new GotoPrevPageAction();
                prevPageAction.execute(noteManager, actionDoneCallback);
            }
        });
    }

    @Override
    public void nextPage() {
        noteManager.syncWithCallback(true, false, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                GotoNextPageAction nextPageAction = new GotoNextPageAction();
                nextPageAction.execute(noteManager, actionDoneCallback);
            }
        });
    }

    @Override
    public void addPage() {

    }

    @Override
    public void deletePage() {

    }

    /**
     * TODO:test effect only.need design undo/redo scale/drag
     * When switch to shape transform handler.we handle touch event as next procedure.
     * When we receive touch event,detect if we already have select shape or not.
     * if does,we assume next touch event is going for control select shape.
     * if doesn't,we assume we are going to select shape.
     */
    private void onBeginShapeSelecting(final MotionEvent motionEvent) {
        Log.e(TAG, "onBeginShapeSelectEvent: ");
        shapeSelectPoints = new TouchPointList();
        new GetSelectedShapeListAction().execute(noteManager, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                GetSelectedShapeListRequest req = (GetSelectedShapeListRequest) request;
                Log.e(TAG, "req.getSelectedShapeList().size():" + req.getSelectedShapeList().size());
                if (req.getSelectedShapeList().size() > 0) {
                    currentControlMode = ControlMode.OperatingMode;
                    selectedRectF = req.getSelectedRectF();
                } else {
                    currentControlMode = ControlMode.SelectMode;
                }
            }
        });
        noteManager.syncWithCallback(true, false, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                mShapeSelectStartPoint = new TouchPoint();
                mShapeSelectPoint = new TouchPoint();
            }
        });
    }

    private void detectTransformAction(RectF selectRect, MotionEvent event) {
        //TODO:dynamic detectionRange By selectRect width/height?
        final int detectionRange = 10;
        RectF leftTopCornerRect = new RectF(selectRect.left - detectionRange, selectRect.top - detectionRange,
                selectRect.left + detectionRange, selectRect.top + detectionRange);
        RectF rightTopCornerRect = new RectF(selectRect.right - detectionRange, selectRect.top - detectionRange,
                selectRect.right + detectionRange, selectRect.top + detectionRange);
        RectF leftBottomCornerRect = new RectF(selectRect.left - detectionRange, selectRect.bottom - detectionRange,
                selectRect.left + detectionRange, selectRect.bottom + detectionRange);
        RectF rightBottomCornerRect = new RectF(selectRect.right - detectionRange, selectRect.bottom - detectionRange,
                selectRect.right + detectionRange, selectRect.bottom + detectionRange);
        List<RectF> detectRectFList = new ArrayList<>();
        detectRectFList.add(leftTopCornerRect);
        detectRectFList.add(rightTopCornerRect);
        detectRectFList.add(leftBottomCornerRect);
        detectRectFList.add(rightBottomCornerRect);
        for (RectF rectF : detectRectFList) {
            if (rectF.contains(event.getX(), event.getY())) {
                transformAction = TransformAction.Zoom;
                return;
            }
        }
        transformAction = TransformAction.Move;
    }

    private void onShapeSelecting(MotionEvent motionEvent) {
        Log.e(TAG, "onShapeSelectingEvent: ");
        if (mShapeSelectStartPoint == null || mShapeSelectPoint == null) {
            return;
        }
        if (mShapeSelectStartPoint.x == 0 && mShapeSelectStartPoint.y == 0) {
            mShapeSelectStartPoint.x = motionEvent.getX();
            mShapeSelectStartPoint.y = motionEvent.getY();
        }
        mShapeSelectPoint.x = motionEvent.getX();
        mShapeSelectPoint.y = motionEvent.getY();
        switch (currentControlMode) {
            case SelectMode:
                new ShapeSelectionAction(mShapeSelectStartPoint, mShapeSelectPoint).execute(noteManager, null);
                break;
            case OperatingMode:
                if (transformAction == TransformAction.Undefined) {
                    detectTransformAction(selectedRectF, motionEvent);
                }
                switch (transformAction) {
                    case Move:
                        new ChangeSelectedShapePositionAction(mShapeSelectPoint, false).execute(noteManager, null);
                        break;
                    case Zoom:
                        new ChangeSelectedShapeScaleAction(mShapeSelectPoint, false).execute(noteManager, null);
                        break;
                }
                break;
        }

        if (shapeSelectPoints != null) {
            int n = motionEvent.getHistorySize();
            for(int i = 0; i < n; ++i) {
                shapeSelectPoints.add(TouchPoint.fromHistorical(motionEvent, i));
            }
            shapeSelectPoints.add(new TouchPoint(motionEvent.getX(), motionEvent.getY(),
                    motionEvent.getPressure(), motionEvent.getSize(), motionEvent.getEventTime()));
        }
    }

    private void onFinishShapeSelecting() {
        Log.e(TAG, "onShapeSelectTouchPointListReceived: ");
        switch (currentControlMode) {
            case SelectMode:
                new SelectShapeByPointListAction(shapeSelectPoints).execute(noteManager, null);
                break;
            case OperatingMode:
                switch (transformAction) {
                    case Move:
                        new ChangeSelectedShapePositionAction(mShapeSelectPoint, true).execute(noteManager, null);
                        break;
                    case Zoom:
                        new ChangeSelectedShapeScaleAction(mShapeSelectPoint, true).execute(noteManager, null);
                        break;
                }
                selectedRectF = null;
                transformAction = TransformAction.Undefined;
                break;
        }
    }

    private List<Integer> buildSubMenuThicknessIDList() {
        List<Integer> resultList = new ArrayList<>();
        resultList.add(THICKNESS_ULTRA_LIGHT);
        resultList.add(THICKNESS_LIGHT);
        resultList.add(THICKNESS_NORMAL);
        resultList.add(THICKNESS_BOLD);
        resultList.add(THICKNESS_CUSTOM_BOLD);
        return resultList;
    }

    private List<Integer> buildSubMenuBGIDList() {
        List<Integer> resultList = new ArrayList<>();
        resultList.add(BG_EMPTY);
        resultList.add(BG_LINE);
        resultList.add(BG_LEFT_GRID);
        resultList.add(BG_GRID_5_5);
        resultList.add(BG_GRID);
        resultList.add(BG_MATS);
        resultList.add(BG_MUSIC);
        resultList.add(BG_ENGLISH);
        resultList.add(BG_LINE_1_6);
        resultList.add(BG_LINE_2_0);
        resultList.add(BG_LINE_COLUMN);
        resultList.add(BG_TABLE_GRID);
        resultList.add(BG_CALENDAR);
        resultList.add(BG_GRID_POINT);
        return resultList;
    }

    private List<Integer> buildSubMenuEraserIDList() {
        List<Integer> resultList = new ArrayList<>();
        resultList.add(ERASE_PARTIALLY);
        resultList.add(ERASE_TOTALLY);
        return resultList;
    }

    private List<Integer> buildSubMenuPenStyleIDList() {
        List<Integer> resultList = new ArrayList<>();
        resultList.add(NORMAL_PEN_STYLE);
        resultList.add(BRUSH_PEN_STYLE);
        resultList.add(LINE_STYLE);
        resultList.add(TRIANGLE_STYLE);
        resultList.add(CIRCLE_STYLE);
        resultList.add(RECT_STYLE);
        resultList.add(TRIANGLE_45_STYLE);
        resultList.add(TRIANGLE_60_STYLE);
        resultList.add(TRIANGLE_90_STYLE);
        return resultList;
    }

    @Subscribe
    public void onDrawingTouchEvent(DrawingTouchEvent event) {
        forwardShapeSelecting(event.getMotionEvent());
    }

    private void forwardShapeSelecting(final MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            onBeginShapeSelecting(motionEvent);
        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            onShapeSelecting(motionEvent);
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            onFinishShapeSelecting();
        }
    }

}
