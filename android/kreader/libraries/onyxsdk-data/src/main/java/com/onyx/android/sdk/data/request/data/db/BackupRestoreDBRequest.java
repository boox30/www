package com.onyx.android.sdk.data.request.data.db;

import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.data.request.data.BaseDataRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * Created by ming on 2017/4/21.
 */

public class BackupRestoreDBRequest extends BaseDataRequest {

    private Map<String, String> backupRestoreDBMap;
    private boolean backup = false;

    public BackupRestoreDBRequest(Map<String, String> backupRestoreDBMap, boolean backup) {
        this.backupRestoreDBMap = backupRestoreDBMap;
        this.backup = backup;
    }

    @Override
    public void execute(DataManager dataManager) throws Exception {
        if (backupRestoreDBMap == null || backupRestoreDBMap.size() == 0) {
            return;
        }
        for (Map.Entry<String, String> entry : backupRestoreDBMap.entrySet()) {
            String originDBPath = entry.getKey();
            String backupDBPath = entry.getValue();
            transferDB(originDBPath, backupDBPath, backup);
        }
    }

    private void transferDB(final String originDBPath, final String backupDBPath, final boolean backup) throws Exception {
        File originDB = new File(originDBPath);
        File backupDB = new File(backupDBPath);

        FileChannel src;
        FileChannel dst;
        if (backup) {
            src = new FileInputStream(originDB).getChannel();
            dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
        }else {
            src = new FileOutputStream(originDB).getChannel();
            dst = new FileInputStream(backupDB).getChannel();
            dst.transferTo(0, dst.size(), src);
        }

        src.close();
        dst.close();
    }
}
