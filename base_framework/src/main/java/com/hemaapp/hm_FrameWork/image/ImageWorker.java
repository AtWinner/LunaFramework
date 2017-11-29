package com.hemaapp.hm_FrameWork.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.util.Util;
import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.PoplarUtil;
import com.hemaapp.hm_FrameWork.R;

import java.util.concurrent.ExecutionException;

/**
 * 图片加载器 基于Glide
 */

public class ImageWorker extends PoplarObject {
    private Context mContext;

    public ImageWorker(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 通过URL加载图片到指定控件
     *
     * @param url          图片URL
     * @param defaultImage 默认图片（Resource）
     * @param imageView    显示图片的控件
     */
    public void loadImageBitmapFromUrl(String url, int defaultImage, ImageView imageView) {
        if (url == null || url.equals("")) {
            imageView.setImageResource(defaultImage);
            return;
        }
        log_i("loadImage:" + url);
        Glide.with(mContext).load(url).placeholder(defaultImage).error(defaultImage).into(imageView);
    }

    /**
     * 通过URL加载图片到控件并显示为圆形
     *
     * @param url          图片URL
     * @param defaultImage 默认图片（Resource）
     * @param imageView    显示图片的控件
     */
    public void loadImageCircleBitmapFromUrl(String url, int defaultImage, ImageView imageView) {
        log_i("loadImage:" + url);
        Glide.with(mContext).load(url).asBitmap().centerCrop().placeholder(defaultImage).into(getCircleImage(imageView));
    }

    /**
     * 通过文件途径加载图片
     *
     * @param path      文件路径
     * @param imageView 显示图片的控件
     */
    public void loadImageBitmapFromPath(String path, ImageView imageView) {
        if (Util.isOnMainThread()) {
            log_i("loadImage:" + path);
            Glide.with(mContext).load(path).into(imageView);
        }
    }

    /**
     * 通过文件途径加载图片
     *
     * @param path      文件路径
     * @param imageView 显示图片的控件
     */
    public void loadImageCircleBitmapFromPath(String path, ImageView imageView) {
        log_i("loadImage:" + path);
        Glide.with(mContext).load(path).asBitmap().centerCrop().into(getCircleImage(imageView));
    }

    /**
     * 通过URL加载Gif图片到指定控件
     *
     * @param url          Gif图片URL
     * @param defaultImage 默认图片（Resource）
     * @param imageView    显示图片的控件
     */
    public void loadGifFromUrl(String url, int defaultImage, ImageView imageView) {
        if (url == null || url.equals("")) {
            imageView.setImageResource(defaultImage);
            return;
        }
        log_i("loadImage:" + url);
        Glide.with(mContext).load(url).asGif().centerCrop().placeholder(defaultImage).error(defaultImage).into(imageView);
    }

    public void loadImageRoundBitmapFromUrl(String url, int defaultImage, ImageView imageView, float radius) {
        log_i("loadImage:" + url);
        Glide.with(mContext).load(url).asBitmap().centerCrop().placeholder(defaultImage).animate(R.anim.show_in_alpha).into(getRoundImage(imageView, radius));
    }

    public void loadRoundResource(int resource, ImageView imageView, float radius) {
        Glide.with(mContext).load(resource).asBitmap().centerCrop().animate(R.anim.show_in_alpha).into(getRoundImage(imageView, radius));
    }

    /**
     * 获取圆形对象
     *
     * @param imageView
     * @return
     */
    public BitmapImageViewTarget getCircleImage(final ImageView imageView) {
        return new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        };
    }

    public BitmapImageViewTarget getRoundImage(final ImageView imageView, final float cornerRadius) {
        return new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                circularBitmapDrawable.setCornerRadius(cornerRadius);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        };
    }

    public void clearTasks() {
//        Glide.with(mContext).onDestroy();
    }

}