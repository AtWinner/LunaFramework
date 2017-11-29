package com.hemaapp.hm_FrameWork.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * 基本工具类
 */
public class BaseUtil {

	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return true如果该字符串为null或者"",否则false
	 */
	public static final boolean isNull(String str) {
		return str == null || "".equals(str.trim());
	}

	/**
	 * 获取屏幕密度规格
	 * 
	 * @param context
	 * @return
	 */
	public static final String getDpi(Context context) {
		float density = context.getResources().getDisplayMetrics().density;
		String dpi = null;
		if (density == 0.75f)
			dpi = "ldpi";
		else if (density == 1.0f)
			dpi = "mdpi";
		else if (density == 1.5f)
			dpi = "hdpi";
		else if (density == 2f)
			dpi = "xhdpi";
		else if (density == 3f)
			dpi = "xxhdpi";
		return dpi;
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
	 * 获取APP版本
	 * 
	 * @param context
	 *            环境
	 * @return String
	 */
	public static final int getAppVersionCode(Context context)
			throws NameNotFoundException {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
		return pi.versionCode;
	}

	/**
	 * 获取APP版本
	 * 
	 * @param context
	 *            环境
	 * @return String
	 */
	public static final String getAppVersionName(Context context)
			throws NameNotFoundException {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
		return pi.versionName;
	}

	/**
	 * 用当前时间给文件命名
	 * 
	 * @return String yyyyMMdd_HHmmss
	 */
	public static final String getFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault());
		return dateFormat.format(date);// + ".jpg";
	}
}
