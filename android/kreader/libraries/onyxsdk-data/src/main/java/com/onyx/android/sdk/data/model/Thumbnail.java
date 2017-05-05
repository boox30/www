package com.onyx.android.sdk.data.model;

import android.graphics.Bitmap;

import com.onyx.android.sdk.data.compatability.OnyxThumbnail;
import com.onyx.android.sdk.data.compatability.OnyxThumbnail.ThumbnailKind;
import com.onyx.android.sdk.data.converter.ThumbKindConverter;
import com.onyx.android.sdk.data.db.ContentDatabase;
import com.onyx.android.sdk.data.utils.ThumbnailUtils;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * Created by suicheng on 2016/9/5.
 */
@Table(database = ContentDatabase.class)
public class Thumbnail extends BaseData {

    @Column(name = "_data")
    private String imageDataPath;

    @Column
    private String originContentPath = null;

    @Column(typeConverter = ThumbKindConverter.class)
    private ThumbnailKind thumbnailKind = ThumbnailKind.Original;

    /**
     * 512x512 at most, or original bmp' size, if it's smaller than 512x512
     *
     * @param bmp
     * @return
     */
    public static Bitmap createLargeThumbnail(Bitmap bmp) {
        return ThumbnailUtils.createLargeThumbnail(bmp);
    }

    /**
     * 256x256 at most, or original bmp' size, if it's smaller than 256x256
     *
     * @param bmp
     * @return
     */
    public static Bitmap createMiddleThumbnail(Bitmap bmp) {
        return ThumbnailUtils.createMiddleThumbnail(bmp);
    }

    /**
     * 128x128 at most, or original bmp' size, if it's smaller than 128x128
     *
     * @param bmp
     * @return
     */
    public static Bitmap createSmallThumbnail(Bitmap bmp) {
        return ThumbnailUtils.createSmallThumbnail(bmp);
    }

    public String getOriginContentPath() {
        return originContentPath;
    }

    public void setOriginContentPath(String path) {
        this.originContentPath = path;
    }

    public ThumbnailKind getThumbnailKind() {
        return thumbnailKind;
    }

    public void setThumbnailKind(ThumbnailKind tk) {
        this.thumbnailKind = tk;
    }

    public String getImageDataPath() {
        return imageDataPath;
    }

    public void setImageDataPath(String imagePath) {
        this.imageDataPath = imagePath;
    }
}
