package com.hemaapp.hm_FrameWork.image;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;

import java.io.File;
import java.util.Arrays;

/**
 * 指定图片加载位置
 */

public class HemaGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //设置格式
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        //缓存到data目录下最大50M
        //缓存目录为程序内部缓存目录
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE));
        //缓存到外部磁盘SD卡上,字节
//        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE));
        //设置内存缓存大小
        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
        int customMemoryCacheSize = (int) (1.2 * defaultMemoryCacheSize);
        int customBitmapPoolSize = (int) (1.2 * defaultBitmapPoolSize);
        builder.setMemoryCache(new LruResourceCache(customMemoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {


    }

    /**
     * 获取图片缓存大小
     * @param context
     * @param textView
     */
    public void getDiskCacheSize(Context context, TextView textView) {
        //获取data下
        new GetDiskCacheSizeTask(textView).execute(new File(context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR));
        //获取sd卡下
        //new GetDiskCacheSizeTask(textView).execute(new File(context.getExternalCacheDir(),DiskCache.Factory.DEFAULT_DISK_CACHE_DIR));
    }


    class GetDiskCacheSizeTask extends AsyncTask<File, Long, Long> {
        private final TextView resultView;

        public GetDiskCacheSizeTask(TextView resultView) {
            this.resultView = resultView;
        }

        @Override
        protected void onPreExecute() {
            resultView.setText("Calculating...");
        }

        @Override
        protected void onProgressUpdate(Long... values) { /* onPostExecute(values[values.length - 1]); */ }

        @Override
        protected Long doInBackground(File... dirs) {
            try {
                long totalSize = 0;
                for (File dir : dirs) {
                    publishProgress(totalSize);
                    totalSize += calculateSize(dir);
                }
                return totalSize;
            } catch (RuntimeException ex) {
                final String message = String.format("Cannot get size of %s: %s", Arrays.toString(dirs), ex);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        resultView.setText("error");
                        Toast.makeText(resultView.getContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
            }
            return 0L;
        }

        @Override
        protected void onPostExecute(Long size) {
            String sizeText = android.text.format.Formatter.formatFileSize(resultView.getContext(), size);
            resultView.setText(sizeText);
        }

        private long calculateSize(File dir) {
            if (dir == null) return 0;
            if (!dir.isDirectory()) return dir.length();
            long result = 0;
            File[] children = dir.listFiles();
            if (children != null)
                for (File child : children)
                    result += calculateSize(child);
            return result;
        }
    }
}
