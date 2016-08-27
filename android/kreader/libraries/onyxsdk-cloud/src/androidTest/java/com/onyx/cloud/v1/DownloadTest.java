package com.onyx.cloud.v1;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Looper;
import android.test.ApplicationTestCase;
import android.util.Log;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.cloud.CloudManager;
import com.onyx.cloud.Constant;
import com.onyx.cloud.OnyxDownloadManager;
import com.onyx.cloud.service.v1.OnyxFileDownloadService;
import com.onyx.cloud.service.v1.ServiceFactory;
import com.onyx.cloud.store.request.CloudFileDownloadRequest;
import com.onyx.cloud.store.request.ParseCoverRequest;

import java.io.File;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;


/**
 * Created by suicheng on 2016/8/16.
 */
public class DownloadTest extends ApplicationTestCase<Application> {

    //2.81M
    private static final String TEST_URL = "http://7xjww9.com1.z0.glb.clouddn.com/Hopetoun_falls.jpg";

    private static final long TEST_FILE_SIZE = 2800000;

    public DownloadTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        FlowConfig.Builder builder = new FlowConfig.Builder(getContext());
        FlowManager.init(builder.build());
    }

    public void testDownloadService() throws Exception {
        OnyxFileDownloadService service = ServiceFactory.getFileDownloadService(Constant.CN_API_BASE);
        Call<ResponseBody> call = service.fileDownload(TEST_URL);
        Response<ResponseBody> response = call.execute();
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertTrue(response.body().contentLength() > TEST_FILE_SIZE);
    }

    private static String getTestFilePath() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(dir, "testFileDownloader.jpg");
        return file.getAbsolutePath();
    }

    public void testCloudFileRequest() throws Throwable {
        final Context context = getApplication().getApplicationContext();
        assertNotNull(context);
        final CountDownLatch latch = new CountDownLatch(1);
        final String tag = UUID.randomUUID().toString();
        final OnyxDownloadManager fileDownloadManager = OnyxDownloadManager.getInstance(context);
        final CloudFileDownloadRequest fileDownloadRequest = new CloudFileDownloadRequest(TEST_URL, getTestFilePath(), tag);
        fileDownloadManager.download(fileDownloadRequest, new BaseCallback() {
            @Override
            public void progress(BaseRequest request, ProgressInfo info) {
                assertTrue(Looper.getMainLooper().getThread() == Thread.currentThread());
            }

            @Override
            public void done(BaseRequest request, Throwable e) {
                assertTrue(Looper.getMainLooper().getThread() == Thread.currentThread());
                assertNull(e);
                onFileDownloadFinished(context, fileDownloadRequest.getPath(), latch);
            }
        });
        waitLatch(latch);
    }

    private void onFileDownloadFinished(Context context, String path, final CountDownLatch latch) {
        CloudManager cloudManager = new CloudManager();
        final ParseCoverRequest coverRequest = new ParseCoverRequest(path);
        cloudManager.submitRequest(context, coverRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                Bitmap bitmap = coverRequest.getBitmap();
                assertNotNull(bitmap);
                latch.countDown();
            }
        });
    }

    private void waitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}