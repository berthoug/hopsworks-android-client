package io.hops.android.streams.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PropertiesTable extends Table{

    private static final String TABLE_NAME = "properties";

    private static final Column ID = new Column("_id", "INTEGER PRIMARY KEY");
    private static final Column NAME =  new Column("name", "TEXT");
    private static final Column VALUE =  new Column("value", "TEXT");

    @Override
    protected String getTableName(){
        return TABLE_NAME;
    }

    @Override
    protected Column[] getColumns(){
        Column[] columns = {ID, NAME, VALUE};
        return columns;
    }

    public static boolean insert(String property, String value) throws StorageNotInitialized{
        SQLiteDatabase db = SQLite.getInstance().getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME.name, property);
        values.put(VALUE.name, value);
        return (db.insert(TABLE_NAME, null, values) != -1);
    }

    public static boolean update(String property, String value)
            throws StorageNotInitialized{
        SQLiteDatabase db = SQLite.getInstance().getWritableDatabase();
        String whereClause = NAME.name + "= ?";
        String[] whereArgs = new String[] { property };

        ContentValues newValues = new ContentValues();
        newValues.put(VALUE.name, value);

        return (db.update(TABLE_NAME, newValues, whereClause, whereArgs) > 0);
    }

    public static String read(String property) throws StorageNotInitialized{
        SQLiteDatabase db = SQLite.getInstance().getReadableDatabase();
        String value = null;
        Cursor cursor = null;
        try{
            String[] columns = {VALUE.name};
            String selection = NAME.name + "= ?";
            String[] selectionArgs = {property};

            cursor = db.query(
                    TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            if (cursor.moveToFirst()){
                value = cursor.getString(cursor.getColumnIndex(VALUE.name));
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return value;
    }

    public static boolean write(String property, String value)
            throws StorageNotInitialized{
        return update(property, value) || insert(property,value);
    }

    public static boolean delete(String property) throws StorageNotInitialized{
        SQLiteDatabase db = SQLite.getInstance().getWritableDatabase();
        String whereClause = NAME.name + "= ?";
        String[] whereArgs = new String[] { property };
        return (db.delete(TABLE_NAME, whereClause, whereArgs) > 0);
    }

}
