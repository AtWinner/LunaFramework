package com.hemaapp.hm_FrameWork.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用户信息工具类
 * 
 * @author YangZitian
 * @creation date 2012-9-28 上午10:30:24
 * 
 */
public class SharedPreferencesUtil {
	private static final String FILENAME = "sp";

	/**
	 * 保存用户信息
	 * 
	 * @param con
	 *            环境
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	public static void save(Context con, String key, String value) {
		SharedPreferences sp = con.getSharedPreferences(FILENAME,
				Activity.MODE_PRIVATE);
		sp.edit().putString(key, value).commit();
	}

	/**
	 * 获取信息
	 * 
	 * @param con
	 *            环境
	 * @param key
	 *            key
	 * @return value
	 */
	public static String get(Context con, String key) {
		SharedPreferences sp = con.getSharedPreferences(FILENAME,
				Activity.MODE_PRIVATE);
		return sp.getString(key, null);
	}

}
