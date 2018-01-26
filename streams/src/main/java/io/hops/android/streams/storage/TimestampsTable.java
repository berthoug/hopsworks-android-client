package io.hops.android.streams.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.hops.android.streams.time.Timestamp;

public class TimestampsTable extends Table{

    private static final String TABLE_NAME = "timestamps";

    private static final Column ID = new Column("_id", "INTEGER PRIMARY KEY");
    private static final Column BOOT_NUM =  new Column("bootNum", "INTEGER");
    private static final Column BOOT_MILLIS = new Column("bootMillis", "INTEGER");
    private static final Column EPOCH_MILLIS = new Column("epochMillis", "INTEGER");

    @Override
    protected String getTableName(){
        return TABLE_NAME;
    }

    @Override
    protected Column[] getColumns(){
        Column[] columns = {ID, BOOT_NUM, BOOT_MILLIS, EPOCH_MILLIS};
        return columns;
    }

    public static boolean insert(Timestamp timestamp) throws StorageNotInitialized{
        SQLiteDatabase db = SQLite.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOOT_NUM.name, timestamp.getBootNum());
        values.put(BOOT_MILLIS.name, timestamp.getBootMillis());
        values.put(EPOCH_MILLIS.name, timestamp.getEpochMillis());
        return (db.insert(TABLE_NAME, null, values) != -1);
    }

    public static boolean update(Timestamp timestamp) throws StorageNotInitialized{
        SQLiteDatabase db = SQLite.getInstance().getWritableDatabase();
        String whereClause = BOOT_NUM.name + "= ?";
        String[] whereArgs = new String[] { String.valueOf(timestamp.getBootNum())};
        ContentValues newValues = new ContentValues();
        newValues.put(BOOT_MILLIS.name, timestamp.getBootMillis());
        newValues.put(EPOCH_MILLIS.name, timestamp.getEpochMillis());
        return (db.update(TABLE_NAME, newValues, whereClause, whereArgs) > 0);
    }

    public static Timestamp read(long bootNum) throws StorageNotInitialized{
        SQLiteDatabase db = SQLite.getInstance().getReadableDatabase();
        Cursor cursor = null;
        try{
            String[] columns = {BOOT_NUM.name, BOOT_MILLIS.name, EPOCH_MILLIS.name};
            String selection = BOOT_NUM.name + "= ?";
            String[] selectionArgs = {String.valueOf(bootNum)};

            cursor = db.query(
                    TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            if (cursor.moveToFirst()){
                return new Timestamp(
                        cursor.getLong(cursor.getColumnIndex(BOOT_NUM.name)),
                        cursor.getLong(cursor.getColumnIndex(BOOT_MILLIS.name)),
                        cursor.getLong(cursor.getColumnIndex(EPOCH_MILLIS.name))
                );
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return null;
    }

    public static boolean write(Timestamp timestamp) throws StorageNotInitialized{
        return update(timestamp) || insert(timestamp);
    }

    public static boolean delete(long bootNum) throws StorageNotInitialized{
        SQLiteDatabase db = SQLite.getInstance().getWritableDatabase();
        String whereClause = BOOT_NUM.name + "= ?";
        String[] whereArgs = new String[] { String.valueOf(bootNum) };
        return (db.delete(TABLE_NAME, whereClause, whereArgs) > 0);
    }

    public static Timestamp loadMaxBootNumTimestamp() throws StorageNotInitialized{
        SQLiteDatabase db = SQLite.getInstance().getReadableDatabase();
        Cursor cursor = null;
        try{
            String[] columns = {"MAX("+BOOT_NUM.name+")"};
            cursor = db.query(
                    TABLE_NAME, columns, null, null, null, null, null, null);
            if (cursor.moveToFirst()){
                long bootNum = cursor.getLong(cursor.getColumnIndex(columns[0]));
                return read(bootNum);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return null;
    }
}
