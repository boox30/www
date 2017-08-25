package com.onyx.android.dr.reader.data;

import com.onyx.android.dr.data.database.DRDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by hehai on 17-8-12.
 */
@Table(database = DRDatabase.class)
public class AfterReadingEntity extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String bookName;
    @Column
    public String title = "";
    @Column
    public String contents = "";
    @Column
    public String time = "";
}
