package com.hemaapp.hm_FrameWork.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.hemaapp.PoplarConfig;
import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.PoplarUtil;
import com.hemaapp.hm_FrameWork.R;
import com.hemaapp.hm_FrameWork.image.ImageWorker;
import com.hemaapp.hm_FrameWork.util.FileUtil;
import com.hemaapp.hm_FrameWork.util.TimeUtil;
import com.hemaapp.hm_FrameWork.util.ToastUtil;
import com.hemaapp.hm_FrameWork.view.photoview.PhotoView;
import com.hemaapp.hm_FrameWork.view.photoview.PhotoViewAttacher;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * 看大图
 * Created by HuHu on 2017-05-13.
 */

public class ShowLargeImageView extends PoplarObject {
    private Dialog mDialog;
    private Context mContext;

    private View layoutFather;
    private PhotoView imgPhoto;
    private ProgressBar pbImage;
    private String urlPath, localPath;
    private View rootView;
    private ImageWorker imageWorker;

    public ShowLargeImageView(Context mContext) {
        this.mContext = mContext;
        mDialog = new Dialog(mContext, R.style.bottom_up_dialog);
        rootView = LayoutInflater.from(mContext).inflate(R.layout.showlargeimageview, null);
        findView();
        setListener();

        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        params.x = 0;// 设置x坐标
        params.y = 0;// 设置y坐标
        params.width = PoplarUtil.getScreenWidth(mContext);
        params.height = PoplarUtil.getScreenHeight(mContext);
        imageWorker = new ImageWorker(mContext);
    }

    private void findView() {
        layoutFather = rootView.findViewById(R.id.layoutFather);
        imgPhoto = (PhotoView) rootView.findViewById(R.id.imgPhoto);
        pbImage = (ProgressBar) rootView.findViewById(R.id.pbImage);
    }

    private void setListener() {
        layoutFather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        imgPhoto.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                cancel();
            }
        });
        imgPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                String[] items = {"保存到手机", "取消"};
                builder.setItems(items, new DialogClickListener());
                builder.show();
                return true;
            }
        });
    }


    private class DialogClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case 0:// 保存到手机
                    copy();
                    break;
            }
        }

        private void copy() {
            if (!FileUtil.isExternalMemoryAvailable()) {
                ToastUtil.showShortToast(mContext, "没有SD卡,不能复制");
                return;
            }
            String imgPath = isNull(urlPath) ? localPath : urlPath;
            String saveDir = FileUtil.getExternalMemoryPath();
            String pakage = mContext.getPackageName();
            String folder = "images";
            int dot = pakage.lastIndexOf('.');
            if (dot != -1) {
                folder = pakage.substring(dot + 1);
            }
            saveDir += "/hemaapp/" + folder;
            String fileName = TimeUtil
                    .getCurrentTime("yyyyMMddHHmmssSSS") + ".jpg";
            String savePath = saveDir + "/" + fileName;
            new GetImageCacheTask(mContext, savePath).execute(imgPath);
        }
    }

    private class GetImageCacheTask extends AsyncTask<String, Void, File> {
        private final Context context;
        private String savePath;

        public GetImageCacheTask(Context context, String savePath) {
            this.context = context;
            this.savePath = savePath;
        }

        @Override
        protected File doInBackground(String... params) {
            String imgUrl = params[0];
            try {
                return Glide.with(context)
                        .load(imgUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            if (result == null) {
                return;
            }
            //此path就是对应文件的缓存路径
            String path = result.getPath();
            boolean state = FileUtil.copy(path, savePath);
            if (state) {
                ToastUtil.showShortToast(mContext, "图片已保存至" + savePath);
            } else {
                ToastUtil.showShortToast(mContext, "图片保存失败");
            }
        }
    }

    public void setImageURL(String urlPath) {
        this.urlPath = urlPath;
//        ImageWorker.getInstance(mContext).loadImageBitmapFromPath(urlPath, imgPhoto);
        /*完全加载原图的代价太高，缩放图片控件压力太大，保存到本地之后就可以查看高清大图了*/
        Glide.with(mContext).load(urlPath).asBitmap().into(new LoadSimpleTarget(PoplarConfig.DEFAULT_IMAGE_SIZE, PoplarConfig.DEFAULT_IMAGE_SIZE));
    }

    public void setImagePath(String localPath) {
        this.localPath = localPath;
        imageWorker.loadImageBitmapFromPath(urlPath, imgPhoto);
    }

    public void show() {
        mDialog.show();
    }

    public void cancel() {
        mDialog.cancel();
    }

    private class LoadSimpleTarget extends SimpleTarget<Bitmap> {
        public LoadSimpleTarget(int width, int height) {
            super(width, height);
        }

        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
            imgPhoto.setImageBitmap(bitmap);

        }
    }
}
