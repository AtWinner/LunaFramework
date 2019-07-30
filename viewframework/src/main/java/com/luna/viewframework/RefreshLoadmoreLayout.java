package com.luna.viewframework;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ListAdapter;

/**
 * 下拉刷新上拉加载布局(亦可用作下拉或上拉回弹的效果的根布局)
 */
public class RefreshLoadmoreLayout extends ViewGroup {

    private static final int[] LAYOUT_ATTRS = new int[]{android.R.attr.enabled};
    private int animationDuration = 500;// 复位动画持续时间(毫秒)
    private int sucessOrFailedDuration = 500;// 刷新(加载)成功(失败)停留时间
    private DecelerateInterpolator mDecelerateInterpolator;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private float speedBeforeEnable = 0.6f;// View在能刷新或者加载之前的滑动速度
    private float speedAfterEnable = 0.3f;// View在能刷新或者加载之后的滑动速度
    private int defaultBeforeDistance = 50;// dp
    private int mTouchSlop;

    private View refreshView;// 刷新视图
    private View contentView;// 主视图
    private View loadmoreView;// 加载视图
    private int contentIndex = 0;
    private int refreshIndex = 0;
    private int loadmoreIndex = 0;

    private float downY;
    private float downX;
    private float firstY;
    private boolean pullDown;
    private int mCurrentContentOffsetTop;

    private boolean isRefreshOrLoading = false;
    private boolean isRefreshable = false;
    private boolean isLoadmoreable = false;

    private final ToPositionForRefreshAnimation refreshAnimation = new ToPositionForRefreshAnimation();
    private final ToPositionForLoadmoreAnimation loadmoreAnimation = new ToPositionForLoadmoreAnimation();

    private RefreshViewListener refreshViewListener;
    private LoadmoreViewListener loadmoreViewListener;
    private OnStartListener onStartListener;

    public RefreshLoadmoreLayout(Context context) {
        this(context, null);
    }

    public RefreshLoadmoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RefreshLoadmoreLayout(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mDecelerateInterpolator = new DecelerateInterpolator(
                DECELERATE_INTERPOLATION_FACTOR);
        float density = getResources().getDisplayMetrics().density;
        defaultBeforeDistance = (int) (defaultBeforeDistance * density);
        final TypedArray a = context
                .obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        if (refreshView != null)
            count--;
        if (loadmoreView != null)
            count--;
        if (count != 1 && !isInEditMode()) {
            throw new IllegalStateException(
                    "HemaRefreshLoadmoreLayout must host only one direct child");
        }

        contentView = getChildAt(contentIndex);
        if (refreshView != null) {
            measureChild(refreshView, widthMeasureSpec, heightMeasureSpec);
        }
        if (contentView != null) {
            final int childWidthMeasureSpec = getMeasuredWidth()
                    - getPaddingLeft() - getPaddingRight();
            final int childHeightMeasureSpec = getMeasuredHeight()
                    - getPaddingTop() - getPaddingBottom();
            contentView.measure(MeasureSpec.makeMeasureSpec(
                    childWidthMeasureSpec, MeasureSpec.EXACTLY), MeasureSpec
                    .makeMeasureSpec(childHeightMeasureSpec,
                            MeasureSpec.EXACTLY));
        }
        if (loadmoreView != null) {
            measureChild(loadmoreView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        layoutRefreshView();
        layoutContentView();
        layoutLoadmoreView();
    }

    @Override
    public void setVisibility(int v) {
        if (getVisibility() != v)
            super.setVisibility(v);
    }

    private void layoutRefreshView() {
        if (refreshView != null) {
            final int width = getMeasuredWidth();
            final int childHeight = refreshView.getMeasuredHeight();
            final int childLeft = getPaddingLeft();
            int childTop = getPaddingTop() - childHeight;
            if (mCurrentContentOffsetTop > 0)
                childTop = mCurrentContentOffsetTop + childTop;
            final int childWidth = width - getPaddingLeft() - getPaddingRight();
            refreshView.layout(childLeft, childTop, childLeft + childWidth,
                    childTop + childHeight);
        }
    }

    private void layoutContentView() {
        if (contentView != null) {
            final int width = getMeasuredWidth();
            final int height = getMeasuredHeight();
            final int childLeft = getPaddingLeft();
            final int childTop = mCurrentContentOffsetTop + getPaddingTop();
            final int childWidth = width - getPaddingLeft() - getPaddingRight();
            final int childHeight = height - getPaddingTop()
                    - getPaddingBottom();
            contentView.layout(childLeft, childTop, childLeft + childWidth,
                    childTop + childHeight);
        }
    }

    private void layoutLoadmoreView() {
        if (loadmoreView != null) {
            final int width = getMeasuredWidth();
            final int height = getMeasuredHeight();
            final int childLeft = getPaddingLeft();
            int childTop = height - getPaddingBottom();
            if (mCurrentContentOffsetTop < 0)
                childTop = mCurrentContentOffsetTop + childTop;
            final int childWidth = width - getPaddingLeft() - getPaddingRight();
            final int childHeight = loadmoreView.getMeasuredHeight();
            loadmoreView.layout(childLeft, childTop, childLeft + childWidth,
                    childTop + childHeight);
        }
    }

    /**
     * @return 子视图是否能 上滑
     */
    public boolean canContentScrollUp() {
        if (contentView == null)
            return false;
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (contentView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) contentView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView
                        .getChildAt(0).getTop() < absListView
                        .getPaddingTop());
            } else {
                return contentView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(contentView, -1);
        }
    }

    /**
     * @return 子视图是否能 下滑
     */
    public boolean canContentScrollDown() {
        if (contentView == null)
            return false;
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (contentView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) contentView;
                ListAdapter adapter = absListView.getAdapter();
                boolean can = adapter != null;
                if (can) {
                    int lastP = absListView.getLastVisiblePosition();
                    int count = adapter.getCount();
                    can = lastP < count - 1;
                    if (!can) {
                        View lastView = absListView.getChildAt(absListView
                                .getChildCount() - 1);

                        can = lastView.getBottom() > getMeasuredHeight()
                                - getPaddingBottom();
                    }
                } else {

                }

                return can;
            } else {
                View childView = null;
                if (contentView instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) contentView;
                    childView = viewGroup.getChildAt(0);
                }

                if (childView == null)
                    return false;
                else {
                    int height = childView.getHeight();
                    return contentView.getScrollY() + getHeight() < height;
                }

            }
        } else {
            return ViewCompat.canScrollVertically(contentView, 1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isRefreshOrLoading)
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downY = ev.getY();
                    downX = ev.getX();
                    firstY = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveY = ev.getY() - downY;
                    float moveX = ev.getX() - downX;

                    float absY = Math.abs(moveY);
                    float absX = Math.abs(moveX);
                    if (absY > absX && absY > mTouchSlop) {
                        if (moveY > 0 && !canContentScrollUp()) {
                            pullDown = true;
                            if(!super.onInterceptTouchEvent(ev)){
                                if (isRefreshable) {
                                    return true;
                                }
                            }
//                            if (isRefreshable) {
//                                return true;
//                            } else {
//                                return super.onInterceptTouchEvent(ev);
//                            }
                        }
                        if (moveY < 0 && !canContentScrollDown()) {
                            pullDown = false;
                            if(!super.onInterceptTouchEvent(ev)){
                                if (isLoadmoreable) {
                                    return true;
                                }
                            }
                           /* if (isLoadmoreable) {
                                return true;
                            } else {
                                return super.onInterceptTouchEvent(ev);
                            }*/
                        }
                    }
                    break;
                default:
                    break;
            }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isRefreshOrLoading) {
            final int action = ev.getAction();
            float moveY;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    onInterceptTouchEvent(ev);
                    if (firstY == 0)
                        firstY = ev.getY();
                    moveY = ev.getY() - firstY;
                    if (pullDown) {
                        if (isRefreshable) {
                            offsetRefresh((int) (moveY));
                        } else {
                            return super.onTouchEvent(ev);
                        }

                    } else {
                        if (isLoadmoreable) {
                            offsetLoadmore(-(int) (moveY));
                        } else {
                            return super.onTouchEvent(ev);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    int move = contentView == null ? 0 : contentView.getTop();
                    if (pullDown) {
                        if (move >= getDistanceToRefresh() && isRefreshable) {
                            startRefresh();
                        } else {
                            animateToPositionForRefresh(0);
                        }
                    } else {
                        if (move <= -getDistanceToLoadmore() && isLoadmoreable) {
                            startLoadmore();
                        } else {
                            animateToPositionForLoadmore(0);
                        }
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    private int getDistanceToRefresh() {
        return refreshView == null ? defaultBeforeDistance : refreshView
                .getMeasuredHeight();
    }

    private int getDistanceToLoadmore() {
        return loadmoreView == null ? defaultBeforeDistance : loadmoreView
                .getMeasuredHeight();
    }

    private void startRefresh() {
        isRefreshOrLoading = true;
        animateToPositionForRefresh(getDistanceToRefresh());
        if (refreshViewListener != null) {
            refreshViewListener.onRefresh(refreshView);
        }
        if (onStartListener != null) {
            onStartListener.onStartRefresh(this);
        }
    }

    /**
     * 停止刷新
     */
    public void stopRefresh() {
        isRefreshOrLoading = false;
        animateToPositionForRefresh(0);
        if (refreshViewListener != null) {
            postDelayed(new Runnable() {

                @Override
                public void run() {
                    refreshViewListener.onReset(refreshView);
                }
            }, animationDuration);
        }
    }

    /**
     * 刷新成功 <b>(该方法会调用{@link #stopRefresh()})
     */
    public void refreshSuccess() {
        refreshViewListener.onSuccess(refreshView);
        postDelayed(new Runnable() {

            @Override
            public void run() {
                stopRefresh();
            }
        }, sucessOrFailedDuration);
    }

    /**
     * 刷新失败<b>(该方法会调用{@link #stopRefresh()})
     */
    public void refreshFailed() {
        refreshViewListener.onFailed(refreshView);
        postDelayed(new Runnable() {

            @Override
            public void run() {
                stopRefresh();
            }
        }, sucessOrFailedDuration);
    }

    private void startLoadmore() {
        isRefreshOrLoading = true;
        animateToPositionForLoadmore(getDistanceToLoadmore());
        if (loadmoreViewListener != null) {
            loadmoreViewListener.onLoadmore(refreshView);
        }
        if (onStartListener != null) {
            onStartListener.onStartLoadmore(this);
        }
    }

    /**
     * 停止加载
     */
    public void stopLoadmore() {
        isRefreshOrLoading = false;
        animateToPositionForLoadmore(0);
        if (loadmoreViewListener != null) {
            postDelayed(new Runnable() {

                @Override
                public void run() {
                    loadmoreViewListener.onReset(loadmoreView);
                }
            }, animationDuration);
        }
    }

    /**
     * 加载成功 <b>(该方法会调用{@link #stopLoadmore()})
     */
    public void loadmoreSuccess() {
        loadmoreViewListener.onSuccess(loadmoreView);
        postDelayed(new Runnable() {

            @Override
            public void run() {
                stopLoadmore();
            }
        }, sucessOrFailedDuration);
    }

    /**
     * 加载失败<b>(该方法会调用{@link #stopLoadmore()})
     */
    public void loadmoreFailed() {
        loadmoreViewListener.onFailed(loadmoreView);
        postDelayed(new Runnable() {

            @Override
            public void run() {
                stopLoadmore();
            }
        }, sucessOrFailedDuration);
    }

    private void animateToPositionForRefresh(int target) {
        refreshAnimation.reset(target);
        refreshAnimation.setDuration(animationDuration);
        refreshAnimation.setInterpolator(mDecelerateInterpolator);
        startAnimation(refreshAnimation);
    }

    private void animateToPositionForLoadmore(int target) {
        loadmoreAnimation.reset(target);
        loadmoreAnimation.setDuration(animationDuration);
        loadmoreAnimation.setInterpolator(mDecelerateInterpolator);
        startAnimation(loadmoreAnimation);
    }

    /**
     * @param offTop contentView顶部距父控件顶部的距离
     */
    private void offsetRefresh(int offTop) {
        if (contentView == null)
            return;
        final int currentOffTopTop = contentView.getTop();
        int distanceToRefresh = getDistanceToRefresh();
        if (offTop < 0) {
            offTop = 0;
        } else if (currentOffTopTop <= distanceToRefresh) {
            offTop = (int) (offTop * speedBeforeEnable);
        } else {
            int i = (int) (offTop - distanceToRefresh / speedBeforeEnable);
            offTop = (int) (distanceToRefresh + i * speedAfterEnable);
        }
        int offset = offTop - currentOffTopTop;
        if (contentView != null) {
            contentView.offsetTopAndBottom(offset);
            mCurrentContentOffsetTop = contentView.getTop();
        }
        if (refreshView != null)
            refreshView.offsetTopAndBottom(offset);

        if (refreshViewListener != null && !refreshAnimation.isRunning) {
            float percent = (float) offTop / (float) distanceToRefresh;
            refreshViewListener.onPulling(refreshView, Math.abs(percent));
        }
        invalidate();
    }

    private int getScorllDistanceForRefresh(int offTop) {
        if (offTop < 0)
            return 0;
        int dis = getDistanceToRefresh();
        if (offTop <= dis)
            return (int) (offTop / speedBeforeEnable);
        else {
            return (int) (dis / speedBeforeEnable + (offTop - dis)
                    / speedAfterEnable);
        }
    }

    /**
     * @param offBottom contentView底部距父控件底部的距离
     */
    private void offsetLoadmore(int offBottom) {
        if (contentView == null)
            return;
        final int currentOffBottom = -contentView.getTop();
        int distanceToLoadmore = getDistanceToLoadmore();
        if (offBottom < 0) {
            offBottom = 0;
        } else if (currentOffBottom <= distanceToLoadmore) {
            offBottom = (int) (offBottom * speedBeforeEnable);
        } else {
            int i = (int) (offBottom - distanceToLoadmore / speedBeforeEnable);
            offBottom = (int) (distanceToLoadmore + i * speedAfterEnable);
        }

        int offset = offBottom - currentOffBottom;
        if (contentView != null) {
            contentView.offsetTopAndBottom(-offset);
            mCurrentContentOffsetTop = contentView.getTop();
        }

        if (loadmoreView != null)
            loadmoreView.offsetTopAndBottom(-offset);
        if (loadmoreViewListener != null && !loadmoreAnimation.isRunning) {
            float percent = (float) offBottom / (float) distanceToLoadmore;
            loadmoreViewListener.onPulling(loadmoreView, Math.abs(percent));
        }

        invalidate();
    }

    private int getScorllDistanceForLoadmore(int offBottom) {
        if (offBottom < 0)
            return 0;
        int dis = getDistanceToLoadmore();
        if (offBottom <= dis)
            return (int) (offBottom / speedBeforeEnable);
        else {
            return (int) (dis / speedBeforeEnable + (offBottom - dis)
                    / speedAfterEnable);
        }
    }

    /**
     * 设置刷新视图
     *
     * @param viewLayoutId
     * @param l
     */
    public void setRefreshView(int viewLayoutId, RefreshViewListener l) {
        setRefreshView(viewLayoutId);
        setRefreshViewListener(l);
    }

    /**
     * 设置刷新视图
     *
     * @param v
     * @param l
     */
    public void setRefreshView(View v, RefreshViewListener l) {
        setRefreshView(v);
        setRefreshViewListener(l);
    }

    /**
     * 设置刷新视图
     *
     * @param viewLayoutId
     */
    public void setRefreshView(int viewLayoutId) {
        View v = LayoutInflater.from(getContext()).inflate(viewLayoutId, null);
        setRefreshView(v);
    }

    /**
     * 设置刷新视图
     *
     * @param v
     */
    public void setRefreshView(View v) {
        if (isInEditMode())
            return;

        if (refreshView == null) {
            if (contentView == null)
                contentIndex++;
            else
                refreshIndex++;

            if (loadmoreView != null)
                refreshIndex++;
        } else
            removeViewAt(refreshIndex);
        refreshView = v;
        addView(refreshView, refreshIndex);
    }

    /**
     * 设置加载视图
     *
     * @param viewLayoutId
     * @param l
     */
    public void setLoadmoreView(int viewLayoutId, LoadmoreViewListener l) {
        setLoadmoreView(viewLayoutId);
        setLoadmoreViewListener(l);
    }

    /**
     * 设置加载视图
     *
     * @param v
     * @param l
     */
    public void setLoadmoreView(View v, LoadmoreViewListener l) {
        setLoadmoreView(v);
        setLoadmoreViewListener(l);
    }

    /**
     * 设置加载视图
     *
     * @param viewLayoutId
     */
    public void setLoadmoreView(int viewLayoutId) {
        View v = LayoutInflater.from(getContext()).inflate(viewLayoutId, null);
        setLoadmoreView(v);
    }

    /**
     * 设置加载视图
     *
     * @param v
     */
    public void setLoadmoreView(View v) {
        if (isInEditMode())
            return;
        if (loadmoreView == null) {
            if (contentView == null)
                contentIndex++;
            else
                loadmoreIndex++;
            if (refreshView != null)
                loadmoreIndex++;
        } else
            removeViewAt(loadmoreIndex);
        loadmoreView = v;
        addView(loadmoreView, loadmoreIndex);
    }

    /**
     * @return 获取刷新视图监听
     */
    public RefreshViewListener getRefreshViewListener() {
        return refreshViewListener;
    }

    /**
     * 设置刷新视图监听
     *
     * @param refreshViewListener
     */
    public void setRefreshViewListener(RefreshViewListener refreshViewListener) {
        this.refreshViewListener = refreshViewListener;
    }

    /**
     * @return 获取加载视图监听
     */
    public LoadmoreViewListener getLoadmoreViewListener() {
        return loadmoreViewListener;
    }

    /**
     * 设置加载视图监听
     *
     * @param loadmoreViewListener
     */
    public void setLoadmoreViewListener(
            LoadmoreViewListener loadmoreViewListener) {
        this.loadmoreViewListener = loadmoreViewListener;
    }

    /**
     * @return 获取开始刷新或者加载更多监听
     */
    public OnStartListener getOnStartListener() {
        return onStartListener;
    }

    /**
     * 设置开始刷新或者加载更多监听
     *
     * @param onStartListener
     */
    public void setOnStartListener(OnStartListener onStartListener) {
        setRefreshable(true);
        setLoadmoreable(true);
        this.onStartListener = onStartListener;
    }

    /**
     * @return 是否能刷新
     */
    public boolean isRefreshable() {
        return isRefreshable;
    }

    /**
     * 设置是否能刷新
     *
     * @param isRefreshable
     */
    public void setRefreshable(boolean isRefreshable) {
        this.isRefreshable = isRefreshable;
        if (refreshView != null)
            refreshView.setVisibility(isRefreshable ? View.VISIBLE
                    : View.INVISIBLE);
    }

    /**
     * @return 是否能加载
     */
    public boolean isLoadmoreable() {
        return isLoadmoreable;
    }

    /**
     * 设置是否能加载
     *
     * @param isLoadmoreable
     */
    public void setLoadmoreable(boolean isLoadmoreable) {
        this.isLoadmoreable = isLoadmoreable;
        if (loadmoreView != null)
            loadmoreView.setVisibility(isLoadmoreable ? View.VISIBLE
                    : View.INVISIBLE);
    }

    /**
     * 设置View在能刷新或者加载之前的滑动速度
     *
     * @param speedBeforeEnable (0-1)默认为0.6,超出范围的视为默认值
     */
    public void setSpeedBefore(float speedBeforeEnable) {
        if (speedBeforeEnable < 0 || speedBeforeEnable > 1)
            speedBeforeEnable = 0.6f;
        this.speedBeforeEnable = speedBeforeEnable;
    }

    /**
     * 设置View在能刷新或者加载之后的滑动速度
     *
     * @param speedAfterEnable (0-1)默认为0.3,超出范围的视为默认值
     */
    public void setSpeedAfterEnable(float speedAfterEnable) {
        if (speedAfterEnable < 0 || speedAfterEnable > 1)
            speedAfterEnable = 0.3f;
        this.speedAfterEnable = speedAfterEnable;
    }

    /**
     * 复位动画持续时间
     *
     * @param animationDuration 毫秒(默认500)
     */
    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    /**
     * 刷新(加载)成功(失败)停留时间
     *
     * @param sucessOrFailedDuration 毫秒(默认500)
     */
    public void setSucessOrFailedDuration(int sucessOrFailedDuration) {
        this.sucessOrFailedDuration = sucessOrFailedDuration;
    }

    // 刷新复位动画
    private class ToPositionForRefreshAnimation extends Animation {
        private int from;
        private int target;
        private boolean isRunning = false;

        private AnimationListener listener = new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                isRunning = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isRunning = false;
            }
        };

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            if (isRunning) {
                int offset = (int) (target + (from - target)
                        * (1 - interpolatedTime));
                offsetRefresh(offset);
                if (offset <= target)
                    isRunning = false;
            }
        }

        /**
         * 重置动画
         *
         * @param target 复位位置
         */
        public void reset(int target) {
            reset();
            this.target = getScorllDistanceForRefresh(target);
            this.from = getScorllDistanceForRefresh(mCurrentContentOffsetTop);
            setAnimationListener(listener);
        }
    }

    // 加载复位动画
    private class ToPositionForLoadmoreAnimation extends Animation {
        private int from;
        private int target;
        private boolean isRunning = false;
        private AnimationListener listener = new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                isRunning = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isRunning = false;
            }
        };

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            if (isRunning) {
                int offset = (int) (target + (from - target)
                        * (1 - interpolatedTime));
                offsetLoadmore(offset);
                if (offset <= target)
                    isRunning = false;
            }
        }

        /**
         * 重置动画
         *
         * @param target 复位位置
         */
        public void reset(int target) {
            reset();
            this.target = getScorllDistanceForLoadmore(target);
            this.from = getScorllDistanceForLoadmore(-mCurrentContentOffsetTop);
            setAnimationListener(listener);
        }
    }

    /**
     * 刷新视图监听
     */
    public interface RefreshViewListener {
        /**
         * 正在下拉
         *
         * @param refreshView 刷新视图
         * @param percent     下拉百分比(为refreshView高度的倍数)
         */
        public void onPulling(View refreshView, float percent);

        /**
         * 视图重置
         *
         * @param refreshView 刷新视图
         */
        public void onReset(View refreshView);

        /**
         * 开始刷新
         *
         * @param refreshView 刷新视图
         */
        public void onRefresh(View refreshView);

        /**
         * 刷新成功
         *
         * @param refreshView 刷新视图
         */
        public void onSuccess(View refreshView);

        /**
         * 刷新失败
         *
         * @param refreshView 刷新视图
         */
        public void onFailed(View refreshView);

    }

    /**
     * 加载视图监听
     */
    public interface LoadmoreViewListener {
        /**
         * 正在下拉
         *
         * @param loadmoreView 加载视图
         * @param percent      上拉百分比(为loadmoreView高度的倍数)
         */
        public void onPulling(View loadmoreView, float percent);

        /**
         * 视图重置
         *
         * @param loadmoreView 加载视图
         */
        public void onReset(View loadmoreView);

        /**
         * 开始加载
         *
         * @param loadmoreView 加载视图
         */
        public void onLoadmore(View loadmoreView);

        /**
         * 刷新成功
         *
         * @param loadmoreView 加载视图
         */
        public void onSuccess(View loadmoreView);

        /**
         * 刷新失败
         *
         * @param loadmoreView 加载视图
         */
        public void onFailed(View loadmoreView);
    }

    /**
     * 开始刷新或者加载更多监听
     */
    public interface OnStartListener {
        /**
         * 开始刷新
         *
         * @param v
         */
        public void onStartRefresh(RefreshLoadmoreLayout v);

        /**
         * 开始加载更多
         *
         * @param v
         */
        public void onStartLoadmore(RefreshLoadmoreLayout v);
    }

}
