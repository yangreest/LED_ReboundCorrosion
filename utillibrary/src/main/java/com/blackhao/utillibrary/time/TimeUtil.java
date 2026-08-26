package com.blackhao.utillibrary.time;

import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Author ： BlackHao
 * Time : 2016/8/13 14:05
 * Description : 时间类型转换工具类
 */
public class TimeUtil {

    private static final String TAG = "TimeUtil";

    /**
     * 默认的时间 String 模式
     */
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * ThreadLocal 缓存 SimpleDateFormat，避免频繁创建对象
     */
    private static final ThreadLocal<SimpleDateFormat> dateFormatCache = new ThreadLocal<>();

    /**
     * 获取 SimpleDateFormat（带缓存）
     */
    private static SimpleDateFormat getDateFormat(String pattern) {
        SimpleDateFormat sdf = dateFormatCache.get();
        if (sdf == null || !sdf.toPattern().equals(pattern)) {
            sdf = new SimpleDateFormat(pattern, Locale.CHINA);
            dateFormatCache.set(sdf);
        }
        return sdf;
    }

    /**
     * 获取当前时间，并返回 String类型的数据
     *
     * @return 当前时间
     */
    public static String getCurrentTime() {
        return getCurrentTime(DEFAULT_PATTERN);
    }

    /**
     * 获取当前时间，并返回 String类型的数据
     *
     * @return 当前时间
     */
    public static String getCurrentTime(String pattern) {
        Date curDate = new Date(System.currentTimeMillis());
        return getDateFormat(pattern).format(curDate);
    }

    /**
     * 通过传入的 Date类型时间，并返回String类型的数据
     */
    public static String formatDateToString(Date curDate, String pattern) {
        return getDateFormat(pattern).format(curDate);
    }

    /**
     * 通过传入的 Date类型时间，并返回 String类型的数据
     */
    public static String formatDateToString(Date curDate) {
        return formatDateToString(curDate, DEFAULT_PATTERN);
    }

    /**
     * 根据毫秒数返回年月日时分秒
     * 使用 Calendar 替代废弃的 Time 类
     */
    public static int[] formatMsecToTimeInfo(long msec) {
        int[] dates = new int[6];
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(msec);
        dates[0] = calendar.get(Calendar.YEAR);
        dates[1] = calendar.get(Calendar.MONTH) + 1;
        dates[2] = calendar.get(Calendar.DAY_OF_MONTH);
        dates[3] = calendar.get(Calendar.HOUR_OF_DAY);
        dates[4] = calendar.get(Calendar.MINUTE);
        dates[5] = calendar.get(Calendar.SECOND);
        return dates;
    }

    /**
     * 通过枚举来返回当前的 int类型的时间类型（年，月，日，时，分，秒，周几）
     * 使用 Calendar 替代废弃的 Time 类
     */
    public static int getTimeType(long msec, TimeType type) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(msec);
        switch (type) {
            case YEAR:
                return calendar.get(Calendar.YEAR);
            case MONTH:
                return calendar.get(Calendar.MONTH);
            case DAY:
                return calendar.get(Calendar.DAY_OF_MONTH);
            case HOUR:
                return calendar.get(Calendar.HOUR_OF_DAY);
            case MINUTE:
                return calendar.get(Calendar.MINUTE);
            case SECOND:
                return calendar.get(Calendar.SECOND);
            case WEEKDAY:
                // Calendar 周日是 1，周六是 7；Time 周日是 0，周六是 6
                return calendar.get(Calendar.DAY_OF_WEEK) - 1;
            default:
                return 0;
        }
    }

    public enum TimeType {
        YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, WEEKDAY
    }

    /**
     * 将 String("yyyy-MM-dd HH:mm:ss")转换成 Date
     */
    public static Date formatStrToDate(String str) {
        return formatStrToDate(str, DEFAULT_PATTERN);
    }

    /**
     * 将 String转换成 Date
     */
    public static Date formatStrToDate(String str, String pattern) {
        try {
            return getDateFormat(pattern).parse(str);
        } catch (ParseException e) {
            Log.e(TAG, "formatStrToDate error: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将毫秒转化成固定格式的时间
     */
    public static String formatMsecToString(long msec) {
        return formatMsecToString(msec, DEFAULT_PATTERN);
    }

    /**
     * 将毫秒转化成固定格式的时间
     */
    public static String formatMsecToString(long msec, String pattern) {
        Date date = new Date(msec);
        return getDateFormat(pattern).format(date);
    }

    /**
     * 将字符串转化成毫秒
     */
    public static long formatStringToMsec(String str, String pattern) {
        try {
            Date date = getDateFormat(pattern).parse(str);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e(TAG, "formatStringToMsec error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 将字符串转化成毫秒
     */
    public static long formatStringToMsec(String str) {
        return formatStringToMsec(str, DEFAULT_PATTERN);
    }

    /**
     * 毫秒转成 时：分：秒
     */
    public static String formatMsec(long ms) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;

        long hour = ms / hh;
        long minute = (ms - hour * hh) / mi;
        long second = (ms - hour * hh - minute * mi) / ss;

        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;

        return strHour + ":" + strMinute + ":" + strSecond;
    }

    /**
     * 判断日期是否在两个指定日期之内
     *
     * @param targetDate 需要判断的日期
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param pattern    时间模式
     */
    public static boolean isBetweenTwoDays(String targetDate, String startDate, String endDate, String pattern) {
        try {
            SimpleDateFormat df = getDateFormat(pattern);
            Date dt1 = df.parse(startDate);
            Date dt2 = df.parse(endDate);
            Date target = df.parse(targetDate);
            return dt1 != null && dt2 != null && target != null
                    && dt1.getTime() <= target.getTime() && dt2.getTime() >= target.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "isBetweenTwoDays error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 判断日期是否在两个指定日期之内
     *
     * @param targetDate 需要判断的日期
     * @param startDate  开始日期
     * @param endDate    结束日期
     */
    public static boolean isBetweenTwoDays(String targetDate, String startDate, String endDate) {
        return isBetweenTwoDays(targetDate, startDate, endDate, DEFAULT_PATTERN);
    }

    /**
     * 获取指定网站的日期时间(必须在子线程调用),用于获取网络时间
     */
    public static long getWebsiteTime() {
        String[] webUrl = {
                "http://www.bjtime.cn",
                "http://www.baidu.com",
                "http://www.taobao.com",
                "http://www.ntsc.ac.cn",
                "http://www.time.ac.cn/"
        };
        for (String aWebUrl : webUrl) {
            try {
                URL url = new URL(aWebUrl);
                URLConnection uc = url.openConnection();
                uc.setConnectTimeout(5 * 1000);
                uc.setReadTimeout(5 * 1000);
                uc.connect();
                long websiteTime = uc.getDate();
                if (websiteTime > formatStringToMsec("2016-01-01 00:00:00")) {
                    return websiteTime;
                }
            } catch (IOException e) {
                Log.e(TAG, "getWebsiteTime error for " + aWebUrl + ": " + e.getMessage());
            }
        }
        return -1;
    }
}
