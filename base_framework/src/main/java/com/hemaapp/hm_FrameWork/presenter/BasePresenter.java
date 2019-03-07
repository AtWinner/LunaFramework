package com.hemaapp.hm_FrameWork.presenter;

import android.content.Context;

import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.net.BaseNetTask;
import com.hemaapp.hm_FrameWork.net.BaseNetTaskExecuteListener;
import com.hemaapp.hm_FrameWork.net.BaseNetWorker;
import com.hemaapp.hm_FrameWork.result.BaseResult;

/**
 * Presenter基础类
 */

public abstract class BasePresenter<V extends BaseView> extends PoplarObject implements BaseExecuteListener {
    protected V mBaseView;
    protected Context mContext;
    protected BaseNetWorker netWorker;

    public BasePresenter(Context mContext, V mBaseView) {
        this.mContext = mContext;
        this.mBaseView = mBaseView;
    }

    /**
     * 获取网络请求工具类
     */
    public BaseNetWorker getNetWorker() {
        if (netWorker == null) {
            netWorker = initNetWorker();
            netWorker.setOnTaskExecuteListener(new NetTaskExecuteListener(mContext));
        }
        return netWorker;
    }

    // 杀掉网络线程
    public void stopNetThread() {
        if (netWorker != null) {
            netWorker.cancelTasks();
        }
    }

    private class NetTaskExecuteListener extends BaseNetTaskExecuteListener {

        public NetTaskExecuteListener(Context context) {
            super(context);
        }

        @Override
        public void onPreExecute(BaseNetWorker netWorker, BaseNetTask netTask) {
            if (mContext != null)
                callBeforeDataBack(netTask);
        }

        @Override
        public void onPostExecute(BaseNetWorker netWorker, BaseNetTask netTask) {
            if (mContext != null)
            callAfterDataBack(netTask);
        }

        @Override
        public void onServerSuccess(BaseNetWorker netWorker, BaseNetTask netTask, BaseResult baseResult) {
            if (mContext != null)
            callBackForServerSuccess(netTask, baseResult);
        }

        @Override
        public void onServerFailed(BaseNetWorker netWorker, BaseNetTask netTask, BaseResult baseResult) {
            if (mContext != null)
            callBackForServerFailed(netTask, baseResult);
        }

        @Override
        public void onExecuteFailed(BaseNetWorker netWorker, BaseNetTask netTask, int failedType) {
            if (mContext != null)
            callBackForGetDataFailed(netTask, failedType);
        }

        @Override
        public boolean onAutoLoginFailed(BaseNetWorker netWorker, BaseNetTask netTask, int failedType, BaseResult baseResult) {
            return onAutoLoginFailedPresenter(netWorker, netTask, failedType, baseResult);
        }


    }

    public boolean isNull(String text) {
        if (null == text || "".equals(text)) {
            return true;
        }
        return false;
    }

    public void showProgressDialog(int text) {
        if (mBaseView != null) {
            mBaseView.showProgressDialog(text);
        }
    }

    public void showProgressDialog(String text) {
        if (mBaseView != null) {
            mBaseView.showProgressDialog(text);
        }
    }

    public void showTextDialog(int text) {
        if (mBaseView != null) {
            mBaseView.showTextDialog(text);
        }
    }

    public void showTextDialog(String text) {
        if (mBaseView != null) {
            mBaseView.showTextDialog(text);
        }
    }

    public void cancelProgressDialog() {
        if (mBaseView != null) {
            mBaseView.cancelProgressDialog();
        }
    }

    public void releasePresenter() {
        mContext = null;
        mBaseView = null;
        netWorker.cancelTasks();
        netWorker = null;
    }
}
