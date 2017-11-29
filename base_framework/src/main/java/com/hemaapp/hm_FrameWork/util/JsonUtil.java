package com.hemaapp.hm_FrameWork.util;

import com.hemaapp.hm_FrameWork.exception.DataParseException;

import org.json.JSONObject;


/**
 * JSON工具类
 */
public class JsonUtil {
	/**
	 * 字符串转JSON
	 * 
	 * @param s
	 *            需要转换的字符串
	 * @return JSONObject
	 * @throws DataParseException
	 */
	public static JSONObject toJsonObject(String s) throws DataParseException {
		if (s != null && s.startsWith("\ufeff")) // 避免低版本utf-8bom头问题
			s = s.substring(1);
		try {
			return new JSONObject(s.trim());
		} catch (Exception e) {
			throw new DataParseException(e);
		}
	}
}
