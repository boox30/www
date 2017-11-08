package com.onyx.android.sdk.data.rxrequest.data.db;

import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.common.references.CloseableReference;
import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.data.DataManagerHelper;
import com.onyx.android.sdk.data.QueryArgs;
import com.onyx.android.sdk.data.model.Library;
import com.onyx.android.sdk.data.model.Metadata;
import com.onyx.android.sdk.data.request.data.db.BaseDBRequest;
import com.onyx.android.sdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suicheng on 2016/9/5.
 */
public class RxLibraryLoadRequest extends RxBaseDBRequest {

    private boolean loadFromCache = true;
    private boolean loadMetadata = true;
    private QueryArgs queryArgs;

    private Map<String, CloseableReference<Bitmap>> thumbnailMap = new HashMap<>();
    private List<Metadata> bookList = new ArrayList<>();
    private List<Library> libraryList = new ArrayList<>();
    private long totalCount;

    public RxLibraryLoadRequest(DataManager dataManager, QueryArgs queryArgs) {
        super(dataManager);
        this.queryArgs = queryArgs;
    }

    public RxLibraryLoadRequest(DataManager dataManager, QueryArgs queryArgs, boolean loadMetadata) {
        super(dataManager);
        this.queryArgs = queryArgs;
        this.loadMetadata = loadMetadata;
    }

    public void setLoadFromCache(boolean loadFromCache) {
        this.loadFromCache = loadFromCache;
    }

    @Override
    public RxLibraryLoadRequest call() throws Exception {
        List<Library> tmpList = DataManagerHelper.loadLibraryListWithCache(getAppContext(), getDataManager(),
                queryArgs.libraryUniqueId, loadFromCache);
        if (!CollectionUtils.isNullOrEmpty(tmpList)) {
            libraryList.addAll(tmpList);
        }
        if (loadMetadata) {
            totalCount = getDataManager().getRemoteContentProvider().count(getAppContext(), queryArgs);
            List<Metadata> metadataList = DataManagerHelper.loadMetadataListWithCache(getAppContext(), getDataManager(),
                    queryArgs, loadFromCache);
            if (!CollectionUtils.isNullOrEmpty(metadataList)) {
                bookList.addAll(metadataList);
                loadBitmaps(getAppContext(), getDataManager());
            }
        }
        return this;
    }

    private void loadBitmaps(Context context, DataManager dataManager) {
        thumbnailMap = DataManagerHelper.loadThumbnailBitmapsWithCache(context, dataManager, bookList);
    }

    public List<Metadata> getBookList() {
        return bookList;
    }

    public List<Library> getLibraryList() {
        return libraryList;
    }

    public Map<String, CloseableReference<Bitmap>> getThumbnailMap() {
        return thumbnailMap;
    }

    public long getTotalCount() {
        return totalCount;
    }
}