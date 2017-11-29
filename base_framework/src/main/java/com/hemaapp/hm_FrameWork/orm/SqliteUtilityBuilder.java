package com.hemaapp.hm_FrameWork.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hemaapp.hm_FrameWork.util.HemaLogger;

import java.io.File;
import java.io.IOException;

public class SqliteUtilityBuilder {

    public static final String TAG = SqliteUtility.TAG;

    static final String DEFAULT_DB = "com_m_default_db";// 默认DB名称

    private String path;// DB的SD卡路径

    private String dbName = DEFAULT_DB;

    private int version = 1;// DB的Version，每次升级DB都默认先清库

    private boolean sdcardDb = false;

    public SqliteUtilityBuilder configDBName(String dbName) {
        this.dbName = dbName;

        return this;
    }

    public SqliteUtilityBuilder configVersion(int version) {
        this.version = version;

        return this;
    }

    public SqliteUtilityBuilder configSdcardPath(String path) {
        this.path = path;
        sdcardDb = true;

        return this;
    }

    public SqliteUtility build(Context context) {
        SQLiteDatabase db = null;

        if (sdcardDb) {
            db = openSdcardDb(path, dbName, version);
            HemaLogger.d(TAG, "打开app数据库" + dbName + ", version = " + db.getVersion());
        } else {
            db = new SqliteDbHelper(context, dbName, version).getWritableDatabase();


            HemaLogger.d(TAG, "打开sdcard库" + dbName + ",version=" + db.getVersion());
        }

        return new SqliteUtility(dbName, db);
    }

    static SQLiteDatabase openSdcardDb(String path, String dbName, int version) {
        SQLiteDatabase db = null;
        File dbf = new File(path + File.separator + dbName + ".db");

        if (dbf.exists()) {
            HemaLogger.d(TAG, "打开库" + dbName);
            db = SQLiteDatabase.openOrCreateDatabase(dbf, null);
        } else {
            dbf.getParentFile().mkdirs();

            try {
                if (dbf.createNewFile()) {
                    HemaLogger.d(TAG, "新建一个库再sd卡，库名 = " + dbName + ",路径 = " + dbf.getAbsolutePath());
                    db = SQLiteDatabase.openOrCreateDatabase(dbf, null);
                }
            } catch (IOException ioex) {
                ioex.printStackTrace();

                throw new RuntimeException("新建库失败, 库名 = " + dbName + ", 路径 = " + path, ioex);
            }
        }

        if (db != null) {
            int dbVersion = db.getVersion();
            HemaLogger.d(TAG, "表" + dbName + "的version" + dbVersion + ", newVersion =" + version);
            if (dbVersion < version) {
                dropDb(db);

                // 更新DB的版本信息
                db.beginTransaction();
                try {
                    db.setVersion(version);
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                } finally {
                    db.endTransaction();
                }
            }

            return db;
        }

        throw new RuntimeException("打开库失败, 库名 = " + dbName + ", 路径 = " + path);
    }

    static class SqliteDbHelper extends SQLiteOpenHelper {

        SqliteDbHelper(Context context, String dbName, int dbVersion) {
            super(context, dbName, null, dbVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            dropDb(db);
            onCreate(db);
        }

    }

    static void dropDb(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type ='table' AND name != 'sqlite_sequence'", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                db.execSQL("DROP TABLE " + cursor.getString(0));
                HemaLogger.d(TAG, "删除表 = " + cursor.getString(0));
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

}
