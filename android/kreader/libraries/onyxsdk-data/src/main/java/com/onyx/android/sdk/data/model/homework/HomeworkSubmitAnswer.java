package com.onyx.android.sdk.data.model.homework;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lxm on 2017/12/9.
 */

public class HomeworkSubmitAnswer {

    public String question;
    public String uniqueId;
    public List<String> value;
    public List<String> attachment;
    public List<String> attachmentUrl;
    public List<String> filePaths;
    public String drawData;
    public List<Bitmap> bitmaps;
    public int correct;
    public String comment;
    public float score;
    public int QuesType;

    public void setAttachment(List<String> attachment) {
        this.attachment = attachment;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void addValue(String v) {
        if (value == null) {
            value = new ArrayList<>();
        }
        value.add(v);
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setQuesType(int quesType) {
        QuesType = quesType;
    }
}
