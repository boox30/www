package com.onyx.android.note.data;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by solskjaer49 on 16/6/30 18:41.
 */

public class PenType {
    // ... type definitions
    // Describes when the annotation will be discarded
    @Retention(RetentionPolicy.SOURCE)
    // Enumerate valid values for this interface
    @IntDef({PENCIL, OILY_PEN, FOUNTAIN_PEN, BRUSH, RULER, ERASER})
    // Create an interface for validating int types
    public @interface PenTypeDef {
    }

    public static final int PENCIL = 0;
    public static final int OILY_PEN = 1;
    public static final int FOUNTAIN_PEN = 2;
    public static final int BRUSH = 3;
    public static final int RULER = 4;
    public static final int ERASER = 5;
}
