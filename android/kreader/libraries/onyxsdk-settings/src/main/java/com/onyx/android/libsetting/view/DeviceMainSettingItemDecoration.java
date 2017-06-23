package com.onyx.android.libsetting.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by solskjaer49 on 2016/11/29 18:25.
 */

public class DeviceMainSettingItemDecoration extends RecyclerView.ItemDecoration {
    private int color = -1;
    private int width = 1;

    //all use default setting.
    public DeviceMainSettingItemDecoration() {
    }

    public DeviceMainSettingItemDecoration(int color, int width) {
        this.color = color;
        this.width = width;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        Paint paint = new Paint();
        paint.setColor(color == -1 ? parent.getContext().getResources().getColor(android.R.color.black) : color);
        paint.setStrokeWidth(width);

        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);

            float x = childView.getX();
            float y = childView.getY();
            int width = childView.getWidth();
            int height = childView.getHeight();

            c.drawLine(x, y, x + width, y, paint);
            c.drawLine(x, y, x, y + height, paint);
            c.drawLine(x + width, y, x + width, y + height, paint);
            c.drawLine(x, y + height, x + width, y + height, paint);
        }
        super.onDraw(c, parent, state);
    }
}