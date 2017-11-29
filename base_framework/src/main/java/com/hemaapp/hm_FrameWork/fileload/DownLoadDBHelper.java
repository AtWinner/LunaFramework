package com.hemaapp.hm_FrameWork.fileload;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.hemaapp.hm_FrameWork.db.DBHelper;

import java.util.ArrayList;

class DownLoadDBHelper extends DBHelper {
	String fileColumns = "id,downPath,savePath,threadCount,contentLength,currentLength";
	String threadColumns = "id,fileID,threadID,startPosition,endPosition,currentPosition";
	private static DownLoadDBHelper dbHelper;

	private DownLoadDBHelper(Context context) {
		super(context);
	}

	static synchronized DownLoadDBHelper getInstance(Context context) {
		return dbHelper == null ? dbHelper = new DownLoadDBHelper(context)
				: dbHelper;
	}

	public boolean insertOrUpdateThreadInfo(ThreadInfo info) {
		synchronized (this) {
			if (!isExist(info))
				return insertThreadInfo(info);
			else
				return updateThreadInfo(info);
		}
	}

	public boolean isExist(ThreadInfo info) {
		synchronized (this) {
			String conditions = "fileID=" + info.getFileID() + " and threadID="
					+ info.getThreadID();
			String sql = "select * from " + FILEDOWNLOADTHREAD + " where "
					+ conditions;
			Cursor cursor = getReadableDatabase().rawQuery(sql, null);
			return cursor != null && cursor.getCount() > 0;
		}
	}

	public boolean insertThreadInfo(ThreadInfo info) {
		synchronized (this) {
			boolean success = true;
			String threadColumns = "fileID,threadID,startPosition,endPosition,currentPosition";
			String sql = "insert into " + FILEDOWNLOADTHREAD + " ("
					+ threadColumns + ") values (?,?,?,?,?)";
			Object[] bindArgs = new Object[] { info.getFileID(),
					info.getThreadID(), info.getStartPosition(),
					info.getEndPosition(), info.getCurrentPosition() };
			try {
				getWritableDatabase().execSQL(sql, bindArgs);
			} catch (SQLException e) {
				success = false;
			}
			return success;
		}
	}

	public boolean updateThreadInfo(ThreadInfo info) {
		synchronized (this) {
			boolean success = true;
			String updateColums = "id=?,fileID=?,threadID=?,startPosition=?,endPosition=?,currentPosition=?";
			String conditions = "fileID=" + info.getFileID() + " and threadID="
					+ info.getThreadID();
			String sql = "update " + FILEDOWNLOADTHREAD + " set "
					+ updateColums + " where " + conditions;

			Object[] bindArgs = new Object[] { info.getId(), info.getFileID(),
					info.getThreadID(), info.getStartPosition(),
					info.getEndPosition(), info.getCurrentPosition() };
			try {
				getWritableDatabase().execSQL(sql, bindArgs);
			} catch (SQLException e) {
				success = false;
			}
			return success;
		}
	}

	public boolean insertOrUpdateFileInfo(FileInfo info) {
		synchronized (this) {
			if (!isExist(info))
				return insertFileInfo(info);
			else
				return updateFileInfo(info);
		}
	}

	public boolean insertFileInfo(FileInfo info) {
		synchronized (this) {
			boolean success = true;
			String fileColumns = "downPath,savePath,threadCount,contentLength,currentLength";
			String sql = "insert into " + FILEDOWNLOAD + " (" + fileColumns
					+ ") values (?,?,?,?,?)";
			Object[] bindArgs = new Object[] { info.getDownPath(),
					info.getSavePath(), info.getThreadCount(),
					info.getContentLength(), info.getCurrentLength() };
			try {
				getWritableDatabase().execSQL(sql, bindArgs);
			} catch (SQLException e) {
				success = false;
			}
			return success;
		}
	}

	public boolean updateFileInfo(FileInfo info) {
		synchronized (this) {
			boolean success = true;
			String updateColums = "id=?,downPath=?,savePath=?,threadCount=?,contentLength=?,currentLength=?";
			String conditions = "downPath='" + info.getDownPath()
					+ "' and savePath='" + info.getSavePath()
					+ "' and threadCount=" + info.getThreadCount();
			String sql = "update " + FILEDOWNLOAD + " set " + updateColums
					+ " where " + conditions;

			Object[] bindArgs = new Object[] { info.getId(),
					info.getDownPath(), info.getSavePath(),
					info.getThreadCount(), info.getContentLength(),
					info.getCurrentLength() };
			try {
				getWritableDatabase().execSQL(sql, bindArgs);
			} catch (SQLException e) {
				success = false;
			}
			return success;
		}
	}

	public boolean isExist(FileInfo info) {
		synchronized (this) {
			String conditions = "downPath='" + info.getDownPath()
					+ "' and savePath='" + info.getSavePath()
					+ "' and threadCount=" + info.getThreadCount();
			String sql = "select * from " + FILEDOWNLOAD + " where "
					+ conditions;
			Cursor cursor = getReadableDatabase().rawQuery(sql, null);
			return cursor != null && cursor.getCount() > 0;
		}
	}

	public FileInfo getFileInfo(String downPath, String savePath,
								int threadCount) {
		synchronized (this) {
			String conditions = "downPath='" + downPath + "' and savePath='"
					+ savePath + "' and threadCount=" + threadCount;
			String sql = "select " + fileColumns + " from " + FILEDOWNLOAD
					+ " where " + conditions;
			FileInfo info = null;
			Cursor cursor = getReadableDatabase().rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new FileInfo(cursor.getInt(0), cursor.getString(1),
						cursor.getString(2), cursor.getInt(3),
						cursor.getInt(4), cursor.getInt(5));
				cursor.close();
			}
			return info;
		}
	}

	public ThreadInfo getThreadInfo(int fileID, int threadID) {
		synchronized (this) {
			String conditions = "fileID=" + fileID + " and threadID="
					+ threadID;
			String sql = "select " + threadColumns + " from "
					+ FILEDOWNLOADTHREAD + " where " + conditions;
			ThreadInfo info = null;
			Cursor cursor = getReadableDatabase().rawQuery(sql, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				info = new ThreadInfo(cursor.getInt(0), cursor.getInt(1),
						cursor.getInt(2), cursor.getInt(3), cursor.getInt(4),
						cursor.getInt(5));
				cursor.close();
			}
			return info;
		}
	}

	public ArrayList<ThreadInfo> getThreadInfos(int fileID) {
		synchronized (this) {
			String conditions = "fileID=" + fileID;
			String sql = "select " + threadColumns + " from "
					+ FILEDOWNLOADTHREAD + " where " + conditions;

			Cursor cursor = getReadableDatabase().rawQuery(sql, null);
			ArrayList<ThreadInfo> infos = null;
			if (cursor != null && cursor.getCount() > 0) {
				infos = new ArrayList<ThreadInfo>();
				cursor.moveToFirst();
				ThreadInfo info;
				for (int i = 0; i < cursor.getCount(); i++) {
					info = new ThreadInfo(cursor.getInt(0), cursor.getInt(1),
							cursor.getInt(2), cursor.getInt(3),
							cursor.getInt(4), cursor.getInt(5));
					infos.add(info);
					cursor.moveToNext();
				}
				cursor.close();
			}
			return infos;
		}
	}

	public boolean deleteThreadInfos(int fileID) {
		synchronized (this) {
			boolean success = true;
			String conditions = "fileID=" + fileID;
			String sql = "delete from " + FILEDOWNLOADTHREAD + " where "
					+ conditions;
			try {
				getWritableDatabase().execSQL(sql);
			} catch (SQLException e) {
				success = false;
			}
			return success;
		}
	}
}
