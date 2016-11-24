package com.onyx.libedu.model;

import com.onyx.android.sdk.data.model.BaseData;
import com.onyx.libedu.db.EduDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * Created by ming on 2016/10/31.
 */
@Table(database = EduDatabase.class)
public class Subject extends BaseData{

    @Column
    private String subjectName = null;

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public static Subject create(String subjectName) {
        Subject subject = new Subject();
        subject.setSubjectName(subjectName);
        return subject;
    }
}
