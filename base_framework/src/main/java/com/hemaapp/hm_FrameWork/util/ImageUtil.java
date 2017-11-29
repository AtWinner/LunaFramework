package com.hemaapp.hm_FrameWork.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;

import com.hemaapp.PoplarConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 图片工具类
 */
public class ImageUtil {
	private static final String TAG = "HemaImageUtil";
	public static final int WIDTH = 1;
	public static final int HEIGHT = 2;
	public static final int QUALITY = 100;// 图片清晰度
	public static final CompressFormat FORMAT = CompressFormat.JPEG;// 图片格式

	/**
	 * 获取圆角(本地图片,按指定大小)
	 *
	 * @param path
	 *            本地路径
	 * @param roundPx
	 *            圆角度 值越大越圆，自己掌握
	 * @param width
	 *            想要的宽度
	 * @param height
	 *            想要的高度
	 * @return
	 * @throws IOException
	 */
	public static Bitmap getRoundedCornerBitmap(String path, float roundPx,
			int width, int height) throws IOException {
		try {
			Bitmap bitmap = getLocalPicture(path, height, width);
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			final RectF rectF = new RectF(rect);

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
			return output;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取圆角图片
	 *
	 * @param bitmap
	 *            原图片
	 * @param roundPx
	 *            圆角度 值越大越圆，自己掌握
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * 删除外部存储的缓存图片
	 */
	public static void deletePicInExternalMemoryCache(Context context,
			String name) {
		File file = context.getExternalCacheDir();
		if (file == null)
			return;
		String path = file.getPath();
		String filepath = path + "/images/" + name;
		FileUtil.deleteFile(filepath);
	}

	/**
	 * 保存图片到外部存储的缓存
	 */
	public static boolean savePicToExternalMemoryCache(Bitmap bitmap,
			Context context, String name) {
		File file = context.getExternalCacheDir();
		if (file == null)
			return false;
		String path = file.getPath();
		String folder = path.replace(FileUtil.getExternalMemoryPath(), "")
				+ "/images/";
		return savePicToExternalMemory(bitmap, folder, name);
	}

	/**
	 * 保存图片到外部存储
	 *
	 * @param bitmap
	 * @param folder
	 *            文件夹名称
	 * @param name
	 *            图片名
	 * @return boolean
	 */
	public static boolean savePicToExternalMemory(Bitmap bitmap, String folder,
			String name) {
		String path = FileUtil.getExternalMemoryPath();
		return (path == null) ? false : savePic(bitmap, path + folder, name);
	}

	/**
	 * 保存图片
	 *
	 * @param bitmap
	 * @param path
	 *            保存路径
	 * @param name
	 *            图片名
	 * @return boolean
	 */
	public static boolean savePic(Bitmap bitmap, String path, String name) {
		if (bitmap == null || path == null || name == null)
			return false;

		File dir = new File(path);
		if (!dir.exists())
			dir.mkdir();
		String savepath = path + name;
		File file = new File(savepath);
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			if (bitmap.compress(FORMAT, QUALITY, out)) {
				out.flush();
			}
		} catch (IOException e) {
			return false;
		}
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 按指定目录压缩图片    不提倡使用  建议使用 {@link #compressPictureDepthWithSaveDir}
	 *
	 * @param path
	 *            图片地址目录
	 * @param reheight
	 *            想要的高度
	 * @param rewidth
	 *            想要的宽度
	 * @param quality
	 *            图片清晰度（0--100 100为无失真压缩），若传0--100以外值则为默认值100
	 * @param savedir
	 *            保存目录
	 * @return String
	 * @throws IOException
	 *
	 */
	@Deprecated
	public static String compressPictureWithSaveDir(String path, int reheight,
			int rewidth, int quality, String savedir, Context context)
			throws IOException {
		Bitmap bitmap = getLocPicByDBYS(path, reheight, rewidth);
		File dir = new File(savedir);
		if (!dir.exists())
			dir.mkdirs();
		// 保存
		String savepath = savedir + BaseUtil.getFileName() + "_"
				+ UUID.randomUUID().toString() + ".jpg";// 保存路径
		File file = new File(savepath);
		FileOutputStream out = new FileOutputStream(file);
		if (quality >= 0 && quality <= 100) {
			if (bitmap.compress(FORMAT, quality, out)) {
				out.flush();
				out.close();
			}
		} else {
			if (bitmap.compress(FORMAT, QUALITY, out)) {
				out.flush();
				out.close();
			}
		}
		bitmap.recycle();
		System.gc();
		HemaLogger.d(TAG, "The new picture's path is " + savepath);
		return savepath;
	}

	/**
	 *
	 * 按指定目录压缩图片 可设置压缩后图片的最大值 默认为200k
	 * @param path
	 * @param reheight
	 * @param rewidth
	 * @param quality
	 * @param savedir
	 * @param context
     * @return
     * @throws IOException
     */
	public static String compressPictureDepthWithSaveDir(String path, int reheight,
			int rewidth, int quality, String savedir, Context context)
			throws IOException {
		Bitmap bitmap = ImageUtil.getLocPicByDBYS(path, reheight, rewidth);
		File dir = new File(savedir);
		if (!dir.exists())
			dir.mkdirs();
		// 保存
		String savepath = savedir + BaseUtil.getFileName() + "_"
				+ UUID.randomUUID().toString() + ".jpg";// 保存路径
		File file = new File(savepath);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int options ;
		if(quality >= 0 && quality <= 100){
			options = quality;
		}else{
			options =  ImageUtil.QUALITY;
		}
		bitmap.compress(CompressFormat.JPEG, options, bos);
		while (bos.toByteArray().length / 1024 > PoplarConfig.MAX_IMAGE_SIZE) {
			bos.reset();// 置为空
			// 每次都减少10
			options -= 10;
			// 压缩options%
			bitmap.compress(CompressFormat.JPEG, options, bos);
		}
		FileOutputStream out = new FileOutputStream(file);
		bos.writeTo(out);
		bos.flush();
		bos.close();
		out.flush();
        out.close();
		bitmap.recycle();
		System.gc();
		return savepath;
	}

	/**
	 * 压缩图片
	 *
	 * @param path
	 *            图片地址
	 * @param reheight
	 *            想要的高度
	 * @param rewidth
	 *            想要的宽度
	 * @param quality
	 *            图片清晰度（0--100 100为无失真压缩），若传0--100以外值则为默认值100
	 * @return String
	 * @throws IOException
	 */
	public static String compressPicture(String path, int reheight,
			int rewidth, int quality, Context context) throws IOException {
		Bitmap bitmap = getLocPicByDBYS(path, reheight, rewidth);
		// 设置压缩图片保存目录
		String savedir = FileUtil.getTempFileDir(context);
		File dir = new File(savedir);
		if (!dir.exists())
			dir.mkdir();
		// 保存
		String savepath = savedir + BaseUtil.getFileName() + "_"
				+ UUID.randomUUID().toString() + ".jpg";// 保存路径
		File file = new File(savepath);
		FileOutputStream out = new FileOutputStream(file);
		if (quality >= 0 && quality <= 100) {
			if (bitmap.compress(FORMAT, quality, out)) {
				out.flush();
				out.close();
			}
		} else {
			if (bitmap.compress(FORMAT, QUALITY, out)) {
				out.flush();
				out.close();
			}
		}
		bitmap.recycle();
		System.gc();
		HemaLogger.d(TAG, "The new picture's path is " + savepath);
		return savepath;
	}

	/**
	 * 以指定宽度或者高度 获取本地图片（等比压缩）
	 *
	 * @param path
	 *            图片路径
	 * @param want
	 *            想要的宽度或高度
	 * @param type
	 *            1宽2高
	 * @return Bitmap
	 * @throws IOException
	 */
	public static Bitmap getLocPicByWidthorHeight(String path, int want,
			int type) throws IOException {
		if (type != WIDTH && type != HEIGHT)
			return null;

		Options options = new Options();
		options.inJustDecodeBounds = true;// 只获取图片大小，不返回Bitmap
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		int inSampleSize = getInSampleSize(path, options, want, type);

		options.inSampleSize = inSampleSize;
		options.inJustDecodeBounds = false;// 返回Bitmap

		FileInputStream fis = new FileInputStream(path);
		bitmap = BitmapFactory.decodeStream(fis, null, options);// 此方式节省了java层内存
		if (bitmap == null)
			return null;
		fis.close();
		int degree = getPictureDegree(path);
		float scale_w = getScale(want, bitmap.getWidth());
		float scale_h = getScale(want, bitmap.getHeight());
		float scale = scale_w > scale_h ? scale_h : scale_w;

		Matrix matrix = new Matrix();
		matrix.postRotate(degree);// 角度旋转
		matrix.postScale(scale, scale);
		HemaLogger.d(TAG, "The picture path is " + path);
		HemaLogger.d(TAG, "The picture inSampleSize is " + inSampleSize);
		HemaLogger.d(TAG, "The picture scale is " + scale);
		HemaLogger.d(TAG, "The picture degree is " + getPictureDegree(path));
		Bitmap bit = Bitmap.createBitmap(bitmap, 0, 0, options.outWidth,
				options.outHeight, matrix, false);
		return bit;
	}

	/**
	 * 等比压缩获取图片
	 *
	 * @param path
	 *            图片路径
	 * @param wantHeight
	 *            想要的高度
	 * @param wantWidth
	 *            想要的宽度
	 * @return Bitmap
	 * @throws IOException
	 */
	public static Bitmap getLocPicByDBYS(String path, int wantHeight,
			int wantWidth) throws IOException {
		Options options = new Options();
		options.inJustDecodeBounds = true;// 只获取图片大小，不返回Bitmap
		BitmapFactory.decodeFile(path, options);

		int wInSampleSize = getInSampleSize(path, options, wantWidth, WIDTH);
		int hInSampleSize = getInSampleSize(path, options, wantHeight, HEIGHT);

		if (wInSampleSize > hInSampleSize)
			return getLocPicByWidthorHeight(path, wantWidth, WIDTH);
		else
			return getLocPicByWidthorHeight(path, wantHeight, HEIGHT);
	}

	/**
	 * 获取本地图片(处理了图片旋转)
	 *
	 * @param path
	 *            图片路径
	 * @return Bitmap
	 * @throws IOException
	 */
	public static Bitmap getLocalPicture(String path) throws IOException {
		Options options = new Options();
		options.inJustDecodeBounds = false;// 返回Bitmap
		FileInputStream fis = new FileInputStream(path);
		Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);// 此方式节省了java层内存
		if (bitmap == null)
			return null;
		fis.close();
		int degree = getPictureDegree(path);
		if (degree == 0)
			return bitmap;
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);// 角度旋转
		HemaLogger.d(TAG, "The picture path is " + path);
		HemaLogger.d(TAG, "The picture degree is " + getPictureDegree(path));
		Bitmap bit = Bitmap.createBitmap(bitmap, 0, 0, options.outWidth,
				options.outHeight, matrix, true);
		return bit;
	}

	/**
	 * 以指定大小获取本地图片
	 *
	 * @param path
	 *            图片路径
	 * @param wantHeight
	 *            想要的高度
	 * @param wantWidth
	 *            想要的宽度
	 * @return Bitmap
	 * @throws IOException
	 */
	public static Bitmap getLocalPicture(String path, int wantHeight,
			int wantWidth) throws IOException {
		try {
			Options options = new Options();
			options.inJustDecodeBounds = true;// 只获取图片大小，不返回Bitmap
			Bitmap bitmap = BitmapFactory.decodeFile(path, options);

			int wInSampleSize = getInSampleSize(path, options, wantWidth, WIDTH);
			int hInSampleSize = getInSampleSize(path, options, wantHeight,
					HEIGHT);
			int inSampleSize = wInSampleSize < hInSampleSize ? wInSampleSize
					: hInSampleSize;

			options.inSampleSize = inSampleSize;
			options.inJustDecodeBounds = false;// 返回Bitmap
			FileInputStream fis = new FileInputStream(path);
			bitmap = BitmapFactory.decodeStream(fis, null, options);// 此方式节省了java层内存
			if (bitmap == null)
				return null;
			fis.close();
			int degree = getPictureDegree(path);
			float scale_w = (degree == 0 || degree == 180) ? getScale(
					wantWidth, bitmap.getWidth()) : getScale(wantWidth,
					bitmap.getHeight());
			float scale_h = (degree == 0 || degree == 180) ? getScale(
					wantHeight, bitmap.getHeight()) : getScale(wantHeight,
					bitmap.getWidth());

			Matrix matrix = new Matrix();
			if (degree == 0 || degree == 180)
				matrix.postScale(scale_w, scale_h);
			else
				matrix.postScale(scale_h, scale_w);
			matrix.postRotate(degree);// 角度旋转
			HemaLogger.d(TAG, "The picture path is " + path);
			HemaLogger.d(TAG, "The picture inSampleSize is " + inSampleSize);
			HemaLogger.d(TAG, "The picture scale is " + scale_w + "-----"
					+ scale_h);
			HemaLogger
					.d(TAG, "The picture degree is " + getPictureDegree(path));
			Bitmap bit = Bitmap.createBitmap(bitmap, 0, 0, options.outWidth,
					options.outHeight, matrix, true);
			return bit;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取图片旋转角度
	 *
	 * @param path
	 *            本地图片地址
	 * @return
	 * @throws IOException
	 */
	public static int getPictureDegree(String path) throws IOException {
		int degree = 0;
		if(BaseUtil.isNull(path)){  //如果为null 不进行旋转处理
			return degree;
		}
		ExifInterface exifInterface = new ExifInterface(path);
		int orientation = exifInterface
				.getAttributeInt(ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_NORMAL);
		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_90:
			degree = 90;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			degree = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			degree = 270;
			break;
		}
		return degree;
	}

	/**
	 * 获取等比压缩后的大小
	 *
	 * @param path
	 *            本地图片地址
	 * @param wantWidth
	 *            想要的宽度
	 * @param wantHeight
	 *            想要的高度
	 * @return {宽,高}
	 * @throws IOException
	 */
	public static int[] getSizeByDBYS(String path, int wantWidth, int wantHeight)
			throws Exception {
		int[] size = { 0, 0 };
		Options options = new Options();
		options.inJustDecodeBounds = true;// 只获取图片大小，不返回Bitmap
		BitmapFactory.decodeFile(path, options);
		int degree = getPictureDegree(path);

		int inSampleSize = getSampleSizeByDBYS(path, options, wantWidth,
				wantHeight);

		int width = (degree == 0 || degree == 180) ? options.outWidth
				/ inSampleSize : options.outHeight / inSampleSize;
		int height = (degree == 0 || degree == 180) ? options.outHeight
				/ inSampleSize : options.outWidth / inSampleSize;

		float scale_w = (float) wantWidth / (float) width;
		float scale_h = (float) wantHeight / (float) height;
		float scale = scale_w > scale_h ? scale_h : scale_w;

		size[0] = (int) (width * scale);
		size[1] = (int) (height * scale);

		return size;
	}

	private static int getSampleSizeByDBYS(String path, Options options,
			int wantWidth, int wantHeight) throws IOException {
		int wInSampleSize = getInSampleSize(path, options, wantWidth, WIDTH);
		int hInSampleSize = getInSampleSize(path, options, wantHeight, HEIGHT);
		int inSampleSize = wInSampleSize > hInSampleSize ? wInSampleSize
				: hInSampleSize;
		return inSampleSize;
	}

	private static int getInSampleSize(String path, Options options, int want,
			int type) throws IOException {
		int degree = getPictureDegree(path);
		int size = 0;
		switch (type) {
		case WIDTH:
			size = (degree == 0 || degree == 180) ? options.outWidth
					: options.outHeight;
			break;
		case HEIGHT:
			size = (degree == 0 || degree == 180) ? options.outHeight
					: options.outWidth;
			break;
		}
		int inSampleSize = size / want;
		return inSampleSize;
	}

	private static float getScale(int want, int size) {
		return (size > want) ? (float) want / (float) size : 1.0f;
	}
}
