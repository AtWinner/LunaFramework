package com.hemaapp.hm_FrameWork.task;

import com.hemaapp.hm_FrameWork.PoplarHttpInfomation;
import com.hemaapp.hm_FrameWork.exception.DataParseException;
import com.hemaapp.hm_FrameWork.net.BaseNetTask;
import com.hemaapp.hm_FrameWork.result.BaseResult;

import org.json.JSONObject;

import java.util.HashMap;


/**
 * Created by CoderHu on 2017-03-15.
 */

public class CurrentTask extends BaseNetTask {
    public CurrentTask(PoplarHttpInfomation information, HashMap<String, String> params) {
        super(information, params);
    }

    public CurrentTask(PoplarHttpInfomation information, HashMap<String, String> params, HashMap<String, String> files) {
        super(information, params, files);
    }

    @Override
    public Object parse(JSONObject jsonObject) throws DataParseException {
        return new BaseResult(jsonObject);
    }
}
