package com.onyx.kreader.reflow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.onyx.kreader.api.ReaderBitmapList;
import com.onyx.kreader.utils.FileUtils;
import com.onyx.kreader.utils.ReaderImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zhuzeng
 * Date: 3/27/14
 * Time: 10:33 AM
 * cache root
 *   |--- hashcode of settings
 *               |--- index, settings and page index.
 *               |--- page number - sub page.png
 */
public class ReaderScannedPageReflowManager {
    static private final String INDEX_FILE_NAME = "reflow-index.json";
    static private final String IMG_EXTENSION = ".png";


    private Map<String, ReaderBitmapList> pageMap;
    private String cacheRoot;
    private ReaderScannedPageReflowSettings settings;
    public ReaderScannedPageReflowManager(final String root, int dw, int dh) {
        super();
        cacheRoot = root;
        settings = ReaderScannedPageReflowSettings.createSettings();
        settings.dev_width = dw;
        settings.dev_height = dh;
    }

    public ReaderScannedPageReflowSettings getSettings() {
        return settings;
    }

    static public File cacheFilePath(final String root, final ReaderScannedPageReflowSettings settings, final String fileName) {
        if (settings == null || root == null) {
            return null;
        }
        File file = new File(root, settings.md5() + "-" + fileName);
        return file;
    }

    public void updateSettings(final ReaderScannedPageReflowSettings s) {
        settings.update(s);
        pageMap = null;
        loadPageMap();
    }

    public void reflowBitmap(Bitmap bitmap, int pageWidth, int pageHeight, double scale, final String pageName) {
        settings.page_width = (int)(pageWidth * scale);
        settings.page_height = (int)(pageHeight * scale);
        Log.i("reflow settings", JSON.toJSONString(settings));
        if (ReaderImageUtils.reflowScannedPage(bitmap, pageName, this)) {
            savePageMap();
        }
    }

    public void loadPageMap() {
        if (pageMap != null) {
            return;
        }

        try {
            File file = cacheFilePath(cacheRoot, settings, INDEX_FILE_NAME);
            if (file != null && FileUtils.fileExist(file.getAbsolutePath())) {
                String json = FileUtils.readContentOfFile(file);
                pageMap = JSON.parseObject(json, new TypeReference<Map<String, ReaderBitmapList>>(){});
            }
        } catch (Exception e) {
            e.printStackTrace();
            pageMap = null;
        }

        if (pageMap == null) {
            pageMap = new HashMap<String, ReaderBitmapList>();
        }
    }

    private void savePageMap() {
        try {
            File file = cacheFilePath(cacheRoot, settings, INDEX_FILE_NAME);
            String json = JSON.toJSONString(pageMap);
            FileUtils.saveContentToFile(json, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap loadBitmapFromFile(final String key) {
        File file = cacheFilePath(cacheRoot, settings, key);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        final String path = file.getAbsolutePath() + IMG_EXTENSION;
        if (!FileUtils.fileExist(path)) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    private boolean saveBitmapToFile(final String key, Bitmap bitmap) {
        final String path = cacheFilePath(cacheRoot, settings, key).getAbsolutePath() + IMG_EXTENSION;
        return saveBitmap(bitmap, path);
    }

    static public boolean saveBitmap(Bitmap bitmap, final String path) {
        try {
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean putBitmap(final String key, final Bitmap bitmap) {
        return saveBitmapToFile(key, bitmap);
    }

    public Bitmap getBitmap(final String key) {
        final Bitmap bitmap = loadBitmapFromFile(key);
        return bitmap;
    }

    private ReaderBitmapList getSubPageList(final String pageName) {
        ReaderBitmapList list = pageMap.get(pageName);
        if (list == null) {
            list = new ReaderBitmapList();
            pageMap.put(pageName, list);
        }
        return list;
    }

    public boolean atBegin(final String pageName) {
        return getSubPageList(pageName).atBegin();
    }

    public boolean atEnd(final String pageName) {
        return getSubPageList(pageName).atEnd();
    }

    public void moveToEnd(final String pageName) {
        getSubPageList(pageName).moveToEnd();
    }

    public void moveToBegin(final String pageName) {
        getSubPageList(pageName).moveToBegin();
    }

    public boolean next(final String pageName) {
        return getSubPageList(pageName).next();
    }

    public boolean prev(final String pageName) {
        return getSubPageList(pageName).prev();
    }

    public void clear(final String pageName) {
        getSubPageList(pageName).clear();
    }

    static public final String subpageCacheKey(final String pageKey, int index) {
        return String.format("%s-%d", pageKey, index);
    }

    static public final String pageKey(int page) {
        return String.valueOf(page);
    }

    public Bitmap getCurrentBitmap(final String pageName) {
        ReaderBitmapList list = getSubPageList(pageName);
        if (list != null && list.getCurrentBitmap() != null) {
            return list.getCurrentBitmap();
        }
        if (list.isEmpty()) {
            return null;
        }
        int index = list.getCurrent();
        return getBitmap(subpageCacheKey(pageName, index));
    }

    public void addBitmap(final String pageName, int subPage, Bitmap bitmap) {
        ReaderBitmapList list = getSubPageList(pageName);
        list.addBitmap(bitmap);
        putBitmap(subpageCacheKey(pageName, subPage), bitmap);
        bitmap.recycle();
    }

    public void clearAllCacheFiles() {
        File dir = new File(cacheRoot);
        File[] list = dir.listFiles();
        if (list == null) {
            return;
        }

        for (File f : list) {
            f.delete();
        }
        if (pageMap != null) {
            for(ReaderBitmapList entry : pageMap.values()) {
                if (entry != null) {
                    entry.clearAllBitmap();
                }
            }
        }
        pageMap.clear();
        savePageMap();
    }

}
