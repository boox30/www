package com.onyx.android.sdk.scribble.data;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.onyx.android.sdk.scribble.shape.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuzeng on 4/23/16.
 * Manager for a list of shapes in single page.
 * To make it easy for activity or other class to manage shapes.
 * ShapeManager->NotePage->Shape->ShapeModel
 */
public class NotePage {

    private String documentUniqueId;
    private String pageUniqueId;
    private String subPageName;

    private List<Shape> shapeList = new ArrayList<>();
    private List<Shape> newAddedShapeList = new ArrayList<>();
    private List<Shape> removedShapeList = new ArrayList<>();

    private int currentShapeType;
    private Shape currentShape;
    private boolean loaded = false;
    private UndoRedoManager undoRedoManager = new UndoRedoManager();


    public static abstract class RenderCallback {
        public abstract boolean isRenderAbort();
    }

    public NotePage() {
    }

    public NotePage(final String docId, final String pageId, final String spn) {
        documentUniqueId = docId;
        pageUniqueId = pageId;
        subPageName = spn;
    }

    public final String getDocumentUniqueId() {
        return documentUniqueId;
    }

    public final String getPageUniqueId() {
        return pageUniqueId;
    }

    public final String getSubPageName() {
        return subPageName;
    }

    public void clear(boolean addToHistory) {
        if (shapeList.size() > 0 && addToHistory) {
            removedShapeList.addAll(shapeList);
            undoRedoManager.addToHistory(ShapeActions.removeShapeListAction(shapeList), false);
        }
        shapeList.clear();
        newAddedShapeList.clear();
    }

    public void addShapeFromModel(final Shape shape) {
        shapeList.add(shape);
    }

    public void addShape(final Shape shape, boolean addToHistory) {
        updateShape(shape);
        newAddedShapeList.add(shape);
        shapeList.add(shape);
        if (addToHistory) {
            undoRedoManager.addToHistory(ShapeActions.addShapeAction(shape), false);
        }
        if (removedShapeList.contains(shape)){
            removedShapeList.remove(shape);
        }
    }

    public void addShapeList(final List<Shape> shapes) {
        addShapeList(shapes,true);
    }

    public void addShapeList(final List<Shape> shapes, boolean addToHistory) {
        for (Shape shape : shapes) {
            addShape(shape, addToHistory);
        }
    }

    public void removeShapeList(final List<Shape> shapes, boolean addToHistory) {
        for (Shape shape : shapes) {
            removeShape(shape, addToHistory);
        }
    }

    public boolean canRedo() {
        return undoRedoManager.canRedo();
    }

    public void redo() {
        NotePageUndoRedoManager.redo(this, undoRedoManager);
    }

    public boolean canUndo() {
        return undoRedoManager.canUndo();
    }

    public void undo() {
        NotePageUndoRedoManager.undo(this, undoRedoManager);
    }

    private void updateShape(final Shape shape) {
        shape.setDocumentUniqueId(getDocumentUniqueId());
        shape.setPageUniqueId(getPageUniqueId());
        shape.setSubPageUniqueId(getSubPageName());
        shape.ensureShapeUniqueId();
        shape.updateBoundingRect();
    }

    public void removeShape(final Shape shape, boolean addToActionHistory) {
        updateShape(shape);
        removedShapeList.add(shape);
        newAddedShapeList.remove(shape);
        shapeList.remove(shape);
        if (addToActionHistory) {
            undoRedoManager.addToHistory(ShapeActions.removeShapeAction(shape), false);
        }
    }

    public void removeShapesByTouchPointList(final TouchPointList touchPointList, final float radius) {
        if (touchPointList == null) {
            return;
        }
        Map<String, Shape> hitShapes = new HashMap<>();
        for(Shape shape : shapeList) {
            for(TouchPoint touchPoint : touchPointList.getPoints()) {
                if (shape.fastHitTest(touchPoint.getX(), touchPoint.getY(), radius)) {
                    hitShapes.put(shape.getShapeUniqueId(), shape);
                    break;
                }
            }
        }

        for(Map.Entry<String, Shape> entry : hitShapes.entrySet()) {
            for(TouchPoint touchPoint : touchPointList.getPoints()) {
                if (entry.getValue().hitTest(touchPoint.getX(), touchPoint.getY(), radius)) {
                    removeShape(entry.getValue(), true);
                    break;
                }
            }
        }
    }

    public final List<Shape> getShapeList() {
        return shapeList;
    }

    public final List<Shape> deatchShapeList() {
        final List<Shape> list = shapeList;
        shapeList = null;
        return list;
    }

    public void render(final RenderContext renderContext, final RenderCallback callback) {
        if (shapeList == null) {
            return;
        }
        for(Shape shape : shapeList) {
            shape.render(renderContext);
            if (callback != null && callback.isRenderAbort()) {
                break;
            }
        }
    }

    public void prepareShapePool(int shapeType) {
        currentShapeType = shapeType;
    }

    // create a new shape if not exist and make it as current shape.
    public final Shape getShapeFromPool() {
        return currentShape = ShapeFactory.createShape(currentShapeType);
    }

    public final Shape getCurrentShape() {
        return currentShape;
    }

    /**
     * return null if not exists
     * @param context
     * @param docUniqueId
     * @param pageName
     * @param subPageName
     * @return
     */
    public static final NotePage loadPage(final Context context, final String docUniqueId, final String pageName, final String subPageName) {
        final List<ShapeModel> list = ShapeDataProvider.loadShapeList(context, docUniqueId, pageName, subPageName);
        final NotePage notePage = createPage(context, docUniqueId, pageName, subPageName);
        for(ShapeModel model : list) {
            final Shape shape = ShapeFactory.shapeFromModel(model);
            notePage.addShapeFromModel(shape);
        }
        return notePage;
    }

    public void loadPage(final Context context) {
        newAddedShapeList.clear();
        removedShapeList.clear();
        final List<ShapeModel> modelList = ShapeDataProvider.loadShapeList(context, getDocumentUniqueId(), getPageUniqueId(), getSubPageName());
        for(ShapeModel model : modelList) {
            addShapeFromModel(ShapeFactory.shapeFromModel(model));
        }
        setLoaded(true);
    }

    public static final NotePage createPage(final Context context, final String docUniqueId, final String pageName, final String subPageName) {
        final NotePage page = new NotePage(docUniqueId, pageName, subPageName);
        return page;
    }

    public List<ShapeModel> getNewAddedShapeModeList() {
        List<ShapeModel> modelList = new ArrayList<ShapeModel>(newAddedShapeList.size());
        for(Shape shape : newAddedShapeList) {
            final ShapeModel model = ShapeFactory.modelFromShape(shape);
            modelList.add(model);
        }
        return modelList;
    }

    public List<String> getRemovedShapeIdList() {
        List<String> list = new ArrayList<>();
        for(Shape shape: removedShapeList) {
            list.add(shape.getShapeUniqueId());
        }
        return list;
    }

    public boolean savePageInBackground(final Context context) {
        return false;
    }

    public boolean savePage(final Context context) {
        List<ShapeModel> modelList = new ArrayList<ShapeModel>(newAddedShapeList.size());
        for(Shape shape : newAddedShapeList) {
            final ShapeModel model = ShapeFactory.modelFromShape(shape);
            modelList.add(model);
        }
        if (modelList.size() > 0) {
            ShapeDataProvider.saveShapeList(context, modelList);
        }

        List<String> list = new ArrayList<>();
        for(Shape shape: removedShapeList) {
            list.add(shape.getShapeUniqueId());
        }
        ShapeDataProvider.removeShapesByIdList(context, list);
        newAddedShapeList.clear();
        removedShapeList.clear();
        return true;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean l) {
        loaded = l;
    }

    public boolean hasShapes() {
        return shapeList != null && shapeList.size() > 0;
    }

    public boolean hasPendingShapes() {
        return newAddedShapeList.size() > 0 || removedShapeList.size() > 0;
    }

    public void remove() {

    }
}
