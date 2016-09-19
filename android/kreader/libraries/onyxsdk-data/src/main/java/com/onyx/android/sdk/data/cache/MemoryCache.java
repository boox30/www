package com.onyx.android.sdk.data.cache;

import com.onyx.android.sdk.data.provider.DataProviderBase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by suicheng on 2016/9/15.
 */
public class MemoryCache {
    private Map<String, LibraryCache> memoryCacheMap = new LinkedHashMap<>();
    private static MemoryCache instance;
    private DataProviderBase dataProvider;

    private MemoryCache(DataProviderBase dataProvider) {
        this.dataProvider = dataProvider;
    }

    public static synchronized MemoryCache getInstance(DataProviderBase dataProvider) {
        if (instance == null) {
            instance = new MemoryCache(dataProvider);
        }
        return instance;
    }

    public void clearDataCache(String id) {
        memoryCacheMap.get(id).clear();
    }

    public void clearAllDataCache() {
        memoryCacheMap.clear();
    }

    public void putDataCache(String id, LibraryCache cache) {
        memoryCacheMap.put(id, cache);
    }

    public LibraryCache getDataCache(String id) {
        LibraryCache cache = memoryCacheMap.get(id);
        if (cache == null) {
            cache = new LibraryCache(dataProvider);
        }
        return cache;
    }
}
