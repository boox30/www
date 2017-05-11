package com.onyx.kreader.reader;

import android.content.Context;

import com.onyx.kreader.reader.data.ReaderDataHolder;
import com.onyx.kreader.reader.opengl.PageRenderView;

/**
 * Created by ming on 2017/5/5.
 */

public class ReaderRender {

    public static void renderPage(final Context context, final ReaderDataHolder readerDataHolder, final PageRenderView renderView) {
        drawHighLight(readerDataHolder, renderView);
    }

    private static void drawHighLight(final ReaderDataHolder readerDataHolder, final PageRenderView renderView) {
        if (readerDataHolder.getSelectionManager().hasSelection()) {
            renderView.updateIOpenGLObjects(readerDataHolder.getSelectionManager().getSelectionRectangles());
        }
    }
}
