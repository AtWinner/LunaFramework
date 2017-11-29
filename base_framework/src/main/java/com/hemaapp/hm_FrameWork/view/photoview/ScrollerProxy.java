/*
 * Copyright (C) 2014 The Android Client Of Demo Project
 * 
 *     The BeiJing PingChuanJiaHeng Technology Co., Ltd.
 * 
 * Author:Yang ZiTian
 * You Can Contact QQ:646172820 Or Email:mail_yzt@163.com
 */
package com.hemaapp.hm_FrameWork.view.photoview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.widget.OverScroller;
import android.widget.Scroller;

public abstract class ScrollerProxy {

	public static ScrollerProxy getScroller(Context context) {
		if (VERSION.SDK_INT < 9) {
			return new PreGingerScroller(context);
		} else {
			return new GingerScroller(context);
		}
	}

	public abstract boolean computeScrollOffset();

	public abstract void fling(int startX, int startY, int velocityX,
			int velocityY, int minX, int maxX, int minY, int maxY, int overX,
			int overY);

	public abstract void forceFinished(boolean finished);

	public abstract int getCurrX();

	public abstract int getCurrY();

	@TargetApi(9)
	private static class GingerScroller extends ScrollerProxy {

		private OverScroller mScroller;

		public GingerScroller(Context context) {
			mScroller = new OverScroller(context);
		}

		@Override
		public boolean computeScrollOffset() {
			return mScroller.computeScrollOffset();
		}

		@Override
		public void fling(int startX, int startY, int velocityX, int velocityY,
				int minX, int maxX, int minY, int maxY, int overX, int overY) {
			mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX,
					minY, maxY, overX, overY);
		}

		@Override
		public void forceFinished(boolean finished) {
			mScroller.forceFinished(finished);
		}

		@Override
		public int getCurrX() {
			return mScroller.getCurrX();
		}

		@Override
		public int getCurrY() {
			return mScroller.getCurrY();
		}
	}

	private static class PreGingerScroller extends ScrollerProxy {

		private Scroller mScroller;

		public PreGingerScroller(Context context) {
			mScroller = new Scroller(context);
		}

		@Override
		public boolean computeScrollOffset() {
			return mScroller.computeScrollOffset();
		}

		@Override
		public void fling(int startX, int startY, int velocityX, int velocityY,
				int minX, int maxX, int minY, int maxY, int overX, int overY) {
			mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX,
					minY, maxY);
		}

		@Override
		public void forceFinished(boolean finished) {
			mScroller.forceFinished(finished);
		}

		@Override
		public int getCurrX() {
			return mScroller.getCurrX();
		}

		@Override
		public int getCurrY() {
			return mScroller.getCurrY();
		}
	}
}
