package com.onyx.android.sample.utils;

import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.android.sdk.utils.TestUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by wangxu on 31/10/2017.
 *
 * openssl AES 128 CFB mode cmd on linux:
 *
 * encrypt：
 * openssl enc -aes-128-cfb -in update.zip -out encrypt.zip -K 50E1723DC328D98F133E321FC2908B78 -iv 1528E9AD498FF118AB7ECB3025AD0DC6 -p
 * output information:
 * salt=5C0000006E000000
 * key=50E1723DC328D98F133E321FC2908B78
 * iv =1528E9AD498FF118AB7ECB3025AD0DC6
 *
 * decrypt：
 * openssl enc -aes-128-cfb -d -in encrypt.zip -out new.zip -K 50E1723DC328D98F133E321FC2908B78 -iv 1528E9AD498FF118AB7ECB3025AD0DC6 -p
 * output information:
 * salt=5C0000006E000000
 * key=50E1723DC328D98F133E321FC2908B78
 * iv =1528E9AD498FF118AB7ECB3025AD0DC6
 *
 * key and vi are variable, but need to remember them, decryption requires the key and vi
 */

public class AesDecryptUtil {
    private static final String ALGORITHM = "AES";
    private static final String AES_CBC_PADDING = "AES/CBC/PKCS5Padding";

    public static boolean decrypt(final String key, final String iv, final String encryptFilePath, final String newFilePath) {
        boolean result = false;
        File encryptFile = new File(encryptFilePath);
        if (!encryptFile.exists()) {
            return result;
        }
        FileOutputStream fos = null;
        FileInputStream fis = null;
        CipherInputStream cis = null;
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PADDING);
            final SecretKeySpec keySpec = new SecretKeySpec(hexToBytes(key), ALGORITHM);
            final IvParameterSpec ivSpec = new IvParameterSpec(hexToBytes(iv));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            fos = new FileOutputStream(new File(newFilePath));
            fis = new FileInputStream(encryptFile);
            cis = new CipherInputStream(fis, cipher);

            final int bufSize = 1024 * 8;
            int len = 0;
            byte[] buffer = new byte[bufSize];
            while (-1 != (len = cis.read(buffer, 0, bufSize))) {
                fos.write(buffer, 0, len);
            }
            result = true;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(fis);
            closeStream(cis);
            closeStream(fos);
        }
        return result;
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));

        return data;
    }

    public static String decryptFile(final String srcFile, final String newFile,
                                      final String key, final String initVector) throws IOException, NoSuchAlgorithmException {
        return decryptOrEncryptFile(srcFile, newFile, key, initVector, false);
    }

    public static String encryptFile(final String srcFile, final String newFile,
                                      final String key, final String initVector) throws IOException, NoSuchAlgorithmException {
        return decryptOrEncryptFile(srcFile, newFile, key, initVector, true);
    }

    public static String generateRandomString(final int len) {
        final String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int number = TestUtils.randInt(0, base.length() - 1);
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String decryptOrEncryptFile(final String srcFile, final String newFile,
                                      final String key, final String initVector, boolean encrypt) throws IOException, NoSuchAlgorithmException {
        if (StringUtils.isNullOrEmpty(srcFile) || StringUtils.isNullOrEmpty(newFile)
                || StringUtils.isNullOrEmpty(key) || StringUtils.isNullOrEmpty(initVector)) {
            return null;
        }
        if (!FileUtils.ensureFileExists(srcFile)) {
            return null;
        }
        if (key.length() < 32 || initVector.length() < 32) {
            return null;
        }
        final String[] cmd = getCmd(srcFile, newFile, key, initVector, encrypt);
        final String workDirectory = "/system/bin/";
        final String result = runShellCmd(cmd, workDirectory);
        if (StringUtils.isNullOrEmpty(result)) {
            return null;
        }
        if (!FileUtils.ensureFileExists(newFile)) {
            return null;
        }
        return FileUtils.computeMD5(new File(newFile));
    }

    public static String[] getCmd(final String srcFile, final String newFile,
                                  final String key, final String initVector, boolean encrypt) {
        if (encrypt) {
            return new String[] {"/system/bin/openssl", "-i",
                    srcFile, "-o", newFile, "-k", key, "-v", initVector};
        }
        return new String[] {"/system/bin/openssl", "-d", "-i",
                srcFile, "-o", newFile, "-k", key, "-v", initVector};
    }

    public static synchronized String runShellCmd(String[] cmd, String workDirectory) {
        StringBuffer result = new StringBuffer();
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            InputStream in = null;
            if (workDirectory != null) {
                builder.directory(new File(workDirectory));
                builder.redirectErrorStream(true);
                Process process = builder.start();

                in = process.getInputStream();
                byte[] re = new byte[1024];
                while (in.read(re) != -1) {
                    result = result.append(new String(re));
                }
            }
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }
}
