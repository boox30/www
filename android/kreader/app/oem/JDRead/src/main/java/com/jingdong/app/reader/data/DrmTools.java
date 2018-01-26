package com.jingdong.app.reader.data;

import android.content.Context;

public class DrmTools {
    static {
        System.loadLibrary("jdrebr");
    }

    /**
     * 根据UUID生成DeviceID
     * @param     Para01 - 设备UUID
     * @return    DeviceID字符串
     */
    public static native String API01(String Para01);

    /**
     * 解密多媒体文件
     * @param     Para01 - 授权文件内容字符串
     * @param     Para02 - DeviceID字符串
     * @param     Para02 - 用户随机数
     * @param     Para03 - 密文文件的全路径文件名
     * @param     Para04 - 解密文件的全路径文件名
     * @return    解密是否成功
     */
    public static native boolean API02(String Para01, String Para02, String Para03, String Para04, String Para05);

    public static boolean decryptApk(String cert, String random, String oldPath, String newPath,Context context) {
        return API02(cert, hashDevicesInfo(context), random, oldPath, newPath);
    }

    public static String hashDevicesInfo(Context context) {
        String device = CommonUtil.getDeviceId(context) + "ibyxt270";
        return API01(device);
    }

    public static String getHardwareId(String uuid) {
        return API01(uuid);
    }
}