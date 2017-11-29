package com.hemaapp.hm_FrameWork.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "lunaframework.db";
    /**
     * 文件下载信息表
     */
    protected static final String FILEDOWNLOAD = "filedownload";
    /**
     * 文件下载信息线程表
     */
    protected static final String FILEDOWNLOADTHREAD = "filedownload_thread";

    public DBHelper(Context context) {
        super(context, DBNAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建文件下载信息表
        String sql = "create table "
                + FILEDOWNLOAD
                + " (id integer primary key,downPath text,savePath text,threadCount integer,contentLength integer,currentLength integer)";
        db.execSQL(sql);
        sql = "create table "
                + FILEDOWNLOADTHREAD
                + " (id integer primary key,fileID integer,threadID integer,startPosition integer,endPosition integer,currentPosition integer)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlitedatabase, int i, int j) {

    }

}
