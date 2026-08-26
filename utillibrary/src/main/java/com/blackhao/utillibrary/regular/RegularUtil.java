package com.blackhao.utillibrary.regular;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.util.Patterns.DOMAIN_NAME;
import static android.util.Patterns.GOOD_IRI_CHAR;

/**
 * 正则表达式工具类
 * Created by ZhangHao on 2017/3/8.
 */

public class RegularUtil {

    //验证手机号（更新支持 13x/14x/15x/16x/17x/18x/19x 号段）
    private static final String REGEX_MOBILE = "^(13[0-9]|14[0-9]|15[0-9]|16[0-9]|17[0-9]|18[0-9]|19[0-9])\\d{8}$";

    //验证座机号,正确格式：xxx/xxxx-xxxxxxx/xxxxxxxx
    private static final String REGEX_TEL = "^0\\d{2,3}[- ]?\\d{7,8}";

    //验证邮箱
    private static final String REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";

    //验证url
    private static final String REGEX_URL = "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
            + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
            + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
            + "(?:" + DOMAIN_NAME + ")"
            + "(?:\\:\\d{1,5})?)"
            + "(\\/(?:(?:[" + GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~"
            + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
            + "(?:\\b|$)";

    //验证汉字
    private static final String REGEX_CHZ = "^[\\u4e00-\\u9fa5]+$";

    //验证用户名,取值范围为a-z,A-Z,0-9,"_",汉字，不能以"_"结尾,用户名必须是6-20位
    private static final String REGEX_USERNAME = "^[\\w\\u4e00-\\u9fa5]{6,20}(?<!_)$";

    //验证IP地址
    private static final String REGEX_IP = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";

    //预编译 Pattern 提高性能
    private static final Pattern PATTERN_MOBILE = Pattern.compile(REGEX_MOBILE);
    private static final Pattern PATTERN_TEL = Pattern.compile(REGEX_TEL);
    private static final Pattern PATTERN_EMAIL = Pattern.compile(REGEX_EMAIL);
    private static final Pattern PATTERN_URL = Pattern.compile(REGEX_URL);
    private static final Pattern PATTERN_CHZ = Pattern.compile(REGEX_CHZ);
    private static final Pattern PATTERN_USERNAME = Pattern.compile(REGEX_USERNAME);
    private static final Pattern PATTERN_IP = Pattern.compile(REGEX_IP);
    private static final Pattern PATTERN_ILLEGAL_CHAR = Pattern.compile("[\\[\\]]|\n|\r|\t");

    /**
     * @param string 待验证文本
     * @return 是否符合手机号格式
     */
    public static boolean isMobile(String string) {
        return isMatch(PATTERN_MOBILE, string);
    }

    /**
     * @param string 待验证文本
     * @return 是否符合座机号码格式
     */
    public static boolean isTel(String string) {
        return isMatch(PATTERN_TEL, string);
    }

    /**
     * @param string 待验证文本
     * @return 是否符合邮箱格式
     */
    public static boolean isEmail(String string) {
        return isMatch(PATTERN_EMAIL, string);
    }

    /**
     * @param string 待验证文本
     * @return 是否符合网址格式
     */
    public static boolean isURL(String string) {
        return isMatch(PATTERN_URL, string);
    }

    /**
     * @param string 待验证文本
     * @return 是否符合汉字
     */
    public static boolean isChz(String string) {
        return isMatch(PATTERN_CHZ, string);
    }

    /**
     * @param string 待验证文本
     * @return 是否符合用户名
     */
    public static boolean isUsername(String string) {
        return isMatch(PATTERN_USERNAME, string);
    }

    /**
     * @param string 待验证文本
     * @return 是否符合IP格式
     */
    public static boolean isIP(String string) {
        return isMatch(PATTERN_IP, string);
    }

    /**
     * 使用预编译 Pattern 匹配
     *
     * @param pattern 预编译的正则表达式
     * @param string  要匹配的字符串
     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
     */
    private static boolean isMatch(Pattern pattern, String string) {
        return !TextUtils.isEmpty(string) && pattern.matcher(string).matches();
    }

    /**
     * 判断是否含有非法字符
     *
     * @param str 待检查字符串
     * @return true为包含，false为不包含
     */
    public static boolean isIllegalChar(String str) {
        return PATTERN_ILLEGAL_CHAR.matcher(str).find();
    }

}
