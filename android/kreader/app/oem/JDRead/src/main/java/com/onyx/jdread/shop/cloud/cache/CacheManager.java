package com.onyx.jdread.shop.cloud.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.main.common.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created on 2017/04/21.
 *
 * @author hehai
 */

public final class CacheManager {

    public static final String TAG = "CacheManager";

    //max cache size 10mb
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 10;

    private static final int DISK_CACHE_INDEX = 0;

    private static final String CACHE_DIR = "responses";

    private DiskLruCache mDiskLruCache;

    private volatile static CacheManager mCacheManager;
    private static final int BUFFER = 1024;

    public static CacheManager getInstance() {
        if (mCacheManager == null) {
            synchronized (CacheManager.class) {
                if (mCacheManager == null) {
                    mCacheManager = new CacheManager();
                }
            }
        }
        return mCacheManager;
    }

    private CacheManager() {
        File diskCacheDir = getDiskCacheDir(JDReadApplication.getInstance(), CACHE_DIR);
        if (!diskCacheDir.exists()) {
            boolean b = diskCacheDir.mkdirs();
            Log.d(TAG, "!diskCacheDir.exists() --- diskCacheDir.mkdirs()=" + b);
        }
        if (diskCacheDir.getUsableSpace() > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir,
                        getAppVersion(JDReadApplication.getInstance()), 1/*How many value the key corresponds to*/, DISK_CACHE_SIZE);
                Log.d(TAG, "mDiskLruCache created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Synchronize set cache
     */
    public void putCache(String key, String value) {
        if (mDiskLruCache == null) return;
        OutputStream os = null;
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(encryptMD5(key));
            os = editor.newOutputStream(DISK_CACHE_INDEX);
            os.write(value.getBytes());
            os.flush();
            editor.commit();
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Asynchronous set cache
     */
    public void setCache(final String key, final String value) {
        new Thread() {
            @Override
            public void run() {
                putCache(key, value);
            }
        }.start();
    }

    /**
     * Synchronize access cache
     */
    public String getCache(String key) {
        if (mDiskLruCache == null) {
            return null;
        }
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            String s = encryptMD5(key);
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(s);
            if (snapshot != null) {
                fis = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                bos = new ByteArrayOutputStream();
                byte[] buf = new byte[BUFFER];
                int len;
                while ((len = fis.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                byte[] data = bos.toByteArray();
                return new String(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Asynchronous access cache
     */
    public void getCache(final String key, final CacheCallback callback) {
        new Thread() {
            @Override
            public void run() {
                String cache = getCache(key);
                callback.onGetCache(cache);
            }
        }.start();
    }

    /**
     * remove cache
     */
    public boolean removeCache(String key) {
        if (mDiskLruCache != null) {
            try {
                return mDiskLruCache.remove(encryptMD5(key));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * get disk cache directory
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * url md5
     */
    public static String encryptMD5(String string) {
        try {
            byte[] hash = MessageDigest.getInstance(Constants.MD_5).digest(
                    string.getBytes(Constants.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }

    /**
     * get app version
     */
    private int getAppVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi == null ? 0 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
}