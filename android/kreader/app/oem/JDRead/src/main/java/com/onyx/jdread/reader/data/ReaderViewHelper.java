package com.onyx.jdread.reader.data;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceView;

import com.onyx.android.sdk.reader.common.ReaderDrawContext;
import com.onyx.android.sdk.reader.common.ReaderViewInfo;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.reader.actions.NextPageAction;
import com.onyx.jdread.reader.actions.PrevPageAction;
import com.onyx.jdread.reader.actions.ShowSettingMenuAction;

/**
 * Created by huxiaomao on 2017/12/22.
 */

public class ReaderViewHelper {
    private SurfaceView readPageView;
    private Paint paint = new Paint();
    private static final int DEFAULT_MULTIPLEX = 1;
    public float dpiMultiplex = 1.0f;
    public ReaderViewInfo readerViewInfo;

    public ReaderViewHelper() {
        initData();
    }

    private void initData() {
        paint.setColor(Color.BLACK);
        paint.setTextSize(11 * (JDReadApplication.getInstance() != null ? dpiMultiplex : DEFAULT_MULTIPLEX));
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setStrokeWidth(0);
    }

    public final ReaderViewInfo getReaderViewInfo() {
        return readerViewInfo;
    }

    public SurfaceView getReadPageView() {
        return readPageView;
    }

    public void setReadPageView(SurfaceView readPageView) {
        this.readPageView = readPageView;
    }

    public int getPageViewWidth() {
        return readPageView.getWidth();
    }

    public int getPageViewHeight() {
        return readPageView.getHeight();
    }

    public ReaderViewInfo createReaderViewInfo() {
        readerViewInfo = new ReaderViewInfo();
        return readerViewInfo;
    }

    public void updatePageView(ReaderDataHolder readerDataHolder) {
        try {
            ReaderDrawContext context = ReaderDrawContext.create(false);
            readerDataHolder.getReader().getReaderHelper().getReaderLayoutManager().drawVisiblePages(readerDataHolder.getReader(), context, createReaderViewInfo());
            draw(context.renderingBitmap.getBitmap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Bitmap bitmap) {
        if (readPageView == null) {
            return;
        }
        if (bitmap == null) {
            return;
        }
        paint.setDither(true);
        Rect rect = new Rect(readPageView.getLeft(), readPageView.getTop(),
                readPageView.getRight(), readPageView.getBottom());
        Canvas canvas = readPageView.getHolder().lockCanvas();
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        readPageView.getHolder().unlockCanvasAndPost(canvas);
    }

    public void showTouchFunctionRegion(Canvas canvas) {
        Paint.Style oldStyle = paint.getStyle();
        paint.setStyle(Paint.Style.STROKE);
        int oldColor = paint.getColor();
        paint.setColor(Color.BLACK);

        Rect rect = ShowSettingMenuAction.getRegionOne();
        canvas.drawRect(rect, paint);
        rect = ShowSettingMenuAction.getRegionTwo();
        canvas.drawRect(rect, paint);
        rect = PrevPageAction.getRegionOne();
        canvas.drawRect(rect, paint);
        rect = NextPageAction.getRegionOne();
        canvas.drawRect(rect, paint);
        rect = NextPageAction.getRegionTwo();
        canvas.drawRect(rect, paint);

        paint.setColor(oldColor);
        paint.setStyle(oldStyle);
    }
}