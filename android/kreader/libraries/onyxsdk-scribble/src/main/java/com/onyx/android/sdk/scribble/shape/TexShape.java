package com.onyx.android.sdk.scribble.shape;

import android.graphics.RectF;

import com.onyx.android.sdk.scribble.data.ShapeExtraAttributes;
import com.onyx.android.sdk.utils.StringUtils;

/**
 * Created by zhuzeng on 4/19/16.
 */
public class TexShape extends BaseShape  {

    /**
     * rectangle, circle, etc.
     * @return
     */
    public int getType() {
        return ShapeFactory.SHAPE_TEXT;
    }

    public void render(final RenderContext renderContext) {
        ShapeExtraAttributes shapeExtraAttributes = getShapeExtraAttributes();
        if (shapeExtraAttributes == null) {
            return;
        }
        String content = shapeExtraAttributes.getTextContent();
        if (StringUtils.isNullOrEmpty(content)) {
            return;
        }

        float sx = getDownPoint().getX();
        float sy = getDownPoint().getY();
        float ex = getCurrentPoint().getX();
        float ey = getCurrentPoint().getY();
        RectF rect = new RectF(sx, sy, ex, ey);
        if (renderContext.matrix != null) {
            renderContext.matrix.mapRect(rect);
        }

        float textSize = shapeExtraAttributes.getTextSize();
        renderContext.paint.setTextSize(textSize);
        renderContext.canvas.drawText(shapeExtraAttributes.getTextContent(), rect.left, rect.bottom - (rect.bottom - rect.top) / 2 , renderContext.paint);
    }

}
