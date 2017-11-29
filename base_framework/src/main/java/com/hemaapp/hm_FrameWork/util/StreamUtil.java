package com.hemaapp.hm_FrameWork.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * 流操作工具类
 */
public class StreamUtil {
	private static final String TAG = "HemaStreamUtil";

	/**
	 * 流转字符串
	 * 
	 * @param is
	 *            需要转换的输入流
	 * @return String
	 * @throws IOException
	 */
	public static String iputStreamToString(InputStream is) throws Exception {
		if (is == null) {
			HemaLogger.w(TAG, "服务器返回流为空");
			throw new Exception("服务器返回流为空");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		try {
			is.close();
		} catch (IOException e) {
			HemaLogger.w(TAG, "when close inputStream");
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 *
	 * @param is  需要转换的流
	 * @return  byte[]
	 * @throws IOException
     */
	public static byte[] read(InputStream is) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = is.read(buffer)) != -1)
			outputStream.write(buffer, 0, len);
		try {
			is.close();
		} catch (IOException e) {
			HemaLogger.w(TAG, "when close inputStream");
			e.printStackTrace();
		}
		return outputStream.toByteArray();
	}

}
