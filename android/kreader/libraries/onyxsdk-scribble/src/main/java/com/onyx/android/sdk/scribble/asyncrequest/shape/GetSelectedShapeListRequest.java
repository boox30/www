package com.onyx.android.sdk.scribble.asyncrequest.shape;

import com.onyx.android.sdk.scribble.asyncrequest.AsyncBaseNoteRequest;
import com.onyx.android.sdk.scribble.asyncrequest.AsyncNoteViewHelper;
import com.onyx.android.sdk.scribble.asyncrequest.NoteManager;
import com.onyx.android.sdk.scribble.shape.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by solskjaer49 on 2017/8/9 16:25.
 */

public class GetSelectedShapeListRequest extends AsyncBaseNoteRequest {
    public List<Shape> getSelectedShapeList() {
        return selectedShapeList;
    }

    private volatile List<Shape> selectedShapeList = new ArrayList<>();

    public GetSelectedShapeListRequest() {
        setPauseInputProcessor(true);
    }

    @Override
    public void execute(final NoteManager parent) throws Exception {
        selectedShapeList = parent.getNoteDocument().getCurrentPage(getContext()).getSelectedShapeList();
    }

}
