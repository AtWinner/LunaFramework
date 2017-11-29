package com.hemaapp.hm_FrameWork;

import android.app.Application;

import com.hemaapp.hm_FrameWork.util.HemaLogger;

/**
 * 该项目自定义Application
 */
public class PoplarApplication extends Application {
    private static final String TAG = "PoplarApplication";

    private static PoplarApplication application;

    public static PoplarApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        application = this;
        super.onCreate();
//        HemaImageWorker.getInstance(this);
    }

    @Override
    public void onLowMemory() {
        HemaLogger.i(TAG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        HemaLogger.i(TAG, "onTerminate");
        super.onTerminate();
    }

}
