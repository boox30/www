package com.onyx.android.dr.request.local;

import com.onyx.android.dr.data.database.GoodSentenceNoteEntity;
import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.data.request.data.BaseDataRequest;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

/**
 * Created by zhouzhiming on 2017/7/5.
 */
public class GoodSentenceExcerptQuery extends BaseDataRequest {
    private List<GoodSentenceNoteEntity> goodSentenceList;

    @Override
    public void execute(DataManager dataManager) throws Exception {
        super.execute(dataManager);
        queryGoodSentenceList();
    }

    public List<GoodSentenceNoteEntity> getGoodSentenceList() {
        return goodSentenceList;
    }

    public void setGoodSentenceList(List<GoodSentenceNoteEntity> goodSentenceList) {
        this.goodSentenceList = goodSentenceList;
    }

    public void queryGoodSentenceList() {
        List<GoodSentenceNoteEntity> goodSentenceList = new Select().from(GoodSentenceNoteEntity.class).queryList();
        if (goodSentenceList != null && goodSentenceList.size() > 0) {
            setGoodSentenceList(goodSentenceList);
        }
    }
}
