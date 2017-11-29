/*
 * Copyright (C) 2014 The Android Client Of Demo Project
 * 
 *     The BeiJing PingChuanJiaHeng Technology Co., Ltd.
 * 
 * Author:Yang ZiTian
 * You Can Contact QQ:646172820 Or Email:mail_yzt@163.com
 */
package com.hemaapp.hm_FrameWork.view.photoview;

import android.os.Build.VERSION;
import android.view.View;

public class Compat {

	private static final int SIXTY_FPS_INTERVAL = 1000 / 60;

	public static void postOnAnimation(View view, Runnable runnable) {
		if (VERSION.SDK_INT >= 16) {
			view.postOnAnimation(runnable);
		} else {
			view.postDelayed(runnable, SIXTY_FPS_INTERVAL);
		}
	}

}
