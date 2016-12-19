package com.onyx.android.sdk.scribble.data;

import android.graphics.Bitmap;

import android.graphics.Color;
import com.onyx.android.sdk.scribble.shape.ShapeFactory;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Index;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

/**
 * Created by zhuzeng on 6/21/16.
 * Represent library container and document. When document id is not null, it's a document
 * when library id is not null, it's a library.
 */
@Table(database = ShapeDatabase.class)
public class NoteModel extends BaseModel {

    public static final int TYPE_LIBRARY = 0;
    public static final int TYPE_DOCUMENT = 1;

    @Column
    @PrimaryKey(autoincrement = true)
    @Index
    long id;

    @Column
    Date createdAt = null;

    @Column
    Date updatedAt = null;

    @Column
    @Unique
    String uniqueId;

    @Column
    String parentUniqueId;

    @Column
    String subPageName;

    @Column
    String title;

    @Column
    String extraAttributes;

    @Column
    int type;

    @Column
    float strokeWidth;

    @Column
    int strokeColor = 0;

    @Column
    int currentShapeType = ShapeFactory.SHAPE_INVALID;

    @Column
    int background;

    @Column
    int lineLayoutBackgroud;

    @Column(typeConverter = ConverterStringList.class)
    PageNameList pageNameList = null;

    Bitmap thumbnail;

    private static final float DEFAULT_STROKE_WIDTH = 2.0f;
    private static float DEFAULT_ERASER_RADIUS = 15.0f;
    private static int DEFAULT_STROKE_COLOR = Color.BLACK;

    public NoteModel() {
    }

    public long getId() {
        return id;
    }

    public void setId(long value) {
        id = value;
    }

    public void setCreatedAt(final Date d) {
        createdAt = d;
    }

    public final Date getCreatedAt() {
        return createdAt;
    }

    public final Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Date d) {
        updatedAt = d;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * the unique id can be generated by itself when it's virtual note
     * also it can be md5 of associated file.
     * @param id
     */
    public void setUniqueId(final String id) {
        uniqueId = id;
    }

    public String getParentUniqueId() {
        return parentUniqueId;
    }

    public void setParentUniqueId(final String name) {
        parentUniqueId = name;
    }

    public String getSubPageName() {
        return subPageName;
    }

    public void setSubPageName(final String spn) {
        subPageName = spn;
    }

    public String getExtraAttributes() {
        return extraAttributes;
    }

    public void setExtraAttributes(final String attributes) {
        extraAttributes = attributes;
    }

    public void setTitle(final String t) {
        title = t;
    }

    public String getTitle() {
        return title;
    }

    public int getType() {
        return type;
    }

    public void setType(int t) {
        type = t;
    }

    public boolean isDocument() {
        return type == TYPE_DOCUMENT;
    }

    public boolean isLibrary() {
        return type == TYPE_LIBRARY;
    }

    public PageNameList getPageNameList() {
        return pageNameList;
    }

    public void setPageNameList(final PageNameList names) {
        pageNameList = names;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(final Bitmap bmp) {
        thumbnail = bmp;
    }

    public void setStrokeWidth(float w) {
        strokeWidth = w;
    }

    public float getStrokeWidth() {
        if (strokeWidth <= 0) {
            return getDefaultStrokeWidth();
        }
        return strokeWidth;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getStrokeColor() {
        if (strokeColor == 0) {
            strokeColor = getDefaultStrokeColor();
        }
        return strokeColor;
    }

    public int getLineLayoutBackgroud() {
        return lineLayoutBackgroud;
    }

    public void setLineLayoutBackgroud(int lineLayoutBackgroud) {
        this.lineLayoutBackgroud = lineLayoutBackgroud;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getCurrentShapeType() {
        return currentShapeType;
    }

    public void setCurrentShapeType(int currentShapeType) {
        this.currentShapeType = currentShapeType;
    }

    private void beforeSave() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        updatedAt = new Date();
    }

    public void save() {
        beforeSave();
        super.save();
    }

    public static float getDefaultStrokeWidth() {
        return DEFAULT_STROKE_WIDTH;
    }

    public static float getDefaultEraserRadius() {
        return DEFAULT_ERASER_RADIUS;
    }

    public static void setDefaultEraserRadius(float defaultEraserRadius) {
        DEFAULT_ERASER_RADIUS = defaultEraserRadius;
    }

    public static int getDefaultStrokeColor() {
        return DEFAULT_STROKE_COLOR;
    }

    public static void setDefaultStrokeColor(int color) {
        DEFAULT_STROKE_COLOR = color;
    }

    public static int getDefaultBackground() {
        return NoteBackgroundType.EMPTY;
    }

    public static int getDefaultLineLayoutBackground() {
        return NoteBackgroundType.LINE;
    }

    public static NoteModel createNote(final String documentUniqueId, final String parentUniqueId, final String title) {
        final NoteModel document = new NoteModel();
        document.setType(TYPE_DOCUMENT);
        document.setUniqueId(documentUniqueId);
        document.setParentUniqueId(parentUniqueId);
        document.setTitle(title);
        return document;
    }

    public static NoteModel createLibrary(final String libraryUniqueId, final String parentUniqueId, final String title) {
        final NoteModel library = new NoteModel();
        library.setType(TYPE_LIBRARY);
        library.setUniqueId(libraryUniqueId);
        library.setParentUniqueId(parentUniqueId);
        library.setTitle(title);
        return library;
    }


}
