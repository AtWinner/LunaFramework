package com.hemaapp.hm_FrameWork.presenter;

import com.hemaapp.hm_FrameWork.net.BaseNetTask;
import com.hemaapp.hm_FrameWork.net.BaseNetWorker;
import com.hemaapp.hm_FrameWork.result.BaseResult;

/**
 * Created by HuHu on 2017-05-18.
 */

public interface BaseExecuteListener {
    /**
     * 初始化NetWorker
     */
    BaseNetWorker initNetWorker();

    /**
     * 服务器处理成功
     *
     * @param netTask
     * @param baseResult
     */
    void callBackForServerSuccess(BaseNetTask netTask, BaseResult baseResult);

    /**
     * 服务器处理失败
     *
     * @param netTask
     * @param baseResult
     */
    void callBackForServerFailed(BaseNetTask netTask, BaseResult baseResult);

    /**
     * 获取数据失败
     *
     * @param netTask
     * @param failedType 失败原因
     *                   <p>
     *                   See {@link BaseNetWorker#FAILED_DATAPARSE
     *                   BaseNetWorker.FAILED_DATAPARSE},
     *                   {@link BaseNetWorker#FAILED_HTTP BaseNetWorker.FAILED_HTTP},
     *                   {@link BaseNetWorker#FAILED_NONETWORK
     *                   BaseNetWorker.FAILED_HTTP}
     *                   </p>
     */
    void callBackForGetDataFailed(BaseNetTask netTask, int failedType);

    /**
     * 自动登录失败
     *
     * @param netWorker
     * @param netTask
     * @param failedType 如果failedType为0表示服务器处理失败,其余参照
     *                   {@link BaseNetWorker#FAILED_DATAPARSE
     *                   BaseNetWorker.FAILED_DATAPARSE},
     *                   {@link BaseNetWorker#FAILED_HTTP BaseNetWorker.FAILED_HTTP},
     *                   {@link BaseNetWorker#FAILED_NONETWORK
     *                   BaseNetWorker.FAILED_NONETWORK}
     * @param baseResult 执行结果(仅当failedType为0时有值,其余为null)
     * @return true表示拦截该任务执行流程,
     * 不会继续调用callBackForServerFailed或者callBackForGetDataFailed方法;
     * false反之
     */
    boolean onAutoLoginFailedPresenter(BaseNetWorker netWorker, BaseNetTask netTask, int failedType, BaseResult baseResult);

    void callBeforeDataBack(BaseNetTask netTask);

    void callAfterDataBack(BaseNetTask netTask);
}
