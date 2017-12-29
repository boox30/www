package com.onyx.android.plato.utils;

import com.onyx.android.plato.R;
import com.onyx.android.plato.SunApplication;
import com.onyx.android.plato.common.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by li on 2017/10/11.
 */

public class StringUtil {
    private static Map<String, String> homeworkMap = new HashMap<String, String>() {
        {
            put(Constants.TASK, SunApplication.getInstance().getResources().getString(R.string.homework_course_exercise));
            put(Constants.EXAM, SunApplication.getInstance().getResources().getString(R.string.homework_course_test_paper));
        }
    };

    public static String transitionHomeworkType(String str) {
        if (!homeworkMap.containsKey(str)) {
            return str;
        }
        return homeworkMap.get(str);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.equals("");
    }

    public static String distinguishAnswer(String answer) {
        if (isNullOrEmpty(answer)) {
            return SunApplication.getInstance().getResources().getString(R.string.no_answer);
        } else {
            return answer;
        }
    }

    public static String backWithType(String type) {
        if (Constants.CORRECT_TYPE.equals(type)) {
            return SunApplication.getInstance().getResources().getString(R.string.correct_info);
        } else {
            return SunApplication.getInstance().getResources().getString(R.string.new_info);
        }
    }

    public static boolean isCorrect(String type) {
        if (StringUtil.isNullOrEmpty(type)) {
            return true;
        }
        if (type.equals(Constants.PRACTICE)) {
            return false;
        } else if(type.equals(Constants.CORRECT)) {
            return true;
        }
        return true;
    }
}
