/*
 * Copyright (C) 2015 Actinarium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onyx.android.sdk.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import com.onyx.android.sdk.ui.R;

/**
 * A TextView implementation, which is infinitely easier to align to 4dp baseline grid than the usual TextView.
 * Allows to set leading (first and other lines) and last line descent for precise placement on the grid
 *
 * @author Paul Danyliuk
 */
public class AlignTextView extends android.widget.TextView {

    private boolean mRequiresAdjustment = false;

    private int mLeading;
    private int mFirstLineLeading;
    private int mLastLineDescent;

    public boolean isChecked() {
        return isChecked;
    }

    private boolean isChecked = false;

    public static final int DEFAULT_BEHAVIOR = 0;

    public AlignTextView(Context context) {
        super(context);
        mLeading = DEFAULT_BEHAVIOR;
        mFirstLineLeading = DEFAULT_BEHAVIOR;
        mLastLineDescent = DEFAULT_BEHAVIOR;
    }

    public AlignTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        commonInit(context, attrs, 0, 0);
    }

    public AlignTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        commonInit(context, attrs, defStyleAttr, 0);
    }

    private void commonInit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.AlignTextView, defStyleAttr, defStyleRes);

        try {
            mLeading = array.getDimensionPixelSize(R.styleable.AlignTextView_leading, DEFAULT_BEHAVIOR);
            mFirstLineLeading = array.getDimensionPixelSize(R.styleable.AlignTextView_firstLineLeading, mLeading);
            mLastLineDescent = array.getDimensionPixelSize(R.styleable.AlignTextView_lastLineDescent, DEFAULT_BEHAVIOR);
        } finally {
            array.recycle();
        }

        if (mFirstLineLeading != DEFAULT_BEHAVIOR || mLastLineDescent != DEFAULT_BEHAVIOR) {
            setIncludeFontPadding(false);
            mRequiresAdjustment = true;
        } else if (mLeading != DEFAULT_BEHAVIOR) {
            mRequiresAdjustment = true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // todo: don't call this on every measurement pass - only once on init and when text metrics (size etc) is changed
        // (although that would require overriding a ton of methods)
        if (mRequiresAdjustment) {
            adjustMetrics();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getLeading() {
        return mLeading;
    }

    public void setLeading(int leading) {
        if (mLeading == leading) {
            return;
        }

        mLeading = leading;
        refreshRequireAdjustment();
        if (mRequiresAdjustment) {
            adjustMetrics();
        }
        invalidate();
    }

    public int getFirstLineLeading() {
        return mFirstLineLeading;
    }

    public void setFirstLineLeading(int firstLineLeading) {
        if (mFirstLineLeading == firstLineLeading) {
            return;
        }

        mFirstLineLeading = firstLineLeading;
        refreshRequireAdjustment();
        if (mRequiresAdjustment) {
            adjustMetrics();
        }
        invalidate();
    }

    public int getLastLineDescent() {
        return mLastLineDescent;
    }

    public void setLastLineDescent(int lastLineDescent) {
        if (mLastLineDescent == lastLineDescent) {
            return;
        }

        mLastLineDescent = lastLineDescent;
        refreshRequireAdjustment();
        if (mRequiresAdjustment) {
            adjustMetrics();
        }
        invalidate();
    }

    private void refreshRequireAdjustment() {
        mRequiresAdjustment = mLeading != DEFAULT_BEHAVIOR
                || mLastLineDescent != DEFAULT_BEHAVIOR
                || mFirstLineLeading != DEFAULT_BEHAVIOR;
    }

    private void adjustMetrics() {
        Paint.FontMetricsInt metrics = getPaint().getFontMetricsInt();

        // Required extra for interline leading.
        // Own height is measured from ascent to descent (ascent < descent)
        if (mLeading != DEFAULT_BEHAVIOR) {
            final int extra = mLeading - (metrics.descent - metrics.ascent);
            setLineSpacing(extra, 1);
        }

        // Required top padding to set first line leading
        // With setIncludeFontPadding=false, the first baseline is placed -{ascent} pixels from the top
        final int paddingTop;
        if (mFirstLineLeading != DEFAULT_BEHAVIOR) {
            paddingTop = mFirstLineLeading + metrics.ascent;
        } else {
            paddingTop = getPaddingTop();
        }

        // Required bottom padding
        // With setIncludeFontPadding=false, the view's bottom os placed {descent} pixels from the bottom
        final int paddingBottom;
        if (mLastLineDescent != DEFAULT_BEHAVIOR) {
            paddingBottom = mLastLineDescent - metrics.descent;
        } else {
            paddingBottom = getPaddingBottom();
        }

        setPadding(getPaddingLeft(), paddingTop, getPaddingRight(), paddingBottom);
    }

    public void setChecked(boolean isChecked){
        if (isChecked) {
            setBackgroundResource(R.drawable.reader_layer_menu_font_style_font_size_button_chosen_bg);
            setTextColor(getResources().getColor(android.R.color.white));
        }else {
            setBackgroundResource(R.drawable.imagebtn_bg);
            setTextColor(getResources().getColor(android.R.color.black));
        }
        this.isChecked = isChecked;
    }
}