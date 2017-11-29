package com.luna.viewframework;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * 自定义下拉刷新(上拉加载)布局
 * Upgrade by Hufanglin on 2016/2/20.
 */
public class CardRefreshLoadmoreLayout extends RefreshLoadmoreLayout {
    private boolean isLoading;
    private boolean isRefreshing;
    private int[] imageRes = new int[]{R.mipmap.icon_anim1, R.mipmap.icon_anim2, R.mipmap.icon_anim3, R.mipmap.icon_anim4, R.mipmap.icon_anim5};
    private RefeshListener refresh = new RefeshListener();

    public CardRefreshLoadmoreLayout(Context context) {
        this(context, null);
    }

    public CardRefreshLoadmoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardRefreshLoadmoreLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        setRefreshView(R.layout.card_refresh_normal, refresh);
        setLoadmoreView(R.layout.loadmore_normal, new LoadmoreListener());
        setAnimationDuration(300);
        // setSucessOrFailedDuration(300);
    }

    private class RefeshListener implements RefreshViewListener {
        private ImageView refreshImageLoad;
        private AnimationDrawable animationDrawable;
        private TextView txtRefresh;


        private boolean pull_min1 = true;
        private boolean pull_max1 = false;

        private RefeshListener() {
        }


        @Override
        public void onPulling(View refreshView, float percent) {
            findView(refreshView);
            Log.e("percent", "" + percent);
            if (percent <= 1) {
                int index = (int) (percent * 10);
                index = index - 5;
                if (index < 0) {
                    index = 0;
                }
                if (index >= imageRes.length) {
                    index = imageRes.length - 1;
                }
                refreshImageLoad.setImageResource(imageRes[index]);
                txtRefresh.setText("下拉刷新");
                /*numberCircleProgressBar.setProgress((int) (percent * 100));
                if (pull_max1) {
                    textView.setText("下拉刷新");
//                    arrowView.startAnimation(mRotateDownAnim);
                }*/
                pull_min1 = true;
                pull_max1 = false;
            } else {
                /*if (pull_min1) {
                    textView.setText("松开刷新");
                    numberCircleProgressBar.setProgress(100);
//                    arrowView.startAnimation(mRotateUpAnim);
                }*/
                refreshImageLoad.setImageResource(imageRes[imageRes.length - 1]);
                txtRefresh.setText("松开刷新");
                pull_min1 = false;
                pull_max1 = true;
            }
        }

        @Override
        public void onReset(View refreshView) {
            findView(refreshView);
            txtRefresh.setText("下拉刷新");

        }

        @Override
        public void onRefresh(View refreshView) {
            findView(refreshView);
//            arrowView.clearAnimation();
//            arrowView.setVisibility(View.INVISIBLE);
            /*numberCircleProgressBar.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            textView.setText("正在刷新");*/
            refreshImageLoad.setImageResource(R.drawable.image_load_anim);
            animationDrawable = (AnimationDrawable) refreshImageLoad.getDrawable();
            animationDrawable.start();
            txtRefresh.setText("正在刷新");
            isRefreshing = true;
        }

        @Override
        public void onSuccess(View refreshView) {
            findView(refreshView);
//            arrowView.setVisibility(View.GONE);
            /*numberCircleProgressBar.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            textView.setText("刷新成功");*/
            if (animationDrawable != null)
                animationDrawable.stop();
            txtRefresh.setText("刷新成功");
            isRefreshing = false;
        }

        @Override
        public void onFailed(View refreshView) {
            findView(refreshView);
//            arrowView.setVisibility(View.GONE);
            /*numberCircleProgressBar.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            textView.setText("刷新失败");*/
            if (animationDrawable != null)
                animationDrawable.stop();
            txtRefresh.setText("刷新失败");
            isRefreshing = false;
        }

        private void findView(View fartherView) {
            if (refreshImageLoad == null || txtRefresh == null) {
                txtRefresh = (TextView) fartherView.findViewById(R.id.txtRefresh);
                refreshImageLoad = (ImageView) fartherView.findViewById(R.id.refreshImageLoad);
            }
          /*  if (numberCircleProgressBar == null || textView == null || progressBar == null) {
                numberCircleProgressBar = (NumberCircleProgressBar) fartherView
                        .findViewById(R.id.numberCircleProgressBar);
                textView = (TextView) fartherView
                        .findViewById(R.id.refresh_textview);
                progressBar = (ProgressBar) fartherView
                        .findViewById(R.id.refresh_progressbar);
            }*/
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
        View v = LayoutInflater.from(this.getContext()).inflate(R.layout.card_refresh_normal, (ViewGroup) null);
        refresh.onRefresh(v);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }


}