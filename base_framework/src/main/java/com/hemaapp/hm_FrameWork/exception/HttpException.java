package com.hemaapp.hm_FrameWork.exception;

/**
 * 网络请求异常
 */
public class HttpException extends Exception {

	/**
	 * @param e
	 */
	public HttpException(Exception e) {
		e.printStackTrace();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
