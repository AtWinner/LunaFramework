package com.luna.viewframework;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Scroller;


/**
 * 配合侧滑删除使用
 * 来自张茂宽
 * Created by Hufanglin on 2016/3/16.
 */
public class ScrollLinerLayout extends LinearLayout {
    Context context;

    public ScrollLinerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        mScroller = new Scroller(context);
    }

    private Scroller mScroller;

    @Override
    public void computeScroll() {
        if (mScroller == null) {
            return;
        }
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        }
    }

    public void snapToScreen(int whichScreen) {
        if (mScroller == null) {
            return;
        }
        int curscrollerx = getScrollX();
        mScroller.startScroll(curscrollerx, 0, whichScreen - curscrollerx, 0, dip2px(getContext(), 70));
        invalidate();
    }

    public void isScrollAble(boolean able) {
        if (able) {
            mScroller = new Scroller(context);
        } else {
            mScroller = null;
        }
    }

    public int dip2px(Context context, double dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}