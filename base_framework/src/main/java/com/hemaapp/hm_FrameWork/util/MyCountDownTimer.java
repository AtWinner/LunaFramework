package com.hemaapp.hm_FrameWork.util;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * 定时器哦
 * Created by Hufanglin on 2016/2/20.
 */
public class MyCountDownTimer extends CountDownTimer {
    private TextView textView;
    private View beforeButton;
    private View afterLayout;
    private TextView actionView;

    /**
     * 构造函数
     *
     * @param millisInFuture    倒计时时长
     * @param countDownInterval 单位时间
     * @param textView          倒计时文本框
     * @param beforeButton      点击前按钮
     * @param afterLayout       点击后布局
     * @param actionView        提示文本框
     */
    public MyCountDownTimer(long millisInFuture, long countDownInterval, TextView textView, View beforeButton, View afterLayout, TextView actionView) {
        super(millisInFuture, countDownInterval);
        this.textView = textView;
        this.beforeButton = beforeButton;
        this.afterLayout = afterLayout;
        this.actionView = actionView;
    }

    public MyCountDownTimer(long millisInFuture, long countDownInterval, TextView textView, View beforeButton, View afterLayout) {
        super(millisInFuture, countDownInterval);
        this.textView = textView;
        this.beforeButton = beforeButton;
        this.afterLayout = afterLayout;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        Log.e("tick", String.valueOf(millisUntilFinished));
        textView.setText(String.valueOf(millisUntilFinished / 1000) + "s");
    }

    @Override
    public void onFinish() {
        afterLayout.setVisibility(View.INVISIBLE);
        beforeButton.setVisibility(View.VISIBLE);
        if (actionView != null)
            actionView.setText("");
    }
}
