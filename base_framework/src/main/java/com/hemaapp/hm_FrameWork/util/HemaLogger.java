package com.hemaapp.hm_FrameWork.util;

import android.util.Log;

import com.hemaapp.PoplarConfig;

import java.util.ArrayList;

/**
 * 打印
 */
public class HemaLogger {
	private static final String ERROR = "The log message is null.";
	private static final int LENGTH_PER = 4000;

	private enum Level {
		V, D, I, W, E
	}

	public static void println(Object msg) {
		if (PoplarConfig.LOG)
			System.out.println(msg);
	}

	public static void v(String tag, String msg) {
		log(tag, msg, Level.V);
	}

	public static void d(String tag, String msg) {
		log(tag, msg, Level.D);
	}

	public static void i(String tag, String msg) {
		log(tag, msg, Level.I);
	}

	public static void w(String tag, String msg) {
		log(tag, msg, Level.W);
	}

	public static void e(String tag, String msg) {
		log(tag, msg, Level.E);
	}

	private static ArrayList<String> split(String msg) {
		ArrayList<String> strings = null;
		if (msg != null) {
			int length = msg.length();
			int count = length / LENGTH_PER;
			int remain = length % LENGTH_PER;
			strings = new ArrayList<String>();
			if (count == 0)
				strings.add(msg);
			else {
				for (int i = 0; i < count; i++) {
					int start = i * LENGTH_PER;
					int end = start + LENGTH_PER - 1;
					strings.add(msg.substring(start, end));
				}
				if (remain != 0) {
					int start = count * LENGTH_PER;
					int end = length - 1;
					strings.add(msg.substring(start, end));
				}
			}
		}
		return strings;
	}

	private static void log(String tag, String log, Level level) {
		if (!PoplarConfig.LOG)
			return;
		
		// 将字符串分段打印,解决logcat打印不全的问题
		ArrayList<String> strings = split(log);
		switch (level) {
		case V:
			if (strings == null)
				Log.v(tag, ERROR);
			else
				for (String string : strings)
					Log.v(tag, string);
			break;
		case D:
			if (strings == null)
				Log.d(tag, ERROR);
			else
				for (String string : strings)
					Log.d(tag, string);
			break;
		case I:
			if (strings == null)
				Log.i(tag, ERROR);
			else
				for (String string : strings)
					Log.i(tag, string);
			break;
		case W:
			if (strings == null)
				Log.w(tag, ERROR);
			else
				for (String string : strings)
					Log.w(tag, string);
			break;
		case E:
			if (strings == null)
				Log.e(tag, ERROR);
			else
				for (String string : strings)
					Log.e(tag, string);
			break;
		}
	}

	/**
	 * 打印异常
	 * @param tag
	 * @param clazz
     * @param e
     */
	public static void printExc(String tag,Class<?> clazz, Throwable e) {
		try {
			if (PoplarConfig.LOG) {
				e.printStackTrace();
			}
			else {
				String clazzName = clazz == null ? "Unknow" : clazz.getSimpleName();

				Log.v(tag, String.format("class[%s], %s", clazzName, e + ""));
			}
		} catch (Throwable ee) {
			ee.printStackTrace();
		}
	}
}
