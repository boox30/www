package com.onyx.kreader.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.jakewharton.disklrucache.DiskLruCache;
import com.onyx.kreader.common.Benchmark;
import com.onyx.kreader.utils.FileUtils;

import java.io.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Joy on 2016/5/5.
 */
public class BitmapLruCache implements Closeable {

    public static class Builder {
        private static final int MEGABYTE = 1024 * 1024;
        private static final float DEFAULT_MEMORY_CACHE_HEAP_RATIO = 1f / 4f;
        private static final float MAX_MEMORY_CACHE_HEAP_RATIO = 0.75f;
        private static final int DEFAULT_DISK_CACHE_MAX_SIZE_MB = 100;
        private static final int DEFAULT_MEM_CACHE_MAX_SIZE_MB = 30;

        private boolean memoryCacheEnabled;
        private int memoryCacheMaxSize;
        private boolean diskCacheEnabled;
        private long diskCacheMaxSize;
        private File diskCacheLocation;

        public Builder() {
            // Memory Cache is enabled by default, with a small maximum size
            memoryCacheEnabled = true;
            memoryCacheMaxSize = DEFAULT_MEM_CACHE_MAX_SIZE_MB * MEGABYTE;

            // Disk Cache is disabled by default, but it's default size is set
            diskCacheMaxSize = DEFAULT_DISK_CACHE_MAX_SIZE_MB * MEGABYTE;
        }

        public BitmapLruCache build() {
            final BitmapLruCache cache = new BitmapLruCache();
            if (isValidOptionsForMemoryCache()) {
                cache.setMemoryCache(new BitmapMemoryLruCache(memoryCacheMaxSize));
            }
            if (isValidOptionsForDiskCache()) {
                try {
                    DiskLruCache diskCache = DiskLruCache.open(diskCacheLocation, 0, 1, diskCacheMaxSize);
                    cache.setDiskCache(diskCache);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return cache;
        }

        public Builder setMemoryCacheEnabled(boolean enabled) {
            memoryCacheEnabled = enabled;
            return this;
        }

        public Builder setMemoryCacheMaxSize(int maxSize) {
            memoryCacheMaxSize = maxSize;
            return this;
        }

        public Builder setMemoryCacheMaxSizeUsingHeapSize() {
            return setMemoryCacheMaxSizeUsingHeapSize(DEFAULT_MEMORY_CACHE_HEAP_RATIO);
        }

        public Builder setMemoryCacheMaxSizeUsingHeapSize(float percentageOfHeap) {
            int size = Math.round(getHeapSize() * Math.min(percentageOfHeap, MAX_MEMORY_CACHE_HEAP_RATIO));
            return setMemoryCacheMaxSize(size);
        }

        public Builder setDiskCacheEnabled(boolean enabled) {
            diskCacheEnabled = enabled;
            return this;
        }

        public Builder setDiskCacheMaxSize(long maxSize) {
            diskCacheMaxSize = maxSize;
            return this;
        }

        public Builder setDiskCacheLocation(File location) {
            this.diskCacheLocation = location;
            return this;
        }

        private static long getHeapSize() {
            return Runtime.getRuntime().maxMemory();
        }

        private boolean isValidOptionsForMemoryCache() {
            return memoryCacheEnabled && memoryCacheMaxSize > 0;
        }

        private boolean isValidOptionsForDiskCache() {
            boolean valid = diskCacheEnabled;

            if (valid) {
                if (null == diskCacheLocation) {
                    valid = false;
                } else if (!diskCacheLocation.canWrite()) {
                    valid = false;
                }
            }

            return valid;
        }
    }

    private BitmapMemoryLruCache memoryCache;
    private DiskLruCache diskCache;

    private ExecutorService diskCacheWriteExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    });

    private void setMemoryCache(BitmapMemoryLruCache memoryCache) {
        this.memoryCache = memoryCache;
    }

    private void setDiskCache(DiskLruCache diskCache) {
        this.diskCache = diskCache;
    }

    public Bitmap get(String key) {
        return get(key, null);
    }

    public Bitmap get(String key, BitmapFactory.Options options) {
        Bitmap bitmap = getFromMemoryCache(key);
        if (bitmap != null) {
            return bitmap;
        }
        return getFromDiskCache(key, options);
    }

    public Bitmap getFromMemoryCache(final String key) {
        if (memoryCache == null) {
            return null;
        }

        Bitmap result;
        synchronized (memoryCache) {
            result = memoryCache.get(key);
            // If we get a value, but it has a invalid bitmap, remove it
            if (result != null && result.isRecycled()) {
                memoryCache.remove(key);
                result = null;
            }
        }

        return result;
    }

    public Bitmap getFromDiskCache(final String key, final BitmapFactory.Options options) {
        if (diskCache == null) {
            return null;
        }

        try {
            DiskLruCache.Snapshot snapshot = diskCache.get(key);
            if (snapshot == null) {
                return null;
            }
            try {
                InputStream is = snapshot.getInputStream(0);
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                    if (bitmap != null) {
                        if (memoryCache != null) {
                            memoryCache.put(key, bitmap);
                        }
                    } else {
                        diskCache.remove(key);
                        diskCache.flush();
                    }
                } finally {
                    FileUtils.closeQuietly(is);
                }
            } finally {
                snapshot.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Bitmap put(String key, Bitmap copy) {
        return put(key, copy, Bitmap.CompressFormat.PNG, 100);
    }

    public Bitmap put(final String key, final Bitmap bitmap,
                      Bitmap.CompressFormat compressFormat, int compressQuality) {
        putMemoryCache(key, bitmap);
        putDiskCache(key, bitmap, compressFormat, compressQuality);
        return bitmap;
    }

    @Override
    public void close() throws IOException {
        if (memoryCache != null) {
            memoryCache.evictAll();
        }
        if (diskCache != null) {
            diskCache.close();
        }
    }

    private void putMemoryCache(final String key, final Bitmap bitmap) {
        Benchmark benchmark = new Benchmark();
        if (memoryCache != null) {
            memoryCache.put(key, bitmap);
        }
        benchmark.report("put memory cache");
    }

    private void putDiskCache(final String key, final Bitmap bitmap,
                              final Bitmap.CompressFormat compressFormat,
                              final int compressQuality) {
        if (diskCache == null) {
            return;
        }

        diskCacheWriteExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Benchmark benchmark = new Benchmark();
                    DiskLruCache.Editor editor = diskCache.edit(key);
                    OutputStream os = editor.newOutputStream(0);
                    try {
                        bitmap.compress(compressFormat, compressQuality, os);
                        benchmark.report("write disk cache");
                        os.flush();
                        benchmark.report("flush disk cache");
                        editor.commit();
                        diskCache.flush();
                    } finally {
                        FileUtils.closeQuietly(os);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
