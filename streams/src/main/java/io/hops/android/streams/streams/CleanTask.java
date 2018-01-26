package io.hops.android.streams.streams;

import io.hops.android.streams.records.Record;
import io.hops.android.streams.storage.RecordsTable;
import io.hops.android.streams.storage.SQLiteNotInitialized;


public class CleanTask implements Runnable{


    private Class<? extends Record> cls;

    public CleanTask(Class<? extends Record> cls){
        this.cls = cls;
    }

    @Override
    public void run() {
        try {
            RecordsTable.deleteAckedRecords(cls);
        } catch (SQLiteNotInitialized SQLiteNotInitialized) {
            SQLiteNotInitialized.printStackTrace();
        }

    }
}