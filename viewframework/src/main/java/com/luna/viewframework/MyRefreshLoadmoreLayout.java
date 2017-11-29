package com.luna.viewframework;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * 自定义下拉刷新(上拉加载)布局
 * Upgrade by Hufanglin on 2016/2/20.
 */
public class MyRefreshLoadmoreLayout extends RefreshLoadmoreLayout {
    private boolean isLoading;
    private boolean isRefreshing;
    private RefeshListener refresh = new RefeshListener();

    public MyRefreshLoadmoreLayout(Context context) {
        this(context, null);
    }

    public MyRefreshLoadmoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyRefreshLoadmoreLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        setRefreshView(R.layout.refresh_normal, refresh);
        setLoadmoreView(R.layout.loadmore_normal, new LoadmoreListener());
        setAnimationDuration(300);
        // setSucessOrFailedDuration(300);
    }

    private class RefeshListener implements RefreshViewListener {
        private final int ROTATE_ANIM_DURATION = 180;
        private Animation mRotateUpAnim;
        private Animation mRotateDownAnim;

        //        private ImageView arrowView;
        private TextView textView;
        private ProgressBar progressBar;
        private NumberCircleProgressBar numberCircleProgressBar;

        private boolean pull_min1 = true;
        private boolean pull_max1 = false;

        private RefeshListener() {
            mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
            mRotateUpAnim.setFillAfter(true);
            mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
            mRotateDownAnim.setFillAfter(true);
        }


        @Override
        public void onPulling(View refreshView, float percent) {
            findView(refreshView);
            if (percent < 1) {
                numberCircleProgressBar.setProgress((int) (percent * 100));
                if (pull_max1) {
                    textView.setText("下拉刷新");
//                    arrowView.startAnimation(mRotateDownAnim);
                }
                pull_min1 = true;
                pull_max1 = false;
            } else {
                if (pull_min1) {
                    textView.setText("松开刷新");
                    numberCircleProgressBar.setProgress(100);
//                    arrowView.startAnimation(mRotateUpAnim);
                }
                pull_min1 = false;
                pull_max1 = true;
            }
        }

        @Override
        public void onReset(View refreshView) {
            findView(refreshView);
//            arrowView.clearAnimation();
//            arrowView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            numberCircleProgressBar.setVisibility(View.VISIBLE);
            textView.setText("下拉刷新");

        }

        @Override
        public void onRefresh(View refreshView) {
            findView(refreshView);
//            arrowView.clearAnimation();
//            arrowView.setVisibility(View.INVISIBLE);
            numberCircleProgressBar.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            textView.setText("正在刷新");
            isRefreshing = true;
        }

        @Override
        public void onSuccess(View refreshView) {
            findView(refreshView);
//            arrowView.setVisibility(View.GONE);
            numberCircleProgressBar.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            textView.setText("刷新成功");
            isRefreshing = false;
        }

        @Override
        public void onFailed(View refreshView) {
            findView(refreshView);
//            arrowView.setVisibility(View.GONE);
            numberCircleProgressBar.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            textView.setText("刷新失败");
            isRefreshing = false;
        }

        private void findView(View fartherView) {
            if (numberCircleProgressBar == null || textView == null || progressBar == null) {
                numberCircleProgressBar = (NumberCircleProgressBar) fartherView
                        .findViewById(R.id.numberCircleProgressBar);
                textView = (TextView) fartherView
                        .findViewById(R.id.refresh_textview);
                progressBar = (ProgressBar) fartherView
                        .findViewById(R.id.refresh_progressbar);
            }
        }

    }

    private class LoadmoreListener implements LoadmoreViewListener {
        private TextView textView;
        private ProgressBar progressBar;

        @Override
        public void onPulling(View loadmoreView, float percent) {
            findView(loadmoreView);
            if (percent < 1) {
                textView.setText("上拉加载");
            } else {
                textView.setText("松开加载");
            }

        }

        @Override
        public void onReset(View loadmoreView) {
            findView(loadmoreView);
            progressBar.setVisibility(View.GONE);
            textView.setText("上拉加载");

        }

        @Override
        public void onLoadmore(View loadmoreView) {
            findView(loadmoreView);
            progressBar.setVisibility(View.VISIBLE);
            textView.setText("正在加载");
            isLoading = true;
        }

        @Override
        public void onSuccess(View loadmoreView) {
            findView(loadmoreView);
            progressBar.setVisibility(View.GONE);
            textView.setText("加载成功");
            isLoading = false;
        }

        @Override
        public void onFailed(View loadmoreView) {
            findView(loadmoreView);
            progressBar.setVisibility(View.GONE);
            textView.setText("加载失败");
            isLoading = false;
        }

        private void findView(View fartherView) {
            if (textView == null || progressBar == null) {
                textView = (TextView) fartherView
                        .findViewById(R.id.loadmore_textview);
                progressBar = (ProgressBar) fartherView
                        .findViewById(R.id.loadmore_progressbar);
            }
        }

    }

    /**
     * 手动开始刷新
     */
    public void startRefresh() {
        View v = LayoutInflater.from(this.getContext()).inflate(R.layout.refresh_normal, (ViewGroup) null);
        refresh.onRefresh(v);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }


}