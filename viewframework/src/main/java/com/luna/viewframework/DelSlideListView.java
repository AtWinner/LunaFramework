package com.luna.viewframework;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * 侧滑删除
 * Created by HuHu on 2016-06-02.
 */
public class DelSlideListView extends ListView implements
        GestureDetector.OnGestureListener, View.OnTouchListener {

    private GestureDetector mDetector;
    private String TAG = "DelSlideListView";
    private int px = 0;
    private Context context = null;

    public DelSlideListView(Context context) {
        super(context);
        init(context);
        this.context = context;
    }

    public DelSlideListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        this.context = context;
    }

    private int standard_touch_target_size = 0;
    private float mLastMotionX;
    // 有item被拉出

    public boolean deleteView = false;
    // 当前拉出的view

    private ScrollLinerLayout mScrollLinerLayout = null;
    // 滑动着

    private boolean scroll = false;
    // 禁止拖动

    private boolean forbidScroll = false;
    // 禁止拖动

    private boolean clicksameone = false;
    // 当前拉出的位置

    private int position;
    // 消息冻结

    private boolean freeze = false;

    private void init(Context mContext) {
        mDetector = new GestureDetector(mContext, this);
        // mDetector.setIsLongpressEnabled(false);

        standard_touch_target_size = (int) getResources().getDimension(
                R.dimen.delete_action_len);
        this.setOnTouchListener(this);
    }

    public void reset() {
        reset(false);
    }

    public void reset(boolean noaction) {
        position = -1;
        deleteView = false;
        if (mScrollLinerLayout != null) {
            if (!noaction) {
                mScrollLinerLayout.snapToScreen(0);
            } else {
                mScrollLinerLayout.scrollTo(0, 0);
            }
            mScrollLinerLayout = null;
        }
        scroll = false;
    }

    public boolean onDown(MotionEvent e) {
        Log.i(TAG, "onDown");

        mLastMotionX = e.getX();
        int p = this.pointToPosition((int) e.getX(), (int) e.getY())
                - this.getFirstVisiblePosition();
        if (deleteView) {
            if (p != position) {
                // 吃掉，不在有消息

                freeze = true;
                return true;
            } else {
                clicksameone = true;
            }
        }
        position = p;
        scroll = false;
        return false;
    }

    public void onLongPress(MotionEvent e) {
        Log.i(TAG, "onLongPress");
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // Log.i(TAG, "onScroll" + e1.getX() + ":" + distanceX);

        // 第二次

        if (scroll) {
            int deltaX = (int) (mLastMotionX - e2.getX());
            if (deleteView) {
                deltaX += standard_touch_target_size;
            }
            if (deltaX >= 0 && deltaX <= standard_touch_target_size) {
                mScrollLinerLayout.scrollBy(
                        deltaX - mScrollLinerLayout.getScrollX(), 0);
            }
            return true;
        }
        if (!forbidScroll) {
            forbidScroll = true;
            // x方向滑动，才开始拉动

            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                View v = this.getChildAt(position);
                boolean ischild = v instanceof ScrollLinerLayout;
                if (ischild) {
                    mScrollLinerLayout = (ScrollLinerLayout) v;
                    scroll = true;
                    int deltaX = (int) (mLastMotionX - e2.getX());
                    if (deleteView) {
                        // 再次点击的时候，要把deltax增加

                        deltaX += standard_touch_target_size;
                    }
                    if (deltaX >= 0 && deltaX <= standard_touch_target_size) {
                        mScrollLinerLayout.scrollBy(
                                (int) (e1.getX() - e2.getX()), 0);
                    }
                }
            }
        }
        return false;
    }

    public void onShowPress(MotionEvent e) {
        Log.i(TAG, "onShowPress");
    }

    public boolean onSingleTapUp(MotionEvent e) {
        Log.i(TAG, "onSingleTapUp");
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scroll || deleteView) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            boolean isfreeze = freeze;
            boolean isclicksameone = clicksameone;
            forbidScroll = false;
            clicksameone = false;
            freeze = false;
            if (isfreeze) {
                // 上一个跟当前点击不一致 还原

                reset();
                return true;
            }
            int deltaX2 = (int) (mLastMotionX - event.getX());
            // 不存在

            Log.i(TAG, "scroll:" + scroll + "deltaX2:" + deltaX2);

            if (scroll && deltaX2 >= standard_touch_target_size / 2) {
                // mScrollLinerLayout.snapToScreen(standard_touch_target_size*5/3);

                px = dip2px(getContext(), 60);
                if (getSp(context, "w") != null) {
                    px = Integer.parseInt(getSp(
                            context, "w")) / 6;
                }
                mScrollLinerLayout.snapToScreen(px); // 120

                deleteView = true;
                scroll = false;
                return true;
            }
            if (deleteView && scroll
                    && deltaX2 >= -standard_touch_target_size / 2) {
                // mScrollLinerLayout.snapToScreen(standard_touch_target_size*3);
                px = dip2px(getContext(), 60);
                if (getSp(context, "w") != null) {
                    px = Integer.parseInt(getSp(
                            context, "w")) / 6;
                }
                mScrollLinerLayout.snapToScreen(px);
                deleteView = true;
                scroll = false;
                return true;
            }
            if (isclicksameone || scroll) {
                reset();
                return true;
            }
            reset();
        }
        if (freeze) {
            return true;
        }
        Log.i(TAG, "onTouchEvent");
        return mDetector.onTouchEvent(event);

    }

    public void deleteItem() {
        Log.i(TAG, "deleteItem");
        reset(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.
     * MotionEvent, android.view.MotionEvent, float, float)
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        return false;
    }

//    @Override
//    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
//                MeasureSpec.AT_MOST);
//        super.onMeasure(widthMeasureSpec, expandSpec);
//    }


    public int dip2px(Context context, double dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public String getSp(Context con, String key) {
        SharedPreferences sp = con.getSharedPreferences("sp_view", Activity.MODE_PRIVATE);
        return sp.getString(key, null);
    }
}
