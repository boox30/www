package com.onyx.android.sample.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.onyx.android.sdk.utils.BitmapUtils;
import com.onyx.android.sdk.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 10/8/2017.
 */

public class ImageUtils {
    private List<Rect> list = new ArrayList<>();
    public static int threshold = 3;

    public static int SOMETHING_MERGED  = 0x01;
    public static int PENDING           = 0x02;
    public static int NOTHING_TO_MERGE  = 0x04;

    // return full processed for upd.
    static public int merge(final Bitmap upd, final Bitmap workingBuffer, final Bitmap mcu, int maxFrame) {
        int updState = NOTHING_TO_MERGE;
        for(int y = 0; y < upd.getHeight(); ++y) {
            for(int x = 0; x < upd.getWidth(); ++x) {
                int v1 = upd.getPixel(x, y);
                int v2 = workingBuffer.getPixel(x, y);
                if (v1 == Color.TRANSPARENT) {
                    continue;
                }
                if (v1 == v2) {
                    continue;
                }
                int state = (mcu.getPixel(x, y) & 0xff);
                if (state >= maxFrame) {
                    workingBuffer.setPixel(x, y, v1);
                    upd.setPixel(x, y, Color.TRANSPARENT);
                    mcu.setPixel(x, y, Color.argb(0xff, 0, 0, 0));
                    updState |= SOMETHING_MERGED;
                } else {
                    updState |= PENDING;
                }
            }
        }
        return updState;
    }

    static public Bitmap merge(final Bitmap originUpd,
                               final Bitmap originWb,
                               final Bitmap mergedUpd,
                               final Bitmap mergedWb) {
        int width = originUpd.getWidth();
        int height = originUpd.getHeight();
        Bitmap result = Bitmap.createBitmap(width * 4, originUpd.getHeight(), originUpd.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        paint.setColor(Color.RED);

        canvas.drawBitmap(originUpd, 0f, 0f, null);
        canvas.drawBitmap(originWb, width, 0, null);
        canvas.drawBitmap(mergedUpd, width * 2, 0, null);
        canvas.drawBitmap(mergedWb, width * 3, 0, null);
        for(int i = 1; i <4; ++i) {
            canvas.drawLine(width * i, 0, width * i, height, paint);
        }
        return result;
    }

    static public void nextFrame(final Bitmap mcu, int maxFrame, int step) {
        for(int y = 0; y < mcu.getHeight(); ++y) {
            for(int x = 0; x < mcu.getWidth(); ++x) {
                int v1 = (mcu.getPixel(x, y) & 0xff);
                if (v1 < maxFrame) {
                    mcu.setPixel(x, y, Color.argb(0xff, 0, 0, v1 + step));
                }
            }
        }
    }

    public void diff(final String first, final String second) {
        list.clear();
        final Bitmap firstBitmap = loadBitmapFromFile(first);
        final Bitmap secondBitmap = loadBitmapFromFile(second);

        for(int y = 0; y < firstBitmap.getHeight(); ++y) {
            for(int x = 0; x < firstBitmap.getWidth(); ++x) {
                int v1 = firstBitmap.getPixel(x, y);
                int v2 = secondBitmap.getPixel(x, y);
                if (v1 == v2) {
                    continue;
                }
                tryToMerge(x, y, threshold, threshold);
            }
        }

        drawRectanglesOnBitmap(secondBitmap, list);

        final String path = second + ".diff-" + list.size() + ".png";
        FileUtils.deleteFile(path);
        BitmapUtils.saveBitmap(secondBitmap, path);
    }

    private boolean tryToMerge(int x, int y, int xthreshold, int ythreshold) {
        for(int i = list.size() - 1; i >= 0; --i) {
            Rect rect = list.get(i);
            if (contains(rect, x, y, xthreshold, ythreshold)) {
                rect.union(x, y);
                return true;
            }
        }
        Rect rect = new Rect(x, y, x + 1, y + 1);
        list.add(rect);
        return false;
    }

    public static boolean contains(final Rect rect, int x, int y, int xthreshold, int ythreshold) {
        return x >= rect.left &&
                x < rect.right + xthreshold &&
                y >= rect.top &&
                y < rect.bottom + ythreshold;
    }

    public static void union(final Rect rect, int x, int y) {
        rect.union(x, y);
    }

    public static void drawRectanglesOnBitmap(final Bitmap bitmap, final List<Rect> list) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3.0F);
        int value = 1;
        for(Rect rect : list) {
            paint.setColor(Color.rgb(value, value, value));
            canvas.drawRect(rect, paint);
            value += 10;
            value %= 255;
        }
    }


    public static Bitmap loadBitmapFromFile(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    public static Bitmap create(final Bitmap origin) {
        return origin.copy(origin.getConfig(), true);
    }

}
