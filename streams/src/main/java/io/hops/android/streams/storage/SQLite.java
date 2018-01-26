package io.hops.android.streams.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLite extends SQLiteOpenHelper{

    private static final Object lock = new Object();
    private static SQLite sqLite = null;
    private static final String SQL_LITE_FILENAME = "Streams.db";
    private static final int DATABASE_VERSION = 5;

    private SQLite(Context context){
        super(context, SQL_LITE_FILENAME, null, DATABASE_VERSION);
    }

    public static SQLite init(Context c){
        if (sqLite == null) {
            synchronized (lock) {
                if (sqLite == null) {
                    sqLite = new SQLite(c.getApplicationContext());
                }
            }
        }
        return sqLite;
    }

    public static SQLite getInstance() throws SQLiteNotInitialized {
        if (sqLite == null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (sqLite==null){
                throw new SQLiteNotInitialized(
                        "You need to call SQLite.init(context) " +
                                "before any other call to the android-streams library " +
                                "is executed. Like on your Activity's onCreate() method.");
            }
        }
        return sqLite;
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
