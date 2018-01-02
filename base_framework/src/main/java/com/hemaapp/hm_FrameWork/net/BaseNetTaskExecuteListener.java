package com.hemaapp.hm_FrameWork.net;

import android.content.Context;
import android.widget.Toast;

import com.hemaapp.PoplarConfig;
import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.PoplarUser;
import com.hemaapp.hm_FrameWork.R;
import com.hemaapp.hm_FrameWork.result.ArrayResult;
import com.hemaapp.hm_FrameWork.result.BaseResult;

import java.util.ArrayList;

public abstract class BaseNetTaskExecuteListener extends PoplarObject implements BaseNetWorker.OnTaskExecuteListener {
    public Context mContext;
    private ArrayList<BaseNetTask> failedTasks;// token失效任务队列

    public BaseNetTaskExecuteListener(Context context) {
        mContext = context;
    }

    @Override
    public void onNewExecuteFailed(BaseNetWorker netWorker, BaseNetTask netTask,
                                   int failedType) {
        if (PoplarConfig.TOAST_NET_ENABLE)
            switch (failedType) {
                case BaseNetWorker.FAILED_DATAPARSE:
                    Toast.makeText(mContext, R.string.msg_data, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case BaseNetWorker.FAILED_HTTP:
                    Toast.makeText(mContext, R.string.msg_http, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case BaseNetWorker.FAILED_NONETWORK:
                    Toast.makeText(mContext, R.string.msg_nonet, Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
            }

        int taskId = netTask.getId();
        if (taskId == PoplarConfig.ID_LOGIN || taskId == PoplarConfig.ID_THIRDSAVE) {// 登录任务
            if (failedTasks != null && failedTasks.size() > 0) {// token失效的自动登录，所有任务执行失败
                for (BaseNetTask failedTask : failedTasks) {
                    if (!onAutoLoginFailed(netWorker,
                            failedTask, failedType, null))
                        onExecuteFailed(netWorker, netTask, failedType);
                }
                failedTasks.clear();
            } else {
                onExecuteFailed(netWorker, netTask, failedType);
            }
        } else {
            onExecuteFailed(netWorker, netTask,
                    failedType);
        }
    }

    @Override
    public void onExecuteSuccess(BaseNetWorker worker, BaseNetTask task,
                                 Object result) {

        BaseResult baseResult = (BaseResult) result;
        BaseNetTask netTask = (BaseNetTask) task;
        BaseNetWorker netWorker = (BaseNetWorker) worker;
        if (baseResult.isSuccess()) {// 服务器处理成功
            int taskId = netTask.getId();
            if (taskId == PoplarConfig.ID_LOGIN || taskId == PoplarConfig.ID_THIRDSAVE) {// 如果为登录接口，保存用户信息
                @SuppressWarnings("unchecked")
                ArrayResult<PoplarUser> uResult = (ArrayResult<PoplarUser>) baseResult;
                PoplarUser user = uResult.getObjects().get(0);
                String token = user.getToken();
                if (failedTasks != null && failedTasks.size() > 0) {// token失效时的登录，只再次执行失败任务，不做其他操作
                    for (BaseNetTask failedTask : failedTasks) {
                        failedTask.getParams().put("token", token);
                        netWorker.executeTask(failedTask);
                    }
                    failedTasks.clear();
//                    checkUpdate(user);
//                    return;
                }
            }
            onServerSuccess(netWorker, netTask, baseResult);
        } else {// 服务器处理失败
            if (baseResult.getError_code() == 100) {// 访问令牌失效
                if (failedTasks == null)
                    failedTasks = new ArrayList<BaseNetTask>();
                failedTasks.add(netTask);
                if (failedTasks.size() <= 1) {// 确保token失效登录只执行一次
                    netWorker.getAccessToken();
                }
            } else if (baseResult.getError_code() == 101) {// token失效自动登录，并重新执行该任务
                if (failedTasks == null)
                    failedTasks = new ArrayList<BaseNetTask>();
                failedTasks.add(netTask);
                if (failedTasks.size() <= 1) {// 确保token失效登录只执行一次
                    if (!netWorker.thirdSave())// 如果不是第三方登录则调用框架自身登录方法
                        netWorker.clientLogin();
                }
            } else {
                int taskId = netTask.getId();
                if (taskId == PoplarConfig.ID_LOGIN
                        || taskId == PoplarConfig.ID_THIRDSAVE) {// 登录任务
                    if (failedTasks != null && failedTasks.size() > 0) {// token失效的自动登录，所有任务执行失败
                        for (BaseNetTask failedTask : failedTasks) {
                            if (!onAutoLoginFailed((BaseNetWorker) netWorker,
                                    failedTask, 0, baseResult))
                                onServerFailed(netWorker, netTask, baseResult);
                        }
                        failedTasks.clear();
                    } else {
                        onServerFailed(netWorker, netTask, baseResult);
                    }
                } else {
                    onServerFailed(netWorker, netTask, baseResult);
                }
            }

        }
    }

    /**
     * Runs on the UI thread before the task run.
     *
     * @param netWorker
     * @param netTask
     */
    public abstract void onPreExecute(BaseNetWorker netWorker,
                                      BaseNetTask netTask);

    /**
     * Runs on the UI thread after the task run.
     *
     * @param netWorker
     * @param netTask
     */
    public abstract void onPostExecute(BaseNetWorker netWorker,
                                       BaseNetTask netTask);

    /**
     * 服务器处理成功
     *
     * @param netWorker
     * @param netTask
     * @param baseResult
     */
    public abstract void onServerSuccess(BaseNetWorker netWorker,
                                         BaseNetTask netTask, BaseResult baseResult);

    /**
     * 服务器处理失败
     *
     * @param netWorker
     * @param netTask
     * @param baseResult
     */
    public abstract void onServerFailed(BaseNetWorker netWorker,
                                        BaseNetTask netTask, BaseResult baseResult);

    /**
     * Runs on the UI thread when the task run failed.
     *
     * @param netWorker
     * @param netTask
     * @param failedType the type of cause the task failed.
     *                   <p>
     *                   See {@link BaseNetWorker#FAILED_DATAPARSE
     *                   BaseNetWorker.FAILED_DATAPARSE},
     *                   {@link BaseNetWorker#FAILED_HTTP BaseNetWorker.FAILED_HTTP},
     *                   {@link BaseNetWorker#FAILED_NONETWORK
     *                   BaseNetWorker.FAILED_NONETWORK}
     *                   </p>
     */
    public abstract void onExecuteFailed(BaseNetWorker netWorker,
                                         BaseNetTask netTask, int failedType);

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
     * @return true表示拦截该任务执行流程, 不会继续调用onExecuteFailed或者onServerFailed方法; false反之
     */
    public abstract boolean onAutoLoginFailed(BaseNetWorker netWorker,
                                              BaseNetTask netTask, int failedType, BaseResult baseResult);
}
