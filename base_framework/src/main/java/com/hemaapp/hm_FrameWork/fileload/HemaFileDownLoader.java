package com.hemaapp.hm_FrameWork.fileload;

import android.content.Context;
import android.os.Looper;
import android.os.Message;

import com.hemaapp.hm_FrameWork.PoplarObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * 文件下载器(多线程异步断点续传)
 */
public class HemaFileDownLoader extends PoplarObject {
    private static final int START = 0;
    private static final int LOADING = 1;
    private static final int STOP = 2;
    private static final int FAILED = 3;
    private static final int SUCCESS = 4;

    private int threadCount = 4;// 异步线程数
    private Context context;// 上下文
    private String downPath;// 文件下载地址
    private String savePath;// 文件保存地址
    private EventHandler eventHandler;// 用于更新UI
    private FileInfo fileInfo;// 下载文件信息

    private ControlThread controlThread;

    private HemaFileDownLoader() {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            eventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            eventHandler = new EventHandler(this, looper);
        } else {
            eventHandler = null;
        }
    }

    /**
     * 文件下载器(多线程异步断点续传)
     *
     * @param context  上下文
     * @param downPath 文件下载地址
     * @param savePath 文件保存地址
     */
    public HemaFileDownLoader(Context context, String downPath, String savePath) {
        this();
        this.context = context;
        this.downPath = downPath;
        this.savePath = savePath;
    }

    /**
     * 开始下载
     */
    public void start() {
        if (isLoading()) {
            log_d("正在下载,请勿重复操作");
            return;
        }
        controlThread = new ControlThread();
        controlThread.start();
    }

    /**
     * @return 是否正在下载
     */
    public boolean isLoading() {
        return controlThread != null && controlThread.isAlive();
    }

    /**
     * 停止下载
     */
    public void stop() {
        if (controlThread != null)
            controlThread.stopLoad();
    }

    /**
     * @return 文件是否已经下载完成
     */
    public boolean isFileLoaded() {
        boolean isLoaded;
        // 首先判断文件是否存在
        File file = new File(savePath);
        isLoaded = file.exists();

        FileInfo fileInfo = getFileInfo();
        if (isLoaded) {
            // 如果文件存在,再判断下载进度
            if (fileInfo != null) {
                int curr = fileInfo.getCurrentLength();
                int cont = fileInfo.getContentLength();
                isLoaded = cont != 0 && curr == cont;
            } else {
                isLoaded = false;
            }
        } else {
            // 如果文件不存在,删除下载线程信息
            if (fileInfo != null) {
                // DownLoadDBHelper dbHelper = new DownLoadDBHelper(context);
                DownLoadDBHelper dbHelper = DownLoadDBHelper
                        .getInstance(context);
                dbHelper.deleteThreadInfos(fileInfo.getId());
                dbHelper.close();
            }

        }
        return isLoaded;
    }

    /**
     * 下载控制线程
     */
    private class ControlThread extends Thread {
        private DownLoadDBHelper dbHelper;// 数据库帮助工具
        private ArrayList<DownLoadThread> downLoadThreads = new ArrayList<DownLoadThread>();

        // 停止下载
        private void stopLoad() {
            for (DownLoadThread thread : downLoadThreads) {
                thread.setStop(true);
            }
        }

        // 获取文件信息
        private FileInfo getFileInfo(int contentLenght) {
            // 先从数据库取
            FileInfo fileInfo = dbHelper.getFileInfo(downPath, savePath,
                    threadCount);

            if (fileInfo == null) {
                // 如果数据库没有,新建一个
                fileInfo = new FileInfo(0, downPath, savePath, threadCount,
                        contentLenght, 0);
                // 插入数据库
                dbHelper.insertFileInfo(fileInfo);
                // 再次从数据库取出,更新id等信息
                fileInfo = dbHelper
                        .getFileInfo(downPath, savePath, threadCount);
            }

            return fileInfo;
        }

        // 获取下载线程信息
        private ThreadInfo getThreadInfo(int fileID, int threadID,
                                         int startPosition, int endPosition) {
            // 先从数据库取
            ThreadInfo info = dbHelper.getThreadInfo(fileID, threadID);

            if (info == null) {
                // 如果数据库没有,新建一个
                info = new ThreadInfo(0, fileID, threadID, startPosition,
                        endPosition, startPosition);
                // 插入数据库
                dbHelper.insertThreadInfo(info);
                // 再次从数据库取出,更新id等信息
                info = dbHelper.getThreadInfo(fileID, threadID);
            }

            return info;
        }

        @Override
        public void run() {
            // dbHelper = new DownLoadDBHelper(context);
            dbHelper = DownLoadDBHelper.getInstance(context);

            fileInfo = getFileInfo(0);

            if (isFileLoaded()) {
                eventHandler.sendEmptyMessage(SUCCESS);
            } else {
                eventHandler.sendEmptyMessage(START);

                try {
                    URL url = new URL(downPath);
                    URLConnection conn = url.openConnection();
                    // 获取下载文件的总大小
                    int contentLength = conn.getContentLength();
                    if (contentLength > 0) {
                        fileInfo.setContentLength(contentLength);
                        dbHelper.updateFileInfo(fileInfo);
                    }

                    // 处理保存文件信息
                    File file = new File(savePath);
                    File filedir = file.getParentFile();
                    if (!filedir.exists())
                        filedir.mkdirs();
                    // 创建临时文件
                    if (!file.exists() && contentLength > 0) {
                        log_d("开始创建临时文件");
                        long start = System.currentTimeMillis();
                        FileOutputStream fos = new FileOutputStream(file);
                        int bs = contentLength % 1024;
                        int kbs = contentLength / 1024;
                        byte[] kbf = new byte[1024];
                        byte[] bf = new byte[1];

                        for (int i = 0; i < kbs; i++) {
                            fos.write(kbf);
                        }
                        for (int i = 0; i < bs; i++) {
                            fos.write(bf);
                        }
                        fos.close();
                        long end = System.currentTimeMillis();
                        log_d("结束创建临时文件,用时" + (end - start));
                    }

                    // 初始化下载线程
                    int threadLength = contentLength / threadCount;
                    int remainLength = contentLength % threadCount;
                    downLoadThreads.clear();
                    for (int i = 0; i < threadCount; i++) {
                        int startPosition = i * threadLength;
                        // 如果是最后一段文件,把整除后剩余部分加上
                        int endPosition = i == threadCount - 1 ? (i + 1)
                                * threadLength - 1 + remainLength : (i + 1)
                                * threadLength - 1;

                        ThreadInfo info = getThreadInfo(fileInfo.getId(), i,
                                startPosition, endPosition);

                        DownLoadThread fdt = new DownLoadThread(context, url,
                                file, info);
                        downLoadThreads.add(fdt);
                    }

                    // 启动线程，分别下载自己需要下载的部分
                    for (DownLoadThread thread : downLoadThreads) {
                        thread.start();
                    }

                    // 开启计时循环,监控下载进度
                    boolean isRun = true;
                    while (isRun) {
                        checkThreads();

                        fileInfo.setCurrentLength(currentLength);
                        dbHelper.updateFileInfo(fileInfo);

                        eventHandler.sendEmptyMessage(LOADING);

                        if (failed) {// 如果有一个线程失败,判断为下载失败。停止所有下载线程
                            stopLoad();
                        }

                        if (stoped) {
                            if (failed)
                                eventHandler.sendEmptyMessage(FAILED);
                            else {
                                eventHandler.sendEmptyMessage(STOP);
                            }
                            isRun = false;
                        }

                        if (finished) {
                            eventHandler.sendEmptyMessage(SUCCESS);
                            isRun = false;
                        }
                        // 休息500毫秒后再读取下载进度
                        sleep(500);
                    }
                } catch (Exception e) {
                    stopLoad();
                    eventHandler.sendEmptyMessage(FAILED);
                }
            }
            dbHelper.close();
        }

        int currentLength = 0;
        boolean finished;
        boolean failed;
        boolean stoped;

        private void checkThreads() {
            currentLength = 0;
            finished = true;
            stoped = true;
            failed = false;

            for (int i = 0; i < downLoadThreads.size(); i++) {
                DownLoadThread thread = downLoadThreads.get(i);
                currentLength += thread.getDownloadLength();

                if (!thread.isFinished()) {// 所有线程都完成,判断为下载完成
                    finished = false;
                }

                if (!thread.isStop()) {// 所有线程都停止,判断为下载停止
                    stoped = false;
                }

                if (thread.isFailed()) {// 如果一个线程失败,判断为文件下载失败
                    failed = true;
                }
            }
        }

    }

    private static class EventHandler extends android.os.Handler {
        private HemaFileDownLoader downLoader;

        public EventHandler(HemaFileDownLoader downLoader, Looper looper) {
            super(looper);
            this.downLoader = downLoader;
        }

        @Override
        public void handleMessage(Message msg) {
            HemaDownLoadListener listener = downLoader
                    .getHemaDownLoadListener();
            if (listener != null)
                switch (msg.what) {
                    case START:
                        listener.onStart(downLoader);
                        break;
                    case LOADING:
                        listener.onLoading(downLoader);
                        break;
                    case STOP:
                        listener.onStop(downLoader);
                        break;
                    case FAILED:
                        listener.onFailed(downLoader);
                        break;
                    case SUCCESS:
                        listener.onSuccess(downLoader);
                        break;
                }
        }

    }

    /**
     * @return 异步线程数(默认1)
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * 设置异步线程数
     *
     * @param threadCount (默认1)
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    /**
     * @return 文件下载信息 或者 null
     */
    public FileInfo getFileInfo() {
        if (fileInfo == null) {
            // DownLoadDBHelper dbHelper = new DownLoadDBHelper(context);
            DownLoadDBHelper dbHelper = DownLoadDBHelper.getInstance(context);
            fileInfo = dbHelper.getFileInfo(downPath, savePath, threadCount);
            dbHelper.close();
        }
        return fileInfo;
    }

    /**
     * @return 文件下载路径
     */
    public String getDownPath() {
        return downPath;
    }

    /**
     * @return 文件保存路径
     */
    public String getSavePath() {
        return savePath;
    }

    private HemaDownLoadListener hemaDownLoadListener;

    public HemaDownLoadListener getHemaDownLoadListener() {
        return hemaDownLoadListener;
    }

    public void setHemaDownLoadListener(
            HemaDownLoadListener hemaDownLoadListener) {
        this.hemaDownLoadListener = hemaDownLoadListener;
    }

    /**
     * 下载监听
     */
    public interface HemaDownLoadListener {

        public void onStart(HemaFileDownLoader loader);

        public void onSuccess(HemaFileDownLoader loader);

        public void onFailed(HemaFileDownLoader loader);

        public void onLoading(HemaFileDownLoader loader);

        public void onStop(HemaFileDownLoader loader);
    }

}
