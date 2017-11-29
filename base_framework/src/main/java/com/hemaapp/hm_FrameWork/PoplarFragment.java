package com.hemaapp.hm_FrameWork;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hemaapp.PoplarConfig;
import com.hemaapp.hm_FrameWork.image.ImageWorker;
import com.hemaapp.hm_FrameWork.presenter.BasePresenter;
import com.hemaapp.hm_FrameWork.presenter.BaseView;
import com.hemaapp.hm_FrameWork.util.BaseUtil;
import com.hemaapp.hm_FrameWork.util.HemaLogger;
import com.hemaapp.hm_FrameWork.util.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 基本Fragment框架
 */
public abstract class PoplarFragment<T extends BasePresenter> extends Fragment implements BaseView {
    protected static final String NO_NETWORK = "无网络连接，请检查网络设置。";
    protected static final String FAILED_GETDATA_HTTP = "请求异常。";
    protected static final String FAILED_GETDATA_DATAPARSE = "数据异常。";
    /**
     * 打印TAG，类名
     */
    private String TAG;
    public PoplarActivity activity;
    public T mPresenter;
    /**
     * 根view
     */
    protected View rootView;
    /**
     * 根view id
     */
    private int rootViewId;
    private static Fragment currForResultFragment;
    public ImageWorker imageWorker;

    protected PoplarFragment() {
        TAG = getLogTag();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (PoplarActivity) getActivity();
        imageWorker = new ImageWorker(activity.getApplicationContext());
        if (rootView == null)
            rootView = LayoutInflater.from(activity).inflate(rootViewId,
                    null, false);
        init();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null)
            for (Fragment fragment : fragments) {
                fragment.onHiddenChanged(hidden);
            }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageWorker != null)
            imageWorker.clearTasks();// 取消图片下载任务
        ToastUtil.cancelAllToast();
        cancelProgressDialog();
        cancelTextDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    /**
     * 页面初始化
     */
    private void init() {
        findView();
        setListener();
        initPresenter();
    }

    /**
     * 设置根view
     *
     * @param layoutResID
     */
    public void setContentView(int layoutResID) {
        rootViewId = layoutResID;
    }

    /**
     * 设置根view
     *
     * @param v
     */
    public void setContentView(View v) {
        rootView = v;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (currForResultFragment == null)
            currForResultFragment = this;
        if (getParentFragment() != null)
            getParentFragment().startActivityForResult(intent, requestCode);
        else
            super.startActivityForResult(intent, requestCode);
    }

    // 获取打印TAG，即类名
    private String getLogTag() {
        return getClass().getSimpleName();
    }

    public View findViewById(int id) {
        View view = null;
        if (rootView != null)
            view = rootView.findViewById(id);
        return view;
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
        FragmentManager manager = getChildFragmentManager();
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
                if (!fm.equals(fragment))
                    transaction.hide(fm);

        transaction.show(fragment);
        transaction.commit();
    }

    public void showProgressDialog(String text) {
        activity.showProgressDialog(text);
    }

    public void showProgressDialog(int text) {
        activity.showProgressDialog(text);
    }

    @Override
    public void showProgressDialog(String text, boolean cancelable) {
        activity.showProgressDialog(text, cancelable);
    }

    @Override
    public void showProgressDialog(int text, boolean cancelable) {
        activity.showProgressDialog(text, cancelable);
    }

    public void cancelProgressDialog() {
        activity.cancelProgressDialog();
    }

    public void showTextDialog(String text) {
        activity.showTextDialog(text);
    }

    public void showTextDialog(int text) {
        activity.showTextDialog(text);
    }

    public void cancelTextDialog() {
        activity.cancelTextDialog();
    }

    /**
     * 无网络提示
     */
    protected void noNetWork() {
        ToastUtil.showLongToast(getActivity(), NO_NETWORK);
    }

    /**
     * 初始化三部曲之：查找控件
     */
    protected abstract void findView();

    /**
     * 初始化三部曲之：设置监听
     */
    protected abstract void setListener();

    /**
     * 初始化三部曲之：设置数据中间人
     */
    protected abstract void initPresenter();

    // 友盟相关
    @Override
    public void onResume() {
        super.onResume();
        if (PoplarConfig.UMENG_ENABLE)
            MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (PoplarConfig.UMENG_ENABLE)
            MobclickAgent.onPageEnd(getClass().getSimpleName());
    }
    // 友盟相关end

    @Override
    public void destroy() {

    }


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
     * 解析时，判断是否为空
     *
     * @param jsonObject
     * @param s
     * @return
     * @throws JSONException
     */
    protected String get(JSONObject jsonObject, String s) throws JSONException {
        return jsonObject.optString(s);
    }

    /**
     * 解析时，判断是否为空
     *
     * @param jsonObject
     * @param s
     * @return 若为空返回0
     * @throws JSONException
     */
    protected int getInt(JSONObject jsonObject, String s) throws JSONException {
        return jsonObject.optInt(s);
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
}
