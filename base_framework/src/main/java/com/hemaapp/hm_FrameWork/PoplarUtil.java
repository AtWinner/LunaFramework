package com.hemaapp.hm_FrameWork;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hemaapp.PoplarConfig;
import com.hemaapp.hm_FrameWork.util.BaseUtil;
import com.hemaapp.hm_FrameWork.util.SharedPreferencesUtil;
import com.hemaapp.hm_FrameWork.util.TimeUtil;
import com.hemaapp.hm_FrameWork.util.Md5Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 */
public class PoplarUtil {

    /**
     * 转换时间InMillis
     *
     * @param time 时间字符串
     * @return long(如果时间字符串无法转换返回0)
     */
    public static long timeInMillis(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(time);
            return date.getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取当前版本号(注意此处获取的为3位服务器版本号)
     *
     * @param context
     * @return 当前版本号
     */
    public static final String getAppVersionForSever(Context context) {
        String version = null;
        try {
            version = BaseUtil.getAppVersionName(context);
            String[] vs = version.split("\\.");
            if (vs.length >= 4) {
                version = vs[0] + "." + vs[1] + "." + vs[2];
            }
        } catch (NameNotFoundException e) {
            version = "1.0.0";
        }
        return version;
    }

    /**
     * 获取当前版本号(注意此处获取的为当前4位版本号的全部字符串)
     *
     * @param context
     * @return 当前版本号
     */
    public static final String getAppVersionForTest(Context context) {
        String version = null;
        try {
            version = BaseUtil.getAppVersionName(context);
        } catch (NameNotFoundException e) {
            version = "1.0.0.0";
        }
        return version;
    }

    /**
     * 比较app版本是否需要升级
     *
     * @param current
     * @param service
     * @return
     */
    public static boolean isNeedUpDate(String current, String service) {
        if (BaseUtil.isNull(current) || BaseUtil.isNull(service))
            return false;

        String[] c = current.split("\\."); // 2.2.3
        String[] s = service.split("\\."); // 2.4.0
        long fc = Long.valueOf(c[0]); // 2
        long fs = Long.valueOf(s[0]); // 2
        if (fc > fs)
            return false;
        else if (fc < fs) {
            return true;
        } else {
            long sc = Long.valueOf(c[1]); // 2
            long ss = Long.valueOf(s[1]); // 4
            if (sc > ss)
                return false;
            else if (sc < ss) {
                return true;
            } else {
                long tc = Long.valueOf(c[2]); // 3
                long ts = Long.valueOf(s[2]); // 0
                if (tc >= ts)
                    return false;
                else
                    return true;
            }
        }
    }

    /**
     * 转换时间显示形式(与当前系统时间比较),在发表话题、帖子和评论时使用
     *
     * @param time 时间字符串
     * @return String
     */
    public static String transTime(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault());
            String current = TimeUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss");
            String dian24 = TimeUtil.TransTime(current, "yyyy-MM-dd")
                    + " 24:00:00";
            String dian00 = TimeUtil.TransTime(current, "yyyy-MM-dd")
                    + " 00:00:00";
            Date now = null;
            Date date = null;
            Date d24 = null;
            Date d00 = null;

            now = sdf.parse(current); // 将当前时间转化为日期
            date = sdf.parse(time); // 将传入的时间参数转化为日期
            d24 = sdf.parse(dian24);
            d00 = sdf.parse(dian00);

            long diff = now.getTime() - date.getTime(); // 获取二者之间的时间差值
            long min = diff / (60 * 1000);
            if (min <= 5)
                return "刚刚";
            if (min < 60)
                return min + "分钟前";

            if (now.getTime() <= d24.getTime()
                    && date.getTime() >= d00.getTime())
                return "今天" + TimeUtil.TransTime(time, "HH:mm");

            int sendYear = Integer
                    .valueOf(TimeUtil.TransTime(time, "yyyy"));
            int nowYear = Integer.valueOf(TimeUtil.TransTime(current,
                    "yyyy"));
            if (sendYear < nowYear)
                return TimeUtil.TransTime(time, "yyyy-MM-dd HH:mm");
            else
                return TimeUtil.TransTime(time, "MM-dd HH:mm");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 转换时间显示形式(与当前系统时间比较),在显示即时聊天的时间时使用
     *
     * @param time 时间字符串
     * @return String
     */
    public static String transTimeChat(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault());
            String current = TimeUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss");
            String dian24 = TimeUtil.TransTime(current, "yyyy-MM-dd")
                    + " 24:00:00";
            String dian00 = TimeUtil.TransTime(current, "yyyy-MM-dd")
                    + " 00:00:00";
            Date now = null;
            Date date = null;
            Date d24 = null;
            Date d00 = null;
            try {
                now = sdf.parse(current); // 将当前时间转化为日期
                date = sdf.parse(time); // 将传入的时间参数转化为日期
                d24 = sdf.parse(dian24);
                d00 = sdf.parse(dian00);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long diff = now.getTime() - date.getTime(); // 获取二者之间的时间差值
            long min = diff / (60 * 1000);
            if (min <= 5)
                return "刚刚";
            if (min < 60)
                return min + "分钟前";

            if (now.getTime() <= d24.getTime()
                    && date.getTime() >= d00.getTime())
                return "今天" + TimeUtil.TransTime(time, "HH:mm");

            int sendYear = Integer
                    .valueOf(TimeUtil.TransTime(time, "yyyy"));
            int nowYear = Integer.valueOf(TimeUtil.TransTime(current,
                    "yyyy"));
            if (sendYear < nowYear)
                return TimeUtil.TransTime(time, "yyyy-MM-dd HH:mm");
            else
                return TimeUtil.TransTime(time, "MM-dd HH:mm");
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 隐藏手机号和邮箱显示
     *
     * @param old     需要隐藏的手机号或邮箱
     * @param keytype 1手机2邮箱
     * @return
     */
    public static String hide(String old, String keytype) {
        try {
            if ("1".equals(keytype))
                return old.substring(0, 3) + "****" + old.substring(7, 11);
            else {
                StringBuilder sb = new StringBuilder();
                String[] s = old.split("@");
                int l = s[0].length();
                int z = l / 3;
                sb.append(s[0].substring(0, z));
                int y = l % 3;
                for (int i = 0; i < z + y; i++)
                    sb.append("*");
                sb.append(s[0].substring(z * 2 + y, l));
                sb.append("@");
                if (s[1] == null) {

                }
                sb.append(s[1]);
                return sb.toString();
            }
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        // Returns a list of application processes that are running on the
        // device
        ActivityManager activityManager = (ActivityManager) context
                .getApplicationContext().getSystemService(
                        Context.ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();
        System.out.println("packageName=" + packageName);
        List<RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断当前用户是否是第三方登录
     *
     * @param context
     * @return
     */
    public static boolean isThirdSave(Context context) {
        String thirdsave = SharedPreferencesUtil.get(context, "thirdsave");
        return "true".equals(thirdsave);
    }

    /**
     * 设置当前用户是否是第三方登录
     *
     * @param context
     * @param thirdsave
     */
    public static void setThirdSave(Context context, boolean thirdsave) {
        SharedPreferencesUtil.save(context, "thirdsave", thirdsave ? "true"
                : "false");
    }


    /**
     * 密码加密
     *
     * @param text
     * @return
     */
    public static String encryptPwd(String text, boolean useMd5) {
        if (!useMd5) {
            return text;
        }
        return Md5Util.getMd5(PoplarConfig.PASSWORD_KEY + Md5Util.getMd5(text));
    }

    public static String encryptPwd(String text) {
        return Md5Util.getMd5(PoplarConfig.PASSWORD_KEY + Md5Util.getMd5(text));
    }

    /**
     * 根据包名判断是否安装某个应用
     *
     * @param context
     * @param packageName 包名
     * @return
     */
    public static boolean isAppAvailible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(packageName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 打电话
     *
     * @param mContext
     * @param mobile
     * @return
     */
    public static void call(Context mContext, String mobile) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mobile));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);//内部类
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 使用正则表达式验证输入的手机号是否合法
     *
     * @param phoneNumber 手机号
     * @return 合法:true;
     */
    public static boolean checkPhoneNumber(String phoneNumber) {
        Pattern p = Pattern.compile("^[1][3-9]+\\d{9}");
        Matcher m = p.matcher(phoneNumber);
        Log.e("验证手机号", String.valueOf(m.matches()));
        return m.matches();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, double dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
