package com.hemaapp.hm_FrameWork;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hemaapp.hm_FrameWork.util.BaseUtil;
import com.hemaapp.hm_FrameWork.util.HemaLogger;

public abstract class PoplarAdapter extends BaseAdapter {

    /**
     * 打印TAG，类名
     */
    private String TAG;
    protected Context mContext;
    protected Fragment mFragment;

    protected static final int VIEWTYPE_EMPTY = 0;
    protected static final int VIEWTYPE_NORMAL = 1;

    private String emptyString = "";
    private TextView emptyTextView;

    public PoplarAdapter(Context mContext) {
        TAG = getLogTag();
        this.mContext = mContext;
    }

    public PoplarAdapter(PoplarFragment mFragment) {
        TAG = getLogTag();
        this.mFragment = mFragment;
        this.mContext = mFragment.getActivity();
    }

    @Override
    public int getItemViewType(int position) {
        if (isEmpty())
            return VIEWTYPE_EMPTY;
        return VIEWTYPE_NORMAL;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * 获取列表为空时的显示View(调用此方法(不重写getItemViewType时)需重写isEmpty()方法)
     *
     * @return a view 传递getView方法中的ViewGroup参数即可
     */
    public View getEmptyView(ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem_empty, null);
        emptyTextView = (TextView) view.findViewById(R.id.textView);
        emptyTextView.setText(emptyString);
        int width = parent.getWidth();
        int height = parent.getHeight();
        LayoutParams params = new LayoutParams(width, height);
        view.setLayoutParams(params);
        return view;
    }

    /**
     * 设置空列表提示语
     *
     * @param emptyString
     */
    public void setEmptyString(String emptyString) {
        if (emptyTextView != null) {
            emptyTextView.setText(emptyString);
        }
        this.emptyString = emptyString;
    }

    /**
     * 设置空列表提示语
     *
     * @param emptyStrID
     */
    public void setEmptyString(int emptyStrID) {
        emptyString = mContext.getResources().getString(emptyStrID);
        setEmptyString(emptyString);
    }


    /**
     * 获取打印TAG，即类名
     *
     * @return
     */
    private String getLogTag() {
        return getClass().getSimpleName();
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
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    protected boolean isNull(String str) {
        return BaseUtil.isNull(str);
    }

}
