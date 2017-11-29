package com.hemaapp.hm_FrameWork.fileload;

import com.hemaapp.hm_FrameWork.PoplarObject;

public class FileInfo extends PoplarObject {

    private int id;// 文件在数据库中的id
    private String downPath;// 文件网络地址
    private String savePath;// 文件保存地址
    private int threadCount;// 下载线程数
    private int contentLength;// 文件长度
    private int currentLength;// 已下载的文件长度

    FileInfo(int id, String downPath, String savePath, int threadCount,
             int contentLength, int currentLength) {
        super();
        this.id = id;
        this.downPath = downPath;
        this.savePath = savePath;
        this.threadCount = threadCount;
        this.contentLength = contentLength;
        this.currentLength = currentLength;
    }

    public int getId() {
        return id;
    }

    public String getDownPath() {
        return downPath;
    }

    public String getSavePath() {
        return savePath;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getContentLength() {
        return contentLength;
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setCurrentLength(int currentLength) {
        this.currentLength = currentLength;
    }

}
