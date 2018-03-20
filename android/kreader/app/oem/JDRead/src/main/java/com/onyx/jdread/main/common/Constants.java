package com.onyx.jdread.main.common;

import android.os.Environment;

import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.device.EnvironmentUtil;

import java.io.File;

/**
 * Created by 12 on 2016/12/6.
 */

public class Constants {
    public static final String COOKIE_KEY = "wskey";
    public static final String COOKIE_DOMAIN = ".jd.com";
    public static final String UTF_8 = "UTF-8";
    public static final String CLIENT_VERSION = "2.6.3";
    public static final int SYSTEM_SETTING_PRESS_COUNT = 6;
    public static final long CHECK_UPDATE_INTERVAL = 24 * 60 * 60 * 1000;
    public static final float TOAST_LOCATION = 0.55f;
    public static boolean isUseCache = true;
    public static final String POST = "POST";
    public static final String MD_5 = "MD5";

    public static final String SP_KEY_BOOK_ID = "book_id";
    public final static String SP_KEY_ACCOUNT = "user_account";
    public final static String SP_KEY_PASSWORD = "user_password";
    public static final String SP_KEY_USER_IMAGE_URL = "user_image_url";
    public static final String SP_KEY_USER_NICK_NAME = "user_nick_name";
    public static final String SP_KEY_USER_NAME = "user_name";
    public static final String SP_KEY_LIST_PIN = "listPin";
    public static final String SP_KEY_SHOW_PASSWORD = "show_password";
    public static final String SP_KEY_CATEGORY_LEVEL_ONE_ID = "category_level_one_id";
    public static final String SP_KEY_CATEGORY_LEVEL_TWO_POSITION = "category_level_two_position";
    public static final String SP_KEY_CATEGORY_LEVEL_TWO_ID = "category_level_two_id";
    public static final String SP_KEY_CATEGORY_LEVEL_VALUE = "category_level_value";
    public static final String SP_KEY_CATEGORY_NAME = "category_name";
    public static final String SP_KEY_CATEGORY_ISFREE = "category_isfree";
    public static final String SP_KEY_SUBJECT_NAME = "subject_name";
    public static final String SP_KEY_SUBJECT_MODEL_ID = "subject_model_id";
    public static final String SP_KEY_SUBJECT_MODEL_TYPE = "subject_model_type";
    public static final String SP_KEY_SUBJECT_RANK_TYPE = "subject_rank_type";
    public static final String SP_KEY_KEYWORD = "Keyword";
    public static final String SP_KEY_BOOK_LIST_TYPE = "book_list_type";
    public static final String SP_KEY_SEARCH_BOOK_CAT_ID = "book_search_book_cat_id";

    public static final int BOOK_SHOP_MAIN_CONFIG_CID = 11;
    public static final int BOOK_SHOP_VIP_CONFIG_CID = 12;
    public static final int BOOK_SHOP_NEW_BOOK_CONFIG_CID = 14;
    public static final int BOOK_SHOP_SALE_BOOK_CONFIG_CID = 13;
    public static final int BOOK_COMMENT_PAGE_SIZE = 20;
    public static final int BOOK_CATEGORY_PAGE_SIZE = 20;
    public static final String BOOK_PAGE_SIZE = "20";
    public static final int PAGE_STEP = 1;
    public static final String BOOK_FORMAT = ".JEB";
    public static final String RESULT_CODE_SUCCESS = "0";
    public static final String RESULT_CODE_UNKNOWN_ERROR = "1";
    public static final String RESULT_CODE_NO_FUNCTION = "2";
    public static final String RESULT_CODE_NOT_LOGIN = "3";
    public static final String RESULT_CODE_PARAMS_ERROR = "4";
    public static final String RESULT_CODE_PARAMS_LENGTH_ERROR = "5";
    public static final String RESULT_CODE_PARAMS_LACK = "6";
    public static final String RESULT_CODE_PARAMS_FORMAT_ERRO = "7";
    public static final String RESULT_CODE_ERROR = "8";
    public static final String RESULT_CODE_BOOK_NO_READ_VIP = "100";
    public static final String RESULT_CODE_BOOK_CAN_NOT_READ = "104";
    public static final String RESULT_CODE_BOOK_NOT_FOUND = "300";
    public static final String RESULT_CODE_BOOK_ILLEGAL_ORDER = "106";
    public static final String RESULT_CODE_BOOK_ILLEGAL_DEVICE = "108";
    public static final String RESULT_CODE_BOOK_GENERATE_CERT_ERROR = "111";
    public static final String RESULT_CODE_BOOK_GET_CONTENT_ERROR = "112";
    public static final String RESULT_CODE_BOOK_CERIFY_ORDER_ERROR = "116";
    public static final int RESULT_PAY_ORDER_INSUFFICIENT_BALANCE = 203;

    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final String CODE_STATE_THREE = "3";
    public static final String CODE_STATE_FOUR = "4";
    public static final String CART_TYPE_GET = "get";
    public static final String CART_TYPE_ADD = "update";
    public static final String CART_TYPE_DEL = "delete";
    public static final String CART_TYPE = "type";
    public static final String CART_BOOK_LIST = "book_list";
    public static final String RANDOW_VALUE = "0";
    public static final String HAS_CERT_VALUE = "0";
    public static final int ORDER_TYPE = 0;
    public static final String DEVICE_TYPE_A = "A";
    public static final String PAY_URL = "pay_url";
    public final static long APP_CACHE_MAX_SIZE = 1024 * 1024 * 8;
    public final static String LOCAL_WEB_CACHE_PATH = Environment.getExternalStorageDirectory().getPath() + "/webcache";
    public static final int CATEGORY_TYPE_FREE = 1;
    public static final String ENCRYPTION_DIR = "drdrop";
    public static final String ENCRYPTION_NAME = "dataread.jdr";
    public static final int SHOP_MAIN_INDEX_ZERO = 0;
    public static final int SHOP_MAIN_INDEX_ONE = 1;
    public static final int SHOP_MAIN_INDEX_TWO = 2;
    public static final int SHOP_MAIN_INDEX_THREE = 3;
    public static final int SHOP_MAIN_INDEX_FOUR = 4;
    public static final int SHOP_MAIN_INDEX_FIVE = 5;
    public static final int SHOP_MAIN_INDEX_SIX = 6;
    public static final int SHOP_MAIN_INDEX_SEVEN = 7;
    public static final int SHOP_MAIN_INDEX_EIGHT = 8;
    public static final int SHOP_MAIN_INDEX_NINE = 9;
    public static final int SHOP_MAIN_INDEX_TEN = 10;
    public static final int SHOP_MAIN_INDEX_ELEVEN = 11;
    public static final int SHOP_MAIN_INDEX_TWELVE = 12;
    public static final int CATEGORY_LEVEL_ONE = 1;
    public static final int CATEGORY_LEVEL_TWO = 2;
    public static final int RANK_LIST_SIZE = 6;
    public static final long RESET_PRESS_TIMEOUT = 1000;
    public static final int START_PRODUCTION_TEST_PRESS_COUNT = 6;
    public static final int REMOVE_PRODUCTION_TEST_PRESS_COUNT = 5;
    public static final int BOOK_LIST_TYPE_BOOK_MODEL = 1;
    public static final int BOOK_LIST_TYPE_BOOK_RANK = 2;
    public static final String PARSE_JSON_TYPE = "application/json";
    public static final int WEB_VIEW_TEXT_ZOOM = 100;
    public static final String CURRENT_DAY = "current_day";
    public static final String RECEIVED_VOUCHER = "received_voucher";
    public static final String MAIN_CONFIG_TYPE_EBOOK = "ebook";
    public static final String MAIN_CONFIG_TYPE_ADV = "adv";
    public static final int MODULE_TYPE_ADV_FIX_TWO = 3;
    public static final int MODULE_TYPE_RECOMMEND = 6;

    public static final int SHOP_VIEW_RECYCLE_HEIGHT = 1099;
    public static final int COMMOM_SUBJECT_RECYCLE_HEIGHT = 1107;
    public static final int SHOP_VIEW_TOP_FUNCTION_HEIGHT = 106;
    public static final int SHOP_VIEW_BANNER_HEIGHT = 169;
    public static final int SHOP_VIEW_TITLE_HEIGHT = 169;
    public static final int SHOP_VIEW_SUBJECT_HEIGHT = 437;
    public static final int SHOP_VIEW_END_VIEW_HEIGHT = 327;
    public static final int SHOP_VIEW_END_VIEW_HIGH_HEIGHT = 552;
    public static final int SHOP_VIEW_VIP_INFO_VIEW_HEIGHT = 214;
    public static final String MTP_EXTRA_TAG_OLD_FILE_PATH = "old_file_path";
    public static final String CATEGORY_MATH_CONTENT = "数字内容";
    public static final String CATEGORY_BOY_ORIGINAL = "男生原创";
    public static final String CATEGORY_GIRL_ORIGINAL = "女生原创";
    public static final int RELATE_TYPE_BOOK_LIST = 1;
    public static final int RELATE_TYPE_LINK = 2;
    public static final int RELATE_TYPE_BOOK_DETAIL = 4;
    public static final String PAY_DIALOG_TYPE = "pay_dialog_type";
    public static final int PAY_DIALOG_TYPE_PAY_ORDER = 1;
    public static final int PAY_DIALOG_TYPE_TOP_UP = 2;
    public static final int PAY_DIALOG_TYPE_NET_BOOK = 3;
    public static final String PAY_BY_CASH = "pay_by_cash";
    public static final String ORDER_INFO = "order_info";
    public static final String BANNER_URL = "banner_url";
    public static final long LIMIT_TIME = 500;
    public static final String SEARCH_TYPE = "search_type";
    public static final String TYPE_UNLIMITED = "2";
    public static final String TYPE_BOUGHT = "1";
    public static final int BOOK_DETAIL_TYPE_NET = 1;
    public static final int BOOK_DETAIL_TYPE_PUBLISH = 0;
    public static final String WHOLE_BOOK_DOWNLOAD_TAG = "whole_book";
    public static final String JD_BOOKS_DIR = Device.currentDevice.getExternalStorageDirectory() + "/Books/";
    public static final String MANUAL_FAQ_URL = "https://jdread-api.jd.com/faq";
    public static final int NET_BOOK_STATUS_DOWN = 1;
    public static final String NATIVIE_DIR = EnvironmentUtil.getExternalStorageDirectory() + File.separator + "Notes";
    public static final String EMAIL_DIR = EnvironmentUtil.getExternalStorageDirectory() + File.separator + "TempExport";
    public static final String ZIP_NAME = "jdread";
    public static final String IS_GUIDE = "is_guide";
    public static final String NET_ERROR_TITLE = "net_error_title";
    public static final String NET_ERROR_SHOW_TITLE_BAR = "net_error_show_title_bar";
    public static final String NEW_LINE = "\r\n";
    public static final int TIPS_START = 2;
    public static final int TIPS_SUB_MIDDLE = 5;
    public static final int TIPS_MIDDLE = 8;
    public static final int TIPS_END = 11;
    public static final int SHOP_RANK_MODEL_TYPE_SOARING_LIST = 6;
    public static final int SHOP_RANK_MODEL_TYPE_SALE_NEW_BOOK_LIST = 2;
    public static final int RANK_TYPE_PUBLISH = 1;
    public static final String BOOK_FORMAT_PDF = "pdf";
    public static String KEY_DIRECTORY_CONTENT = "directory_content";
    public static final int INVALID_VALUE = -2;
    public static final String NET_BOOK_DECRYPT_SALT = "1513304880000";
    public static final String NET_BOOK_DIR = "/netbooks/";
    public static final String DIVIDER = "_";
    public static final int MINUTE_STEP = 60;
    public static final int GET_DECRYPT_KEY_POINT = 6;
}
