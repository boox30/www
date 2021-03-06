package com.onyx.android.sdk.data.request.cloud;

import com.onyx.android.sdk.data.CloudManager;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.android.sdk.utils.StringUtils;
import java.io.File;

public class ParseDataRequest extends BaseCloudRequest {
    private static final String TAG = ParseDataRequest.class.getSimpleName();
    private File localFile;
    private String url;
    private String targetMd5;
    private String md5Calculated;

    public ParseDataRequest(final File file, final String urlString, final String md5) {
        localFile = file;
        url = urlString;
        targetMd5 = md5;
    }

    public boolean isValid() {
        if (StringUtils.isNullOrEmpty(md5Calculated)) {
            return false;
        }
        if (StringUtils.isNotBlank(targetMd5)) {
            return md5Calculated.equalsIgnoreCase(targetMd5);
        }
        return false;
    }

    @Override
    public void execute(final CloudManager parent) throws Exception {
        if (localFile.exists()) {
            md5Calculated = FileUtils.computeMD5(localFile);
        }
    }
}
