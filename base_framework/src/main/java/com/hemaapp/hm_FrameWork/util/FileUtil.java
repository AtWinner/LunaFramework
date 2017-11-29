package com.hemaapp.hm_FrameWork.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 文件工具类
 */
public class FileUtil {
    private static final String TAG = "HemaFileUtil";

    /**
     * 复制文件到指定目录
     *
     * @param filePath 被复制文件地址
     * @param savePath 保存文件地址
     * @return 复制是否成功
     */
    public static boolean copy(String filePath, String savePath) {
        File file = new File(filePath);
        if (!file.exists())
            return false;
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(file));
            File temp = new File(savePath);
            if (!temp.exists()) {
                File dirFile = temp.getParentFile();
                if (!dirFile.exists())
                    dirFile.mkdirs();
                temp.createNewFile();
            }

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(temp));
            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭流
            try {
                if (inBuff != null)
                    inBuff.close();
                if (outBuff != null)
                    outBuff.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return true;
    }

    /**
     * 获取外部存储根目录
     *
     * @return 如果有返回String，否则null
     */
    public static final String getExternalMemoryPath() {
        return (isExternalMemoryAvailable()) ? Environment
                .getExternalStorageDirectory().getPath() : null;
    }

    /**
     * 判断是否有外部存储
     *
     * @return 如果有返回true，否则false
     */
    @SuppressLint("NewApi")
    public static final boolean isExternalMemoryAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                && (Environment.getExternalStorageState() != null)
                && (Environment.MEDIA_MOUNTED_READ_ONLY != Environment
                .getExternalStorageState());
    }

    /**
     * 删除临时文件
     *
     * @param context
     * @return
     */
    public static final void deleteTempFile(Context context) {
        File cacheDir = new File(getTempFileDir(context));
        if (!cacheDir.exists())
            return;
        final File[] files = cacheDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
        HemaLogger.d(TAG, "delete " + files.length + " temp files");
    }

    /**
     * 获取临时文件存放目录
     *
     * @param context
     * @return
     */
    public static final String getTempFileDir(Context context) {
        String path = getFileDir(context);
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return path;
    }

    /**
     * 获取临时文件存放目录
     *
     * @param context
     * @return
     */
    public static final String getFileDir(Context context) {
        String path = isExternalMemoryAvailable() ? context
                .getExternalFilesDir(null).getPath() + "/" : context
                .getFilesDir().getPath() + "/";
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return path;
    }

    /**
     * 获取缓存目录
     *
     * @param context
     * @return
     */
    public static final String getCacheDir(Context context) {
        if (isExternalMemoryAvailable()) {
            File file = context.getExternalCacheDir();
            if (file != null) {
                return file.getPath() + "/";
            }
        }
        return context.getCacheDir().getPath() + "/";
    }

    /**
     * 删除文件
     *
     * @param filepath
     */
    public static boolean deleteFile(String filepath) {
        File file = new File(filepath);
        return file.delete();
    }

    /**
     * 获取缓存名
     *
     * @param key
     * @return
     */
    public static String getKeyForCache(String key) {
        String cacheKey;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    } /**
     * 创建文件夹
     *
     * @param path
     * @return
     */
    public static File createDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Log.e("createDir", "创建成功");
            } else {
                Log.e("createDir", "创建失败");
            }
        }
        return dir;

    }
}
