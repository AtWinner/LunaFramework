package com.hemaapp.hm_FrameWork.orm;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.hemaapp.hm_FrameWork.orm.extra.AutoIncrementTableColumn;
import com.hemaapp.hm_FrameWork.orm.extra.Extra;
import com.hemaapp.hm_FrameWork.orm.extra.TableColumn;
import com.hemaapp.hm_FrameWork.orm.extra.TableInfo;
import com.hemaapp.hm_FrameWork.orm.utils.SqlUtils;
import com.hemaapp.hm_FrameWork.orm.utils.TableInfoUtils;
import com.hemaapp.hm_FrameWork.util.HemaLogger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by huhu on 2016/8/24.
 */
public class SqliteUtility {
    public static final String TAG = "SqliteUtility";

    private static Hashtable<String, SqliteUtility> dbCache = new Hashtable<String, SqliteUtility>();

    private String dbName;
    private SQLiteDatabase db;


    SqliteUtility(String dbName, SQLiteDatabase db) {
        this.db = db;
        this.dbName = dbName;
        dbCache.put(dbName, this);

        HemaLogger.d(TAG, "将库" + dbName + "放入到缓存中。");
    }

    public static SqliteUtility getInstance() {
        return getInstance(SqliteUtilityBuilder.DEFAULT_DB);
    }

    public static SqliteUtility getInstance(String dbName) {
        return dbCache.get(dbName);
    }

    /*******************************************
     * 开始Select系列方法
     ****************************************************/

    public <T> T selectById(Extra extra, Class<T> clazz, Object id) {
        try {
            TableInfo tableInfo = checkTable(clazz);

            String selection = String.format(" %s = ? ", tableInfo.getPrimaryKey().getColumn());
            String extraSelection = SqlUtils.appendExtraWhereClause(extra);
            if (!TextUtils.isEmpty(extraSelection))
                selection = String.format("%s and %s", selection, extraSelection);

            List<String> selectionArgList = new ArrayList<String>();
            selectionArgList.add(String.valueOf(id));
            String[] extraSelectionArgs = SqlUtils.appendExtraWhereArgs(extra);
            if (extraSelectionArgs != null && extraSelectionArgs.length > 0)
                selectionArgList.addAll(Arrays.asList(extraSelectionArgs));
            String[] selectionArgs = selectionArgList.toArray(new String[0]);

            List<T> list = select(clazz, selection, selectionArgs, null, null, null, null);
            if (list.size() > 0) {
                return list.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public <T> T selectByField(Extra extra, Class<T> clazz) {
        try {
            List<T> list = select(clazz, null, null, null, null, null, null);
            if (list.size() > 0) {
                return list.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public <T> List<T> select(Extra extra, Class<T> clazz) {
        String selection = SqlUtils.appendExtraWhereClause(extra);
        String[] selectionArgs = SqlUtils.appendExtraWhereArgs(extra);

        return select(clazz, selection, selectionArgs, null, null, null, null);
    }

    public <T> List<T> select(Class<T> clazz, String selection, String[] selectionArgs) {
        return select(clazz, selection, selectionArgs, null, null, null, null);
    }

    public <T> List<T> selectOrderBy(Extra extra, Class<T> clazz, String orderby) {
        String selection = SqlUtils.appendExtraWhereClause(extra);
        String[] selectionArgs = SqlUtils.appendExtraWhereArgs(extra);
        return select(clazz, selection, selectionArgs, null, null, orderby, null);
    }

    public <T> List<T> select(Class<T> clazz, String selection,
                              String[] selectionArgs, String groupBy, String having,
                              String orderBy, String limit) {
        TableInfo tableInfo = checkTable(clazz);

        ArrayList<T> list = new ArrayList<T>();
        HemaLogger.d(TAG, " method[select], table[" + tableInfo.getTableName() + "], selection[" + selection + "], selectionArgs" + JSON.toJSON(selectionArgs) + ", groupBy[" + String.valueOf(groupBy) + "], having[" + String.valueOf(having) + "], orderBy[" + String.valueOf(orderBy) + "], limit[" + String.valueOf(limit) + "]");
        List<String> columnList = new ArrayList<String>();
        columnList.add(tableInfo.getPrimaryKey().getColumn());
        for (TableColumn tableColumn : tableInfo.getColumns())
            columnList.add(tableColumn.getColumn());

        long start = System.currentTimeMillis();
        Cursor cursor = db.query(tableInfo.getTableName(), columnList.toArray(new String[0]),
                selection, selectionArgs, groupBy, having, orderBy, limit);
        HemaLogger.d(TAG, "table[" + tableInfo.getTableName() + "] 查询数据结束，耗时 " + String.valueOf(System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();
        try {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        T entity = clazz.newInstance();

                        // 绑定主键
                        bindSelectValue(entity, cursor, tableInfo.getPrimaryKey());

                        // 绑定其他数据
                        for (TableColumn column : tableInfo.getColumns())
                            bindSelectValue(entity, cursor, column);

                        list.add(entity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        HemaLogger.d(TAG, "table[" + tableInfo.getTableName() + "], 设置数据结束，耗时 " + String.valueOf(System.currentTimeMillis() - start) + " ms");

        HemaLogger.d(TAG, "查询到数据 " + list.size() + " 条");

        return list;
    }

    /*******************************************开始Insert系列方法****************************************************/

    /**
     * 如果主键实体已经存在，则忽略插库
     *
     * @param extra
     * @param entities
     */
    public <T> void insert(Extra extra, T... entities) {
        try {
            if (entities != null && entities.length > 0)
                insert(extra, Arrays.asList(entities));
            else
                HemaLogger.d(TAG, "method[insert(Extra extra, T... entities)], entities is null or empty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果主键实体已经存在，使用新的对象存库
     *
     * @param extra
     * @param entities
     */
    public <T> void insertOrReplace(Extra extra, T... entities) {
        try {
            if (entities != null && entities.length > 0)
                insert(extra, Arrays.asList(entities), "INSERT OR REPLACE INTO ");
            else
                HemaLogger.d(TAG, "method[insertOrReplace(Extra extra, T... entities)], entities is null or empty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> void insert(Extra extra, List<T> entityList) {
        try {
            insert(extra, entityList, "INSERT OR IGNORE INTO ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> void insertOrReplace(Extra extra, List<T> entityList) {
        try {
            insert(extra, entityList, "INSERT OR REPLACE INTO ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> void insert(Extra extra, List<T> entityList, String insertInto) {
        if (entityList == null || entityList.size() == 0) {
            HemaLogger.d(TAG, "method[insert(Extra extra, List<T> entityList)], entityList is null or empty");
            return;
        }

        TableInfo tableInfo = checkTable(entityList.get(0).getClass());

        long start = System.currentTimeMillis();
        db.beginTransaction();
        try {
            String sql = SqlUtils.createSqlInsert(insertInto, tableInfo);

            HemaLogger.v(TAG, insertInto + " sql = " + sql);

            SQLiteStatement insertStatement = db.compileStatement(sql);
            long bindTime = 0;
            long startTime = System.currentTimeMillis();
            for (T entity : entityList) {
                bindInsertValues(extra, insertStatement, tableInfo, entity);
                bindTime += (System.currentTimeMillis() - startTime);
                startTime = System.currentTimeMillis();
                insertStatement.execute();
            }
            HemaLogger.d(TAG, "bindvalues 耗时 " + bindTime + " ms");

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        HemaLogger.d(TAG, "表 " + tableInfo.getTableName() + insertInto + " 数据 " + entityList.size() + " 条， 执行时间 " + String.valueOf(System.currentTimeMillis() - start) + " ms");
    }

    /*******************************************
     * 开始Update系列方法
     ****************************************************/

    public <T> void update(Extra extra, T... entities) {
        try {
            if (entities != null && entities.length > 0)
                insertOrReplace(extra, entities);
            else
                HemaLogger.d(TAG, "method[update(Extra extra, T... entities)], entities is null or empty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> void update(Extra extra, List<T> entityList) {
        try {
            if (entityList != null && entityList.size() > 0)
                insertOrReplace(extra, entityList);
            else
                HemaLogger.d(TAG, "method[update(Extra extra, T... entities)], entities is null or empty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> int update(Class<?> clazz, ContentValues values, String whereClause, String[] whereArgs) {
        try {
            TableInfo tableInfo = checkTable(clazz);

            return db.update(tableInfo.getTableName(), values, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /*******************************************
     * 开始Delete系列方法
     ****************************************************/

    public <T> void deleteAll(Extra extra, Class<T> clazz) {
        try {
            TableInfo tableInfo = checkTable(clazz);

            String where = SqlUtils.appendExtraWhereClauseSql(extra);
            if (!TextUtils.isEmpty(where))
                where = " where " + where;
            String sql = "DELETE FROM '" + tableInfo.getTableName() + "' " + where;

            HemaLogger.d(TAG, "method[delete] table[" + tableInfo.getTableName() + "], sql[" + sql + "]");

            long start = System.currentTimeMillis();
            db.execSQL(sql);
            HemaLogger.d(TAG, "表 " + tableInfo.getTableName() + " 清空数据, 耗时 " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> void deleteById(Extra extra, Class<T> clazz, Object id) {
        try {
            TableInfo tableInfo = checkTable(clazz);

            String whereClause = String.format(" %s = ? ", tableInfo.getPrimaryKey().getColumn());
            String extraWhereClause = SqlUtils.appendExtraWhereClause(extra);
            if (!TextUtils.isEmpty(extraWhereClause))
                whereClause = String.format("%s and %s", whereClause, extraWhereClause);

            List<String> whereArgList = new ArrayList<String>();
            whereArgList.add(String.valueOf(id));
            String[] extraWhereArgs = SqlUtils.appendExtraWhereArgs(extra);
            if (extraWhereArgs != null && extraWhereArgs.length > 0)
                whereArgList.addAll(Arrays.asList(extraWhereArgs));
            String[] whereArgs = whereArgList.toArray(new String[0]);

            HemaLogger.d(TAG, " method[deleteById], table[" + tableInfo.getTableName() + "], id[" + String.valueOf(id) + "], whereClause[" + whereClause + "], whereArgs" + JSON.toJSON(whereArgs) + " ");

            long start = System.currentTimeMillis();
            int rowCount = db.delete(tableInfo.getTableName(), whereClause, whereArgs);

            HemaLogger.d(TAG, "表 " + tableInfo.getTableName() + " 删除数据 " + rowCount + " 条, 耗时 " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> void delete(Class<T> clazz, String whereClause, String[] whereArgs) {
        try {
            TableInfo tableInfo = checkTable(clazz);

            long start = System.currentTimeMillis();
            int rowCount = db.delete(tableInfo.getTableName(), whereClause, whereArgs);

            HemaLogger.d(TAG, "method[delete], table[" + tableInfo.getTableName() + "], whereClause[" + whereClause + "], whereArgs" + JSON.toJSON(whereArgs) + " ");
            HemaLogger.d(TAG, "表 " + tableInfo.getTableName() + " 删除数据 " + rowCount + " 条，耗时 " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*******************************************
     * 系列统计的方法
     ****************************************************/

    public long sum(Class<?> clazz, String column, String whereClause, String[] whereArgs) {
        TableInfo tableInfo = checkTable(clazz);

        if (TextUtils.isEmpty(column))
            return 0;

        String sql = null;
        if (TextUtils.isEmpty(whereClause)) {
            whereArgs = null;
            sql = String.format(" select sum(%s) as _sum_ from %s ", column, tableInfo.getTableName());
        } else {
            sql = String.format(" select sum(%s) as _sum_ from %s where %s ", column, tableInfo.getTableName(), whereClause);
        }

        HemaLogger.d(TAG, "sum() --- > " + sql);
        HemaLogger.d(TAG, "whereArgs:" + whereArgs);

        try {
            long time = System.currentTimeMillis();
            Cursor cursor = db.rawQuery(sql, whereArgs);
            if (cursor.moveToFirst()) {
                long sum = cursor.getLong(cursor.getColumnIndex("_sum_"));
                HemaLogger.d(TAG, "sum = " + String.valueOf(sum) + " 耗时" + String.valueOf(System.currentTimeMillis() - time) + "ms");
                cursor.close();
                return sum;
            }
        } catch (Exception e) {
            HemaLogger.v(TAG, e.getMessage());
        }
        return 0;
    }

    public long count(Class<?> clazz, String whereClause, String[] whereArgs) {
        TableInfo tableInfo = checkTable(clazz);

        String sql = null;
        if (TextUtils.isEmpty(whereClause)) {
            whereArgs = null;
            sql = String.format(" select count(*) as _count_ from %s ", tableInfo.getTableName());
        } else {
            sql = String.format(" select count(*) as _count_ from %s where %s ", tableInfo.getTableName(), whereClause);
        }

        HemaLogger.d(TAG, "count --- > " + sql);
        HemaLogger.d(TAG, "whereArgs:" + whereArgs);

        try {
            long time = System.currentTimeMillis();
            Cursor cursor = db.rawQuery(sql, whereArgs);
            if (cursor.moveToFirst()) {
                long count = cursor.getLong(cursor.getColumnIndex("_count_"));
                HemaLogger.d(TAG, "count = " + String.valueOf(count) + " 耗时" + String.valueOf(System.currentTimeMillis() - time) + "ms");
                cursor.close();
                return count;
            }
        } catch (Exception e) {
            HemaLogger.v(TAG, e.getMessage());
        }
        return 0;
    }

    /*******************************************
     * 系列绑定数据的方法
     ****************************************************/

    private <T> void bindInsertValues(Extra extra, SQLiteStatement insertStatement, TableInfo tableInfo, T entity) {
        int index = 1;

        // 如果是自增主键，不设置值
        if (tableInfo.getPrimaryKey() instanceof AutoIncrementTableColumn)
            ;
        else
            bindInsertValue(insertStatement, index++, tableInfo.getPrimaryKey(), entity);

        for (int i = 0; i < tableInfo.getColumns().size(); i++) {
            TableColumn column = tableInfo.getColumns().get(i);
            bindInsertValue(insertStatement, index++, column, entity);
        }

        // owner
        String owner = extra == null || TextUtils.isEmpty(extra.getOwner()) ? "" : extra.getOwner();
        insertStatement.bindString(index++, owner);
        // key
        String key = extra == null || TextUtils.isEmpty(extra.getKey()) ? "" : extra.getKey();
        insertStatement.bindString(index++, key);
        // createAt
        long createAt = System.currentTimeMillis();
        insertStatement.bindLong(index, createAt);
    }

    private <T> void bindInsertValue(SQLiteStatement insertStatement, int index, TableColumn column, T entity) {
        // 通过反射绑定数据
        try {
            column.getField().setAccessible(true);
            Object value = column.getField().get(entity);
            if (value == null) {
                insertStatement.bindNull(index);
                return;
            }

            if ("object".equalsIgnoreCase(column.getDataType())) {
                insertStatement.bindString(index, JSON.toJSONString(value));
            } else if ("INTEGER".equalsIgnoreCase(column.getColumnType())) {
                insertStatement.bindLong(index, Long.parseLong(value.toString()));
            } else if ("REAL".equalsIgnoreCase(column.getColumnType())) {
                insertStatement.bindDouble(index, Double.parseDouble(value.toString()));
            } else if ("BLOB".equalsIgnoreCase(column.getColumnType())) {
                insertStatement.bindBlob(index, (byte[]) value);
            } else if ("TEXT".equalsIgnoreCase(column.getColumnType())) {
                insertStatement.bindString(index, value.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();

            HemaLogger.w(TAG, "属性 " + column.getField().getName() + " bindvalue 异常");
        }
    }

    private <T> void bindSelectValue(T entity, Cursor cursor, TableColumn column) {
        Field field = column.getField();
        field.setAccessible(true);

        try {
            if (field.getType().getName().equals("int") ||
                    field.getType().getName().equals("java.lang.Integer")) {
                field.set(entity, cursor.getInt(cursor.getColumnIndex(column.getColumn())));
            } else if (field.getType().getName().equals("long") ||
                    field.getType().getName().equals("java.lang.Long")) {
                field.set(entity, cursor.getLong(cursor.getColumnIndex(column.getColumn())));
            } else if (field.getType().getName().equals("float") ||
                    field.getType().getName().equals("java.lang.Float")) {
                field.set(entity, cursor.getFloat(cursor.getColumnIndex(column.getColumn())));
            } else if (field.getType().getName().equals("double") ||
                    field.getType().getName().equals("java.lang.Double")) {
                field.set(entity, cursor.getDouble(cursor.getColumnIndex(column.getColumn())));
            } else if (field.getType().getName().equals("boolean") ||
                    field.getType().getName().equals("java.lang.Boolean")) {
                field.set(entity, Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(column.getColumn()))));
            } else if (field.getType().getName().equals("char") ||
                    field.getType().getName().equals("java.lang.Character")) {
                field.set(entity, cursor.getString(cursor.getColumnIndex(column.getColumn())).toCharArray()[0]);
            } else if (field.getType().getName().equals("byte") ||
                    field.getType().getName().equals("java.lang.Byte")) {
                field.set(entity, (byte) cursor.getInt(cursor.getColumnIndex(column.getColumn())));
            } else if (field.getType().getName().equals("short") ||
                    field.getType().getName().equals("java.lang.Short")) {
                field.set(entity, cursor.getShort(cursor.getColumnIndex(column.getColumn())));
            } else if (field.getType().getName().equals("java.lang.String")) {
                field.set(entity, cursor.getString(cursor.getColumnIndex(column.getColumn())));
            } else if (field.getType().getName().equals("[B")) {
                field.set(entity, cursor.getBlob(cursor.getColumnIndex(column.getColumn())));
            } else {
                String text = cursor.getString(cursor.getColumnIndex(column.getColumn()));
                field.set(entity, JSON.parseObject(text, field.getGenericType()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查table是否已经存在<br/>
     * 不存在，就自动创建<br/>
     * 存在，检查Entity字段是否有增加，有则更新表<br/>
     *
     * @param clazz
     */
    private <T> TableInfo checkTable(Class<T> clazz) {
        TableInfo tableInfo = TableInfoUtils.exist(dbName, clazz);
        if (tableInfo != null) {
        } else {
            tableInfo = TableInfoUtils.newTable(dbName, db, clazz);
        }

        return tableInfo;
    }


}
