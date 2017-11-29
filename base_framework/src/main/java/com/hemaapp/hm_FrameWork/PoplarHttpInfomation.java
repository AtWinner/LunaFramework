package com.hemaapp.hm_FrameWork;

/**
 * 请求信息抽象接口
 */
public interface PoplarHttpInfomation {

	/**
	 * @return 对应NetTask的id
	 */
	public int getId();

	/**
	 * @return 请求地址
	 */
	public String getUrlPath();

	/**
	 * @return 请求描述
	 */
	public String getDescription();

	/**
	 * @return 是否是根路径
	 */
	public boolean isRootPath();
}
