package com.onyx.kreader.reflow;

import android.graphics.Bitmap;

import android.graphics.Color;
import com.onyx.android.sdk.utils.Benchmark;
import com.onyx.kreader.utils.HashUtils;
import com.onyx.kreader.utils.ImageUtils;

import java.io.File;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
public class ImageReflowManager {

    public static final String TAG = ImageReflowManager.class.getSimpleName();

    static private final String INDEX_FILE_NAME = "reflow-index.json";

    private String documentMd5;
    private File cacheRoot;
    private ImageReflowSettings settings;

    private ReflowedSubPageIndex subPageIndex;
    private ReflowedSubPageCache subPageCache;

    private Lock reflowLock = new ReentrantLock(); // to be hold for a short time to check if a page is being reflowed or not
    private Condition reflowReadyCondition = reflowLock.newCondition();

    private ReflowTaskExecutor reflowExecutor = new ReflowTaskExecutor(ImageReflowManager.this);

    public ImageReflowManager(final String documentMd5, final File cacheRoot, int dw, int dh) {
        super();

        this.documentMd5 = documentMd5;
        this.cacheRoot = cacheRoot;
        settings = ImageReflowSettings.createSettings();
        settings.dev_width = dw;
        settings.dev_height = dh;
        subPageCache = ReflowedSubPageCache.create(cacheRoot);
        loadSubPageIndex();
    }

    public ImageReflowSettings getSettings() {
        return settings;
    }

    public void updateViewportSize(int dw, int dh) {
        ImageReflowSettings copy = ImageReflowSettings.copy(settings);
        copy.dev_width = dw;
        copy.dev_height = dh;
        updateSettings(copy);
    }

    public void updateSettings(final ImageReflowSettings s) {
        settings.update(s);
        notifySettingsUpdated();
    }

    public void notifySettingsUpdated() {
        release();
        loadSubPageIndex();
    }

    public void release() {
        reflowExecutor.abort();
        String currentPage = reflowExecutor.getCurrentTaskPage();
        if (currentPage != null) {
            waitUntilSubPagesReady(currentPage);
        }
        ImageUtils.releaseReflowedPages();
        documentMd5 = null;
    }

    public void reflowBitmapAsync(final Bitmap bitmap, final String pageName, final boolean abortPendingTasks) {
        ReflowTask task = new ReflowTask(this, pageName, bitmap);
        task.setAbortPendingTasks(abortPendingTasks);
        reflowExecutor.submitTask(task);
    }

    void signalTaskFinished() {
        reflowLock.lock();
        try {
            reflowReadyCondition.signal();
        } finally {
            reflowLock.unlock();
        }
    }

    /**
     * will be called back from ReflowTask
     *
     * @param pageName
     * @param bitmap
     */
    void reflowBitmap(final String pageName, final Bitmap bitmap) {
        Benchmark benchmark = new Benchmark();
        if (ImageUtils.reflowPage(pageName, bitmap, settings)) {
            benchmark.report("reflow page takes: " + pageName);
            try {
                reflowLock.lock();
                splitSubPages(pageName);
                reflowReadyCondition.signal();
            } finally {
                reflowLock.unlock();
            }
        }
    }

    private void splitSubPages(final String pageName) {
        int[] size = new int[2];
        if (!ImageUtils.getReflowedPageSize(pageName, size)) {
            return;
        }
        int pageCount = size[1] / settings.dev_height + (size[1] % settings.dev_height != 0 ? 1 : 0);
        if (!subPageIndex.isSubPageListReflowComplete(pageName)) {
            for (int i = 0; i < pageCount; i++) {
                subPageIndex.addSubPageBitmap(pageName, null);
            }
        }
        subPageIndex.markSubPageListReflowComplete(pageName);
        subPageIndex.saveSubPageIndex();
    }

    private void waitUntilSubPagesReady(final String pageName) {
        reflowLock.lock();
        try {
            while (reflowExecutor.isPageWaitingReflow(pageName)) {
                reflowReadyCondition.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            reflowLock.unlock();
        }
    }

    public int getCurrentSubPageIndex(final String pageName) {
        return subPageIndex.getSubPageCurrentIndex(pageName);
    }

    public boolean atFirstSubPage(final String pageName) {
        return subPageIndex.atFirstSubPage(pageName);
    }

    public boolean atLastSubPage(final String pageName) {
        if (!subPageIndex.isSubPageListReflowComplete(pageName)) {
            waitUntilSubPagesReady(pageName);
        }
        return subPageIndex.atLastSubPage(pageName);
    }

    public void moveToFirstSubPage(final String pageName) {
        subPageIndex.moveToFirstSubPage(pageName);
    }

    public void moveToLastSubPage(final String pageName) {
        if (!subPageIndex.isSubPageListReflowComplete(pageName)) {
            waitUntilSubPagesReady(pageName);
        }
        subPageIndex.moveToLastSubPage(pageName);
    }

    public void previousSubPage(final String pageName) {
        subPageIndex.previousSubPage(pageName);
    }

    public void nextSubPage(final String pageName) {
        if (!subPageIndex.isSubPageListReflowComplete(pageName)) {
            waitUntilSubPagesReady(pageName);
        }
        subPageIndex.nextSubPage(pageName);
    }

    public void moveToSubSPage(final String pageName, final int index) {
        if (!subPageIndex.isSubPageListReflowComplete(pageName)) {
            waitUntilSubPagesReady(pageName);
        }
        subPageIndex.moveToSubPage(pageName, index);
    }

    public Bitmap getSubPageBitmap(final String pageName, final int subPage) {
        if (isSubPageBitmapInCache(pageName, subPage)) {
            return getSubPageBitmapFromCache(pageName, subPage);
        }
        waitUntilSubPagesReady(pageName);
        return getReflowedSubPage(pageName, subPage);
    }

    public boolean isSubPageReady(final String pageName, final int subPage) {
        return ImageUtils.isPageReflowed(pageName) || isSubPageBitmapInCache(pageName, subPage);
    }

    public String getSubPageKey(final String pageName, final int subPage) {
        return getKeyOfSubPage(documentMd5, settings, pageName, subPage);
    }

    private void loadSubPageIndex() {
        File file = subPageIndexFilePath(cacheRoot, documentMd5, settings, INDEX_FILE_NAME);
        subPageIndex = ReflowedSubPageIndex.load(file);
    }

    private static final String getKeyOfSubPage(final String documentMd5, final ImageReflowSettings settings, final String pageName, int index) {
        return String.format("%s-%s-%d", md5OfDocumentWithSettings(documentMd5, settings), pageName, index);
    }

    private static final String md5OfDocumentWithSettings(final String documentMd5, final ImageReflowSettings settings) {
        return HashUtils.md5(documentMd5 + "-" + settings.md5());
    }

    private Bitmap getReflowedSubPage(final String pageName, final int subPage) {
        int[] size = new int[2];
        if (!ImageUtils.getReflowedPageSize(pageName, size)) {
            return null;
        }
        int top = settings.dev_height * subPage;
        int bottom = Math.min(top + settings.dev_height, size[1]);
        Bitmap bitmap = Bitmap.createBitmap(settings.dev_width, settings.dev_height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        if (!ImageUtils.renderReflowedPage(pageName, 0, top, settings.dev_width, bottom, bitmap)) {
            bitmap.recycle();
            return null;
        }

        return bitmap;
    }

    private boolean isSubPageBitmapInCache(final String pageName, final int subPage) {
        return subPageCache.contains(getSubPageKey(pageName, subPage));
    }

    private void saveSubPagesToCache(final String pageName) {
        int pageCount = subPageIndex.getSubPageCount(pageName);
        for (int i = 0; i < pageCount; i++) {
            Bitmap bitmap = getSubPageBitmap(pageName, i);
            if (bitmap != null) {
                saveSubPageBitmapToCache(pageName, i, bitmap);
            }
        }
    }

    private void saveSubPageBitmapToCache(String pageName, int subPage, Bitmap bitmap) {
        final String pageKey = getSubPageKey(pageName, subPage);
        subPageCache.putCache(pageKey, bitmap);
    }

    private Bitmap getSubPageBitmapFromCache(final String pageName, final int subPage) {
        return subPageCache.get(getSubPageKey(pageName, subPage));
    }

    private static File subPageIndexFilePath(final File root, final String documentMd5, final ImageReflowSettings settings, final String fileName) {
        if (settings == null || root == null) {
            return null;
        }
        File file = new File(root, md5OfDocumentWithSettings(documentMd5, settings) + "-" + fileName);
        return file;
    }
}
