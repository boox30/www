package com.onyx.android.sdk.scribble.request.shape;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;

import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.request.BaseNoteRequest;
import com.onyx.android.sdk.scribble.shape.Shape;
import com.onyx.android.sdk.scribble.shape.ShapeFactory;
import com.onyx.android.sdk.scribble.shape.ShapeSpan;

import java.util.List;
import java.util.Map;

/**
 * Created by zhuzeng on 8/6/16.
 */
public class SpannableRequest extends BaseNoteRequest {
    private static final String TAG = "SpannableRequest";

    public static String SPAN_BUILDER_SYMBOL = "A";
    public static String SPACE_SPAN = " ";
    private Map<String, List<Shape>> subPageSpanTextShapeMap;
    private SpannableStringBuilder spannableStringBuilder;
    private List<Shape> newAddShapes;
    private ShapeSpan lastShapeSpan;

    public SpannableRequest(Map<String, List<Shape>> subPageSpanTextShapeMap, final List<Shape> newAddShapes) {
        this.subPageSpanTextShapeMap = subPageSpanTextShapeMap;
        this.newAddShapes = newAddShapes;
        setPauseInputProcessor(true);
    }

    public void execute(final NoteViewHelper helper) throws Exception {
        setResumeInputProcessor(helper.useDFBForCurrentState());
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < subPageSpanTextShapeMap.size(); ++i) {
            builder.append(SPAN_BUILDER_SYMBOL);
        }
        if (newAddShapes != null && newAddShapes.size() > 0) {
            builder.append(SPAN_BUILDER_SYMBOL);
        }
        builder.append(" ");
        spannableStringBuilder = new SpannableStringBuilder(builder.toString());
        int index = 0;
        for (String groupId : subPageSpanTextShapeMap.keySet()) {
            lastShapeSpan = setSpan(index, subPageSpanTextShapeMap.get(groupId), false);
            index++;
        }
        ShapeSpan newShapeSpan = setSpan(index, newAddShapes, true);
        if (newShapeSpan != null) {
            lastShapeSpan = newShapeSpan;
        }
    }

    private ShapeSpan setSpan(int index, List<Shape> shapeList, boolean needUpdateShape) {
        if (shapeList == null || shapeList.size() == 0) {
            return null;
        }
        ShapeSpan span = new ShapeSpan(shapeList, needUpdateShape);
        spannableStringBuilder.setSpan(span, index, index + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return span;
    }

    public SpannableStringBuilder getSpannableStringBuilder() {
        return spannableStringBuilder;
    }

    public ShapeSpan getLastShapeSpan() {
        return lastShapeSpan;
    }
}
