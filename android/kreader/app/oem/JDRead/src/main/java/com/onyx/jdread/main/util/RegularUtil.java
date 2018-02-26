package com.onyx.jdread.main.util;

import java.util.regex.Pattern;

/**
 * Created by hehai on 17-7-29.
 */

public class RegularUtil {

    private static String POSITIVE_ORDER_CHAR_STRING;
    private static String NEGATIVE_ORDER_CHAR_STRING;

    static {
        for (int i = 33; i < 127; i++) {
            POSITIVE_ORDER_CHAR_STRING += Character.toChars(i)[0];
            NEGATIVE_ORDER_CHAR_STRING += Character.toChars(160 - i)[0];
        }
    }

    /**
     * regex username
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z]\\w{5,17}$";

    /**
     * regex password
     */
    public static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,16}$";

    /**
     * regex china mobile
     */
    private static final String REGEX_CHINA_MOBILE ="1(3[4-9]|4[7]|5[012789]|8[278])\\d{8}";

    /**
     * regex china unicom
     */
    private static final String REGEX_CHINA_UNICOM = "1(3[0-2]|5[56]|8[56])\\d{8}";

    /**
     * regex china telecom
     */
    private static final String REGEX_CHINA_TELECOM = "(?!00|015|013)(0\\d{9,11})|(1(33|53|80|89)\\d{8})";

    /**
     * regex phone number
     */
    private static final String REGEX_PHONE_NUMBER = "^(0(10|2\\d|[3-9]\\d\\d)[- ]{0,3}\\d{7,8}|0?1[3584]\\d{9})$";

    /**
     * regex email
     */
    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    /**
     * regex Chinese char
     */
    public static final String REGEX_CHINESE = "^[\u4e00-\u9fa5],{0,}$";

    /**
     * regex id card
     */
    public static final String REGEX_ID_CARD = "(^\\d{18}$)|(^\\d{15}$)";

    /**
     * regex URL
     */
    public static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";

    /**
     * regex ip
     */
    public static final String REGEX_IP_ADDR = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";

    public static final String REGEX_ALL_CHAR_SAME = "(\\w)(\\1)+";

    public static final String REGEX_LETTER_OR_NUMBER = "((\\d)|([a-z])|([A-Z]))+";

    /**
     * Check the username
     *
     * @param username
     * @return
     */
    public static boolean isUsername(String username) {
        return Pattern.matches(REGEX_USERNAME, username);
    }

    /**
     * Check the password
     *
     * @param password
     * @return
     */
    public static boolean isPassword(String password) {
        return Pattern.matches(REGEX_PASSWORD, password);
    }

    /**
     * Check the phone number
     *
     * @param mobile
     * @return
     */
    public static boolean isMobile(String mobile) {
        return Pattern.matches(REGEX_PHONE_NUMBER, mobile);
    }

    /**
     * Check the mailbox
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        return Pattern.matches(REGEX_EMAIL, email);
    }

    /**
     * Check Chinese characters
     *
     * @param chinese
     * @return
     */
    public static boolean isChinese(String chinese) {
        return Pattern.matches(REGEX_CHINESE, chinese);
    }

    /**
     * Check the identity card
     *
     * @param idCard
     * @return
     */
    public static boolean isIDCard(String idCard) {
        return Pattern.matches(REGEX_ID_CARD, idCard);
    }

    /**
     * Check url
     *
     * @param url
     * @return
     */
    public static boolean isUrl(String url) {
        return Pattern.matches(REGEX_URL, url);
    }

    /**
     * Check ip
     *
     * @param ipAddr
     * @return
     */
    public static boolean isIPAddr(String ipAddr) {
        return Pattern.matches(REGEX_IP_ADDR, ipAddr);
    }

    public static boolean isAllCharSame(String text) {
        return Pattern.matches(REGEX_ALL_CHAR_SAME, text);
    }

    public static boolean isOrderChar(String text) {
        boolean match = Pattern.matches(REGEX_LETTER_OR_NUMBER, text);
        if (!match) {
            return false;
        }
        return POSITIVE_ORDER_CHAR_STRING.contains(text) || NEGATIVE_ORDER_CHAR_STRING.contains(text);
    }
}
