package com.hemaapp.hm_FrameWork.exception;

/**
 * 数据解析异常
 */
public class DataParseException extends Exception {

	/**
	 * @param e
	 */
	public DataParseException(Exception e) {
		e.printStackTrace();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
