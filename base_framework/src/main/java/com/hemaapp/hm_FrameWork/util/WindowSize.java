package com.hemaapp.hm_FrameWork.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.WindowManager;


/**
 * 获取屏幕大小及状态栏高度。。。。工具类
 * 
 * @author YangZitian
 * @creation date 2012-8-29 上午10:54:47
 * 
 */
public class WindowSize {
	private static final String TAG = "HemaWindowSize";
	/**
	 * 屏幕高度
	 */
	private static int height = 0;
	/**
	 * 屏幕宽度
	 */
	private static int width = 0;
	/**
	 * 状态栏高度
	 */
	private static int statusBarHeight = 0;

	@SuppressWarnings("deprecation")
	public static void get(Context context) {
		if (height == 0 || width == 0) {
			Activity ac = (Activity) context;
			WindowManager wm = (WindowManager) ac
					.getSystemService(Context.WINDOW_SERVICE);
			width = wm.getDefaultDisplay().getWidth();// 屏幕宽度
			height = wm.getDefaultDisplay().getHeight();// 屏幕高度
			Rect rect = new Rect();
			ac.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
			statusBarHeight = rect.top; // 状态栏高度
			HemaLogger.d(TAG, "height=" + height + " width=" + width
					+ " statusBarHeight=" + statusBarHeight);
			if (width != 0) {
				SharedPreferencesUtil.save(ac, "windowWidth",
						((Integer) width).toString());
				SharedPreferencesUtil.save(ac, "windowHeight",
						((Integer) height).toString());
				SharedPreferencesUtil.save(ac, "windowStatusBarHeight",
						((Integer) statusBarHeight).toString());
			}
		}
	}

	public static int getHeight(Context context) {
		String h = null;
		if (height == 0) {
			h = SharedPreferencesUtil.get(context, "windowHeight");
			if (h == null)
				get(context);
		}
		return (height != 0) ? height : (h != null) ? Integer.valueOf(h) : 0;
	}

	public static int getWidth(Context context) {
		String h = null;
		if (width == 0) {
			h = SharedPreferencesUtil.get(context, "windowWidth");
			if (h == null)
				get(context);
		}
		return (width != 0) ? width : (h != null) ? Integer.valueOf(h) : 0;
	}

	public static int getStatusBarHeight(Context context) {
		String h = null;
		if (statusBarHeight == 0) {
			h = SharedPreferencesUtil.get(context, "windowStatusBarHeight");
			if (h == null)
				get(context);
		}
		return (statusBarHeight != 0) ? statusBarHeight : (h != null) ? Integer
				.valueOf(h) : 0;
	}

}
