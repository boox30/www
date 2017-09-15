package com.onyx.android.dr.data;

import android.content.Context;

import com.onyx.android.dr.R;
import com.onyx.android.dr.bean.DictFunctionBean;
import com.onyx.android.dr.event.GoodExcerptEvent;
import com.onyx.android.dr.event.NewWordQueryEvent;
import com.onyx.android.dr.event.QueryRecordEvent;
import com.onyx.android.dr.event.VocabularyNotebookEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouzhiming on 17-6-28.
 */
public class DictFunctionConfig {
    public List<DictFunctionBean> dictFunctionData = new ArrayList<>();

    public void loadDictInfo(Context context) {

        DictFunctionBean functionData = new DictFunctionBean(context.getResources().getString(R.string.vocabulary_notebook), R.drawable.new_word_notebook, new VocabularyNotebookEvent());
        dictFunctionData.add(functionData);

        functionData = new DictFunctionBean(context.getResources().getString(R.string.new_word_query), R.drawable.new_word_query, new NewWordQueryEvent(""));
        dictFunctionData.add(functionData);

        functionData = new DictFunctionBean(context.getResources().getString(R.string.good_sentence_excerpt), R.drawable.good_sentence_notebook, new GoodExcerptEvent());
        dictFunctionData.add(functionData);

        functionData = new DictFunctionBean(context.getResources().getString(R.string.query_record), R.drawable.query_record, new QueryRecordEvent());
        dictFunctionData.add(functionData);
    }

    public List<DictFunctionBean> getDictData(int userType) {
        return dictFunctionData;
    }
}
