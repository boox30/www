package com.onyx.jdread.shop.common;

import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.main.common.ClientUtils;
import com.onyx.jdread.main.common.Constants;
import com.onyx.jdread.shop.cloud.cache.EnhancedCacheInterceptor;
import com.onyx.jdread.shop.request.JavaNetCookieJar;
import com.onyx.jdread.shop.request.PersistentCookieStore;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by huxiaomao on 2016/12/2.
 */

public class CloudApiContext {
    public static final String JD_BOOK_SHOP_URL = "https://eink-api.jd.com/eink/api/";
    public static final String JD_BASE_URL = "https://gw-e.jd.com/";
    public static final String JD_BOOK_VERIFY_URL = "http://rights.e.jd.com/";
    public static final String JD_SMOOTH_READ_URL = "https://cread.jd.com/";
    public static final String JD_BOOK_ORDER_URL = "https://order-e.jd.com/";
    public static final String JD_BOOK_STATISTIC_URL = "https://sns-e.jd.com/";
    public static final String JD_BOOK_BASE_URI = "/eink/api/";

    public static class User {
        public static final String SYNC_INFO = "user/sync";
        public static final String GET_USER_INFO = "user";
        public static final String READ_PREFERENCE = "user/features";
        public static final String SIGN_CHECK = "sign/check";
        public static final String SIGN = "sign";
        public static final String READING_VOUCHER = "reading/voucher";
        public static final String USER_GIFT = "gift";
        public static final String RECOMMEND_USER = "recommend/user";
    }

    public static class ReadBean {
        public static final String RECHARGE_PACKAGE = "recharge/package";
        public static final String RECHARGE = "recharge";
        public static final String RECHARGE_STATUS = "recharge/staus";
        public static final String CONSUME_RECORD = "yuedou/consum";
        public static final String READ_BEAN_RECORD = "yuedou/recharge";
    }

    public static class NewBookDetail {
        public static final String BOOK_SPECIAL_PRICE_TYPE = "specialPrice";
        public static final String FUNCTION_ID = "functionId";
        public static final String API_NEW_BOOK_DETAIL = "newBookDetail";
        public static final String DETAIL = "detail";
        public static final String TYPE = "type";
        public static final String BOOK_LIST = "bookList";
        public static final String BOOK_ID = "bookId";
        public static final String NEW_BOOK_REVIEW = "newBookReview";
        public static final String ADD_BOOK_COMMENT = "addBookComment";
        public static final String ADD_BOOK_TO_SMOOTH_CARD = "addNewReadInfo";
        public static final String ADD_BOOKS_TO_SMOOTH_CARD = "addNewReadInfoBatch";
        public static final String SHOPPING_CART = "shoppingCart";
        public static final String USER_BASIC_INFO = "userBasicInfo";
        public static final String SYNC_LOGIN_INFO = "SyncLoginInfo";
        public static final String GET_TOKEN = "genToken";
        public static final String READ_TOTAL_BOOK = "userReadEBookScale";
        public static final String NEW_BOUGHT_BOOK_ORDER = "newBuyedEbookOrderList";
    }

    public static class BookShopURI {
        public static final String SHOP_MAIN_CONFIG_URI = "channel/%s";
        public static final String CATEGORY_URI = "category";
        public static final String SEARCH_URI = "search";
        public static final String BOOK_MODULE_URI = "module/%1s/%2s";
        public static final String BOOK_DETAIL_URI = "ebook/%s";
        public static final String BOOK_RANK_URI = "rank/modules";
        public static final String BOOK_COMMENT_LIST_URI = "ebook/%s/comment";
        public static final String BOOK_RECOMMEND_LIST_URI = "ebook/%s/recommend";
        public static final String BOOK_RANK_LIST_URI = "rank/%1s/%2s";
    }

    public static class AddToSmooth {
        public static final String EBOOK_ID = "ebook_id";
        public static final String CURRENT_PAGE = "currentPage";
        public static final String PAGE_SIZE = "pageSize";
        public static final String SMOOTH_READ_BOOK_LIST = "myNewCardReadBook";
    }

    public static class CategoryLevel2BookList {
        public static final String SORT_TYPE = "sortType";
        public static final String PAGE_SIZE = "pageSize";
        public static final String CAT_ID = "catId";
        public static final String CURRENT_PAGE = "currentPage";
        public static final String SORT_KEY = "sortKey";
        public static final String CLIENT_PLATFORM = "clientPlatform";
        public static final String ROOT_ID = "rootId";
        public static final String CATEGORY_LEVEL2_BOOK_LIST = "categoryBookListV2";
        public static final String PAGE_SIZE_DEFAULT_VALUES = "40";
        public static final int SORT_KEY_DEFAULT_VALUES = SearchBook.SORT_KEY_SALES;
        public static final int SORT_TYPE_DEFAULT_VALUES = SearchBook.SORT_TYPE_DESC;
        public static final int CLIENT_PLATFORM_DEFAULT_VALUES = 1;
        public static final int ROOT_ID_DEFAULT_VALUES = 2;
        public static final int SORT_TYPE_HOT = 1;
        public static final int SORT_TYPE_SALES = 2;
        public static final int SORT_TYPE_NEWEST = 3;
    }

    public static class BookRankList {
        public static final String RANK_LIST_TIME_TYPE = "week";
    }

    public static class SearchBook {
        public static final String SEARCH_TYPE = "search_type";
        public static final String CATE_ID = "cid";
        public static final String FILTER = "filter";
        public static final String SORT = "sort";
        public static final String KEY_WORD = "key_word";
        public static final int SORT_TYPE_DESC = 1;
        public static final int SORT_TYPE_ASC = 2;
        public static final int SORT_KEY_SALES = 1;
        public static final int SORT_KEY_PRICE = 2;
        public static final int SORT_KEY_PRAISE = 3;
        public static final int SORT_KEY_TIME = 4;
        public static final String SEARCH_TYPE_BOOK_SHOP = "1";
        public static final String SEARCH_TYPE_BOOK_COMMUNITY = "2";
        public static final String PAGE_SIZE = "page_size";
        public static final String CURRENT_PAGE = "page";
        public static final int PAGE_SIZE_COUNT = 20;
        public static final int FILTER_DEFAULT = 0;
        public static final int FILTER_VIP = 1;
        public static final int FILTER_SALE = 2;
        public static final int FILTER_FREE = 3;
    }

    public static class BookDownloadUrl {
        public static final String GET_CONTENT = "getContent";
        public static final String ORDER_ID = "orderId";
        public static final String UUID = "uuid";
        public static final String EBOOK_ID = "ebookId";
        public static final String USER_ID = "userId";
    }

    public static class Cert {
        public static final String GET_CERT = "getCert";
        public static final String ORDER_ID = "orderId";
        public static final String ORDER_TYPE = "orderType";
        public static final String DEVICE_TYPE = "deviceType";
        public static final String HAS_RANDOM = "hasRandom";
        public static final String DEVICE_MODEL = "deviceModel";
        public static final String IS_BORROW_BUY = "isBorrowBuy";
        public static final String HAS_CERT = "hasCert";
        public static final String UUID = "uuid";
        public static final String EBOOK_ID = "ebookId";
        public static final String USER_ID = "userId";
    }

    public static class GotoOrder {
        public static final String ORDER_ORDERSTEP1_ACTION = "order_orderStep1.action?";
        public static final String TOKENKEY = "tokenKey=";
        public static final String NUM = "num";
        public static final String PURCHASE_QUANTITY = "1";
        public static final String ID = "Id";
        public static final String THESKUS = "TheSkus";
        public static final String SINGLE_UNION_ID = "singleUnionId";
        public static final String SINGLE_SUB_UNION_ID = "singleSubUnionId";
        public static final String IS_SUPPORT_JS = "isSupportJs";
        public static final String BOOLEAN = "true";
        public static final String CART = "cart";
        public static final String CART_DETAIL = "cart/detail";
    }

    public static String getJDBooxBaseUrl() {
        return JD_BOOK_SHOP_URL;
    }

    public static String getJdBaseUrl() {
        return JD_BASE_URL;
    }

    public static String getJdSmoothReadUrl() {
        return JD_SMOOTH_READ_URL;
    }

    private static CookieHandler addCookie() {
        String a2 = ClientUtils.getWJLoginHelper().getA2();
        if (!StringUtils.isNullOrEmpty(a2)) {
            PersistentCookieStore persistentCookieStore = new PersistentCookieStore(JDReadApplication.getInstance());
            HttpCookie newCookie = new HttpCookie(Constants.COOKIE_KEY, a2);
            newCookie.setDomain(Constants.COOKIE_DOMAIN);
            newCookie.setPath("/");
            newCookie.setVersion(0);
            persistentCookieStore.removeAll();
            persistentCookieStore.add(null, newCookie);
            CookieHandler cookieHandler = new CookieManager(persistentCookieStore, CookiePolicy.ACCEPT_ALL);
            return cookieHandler;
        }
        return null;
    }

    public static OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(addCookie()))
                .build();
        return client;
    }

    public static ReadContentService getService(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(CloudApiContext.getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ReadContentService.class);
    }

    public static ReadContentService getServiceForString(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(CloudApiContext.getClient())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        return retrofit.create(ReadContentService.class);
    }

    public static OkHttpClient getClientNoCookie() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new EnhancedCacheInterceptor())
                .build();
        return client;
    }

    public static ReadContentService getServiceNoCookie(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(CloudApiContext.getClientNoCookie())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ReadContentService.class);
    }
}
