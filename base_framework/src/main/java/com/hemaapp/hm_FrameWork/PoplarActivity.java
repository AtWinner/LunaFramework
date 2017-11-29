package com.hemaapp.hm_FrameWork;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.hemaapp.PoplarActivityManager;
import com.hemaapp.PoplarConfig;
import com.hemaapp.hm_FrameWork.dialog.ProgressDialog;
import com.hemaapp.hm_FrameWork.dialog.TextDialog;
import com.hemaapp.hm_FrameWork.image.ImageWorker;
import com.hemaapp.hm_FrameWork.model.HemaIntent;
import com.hemaapp.hm_FrameWork.presenter.BasePresenter;
import com.hemaapp.hm_FrameWork.presenter.BaseView;
import com.hemaapp.hm_FrameWork.result.BaseResult;
import com.hemaapp.hm_FrameWork.util.BaseUtil;
import com.hemaapp.hm_FrameWork.util.HemaLogger;
import com.hemaapp.hm_FrameWork.util.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;


/**
 * * 基本Activity框架.
 */
public abstract class PoplarActivity<T extends BasePresenter> extends AppCompatActivity implements BaseView {

    protected static final String NO_NETWORK = "无网络连接，请检查网络设置。";
    protected static final String FAILED_GETDATA_HTTP = "请求异常。";
    protected static final String FAILED_GETDATA_DATAPARSE = "数据异常。";

    public T mPresenter;

    /**
     * 是否已被销毁
     */
    protected boolean isDestroyed = false;
    /**
     * 打印TAG，类名
     */
    private String TAG;
    /**
     * 上下文对象，等同于this
     */
    protected Activity mContext;
    /**
     * 下载图片使用
     */
    public ImageWorker imageWorker;
//    private HemaNetWorker netWorker;
    /**
     * 获取传参使用
     */
    protected Intent mIntent;
    /**
     * 输入法管理器
     */
    protected InputMethodManager mInputMethodManager;
    /**
     * a LayoutInflater
     */
    private LayoutInflater mLayoutInflater;
    /**
     * 任务参数集
     */
    private HashMap<String, String> params;
    /**
     * 任务文件集
     */
    private HashMap<String, String> files;

    protected PoplarActivity() {
        TAG = getLogTag();
    }

    private TextDialog textDialog;
    private ProgressDialog progressDialog;
    protected boolean isStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PoplarActivityManager.add(this);
        mContext = this;
        imageWorker = new ImageWorker(getApplicationContext());
        mIntent = getIntent();
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        init(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        isStop = true;
        super.onStop();
    }

    public T getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (onKeyBack())
                    return true;
                else
                    return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_MENU:
                if (onKeyMenu())
                    return true;
                else
                    return super.onKeyDown(keyCode, event);
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 初始化三部曲
    private void init(Bundle savedInstanceState) {
        if (savedInstanceState != null && (savedInstanceState.getSerializable("intent") != null)) {
            mIntent = (HemaIntent) savedInstanceState.getSerializable("intent");
        }
        getExras();
        initPresenter();
        findView();
        setListener();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("intent", new HemaIntent(mIntent));
        super.onSaveInstanceState(outState);
    }

    /**
     * 显示或更换Fragment
     *
     * @param fragmentClass   Fragment.class
     * @param containerViewId Fragment显示的空间ID
     * @param replace         是否替换
     */
    public void toogleFragment(Class<? extends Fragment> fragmentClass,
                               int containerViewId, boolean replace) {
        if (isStop)
            return;
        FragmentManager manager = getSupportFragmentManager();
        String tag = fragmentClass.getName();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag(tag);

        if (fragment == null) {
            try {
                fragment = fragmentClass.newInstance();
                if (replace)
                    transaction.replace(containerViewId, fragment, tag);
                else
                    // 替换时保留Fragment,以便复用
                    transaction.add(containerViewId, fragment, tag);
            } catch (Exception e) {
                // ignore
            }
        } else {
            // nothing
        }
        // 遍历存在的Fragment,隐藏其他Fragment
        List<Fragment> fragments = manager.getFragments();
        if (fragments != null)
            for (Fragment fm : fragments)
                if (fm != null && !fm.equals(fragment))
                    transaction.hide(fm);

        transaction.show(fragment);
        transaction.commit();
    }

    /**
     * 关闭Activity
     *
     * @param enterAnim 进入Activity的动画,若没有传0即可
     * @param exitAnim  退出Activity的动画,若没有传0即可
     */
    public void finish(int enterAnim, int exitAnim) {
        finish();
        overridePendingTransition(enterAnim, exitAnim);
    }

    /**
     * @param enterAnim 进入Activity的动画,若没有传0即可
     * @param exitAnim  退出Activity的动画,若没有传0即可
     */
    public void startActivityForResult(Intent intent, int requestCode,
                                       int enterAnim, int exitAnim) {
        startActivityForResult(intent, requestCode);
        if (getParent() != null)
            getParent().overridePendingTransition(enterAnim, exitAnim);
        else
            overridePendingTransition(enterAnim, exitAnim);
    }

    /**
     * @param enterAnim 进入Activity的动画,若没有传0即可
     * @param exitAnim  退出Activity的动画,若没有传0即可
     */
    public void startActivity(Intent intent, int enterAnim, int exitAnim) {
        startActivity(intent);
        if (getParent() != null)
            getParent().overridePendingTransition(enterAnim, exitAnim);
        else
            overridePendingTransition(enterAnim, exitAnim);
    }

    /**
     * 显示交互弹窗(默认不可以点击弹窗外侧取消)
     *
     * @param text 弹窗提示语
     */
    public void showProgressDialog(String text) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);
        progressDialog.setText(text);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * 显示交互弹窗
     *
     * @param text       弹窗提示语id
     * @param cancelable 是否可以点击弹窗外侧取消
     */
    public void showProgressDialog(String text, boolean cancelable) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);
        progressDialog.setText(text);
        progressDialog.setCancelable(cancelable);
        progressDialog.show();
    }

    /**
     * 显示交互弹窗(默认不可以点击弹窗外侧取消)
     *
     * @param text 弹窗提示语
     */
    public void showProgressDialog(int text) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);
        progressDialog.setText(text);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * 显示交互弹窗
     *
     * @param text       弹窗提示语
     * @param cancelable 是否可以点击弹窗外侧取消
     */
    public void showProgressDialog(int text, boolean cancelable) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);
        progressDialog.setText(text);
        progressDialog.setCancelable(cancelable);
        progressDialog.show();
    }

    /**
     * 取消交互弹窗(同时setCancelable(false))
     */
    public void cancelProgressDialog() {
        if (progressDialog != null) {
            progressDialog.setCancelable(false);
            progressDialog.cancel();
        }
    }

    /**
     * 显示提示弹窗
     *
     * @param text 弹窗提示语
     */
    public void showTextDialog(String text) {
        if (textDialog == null)
            textDialog = new TextDialog(this);
        textDialog.setText(text);
        textDialog.show();
    }

    /**
     * 显示提示弹窗
     *
     * @param text 弹窗提示语id
     */
    public void showTextDialog(int text) {
        if (textDialog == null)
            textDialog = new TextDialog(this);
        textDialog.setText(text);
        textDialog.show();
    }

    /**
     * 取消提示弹窗
     */
    public void cancelTextDialog() {
        if (textDialog != null)
            textDialog.cancel();
    }

    @Override
    protected void onDestroy() {
        /*if (netWorker != null) {
            netWorker.cancelTasks();
            netWorker.setOnTaskExecuteListener(null);
        }*/
        destroy();
        super.onDestroy();
        recyclePics();// 回收图片
    }

    public void destroy() {
        isDestroyed = true;
        PoplarActivityManager.remove(this);
        stopNetThread();// 杀掉网络线程
        if (imageWorker != null)
            imageWorker.clearTasks();// 取消图片下载任务
        ToastUtil.cancelAllToast();
        cancelProgressDialog();
        cancelTextDialog();
    }

    @Override
    public void finish() {
        cancelTextDialog();
        if (progressDialog != null)
            progressDialog.cancelImmediately();
        destroy();
        super.finish();
    }

    protected boolean onKeyBack() {
        finish();
        return true;
    }

    protected boolean onKeyMenu() {
        // TODO Auto-generated method stub
        return false;
    }

    // 友盟相关
    @Override
    protected void onResume() {
        isStop = false;
        super.onResume();
        if (PoplarConfig.UMENG_ENABLE)
            MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (PoplarConfig.UMENG_ENABLE)
            MobclickAgent.onPause(this);
    }

    // 友盟相关end

    @Override
    protected void onNewIntent(Intent intent) {
        isStop = false;
        super.onNewIntent(intent);
    }


    /**
     * 无网络提示
     *
     * @param taskID
     */
    protected void noNetWork(int taskID) {
        noNetWork();
    }

    /**
     * 无网络提示
     */
    protected void noNetWork() {
        ToastUtil.showLongToast(mContext, NO_NETWORK);
    }

    /**
     * 初始化四部曲之：查找控件
     */
    protected abstract void findView();

    /**
     * 初始化四部曲之：获取传参
     */
    protected abstract void getExras();

    /**
     * 初始化四部曲之：设置监听
     */
    protected abstract void setListener();

    /**
     * 初始化四部曲之：设置数据中间人
     */
    protected abstract void initPresenter();


    /**
     * 打印v级别信息
     *
     * @param msg
     */
    protected void log_v(String msg) {
        HemaLogger.v(TAG, msg);
    }

    /**
     * 打印d级别信息
     *
     * @param msg
     */
    protected void log_d(String msg) {
        HemaLogger.d(TAG, msg);
    }

    /**
     * 打印i级别信息
     *
     * @param msg
     */
    protected void log_i(String msg) {
        HemaLogger.i(TAG, msg);
    }

    /**
     * 打印w级别信息
     *
     * @param msg
     */
    protected void log_w(String msg) {
        HemaLogger.w(TAG, msg);
    }

    /**
     * 打印e级别信息
     *
     * @param msg
     */
    protected void log_e(String msg) {
        HemaLogger.e(TAG, msg);
    }

    /**
     * 打印
     *
     * @param msg
     */
    protected void println(Object msg) {
        HemaLogger.println(msg);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return true如果该字符串为null或者"",否则false
     */
    protected boolean isNull(String str) {
        return BaseUtil.isNull(str);
    }

    /**
     * 判断网络任务是否都已完成
     *
     * @return
     */
  /*  protected boolean isNetTasksFinished() {
        return netWorker == null || netWorker.isNetTasksFinished();
    }*/

    /**
     * 获取任务参数集容器
     *
     * @return an empty HashMap
     */
    public HashMap<String, String> getHashParams() {
        if (params == null)
            params = new HashMap<String, String>();
        else
            params.clear();
        return params;
    }

    /**
     * 是否已被销毁
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    /**
     * 获取任务文件集容器
     *
     * @return an empty HashMap
     */
    public HashMap<String, String> getHashFiles() {
        if (files == null)
            files = new HashMap<String, String>();
        else
            files.clear();
        return files;
    }

    /**
     * get a LayoutInflater
     */
    public LayoutInflater getLayoutInflater() {
        return mLayoutInflater == null ? mLayoutInflater = LayoutInflater
                .from(this) : mLayoutInflater;
    }

    // 回收图片
    private void recyclePics() {
        if (imageWorker != null) {
            imageWorker.clearTasks();
        }
    }

    // 杀掉网络线程
    private void stopNetThread() {
        /*if (netWorker != null) {
            netWorker.cancelTasks();
        }*/
    }

    // 获取打印TAG，即类名
    private String getLogTag() {
        return getClass().getSimpleName();
    }

    /**
     * 判断当前是否有可用网络
     *
     * @return 如果有true否则false
     */
    public boolean hasNetWork() {
        ConnectivityManager con = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = con.getActiveNetworkInfo();// 获取可用的网络服务
        return info != null && info.isAvailable();
    }

    /**
     * 设置显示全屏
     */
    protected void showFullScreen() {

        /**
         * 设置不显示title;
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /**
         * 设置显示全屏 ;
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 设置不显示title
     */
    protected void hideTitle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hideTitleBar();
        } else {
            showFullScreen();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void hideTitleBar() {
        // 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    public void showSuccess(String text) {
        TextDialog dialog = new TextDialog(mContext);
        dialog.setText(text);
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }
}
