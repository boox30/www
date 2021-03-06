package com.onyx.android.sdk.data.request.data;

import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.data.QueryArgs;
import com.onyx.android.sdk.data.model.Metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suicheng on 2016/9/2.
 */
public class MetadataRequest extends BaseDataRequest {
    private List<Metadata> list = new ArrayList<>();
    private QueryArgs queryArgs;

    public MetadataRequest(QueryArgs queryArgs) {
        this.queryArgs = queryArgs;
    }

    @Override
    public void execute(DataManager dataManager) throws Exception {
        list.addAll(dataManager.getMetadataList(getContext(), queryArgs));
    }

    public final List<Metadata> getList() {
        return list;
    }

}
