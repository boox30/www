package com.onyx.android.sdk.scribble.request.navigation;

import android.util.Log;
import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;
import com.onyx.android.sdk.scribble.shape.Shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by zhuzeng on 7/3/16.
 */
public class AddShapeRequest extends BaseNoteRequest {

    private static final String TAG = AddShapeRequest.class.getSimpleName();
    private volatile List<Shape> shapeList = Collections.synchronizedList(new ArrayList<Shape>());

    public AddShapeRequest(final List<Shape> list) {
        for(Shape shape : list) {
            Log.e("######", "origin shape with size: " + shape.getPoints().size());
        }
        shapeList.addAll(list);
        setRender(true);
        setPauseInputProcessor(false);
        setResumeInputProcessor(false);
    }

    public void execute(final NoteViewHelper helper) throws Exception {
        long start = System.currentTimeMillis();
        helper.getNoteDocument().getCurrentPage(getContext()).addShapeList(shapeList);
        renderCurrentPage(helper);
        updateShapeDataInfo(helper);
        long end = System.currentTimeMillis();
        Log.e(TAG, "Render in background finished: " + (end - start));
    }


}
