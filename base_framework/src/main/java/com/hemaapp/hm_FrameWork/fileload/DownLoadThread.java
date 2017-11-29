package com.hemaapp.hm_FrameWork.fileload;

import android.content.Context;

import com.hemaapp.PoplarConfig;
import com.hemaapp.hm_FrameWork.util.HemaLogger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

class DownLoadThread extends Thread {
    private static final String TAG = "DownLoadThread";
    private static final int BUFFER_SIZE = 1024;

    private Context context;
    private URL url;
    private File file;
    private ThreadInfo threadInfo;
    private int downloadLength = 0;
    private boolean finished = false;// 当前线程是否下载完成
    private boolean failed = false;// 当前线程是否下载失败
    private boolean stop = false;// 当前线程是否停止

    private DownLoadDBHelper dbHelper;

    public DownLoadThread(Context context, URL url, File file,
                          ThreadInfo threadInfo) {
        this.context = context;
        this.url = url;
        this.file = file;
        this.threadInfo = threadInfo;

        int start = threadInfo.getStartPosition();
        int curr = threadInfo.getCurrentPosition();
        int end = threadInfo.getEndPosition();
        this.downloadLength = curr - start + 1;
        String path = url.toString();
        String threadName = "[文件(" + path + ")下载(" + start + "-" + end + ")线程:"
                + threadInfo.getThreadID() + "]";
        setName(threadName);
    }

    // 下载线程是否已经完成
    private boolean isThreadFinished() {
        boolean isFinished;
        int curr = threadInfo.getCurrentPosition();
        int end = threadInfo.getEndPosition();
        isFinished = end != 0 && curr == end;
        return isFinished;
    }

    @Override
    public void run() {
        if (isThreadFinished()) {
            HemaLogger.d(TAG, getName() + "已经完成");
            finished = true;
            return;
        }

        HemaLogger.d(TAG, getName() + "开始执行");
        // dbHelper = new DownLoadDBHelper(context);
        dbHelper = DownLoadDBHelper.getInstance(context);

        int startPosition = threadInfo.getStartPosition();
        int endPosition = threadInfo.getEndPosition();
        int currentPosition = threadInfo.getCurrentPosition();

        BufferedInputStream bis = null;
        RandomAccessFile fos = null;
        byte[] buf = new byte[BUFFER_SIZE];
        URLConnection con = null;
        try {
            con = url.openConnection();
            con.setConnectTimeout(PoplarConfig.TIMEOUT_CONNECT_FILE);
            con.setReadTimeout(PoplarConfig.TIMEOUT_READ_FILE);
            con.setAllowUserInteraction(true);
            // 设置当前线程下载的起点，终点
            con.setRequestProperty("Range", "bytes=" + currentPosition + "-"
                    + endPosition);
            // 使用RandomAccessFile 对文件进行随机读写操作
            fos = new RandomAccessFile(file, "rw");
            // 设置开始写文件的位置
            fos.seek(currentPosition);
            bis = new BufferedInputStream(con.getInputStream());
            currentPosition--;
            // 开始循环以流的形式读写文件
            int index = 0;
            while (currentPosition < endPosition && !stop) {
                int len = bis.read(buf, 0, BUFFER_SIZE);
                if (len == -1) {
                    break;
                }
                fos.write(buf, 0, len);
                currentPosition = currentPosition + len;
                if (currentPosition > endPosition) {
                    currentPosition = endPosition;
                }
                downloadLength = currentPosition - startPosition + 1;

                threadInfo.setCurrentPosition(currentPosition);
                index++;
                if (index >= 1024) {
                    dbHelper.updateThreadInfo(threadInfo);
                    index = 0;
                }
            }

            if (!stop) {
                finished = true;
                HemaLogger.d(TAG, getName() + "执行成功");
            } else {
                HemaLogger.d(TAG, getName() + "执行中断");
            }
        } catch (Exception e) {
            HemaLogger.d(TAG, getName() + "执行失败");
            failed = true;
        } finally {
            try {
                if (bis != null)
                    bis.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                // ignore
            }
        }
        dbHelper.close();
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isFailed() {
        return failed;
    }

    public int getDownloadLength() {
        return downloadLength;
    }
}