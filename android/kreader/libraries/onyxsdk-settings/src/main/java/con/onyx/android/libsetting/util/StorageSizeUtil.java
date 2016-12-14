/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package con.onyx.android.libsetting.util;

import android.os.Build;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StorageSizeUtil {

    private static final String TAG = StorageSizeUtil.class.getSimpleName();

    /**
     * @return total storage amount in bytes
     */
    public static long getTotalStorageAmount() {
        List<String> pathList = new ArrayList<String>();
        pathList.add("/system");
        pathList.add("/data");
        pathList.add("/cache");
        pathList.add("/mnt/sdcard");
        int len = pathList.size();
        long total = 0;
        for (int i = 0; i < len; i++) {
            Log.i(TAG, "path," + i + ": " + pathList.get(i));
            StatFs stat = new StatFs(pathList.get(i));
            long bytesAvailable;
            if (CommonUtil.apiLevelCheck(Build.VERSION_CODES.JELLY_BEAN_MR2)) {
                bytesAvailable = stat.getBlockSizeLong() * stat.getBlockCountLong();
            } else {
                bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
            }
            total += bytesAvailable;
        }
        return total;
    }

    public static long getStorageAmountForPartitions(List<String> pathList) {
        int len = pathList.size();
        long total = 0;
        for (int i = 0; i < len; i++) {
            StatFs stat = new StatFs(pathList.get(i));
            long bytesAvailable;
            if (CommonUtil.apiLevelCheck(Build.VERSION_CODES.JELLY_BEAN_MR2)) {
                bytesAvailable = stat.getBlockSizeLong() * stat.getBlockCountLong();
            } else {
                bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
            }
            total += bytesAvailable;
        }
        return total;
    }

    public static long getInternalStorageAmount() {
        List<String> list = new ArrayList<String>(1);
        list.add("/mnt/sdcard");
        return getStorageAmountForPartitions(list);
    }

    public static long getExtsdStorageAmount() {
        List<String> list = new ArrayList<String>(1);
        list.add("/mnt/extsd");
        return getStorageAmountForPartitions(list);
    }

    public static long convertBytesToMB(long bytes) {
        return bytes / (1024 * 1024);
    }

    /**
     * @return total storage amount in mega bytes (base on 1024)
     */
    public static long getTotalStorageAmountInMB() {
        return convertBytesToMB(getTotalStorageAmount());
    }

    /**
     * @return the amount with GB only for display that targeting user (base on 1000)
     */
    public static long getDisplayGBForUser(double bytes) {
        return Math.round(bytes / 1000 / 1000 / 1000);
    }

    /**
     * @return total storage amount in giga bytes with correction (3.5 -> 4, 7.6 -> 8)
     */
    public static long getTotalStorageAmountInGB() {
        return getDisplayGBForUser(getTotalStorageAmount());
    }

    /**
     * @return total available storage amount in bytes
     */
    public static long getTotalFreeBytes() {
        long availableAmount = 0;
        String[] paths = {"/system", "/data", "/cache", "/mnt/sdcard"};
        int size = paths.length;
        for (String p : paths) {
            StatFs path = new StatFs(p);
            if (CommonUtil.apiLevelCheck(Build.VERSION_CODES.JELLY_BEAN_MR2)) {
                availableAmount += path.getFreeBlocksLong() * path.getBlockSizeLong();
            } else {
                availableAmount += (long) path.getFreeBlocks() * (long) path.getBlockSize();
            }
        }
        return availableAmount;
    }

    public static long getSDCardFreeBytes() {
        return getFreeBytes("/mnt/sdcard");
    }

    public static long getFreeBytes(String path) {
        long amount = 0;
        if (!new File(path).exists()) {
            return amount;
        }
        StatFs sdPath = new StatFs(path);

        Log.i(TAG, "blocks: " + sdPath.getFreeBlocks());
        Log.i(TAG, "block size: " + sdPath.getBlockSize());
        if (CommonUtil.apiLevelCheck(Build.VERSION_CODES.JELLY_BEAN_MR2)){
            amount =  sdPath.getFreeBlocksLong() *  sdPath.getBlockSizeLong();
        }else {
            amount = (long) sdPath.getFreeBlocks() * (long) sdPath.getBlockSize();
        }
        Log.i(TAG, "amount: " + amount / 1024 / 1024 + "MB");
        return amount;
    }

    public static long getExtsdFreeBytes() {
        return getFreeBytes("/mnt/extsd");
    }

    /**
     * @return get the ratio of free storage amount
     */
    public static int getFreeInternalStorageRatio() {
        return calculateRatio(getInternalStorageAmount(), getSDCardFreeBytes());
    }

    public static int calculateRatio(long total, long free) {
        Log.i(TAG, "total: " + total / 1024 / 1024);
        Log.i(TAG, "free: " + free / 1024 / 1024);
        if (total > 0) {
            return (int) (100 * free / total);
        }
        return 100;
    }

    public static int getFreeExternalStorageRatio() {
        return calculateRatio(getExtsdStorageAmount(), getExtsdFreeBytes());
    }

    public static BigDecimal getFreeStorageInGB() {
        BigDecimal d = new BigDecimal((double) StorageSizeUtil.getSDCardFreeBytes() / (double) (1024 * 1024 * 1024));
        d = d.setScale(2, BigDecimal.ROUND_HALF_UP);
        return d;
    }

}
