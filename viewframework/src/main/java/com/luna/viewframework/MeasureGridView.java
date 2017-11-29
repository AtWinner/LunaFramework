package com.luna.viewframework;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 全部显示图片的GridView
 * Created by Hufanglin on 2016/3/18.
 */
public class MeasureGridView extends GridView {

    public MeasureGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MeasureGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MeasureGridView(Context context) {
        super(context);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}