package com.hemaapp.hm_FrameWork;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import com.hemaapp.hm_FrameWork.util.BaseUtil;
import com.hemaapp.hm_FrameWork.util.FileUtil;

import java.io.File;

/**
 * Created by HuHu on 2017-07-04.
 */

public class PoplarImageWay extends PoplarObject {
    private Activity mContext;
    protected int albumRequestCode;// 相册选择时startActivityForResult方法的requestCode值
    protected int cameraRequestCode;// 拍照选择时startActivityForResult方法的requestCode值
    protected static final String IMAGE_TYPE = ".jpg";// 图片名后缀
    protected String imagePathByCamera;// 拍照时图片保存路径
    private Fragment fragment;

    public PoplarImageWay(Activity mContext, int albumRequestCode, int cameraRequestCode) {
        this.mContext = mContext;
        this.albumRequestCode = albumRequestCode;
        this.cameraRequestCode = cameraRequestCode;
    }

    public PoplarImageWay(Fragment fragment, int albumRequestCode, int cameraRequestCode) {
        this.fragment = fragment;
        this.albumRequestCode = albumRequestCode;
        this.cameraRequestCode = cameraRequestCode;
        mContext = fragment.getActivity();
    }

    public void album() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, albumRequestCode);
            return;
        }
        Intent it1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (fragment != null) {
            fragment.startActivityForResult(it1, albumRequestCode);
        } else if (mContext != null) {
            mContext.startActivityForResult(it1, albumRequestCode);
        }
    }

    /**
     * 相机获取
     * 2017年4月10日
     * 加入响应式权限和适应Android7.0的特性
     */
    public void camera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.CAMERA}, cameraRequestCode);
            return;
        }
        String imageName = BaseUtil.getFileName() + IMAGE_TYPE;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String imageDir = FileUtil.getTempFileDir(mContext);
        imagePathByCamera = imageDir + imageName;
        File file = new File(imageDir);
        if (!file.exists())
            file.mkdir();
        // 设置图片保存路径
        File out = new File(file, imageName);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String packageName = mContext.getPackageName();
            uri = FileProvider.getUriForFile(mContext, packageName + ".fileprovider", out);
        } else {
            uri = Uri.fromFile(out);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        if (fragment != null) {
            fragment.startActivityForResult(intent, cameraRequestCode);
        } else if (mContext != null) {
            mContext.startActivityForResult(intent, cameraRequestCode);
        }
    }

    /**
     * 获取拍照图片路径
     *
     * @return 图片路径
     */
    public String getCameraImage() {
        return imagePathByCamera;
    }
}
