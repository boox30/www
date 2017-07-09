package com.onyx.android.sdk.data.v2;

import com.onyx.android.sdk.data.Constant;
import com.onyx.android.sdk.data.model.v2.AdminApplyModel;
import com.onyx.android.sdk.data.model.v2.CloudGroup;
import com.onyx.android.sdk.data.model.v2.Course;
import com.onyx.android.sdk.data.model.v2.DeviceBind;
import com.onyx.android.sdk.data.model.v2.GroupUserInfo;
import com.onyx.android.sdk.data.model.v2.IndexService;
import com.onyx.android.sdk.data.model.v2.AuthToken;
import com.onyx.android.sdk.data.model.v2.CloudLibrary;
import com.onyx.android.sdk.data.model.v2.CloudMetadata;
import com.onyx.android.sdk.data.model.v2.BaseAuthAccount;
import com.onyx.android.sdk.data.model.ProductResult;
import com.onyx.android.sdk.data.model.v2.NeoAccountBase;
import com.onyx.android.sdk.data.model.v2.UserInfoBind;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by suicheng on 2017/4/26.
 */

public interface ContentService {
    String CONTENT_AUTH_PREFIX = "Bearer ";

    @GET("devices/findByMac")
    Call<IndexService> getIndexService(@Query("mac") final String macAddress,
                                       @Query("installationId") final String installationId);

    @POST("auth/local")
    Call<AuthToken> getAccountToken(@Body final BaseAuthAccount account);

    @GET("users/me")
    Call<ResponseBody> getAccount();

    @GET("courses/my")
    Call<List<Course>> getMyCourses();

    @GET("librarys/my")
    Call<List<CloudLibrary>> loadLibraryList();

    @GET("librarys/books")
    Call<ProductResult<CloudMetadata>> loadBookList(@Query(Constant.WHERE_TAG) final String param);

    @GET("librarys/{id}/books")
    Call<ProductResult<CloudMetadata>> loadBookList(@Path(Constant.ID_TAG) final String libraryId,
                                                    @Query(Constant.WHERE_TAG) final String param);

    @GET("books/{id}")
    Call<CloudMetadata> loadBook(@Path(Constant.ID_TAG) final String idString);

    @POST("groups/{id}/createUserByDevices")
    Call<NeoAccountBase> createUserByDevice(@Path(Constant.ID_TAG) final String groupId,
                                            @Body final DeviceBind deviceBind);

    @GET("userinfos/getByGroup")
    Call<List<UserInfoBind>> getDeviceBindList(@Query("groupId") final String groupId);

    @GET("groups/{id}/groupusers")
    Call<List<NeoAccountBase>> getGroupUserList(@Path(Constant.ID_TAG) final String groupId);

    @GET("groups/{id}")
    Call<CloudGroup> getGroupList(@Path(Constant.ID_TAG) final String parentGroupId);

    @GET("groups//recursive")
    Call<List<CloudGroup>> getRecursiveGroupList();

    @POST("adusers")
    Call<ResponseBody> applyAdminRequest(@Body final AdminApplyModel applyModel);

    @GET("adusers/findByMac")
    Call<IndexService> getAdminIndexService(@Query("mac") final String macAddress);

    @GET("users/findByDeviceMac")
    Call<GroupUserInfo> getGroupUserInfo(@Query("mac") final String macAddress);
}
