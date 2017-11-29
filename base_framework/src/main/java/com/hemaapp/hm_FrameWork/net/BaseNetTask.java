package com.hemaapp.hm_FrameWork.net;

import com.hemaapp.hm_FrameWork.PoplarHttpInfomation;
import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.exception.DataParseException;

import org.json.JSONObject;

import java.util.HashMap;


/**
 * 网络请求任务
 */
public abstract class BaseNetTask<T extends PoplarObject> extends PoplarObject {

    private PoplarHttpInfomation httpInformation;

    private HashMap<String, String> params;
    private HashMap<String, String> files;
    private Class<T> clazz;

    private int tryTimes = 0;

    /**
     * 网络请求任务
     *
     * @param httpInformation 请求信息
     * @param params          任务参数集(参数名,参数值)
     * @param files           任务文件集(参数名,文件的本地路径)
     */
    public BaseNetTask(PoplarHttpInfomation httpInformation, HashMap<String, String> params,
                       HashMap<String, String> files) {
        this(httpInformation, params);
        this.files = files;
    }

    /**
     * 网络请求任务
     *
     * @param httpInformation 请求信息
     * @param params          任务参数集(参数名，参数值)
     */
    public BaseNetTask(PoplarHttpInfomation httpInformation, HashMap<String, String> params) {
        this.httpInformation = httpInformation;
        this.params = params;
    }

    /**
     * 此方法将JSONObject解析为我们自定义的实体类
     *
     * @param jsonObject
     * @return
     * @throws DataParseException
     */
    public abstract Object parse(JSONObject jsonObject)
            throws DataParseException;

    /**
     * 获取 任务ID
     *
     * @return 任务ID
     */
    public int getId() {
        return httpInformation.getId();
    }

    /**
     * 获取任务参数集(参数名,参数值)
     *
     * @return 任务参数集(参数名, 参数值)
     */
    public HashMap<String, String> getParams() {
        return params;
    }

    /**
     * 获取请求地址
     *
     * @return 请求地址
     */
    public String getPath() {
        return httpInformation.getUrlPath();
    }

    /**
     * 获取任务文件集(参数名,文件的本地路径)
     *
     * @return 任务文件集(参数名, 文件的本地路径)
     */
    public HashMap<String, String> getFiles() {
        return files;
    }

    /**
     * 获取尝试次数
     *
     * @return 尝试次数
     */
    public int getTryTimes() {
        return tryTimes;
    }

    public void setTryTimes(int tryTimes) {
        this.tryTimes = tryTimes;
    }

    /**
     * 获取任务描述,如"获取xx列表"
     *
     * @return 任务描述, 如"获取xx列表"
     */
    public String getDescription() {
        return httpInformation.getDescription();
    }

    public PoplarHttpInfomation getHttpInformation() {
        return httpInformation;
    }
}
