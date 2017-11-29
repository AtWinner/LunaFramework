package com.hemaapp.hm_FrameWork.result;

import android.annotation.TargetApi;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hemaapp.hm_FrameWork.exception.DataParseException;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 对HemaArrayResult的进一步扩展，使用反射处理json数据，减少代码数量
 */
public class ArrayParse<T> extends ArrayResult<T> {
    public ArrayParse(JSONObject jsonObject, Class<T> tClass) throws DataParseException {
        super(jsonObject, tClass);
    }

    @Override
    @TargetApi(19)
    public T parse(JSONObject jsonObject, Class<T> classType) throws DataParseException {
        Object classT = null;
//        classT = new Gson().fromJson(String.valueOf(jsonObject), classType);
        try {
            Constructor e = classType.getConstructor(new Class[]{JSONObject.class});
            e.setAccessible(true);
            classT = e.newInstance(new Object[]{jsonObject});
//            classT = JSON.parseObject(String.valueOf(jsonObject), classType);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException var5) {
            var5.printStackTrace();
        }

        return (T) classT;
    }
}
