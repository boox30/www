package com.onyx.jdread.main.data.database;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by hehai on 17-3-7.
 */
@Database(name = JDReadDatabase.NAME, version = JDReadDatabase.VERSION)
public class JDReadDatabase {
    public static final String NAME = "JDRead";
    public static final int VERSION = 1;
    public static final String AUTHORITY = "com.onyx.jdread.provider";
    public static final String BASE_CONTENT_URI = "content://";
}