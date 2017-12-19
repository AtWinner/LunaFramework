package com.hemaapp.hm_FrameWork.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 时间相关工具类
 */
public class TimeUtil {
    /**
     * 转换时间显示形式
     *
     * @param time   时间字符串yyyy-MM-dd HH:mm:ss
     * @param format 格式
     * @return String
     */
    public static String TransTime(String time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        try {
            Date date1 = sdf.parse(time);
            SimpleDateFormat dateFormat = new SimpleDateFormat(format,
                    Locale.getDefault());// "yyyy年MM月dd HH:mm"
            return dateFormat.format(date1);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取系统当前时间
     *
     * @param format 时间格式yyyy-MM-dd HH:mm:ss
     * @return String
     */
    public static String getCurrentTime(String format) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(format,
                Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * 获取格林威治当前时间
     *
     * @param format 时间格式yyyy-MM-dd HH:mm:ss
     * @return String
     */
    public static String getGMTTime(String format) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(format,
                Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }
}
