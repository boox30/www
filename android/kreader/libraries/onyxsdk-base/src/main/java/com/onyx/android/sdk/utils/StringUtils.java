package com.onyx.android.sdk.utils;


import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhuzeng on 10/16/15.
 */
public class StringUtils {

    static public final String UTF16LE = "UTF-16LE";
    static public final String UTF16BE = "UTF-16BE";
    static public final String UTF16 = "UTF-16";

    static public boolean isNullOrEmpty(final String string) {
        return (string == null || string.trim().length() <= 0);
    }

    static public boolean isNotBlank(final String string) {
        return (string != null && string.trim().length() > 0);
    }

    static public boolean isBlank(final String string) {
        return !isNotBlank(string);
    }

    static public String utf16le(final byte [] data) {
        String string = "";
        if (data == null) {
            return string;
        }
        try {
            string = new String(data, UTF16LE);
        } catch (Exception e) {
            Log.w("", e);
        }
        return string;
    }

    static public String utf16(final byte [] data) {
        String string = "";
        try {
            string = new String(data, UTF16);
        } catch (Exception e) {
        }
        return string;
    }

    static public byte[] utf16leBuffer(final String text) {
        byte [] buffer = null;
        try {
            buffer = text.getBytes(UTF16LE);
        } catch (Exception e) {
        }
        return buffer;
    }

    public static String join(Iterable<?> elements, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (Object e : elements) {
            if (sb.length() > 0)
                sb.append(delimiter);
            sb.append(e);
        }
        return sb.toString();
    }

    public static List<String> split(final String string, final String delimiter) {
        if (isNullOrEmpty(string)) {
            return new ArrayList<String>();
        }
        final String [] result = string.split(delimiter);
        return Arrays.asList(result);
    }

    public static String deleteNewlineSymbol(String content){
        if (!isNullOrEmpty(content)){
            content = content.replaceAll("\r\n"," ").replaceAll("\n", " ");
        }
        return content;
    }

    public static String leftTrim(String content) {
        int start = 0, last = content.length() - 1;
        while ((start <= last) && (content.charAt(start) <= ' ')) {
            start++;
        }
        if (start == 0) {
            return content;
        }
        return content.substring(start, last);
    }

    public static String rightTrim(String content) {
        int start = 0, last = content.length() - 1;
        int end = last;
        while ((end >= start) && (content.charAt(end) <= ' ')) {
            end--;
        }
        if (end == last) {
            return content;
        }
        return content.substring(start, end);
    }

    public static String filterUnusedChar(String input) {
        if (!StringUtils.isNullOrEmpty(input)) {
            input = input.trim();
            input = input.replaceAll("\u0000", ""); // removes NUL chars
            input = input.replaceAll("\\u0000", ""); // removes backslash+u0000
        }
        return input;
    }
}
