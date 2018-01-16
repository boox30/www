package com.onyx.jdread.shop.common;

import com.onyx.jdread.main.common.AppBaseInfo;
import com.onyx.jdread.personal.cloud.entity.jdbean.BoughtBookResultBean;
import com.onyx.jdread.personal.cloud.entity.jdbean.GetOrderUrlResultBean;
import com.onyx.jdread.personal.cloud.entity.jdbean.ReadOverInfoBean;
import com.onyx.jdread.personal.cloud.entity.jdbean.ReadTotalInfoBean;
import com.onyx.jdread.personal.cloud.entity.jdbean.ReadUnlimitedResultBean;
import com.onyx.jdread.personal.cloud.entity.jdbean.SaltResultBean;
import com.onyx.jdread.personal.cloud.entity.jdbean.SyncLoginInfoBean;
import com.onyx.jdread.personal.cloud.entity.jdbean.UserInfoBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.AddBookToSmoothCardBookBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.AddOrDelFromCartBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookCartItemBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookCommentsResultBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookDetailResultBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookDownloadUrlResultBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookModelBooksResultBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookModelConfigResultBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.CategoryListResultBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.CertBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.RecommendListResultBean;
import com.onyx.jdread.shop.cloud.entity.jdbean.ShoppingCartBookIdsBean;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by li on 2018/1/9.
 */

public interface ReadContentService {
    @POST("client.action")
    Call<AddBookToSmoothCardBookBean> addBookToSmoothCardBook(@Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                                                              @Query(AppBaseInfo.BODY_KEY) String body,
                                                              @QueryMap Map<String, String> map);

    @POST("client.action")
    Call<AddOrDelFromCartBean> addOrDeleteFromCart(@Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                                                   @Query(AppBaseInfo.BODY_KEY) String body,
                                                   @QueryMap Map<String, String> map);

    @POST("client.action")
    Call<CertBean> getBookCert(@Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                               @QueryMap Map<String, String> map,
                               @Query(AppBaseInfo.BODY_KEY) String body);

    @POST("client.action")
    Call<BookDownloadUrlResultBean> getBookDownloadUrl(@QueryMap Map<String, String> map,
                                                       @Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                                                       @Query(AppBaseInfo.BODY_KEY) String body);

    @POST("client.action")
    Call<BoughtBookResultBean> getBoughtBook(@Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                                             @Query(AppBaseInfo.BODY_KEY) String body,
                                             @QueryMap Map<String, String> map);

    @GET("search")
    Call<BookModelBooksResultBean> getSearchBooks(@QueryMap Map<String, String> baseInfoMap);

    @GET("category")
    Call<CategoryListResultBean> getCategoryList(@QueryMap Map<String, String> baseInfoMap);

    @POST("client.action")
    Call<GetOrderUrlResultBean> getOrderUrl(@Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                                            @Query(AppBaseInfo.BODY_KEY) String body,
                                            @QueryMap Map<String, String> map);

    @GET("mark_books/stat")
    Call<ReadOverInfoBean> getReadOverBook(@Query(AppBaseInfo.JD_USER_NAME) String userName,
                                           @QueryMap Map<String, String> map);

    @POST("client.action")
    Call<ReadTotalInfoBean> getReadTotalBook(@Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                                             @Query(AppBaseInfo.JD_USER_NAME) String userName,
                                             @QueryMap Map<String, String> map);

    @POST("client.action")
    Call<ReadUnlimitedResultBean> getUnlimited(@Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                                               @Query(AppBaseInfo.BODY_KEY) String body,
                                               @QueryMap Map<String, String> map);

    @POST("client.action")
    Call<ShoppingCartBookIdsBean> getCartBookIds(@Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                                                 @Query(AppBaseInfo.BODY_KEY) String body,
                                                 @QueryMap Map<String, String> map);

    @POST("client.action")
    Call<BookCartItemBean> getBookCartItem(@Query(CloudApiContext.NewBookDetail.FUNCTION_ID) String functionID,
                                           @Query(AppBaseInfo.BODY_KEY) String body,
                                           @QueryMap Map<String, String> map);

    @GET("user/sync")
    Call<SyncLoginInfoBean> getSyncLoginInfo(@QueryMap Map<String, String> map);


    @GET("user")
    Call<UserInfoBean> getUserInfo(@QueryMap Map<String, String> map);

    @GET("now")
    Call<SaltResultBean> getSalt(@QueryMap Map<String, String> map);

    @GET("module/{f_type}/{module_id}")
    Call<BookModelBooksResultBean> getBookShopModule(@Path("f_type") int fType,
                                                     @Path("module_id") int moduleId,
                                                     @QueryMap Map<String, String> baseInfoMap);

    @GET("channel/{cid}")
    Call<BookModelConfigResultBean> getShopMainConfig(@Path("cid") int cid,
                                                      @QueryMap Map<String, String> baseInfoMap);

    @GET("rank/modules")
    Call<BookModelConfigResultBean> getBookRank(@QueryMap Map<String, String> baseInfoMap);

    @GET("ebook/{bookId}")
    Call<BookDetailResultBean> getBookDetail(@Path("bookId") long bookId,
                                             @QueryMap Map<String, String> baseInfoMap);

    @GET("ebook/{bookId}/comment")
    Call<BookCommentsResultBean> getBookCommentsList(@Path("bookId") long bookId,
                                                     @QueryMap Map<String, String> baseInfoMap);

    @GET("ebook/{bookId}/recommend")
    Call<RecommendListResultBean> getRecommendList(@Path("bookId") long bookId,
                                                   @QueryMap Map<String, String> baseInfoMap);
}
