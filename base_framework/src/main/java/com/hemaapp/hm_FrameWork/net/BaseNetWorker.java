package com.hemaapp.hm_FrameWork.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hemaapp.PoplarConfig;
import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.exception.DataParseException;
import com.hemaapp.hm_FrameWork.exception.HttpException;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 网络请求发送器
 */
public abstract class BaseNetWorker extends PoplarObject {
    /**
     * 请求成功(-1)
     */
    protected static final int SUCCESS = -1;
    /**
     * 请求异常(-2)
     */
    public static final int FAILED_HTTP = -2;
    /**
     * 数据异常(-3)
     */
    public static final int FAILED_DATAPARSE = -3;
    /**
     * 无网络(-4)
     */
    public static final int FAILED_NONETWORK = -4;
    /**
     * 获取数据前显示
     */
    private static final int BEFORE = -5;

    protected Context context;
    private EventHandler eventHandler;
    private NetThread netThread;
    private OnTaskExecuteListener onTaskExecuteListener;

    public BaseNetWorker(Context mContext) {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            eventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            eventHandler = new EventHandler(this, looper);
        } else {
            eventHandler = null;
        }

        this.context = mContext.getApplicationContext();
    }

    /**
     * 发送post请求并且获取数据.该方法可发送文件数据
     *
     * @param task 网络请求任务new BaseNetTask(任务ID,任务URL, 任务参数集(参数名,参数值))
     */
    public void executeTask(BaseNetTask task) {
        if (hasNetWork()) {
            synchronized (this) {
                if (netThread == null) {
                    netThread = new NetThread(task, false);
                    netThread.start();
                    log_d("网络线程不存在或已执行完毕,开启新线程：" + netThread.getName());
                } else {
                    log_d(netThread.getName() + "执行中,添加网络任务");
                    netThread.addTask(task);
                }
            }
        } else {
            if (onTaskExecuteListener != null) {
                onTaskExecuteListener.onPostExecute(this, task);
                onTaskExecuteListener.onNewExecuteFailed(this, task,
                        FAILED_NONETWORK);
            }
        }
    }

    /**
     * 发送post请求并且获取数据.该方法可发送文件数据 AccessToken
     *
     * @param task 网络请求任务new BaseNetTask(任务ID,任务URL, 任务参数集(参数名,参数值))
     */
    public void executeAccessTokenTask(BaseNetTask task) {
        if (hasNetWork()) {
            synchronized (this) {
                if (netThread == null) {
                    netThread = new NetThread(task, true);
                    netThread.start();
                    log_d("网络线程不存在或已执行完毕,开启新线程：" + netThread.getName());
                } else {
                    log_d(netThread.getName() + "执行中,添加网络任务");
                    netThread.addTask(task);
                }
            }
        } else {
            if (onTaskExecuteListener != null) {
                onTaskExecuteListener.onPostExecute(this, task);
                onTaskExecuteListener.onNewExecuteFailed(this, task,
                        FAILED_NONETWORK);
            }
        }
    }

    /**
     * 判断网络任务是否都已完成
     *
     * @return
     */
    public boolean isNetTasksFinished() {
        synchronized (this) {
            return netThread == null || netThread.tasks.size() <= 0;
        }
    }

    /**
     * 取消网络请求任务
     */
    public void cancelTasks() {
        synchronized (this) {
            if (netThread != null)
                netThread.cancelTasks();
        }
    }

    /**
     * 判断当前是否有可用网络
     *
     * @return 如果有true否则false
     */
    public boolean hasNetWork() {
        ConnectivityManager con = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = con.getActiveNetworkInfo();// 获取可用的网络服务
        return info != null && info.isAvailable();
    }

    private class NetThread extends Thread {
        private ArrayList<BaseNetTask> tasks = new ArrayList<BaseNetTask>();
        private boolean isRun = true;
        private boolean isGetToken;

        NetThread(BaseNetTask task, boolean isGetToken) {
            this.isGetToken = isGetToken;
            tasks.add(task);
            setName("网络线程(" + getName() + ")");
        }

        void addTask(BaseNetTask task) {
            synchronized (BaseNetWorker.this) {
                tasks.add(task);
            }
        }

        void cancelTasks() {
            synchronized (BaseNetWorker.this) {
                tasks.clear();
                netThread = null;
                isRun = false;
            }
        }

        boolean isHaveTask() {
            return tasks.size() > 0;
        }

        @Override
        public void run() {
            log_d(getName() + "开始执行");
            while (isRun) {
                synchronized (BaseNetWorker.this) {
                    if (!isHaveTask()) {
                        isRun = false;
                        netThread = null;
                        break;
                    }
                }
                BaseNetTask currTask = tasks.get(0);
                TR<BaseNetTask, Object> tr = new TR<BaseNetTask, Object>();
                tr.setTask(currTask);
                beforeDoTask(tr);
                Message mess = eventHandler.obtainMessage();
                doTask(tr, mess);
            }
            log_d(getName() + "执行完毕");
        }

        // 给handler发消息,执行请求任务前的操作
        private void beforeDoTask(TR<BaseNetTask, Object> result) {
            Message before = new Message();
            before.what = BEFORE;
            before.obj = result;
            eventHandler.sendMessage(before);
        }

        // 执行网络请求任务
        private void doTask(TR<BaseNetTask, Object> result, Message mess) {
            BaseNetTask task = result.getTask();
            log_d("Do task !!!Try " + (task.getTryTimes() + 1));
            log_d("The Task Description: " + task.getDescription());
            try {
                Object object;
                JSONObject jsonObject;
                if (task.getFiles() == null) {
                    if (isGetToken) {
                        jsonObject = BaseHttpUtil.sendPOSTForJsonObjectGetAccessToken(task.getPath(), task.getParams(), PoplarConfig.ENCODING);
                    } else {
                        jsonObject = BaseHttpUtil.sendPOSTForJSONObject(task.getPath(), task.getParams(),
                                PoplarConfig.ENCODING, AccessInstance.getInstance(context).getAccessToken());
                    }
                    object = task.parse(jsonObject);
                } else {
                    jsonObject = BaseHttpUtil.sendPOSTWithFilesForJSONObject(task.getPath(), task.getFiles(), task.getParams(),
                            PoplarConfig.ENCODING, AccessInstance.getInstance(context).getAccessToken());
                    object = task.parse(jsonObject);
                }
                mess.obj = result.put(task, object);
                mess.what = SUCCESS;
                mess.arg1 = task.getId();
                tasks.remove(task);
                eventHandler.sendMessage(mess);
            } catch (HttpException e) {
                tryAgain(task, FAILED_HTTP, mess, result);
            } catch (DataParseException e) {
                tryAgain(task, FAILED_DATAPARSE, mess, result);
            }
        }

        // 失败后再试几次
        private void tryAgain(BaseNetTask task, int type, Message mess,
                              TR<BaseNetTask, Object> result) {
            task.setTryTimes(task.getTryTimes() + 1);
            if (task.getTryTimes() >= PoplarConfig.TRYTIMES_HTTP) {
                mess.what = type;
                // mess.arg1 = task.getId();
                mess.obj = result;
                tasks.remove(task);
                eventHandler.sendMessage(mess);
            }
        }
    }

    public Context getContext() {
        return context;
    }

    public OnTaskExecuteListener getOnTaskExecuteListener() {
        return onTaskExecuteListener;
    }

    public void setOnTaskExecuteListener(
            OnTaskExecuteListener onTaskExecuteListener) {
        this.onTaskExecuteListener = onTaskExecuteListener;
    }

    private static class EventHandler extends Handler {
        private BaseNetWorker netWorker;

        public EventHandler(BaseNetWorker netWorker, Looper looper) {
            super(looper);
            this.netWorker = netWorker;
        }

        private OnTaskExecuteListener getOnTaskExecuteListener() {
            return netWorker.getOnTaskExecuteListener();
        }

        @Override
        public void handleMessage(Message msg) {
            OnTaskExecuteListener listener = getOnTaskExecuteListener();
            if (listener != null) {
                @SuppressWarnings("unchecked")
                TR<BaseNetTask, Object> result = (TR<BaseNetTask, Object>) msg.obj;
                switch (msg.what) {
                    case SUCCESS:
                        listener.onExecuteSuccess(netWorker, result.getTask(),
                                result.getResult());
                        listener.onPostExecute(netWorker, result.getTask());
                        break;
                    case FAILED_HTTP:
                        listener.onNewExecuteFailed(netWorker, result.getTask(),
                                FAILED_HTTP);
                        listener.onPostExecute(netWorker, result.getTask());
                        break;
                    case FAILED_DATAPARSE:
                        listener.onNewExecuteFailed(netWorker, result.getTask(),
                                FAILED_DATAPARSE);
                        listener.onPostExecute(netWorker, result.getTask());
                        break;
                    case BEFORE:
                        listener.onPreExecute(netWorker, result.getTask());
                        break;
                    default:
                        listener.onPostExecute(netWorker, result.getTask());
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 网络请求任务和请求返回结果的对应关系
     *
     * @param <Task>   网络请求任务
     * @param <Result> 请求返回结果
     */
    private class TR<Task, Result> {
        private Task t;
        private Result r;

        /**
         * 实例化一个 网络请求任务和请求返回结果的对应关系
         *
         * @param t 网络请求任务
         * @param r 请求返回结果
         * @return
         */
        public TR<Task, Result> put(Task t, Result r) {
            setTask(t);
            setResult(r);
            return this;
        }

        /**
         * 设置网络请求任务
         *
         * @param t 网络请求任务实例
         */
        public void setTask(Task t) {
            this.t = t;
        }

        /**
         * 设置请求返回结果
         *
         * @param r 请求返回结果实例
         */
        public void setResult(Result r) {
            this.r = r;
        }

        /**
         * 获取网络请求任务
         *
         * @return 网络请求任务实例
         */
        public Task getTask() {
            return t;
        }

        /**
         * 获取请求返回结果
         *
         * @return 请求返回结果实例
         */
        public Result getResult() {
            return r;
        }
    }

    public interface OnTaskExecuteListener {
        /**
         * Runs on the UI thread before the task run.
         */
        void onPreExecute(BaseNetWorker netWorker, BaseNetTask task);

        /**
         * Runs on the UI thread after the task run.
         */
        void onPostExecute(BaseNetWorker netWorker, BaseNetTask task);

        /**
         * Runs on the UI thread when the task run success.
         *
         * @param result the result of the server back.
         */
        void onExecuteSuccess(BaseNetWorker netWorker, BaseNetTask task,
                              Object result);

        /**
         * Runs on the UI thread when the task run failed.
         *
         * @param failedType the type of cause the task failed.
         *                   <p>
         *                   See {@link BaseNetWorker#FAILED_DATAPARSE
         *                   HemaNetWorker.FAILED_DATAPARSE},
         *                   {@link BaseNetWorker#FAILED_HTTP
         *                   HemaNetWorker.FAILED_HTTP},
         *                   {@link BaseNetWorker#FAILED_NONETWORK
         *                   HemaNetWorker.FAILED_NONETWORK}
         *                   </p>
         */
        void onNewExecuteFailed(BaseNetWorker netWorker, BaseNetTask task,
                                int failedType);
    }

    /**
     * 系统初始化必须有
     */
    public abstract void init();

    /**
     * token失效时自动登录方法
     */
    public abstract void clientLogin();

    /**
     * token失效时自动登录方法(第三方登录)
     *
     * @return 如果当前用户是第三方登录的请返回true否则将自动调用{@link #clientLogin()}
     */
    public abstract boolean thirdSave();

    public abstract void getAccessToken();


}
