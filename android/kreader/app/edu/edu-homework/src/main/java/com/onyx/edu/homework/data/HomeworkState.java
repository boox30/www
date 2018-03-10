package com.onyx.edu.homework.data;

/**
 * Created by lxm on 2017/12/12.
 */

public enum HomeworkState {

    DOING, SUBMITTED, REVIEW, SUBMITTED_AFTER_REVIEW;

    public static HomeworkState getHomeworkState(int state) {
        int size = HomeworkState.values().length;
        if (state >= size) {
            return HomeworkState.DOING;
        }
        return HomeworkState.values()[state];
    }
}
