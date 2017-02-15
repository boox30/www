package com.onyx.android.sdk.data.request.data;

import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.utils.FileUtils;

import java.io.File;

/**
 * Created by ming on 2017/2/8.
 */

public class GetFileMd5Request extends BaseDataRequest {

    private String filePath;
    private String md5;

    public GetFileMd5Request(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void execute(DataManager dataManager) throws Exception {
        File file = new File(filePath);
        md5 = FileUtils.getFileMD5(file);
    }

    public String getMd5() {
        return md5;
    }
}
