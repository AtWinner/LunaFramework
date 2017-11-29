package com.hemaapp.hm_FrameWork.task;

import com.hemaapp.hm_FrameWork.PoplarHttpInfomation;
import com.hemaapp.hm_FrameWork.exception.DataParseException;
import com.hemaapp.hm_FrameWork.net.BaseNetTask;
import com.hemaapp.hm_FrameWork.result.ArrayParse;

import org.json.JSONObject;

import java.util.HashMap;


/**
 * 网络任务，分装无返回值，返回单个对象多个对象及分页
 */
public class ExecuteNetTask<T> extends BaseNetTask {

    private Class<T> classType;

    public ExecuteNetTask(PoplarHttpInfomation information,
                          HashMap<String, String> params, Class<T> classType) {
        super(information, params);
        this.classType = classType;
    }

    public ExecuteNetTask(PoplarHttpInfomation information,
                          HashMap<String, String> params, HashMap<String, String> files, Class<T> classType) {
        super(information, params, files);
        this.classType = classType;
    }

    @Override
    public Object parse(JSONObject jsonObject) throws DataParseException {
        return new ArrayParse<>(jsonObject, classType);
    }

}