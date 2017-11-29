package com.hemaapp.hm_FrameWork.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * 吐司工具类
 */
public class ToastUtil {
	private static Toast mLongToast;
	private static Toast mShortToast;
	private static Handler mHandler;

	static {
		Looper looper;
		if ((looper = Looper.myLooper()) != null) {
			mHandler = new Handler(looper);
		} else if ((looper = Looper.getMainLooper()) != null) {
			mHandler = new Handler(looper);
		} else {
			mHandler = null;
		}
	}

	public static void showLongToast(Context context, String msg,
			long delayMillis) {
		showLongToast(context, msg);
		cancel(delayMillis, Toast.LENGTH_LONG);
	}

	public static void showShortToast(Context context, String msg,
			long delayMillis) {
		showShortToast(context, msg);
		cancel(delayMillis, Toast.LENGTH_SHORT);
	}

	public static void showLongToast(Context context, int msg, long delayMillis) {
		showLongToast(context, msg);
		cancel(delayMillis, Toast.LENGTH_LONG);
	}

	public static void showShortToast(Context context, int msg, long delayMillis) {
		showShortToast(context, msg);
		cancel(delayMillis, Toast.LENGTH_SHORT);
	}

	public static void showLongToast(Context context, String msg) {
		showLong(context, 0, msg);
	}

	public static void showShortToast(Context context, String msg) {
		showShort(context, 0, msg);
	}

	public static void showLongToast(Context context, int msg) {
		showLong(context, msg, null);
	}

	public static void showShortToast(Context context, int msg) {
		showShort(context, msg, null);
	}

	private static synchronized void showLong(Context context, int msg,
			String msgs) {
		if (msgs == null)
			try {
				msgs = context.getResources().getString(msg);
			} catch (Exception e) {
				msgs = "吐司信息为空或资源不存在";
			}

		mHandler.post(new LongRunable(context, msgs));
	}

	private static synchronized void showShort(Context context, int msg,
			String msgs) {
		if (msgs == null)
			try {
				msgs = context.getResources().getString(msg);
			} catch (Exception e) {
				msgs = "吐司信息为空或资源不存在";
			}

		mHandler.post(new ShortRunable(context, msgs));
	}

	private static void cancel(long delayMillis, final int style) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				switch (style) {
				case Toast.LENGTH_LONG:
					cancelLongToast();
					break;
				case Toast.LENGTH_SHORT:
					cancelShortToast();
					break;
				default:
					break;
				}
			}
		}, delayMillis);
	}

	public static void cancelAllToast() {
		cancelLongToast();
		cancelShortToast();
	}

	public static void cancelLongToast() {
		if (mLongToast != null) {
			mLongToast.cancel();
			mLongToast = null;
		}
	}

	public static void cancelShortToast() {
		if (mShortToast != null) {
			mShortToast.cancel();
			mShortToast = null;
		}
	}

	private static class LongRunable implements Runnable {
		Context context;
		String msgs;

		public LongRunable(Context context, String msgs) {
			this.context = context;
			this.msgs = msgs;
		}

		@Override
		public void run() {
			if (mLongToast != null)
				mLongToast.setText(msgs);
			else
				mLongToast = Toast.makeText(context, msgs, Toast.LENGTH_LONG);
			mLongToast.show();
		}
	}

	private static class ShortRunable implements Runnable {
		Context context;
		String msgs;

		public ShortRunable(Context context, String msgs) {
			this.context = context;
			this.msgs = msgs;
		}

		@Override
		public void run() {
			if (mShortToast != null)
				mShortToast.setText(msgs);
			else
				mShortToast = Toast.makeText(context, msgs, Toast.LENGTH_SHORT);
			mShortToast.show();
		}
	}
}
