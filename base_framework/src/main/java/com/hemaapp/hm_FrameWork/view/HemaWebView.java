package com.hemaapp.hm_FrameWork.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class HemaWebView extends WebView {

    private Context context;

    private ProgressBar progressbar;

    public HemaWebView(Context context) {
        this(context, null);
        this.context = context;
    }

    public HemaWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        set(context);
        this.context = context;
    }

    public HemaWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        set(context);
        this.context = context;
    }

    private void set(Context context) {
        progressbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressbar.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,10, 0, 0));
        addView(progressbar);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setPluginState(PluginState.ON);
        setWebViewClient(new HemaWebViewClient());
        setWebChromeClient(new HemaWebChromeClient());
        addJavascriptInterface(new JavascriptInterface(context), "imagelistner");
    }

    private class HemaWebViewClient extends WebViewClient {

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                new URL(url);// 检查是否是合法的URL
                view.loadUrl(url);
                return true;
            } catch (MalformedURLException e) {
                return false;
            }

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // html加载完成之后，添加监听图片的点击js函数
            new Runnable() {
                @Override
                public void run() {
                    addImageClickListner();
                }
            }.run();
        }
    }

    public class JavascriptInterface {
        private Context context;

        public JavascriptInterface(Context context) {
            this.context = context;
        }

        @android.webkit.JavascriptInterface
        public void openImage(String img) {
            if (onImageClickListener != null) {
                onImageClickListener.clickImage(img);
            }
        }
    }

    private OnImageClickListener onImageClickListener;

    /**
     * 设置点击图片事件
     *
     * @param onImageClickListener
     */
    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    public interface OnImageClickListener {
        public void clickImage(String imageUrl);
    }

    /*捕捉webview点击事件结束*/


    /*捕捉webview点击事件*/
    // 注入js函数监听
    private void addImageClickListner() {
        // 这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，在还是执行的时候调用本地接口传递url过去
        loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistner.openImage(this.src);  " +
                "    }  " +
                "}" +
                "})()");
    }

    public class HemaWebChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(GONE);
            } else {
                if (progressbar.getVisibility() == GONE)
                    progressbar.setVisibility(VISIBLE);
                progressbar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }

}
