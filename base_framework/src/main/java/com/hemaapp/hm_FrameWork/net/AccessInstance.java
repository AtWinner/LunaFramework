package com.hemaapp.hm_FrameWork.net;

import android.content.Context;

import com.hemaapp.hm_FrameWork.PoplarApplication;
import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.util.SharedPreferencesUtil;

/**
 * 保存登录令牌
 * Created by huhu on 2017/12/19.
 */

public class AccessInstance extends PoplarObject {
    private static volatile AccessInstance instance = null;
    private Context mContext;
    private String accessToken;

    public AccessInstance() {
        this.mContext = PoplarApplication.getInstance();
    }

    public static AccessInstance getInstance() {
        if (instance == null) {
            synchronized (AccessInstance.class) {
                if (instance == null) {
                    instance = new AccessInstance();
                }
            }
        }
        return instance;
    }

    public String getAccessToken() {
        if (isNull(accessToken)) {
            synchronized (AccessInstance.class) {
                if (isNull(accessToken)) {
                    accessToken = SharedPreferencesUtil.get(mContext, "AccessToken");
                }
            }
        }
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        SharedPreferencesUtil.save(mContext, "AccessToken", accessToken);
    }
}
