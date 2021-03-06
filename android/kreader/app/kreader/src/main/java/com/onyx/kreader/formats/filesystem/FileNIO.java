package com.onyx.kreader.formats.filesystem;

import com.onyx.android.sdk.utils.FileUtils;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by zengzhu on 2/28/16.
 */
public class FileNIO {

    private String path;
    private RandomAccessFile randomAccessFile = null;
    private FileChannel readChannel;


    public FileNIO(final String p) {
        path = p;
    }

    public boolean open() {
        close();
        try {
            randomAccessFile = new RandomAccessFile(path, "r");
            readChannel = randomAccessFile.getChannel();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int read(final ByteBuffer data) {
        try {
            return readChannel.read(data);
        } catch (Exception e) {
            return -1;
        }
    }

    public void close() {
        if (readChannel != null && readChannel.isOpen()) {
            FileUtils.closeQuietly(readChannel);
        }
        if (randomAccessFile != null) {
            FileUtils.closeQuietly(randomAccessFile);
        }
        randomAccessFile = null;
        readChannel = null;
    }
}
