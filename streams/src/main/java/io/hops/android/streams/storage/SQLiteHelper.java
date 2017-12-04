package io.hops.android.streams.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 5;

    public SQLiteHelper(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    public SQLiteHelper(Context context, String name, int db_version) {
        super(context, name, null, db_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(new PropertiesTable().create());
        db.execSQL(new TimestampsTable().create());
        db.execSQL(new RecordsTable().create());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(new PropertiesTable().drop());
        db.execSQL(new TimestampsTable().drop());
        db.execSQL(new RecordsTable().drop());
        db.execSQL(new PropertiesTable().create());
        db.execSQL(new TimestampsTable().create());
        db.execSQL(new RecordsTable().create());
    }

}
