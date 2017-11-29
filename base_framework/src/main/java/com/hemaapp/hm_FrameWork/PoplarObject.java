package com.hemaapp.hm_FrameWork;

import com.hemaapp.hm_FrameWork.util.BaseUtil;
import com.hemaapp.hm_FrameWork.util.HemaLogger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by HuHu on 2017-05-12.
 */

public class PoplarObject {
    /**
     * 打印TAG，类名
     */
    private String TAG;

    public PoplarObject() {
        TAG = getLogTag();
        toString();
    }

    /**
     * 获取打印TAG，即类名
     *
     * @return
     */
    private String getLogTag() {
        return getClass().getSimpleName();
    }

    /**
     * 打印v级别信息
     *
     * @param msg
     */
    protected void log_v(String msg) {
        HemaLogger.v(TAG, msg);
    }

    /**
     * 打印d级别信息
     *
     * @param msg
     */
    protected void log_d(String msg) {
        HemaLogger.d(TAG, msg);
    }

    /**
     * 打印i级别信息
     *
     * @param msg
     */
    protected void log_i(String msg) {
        HemaLogger.i(TAG, msg);
    }

    /**
     * 打印w级别信息
     *
     * @param msg
     */
    protected void log_w(String msg) {
        HemaLogger.w(TAG, msg);
    }

    /**
     * 打印e级别信息
     *
     * @param msg
     */
    protected void log_e(String msg) {
        HemaLogger.e(TAG, msg);
    }

    /**
     * 打印
     *
     * @param msg
     */
    protected void println(Object msg) {
        HemaLogger.println(msg);
    }

    /**
     * 解析时，判断是否为空
     *
     * @param jsonObject
     * @param s
     * @return
     * @throws JSONException
     */
    protected String get(JSONObject jsonObject, String s) throws JSONException {
        return jsonObject.optString(s);
    }

    /**
     * 解析时，判断是否为空
     *
     * @param jsonObject
     * @param s
     * @return 若为空返回0
     * @throws JSONException
     */
    protected int getInt(JSONObject jsonObject, String s) throws JSONException {
        return jsonObject.optInt(s);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return true如果该字符串为null或者"",否则false
     */
    protected boolean isNull(String str) {
        return BaseUtil.isNull(str);
    }
}
