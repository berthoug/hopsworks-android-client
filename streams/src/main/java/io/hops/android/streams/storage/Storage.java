package io.hops.android.streams.storage;

import java.util.ArrayList;
import io.hops.android.streams.records.Record;
import io.hops.android.streams.time.Timer;
import io.hops.android.streams.time.Timestamp;

import android.content.Context;

public class Storage {

    private static final Object lock = new Object();
    private static Storage storage = null;
    private static SQLiteHelper sqLiteHelper = null;
    public static final String dbFileName = "Streams.db";

    private Storage(Context context){
        sqLiteHelper =  new SQLiteHelper(
                context, dbFileName);
    }

    public static Storage init(Context c){
        if (storage == null) {
            synchronized (lock) {
                if (storage == null) {
                    storage = new Storage(c.getApplicationContext());
                }
            }
        }
        return storage;
    }

    public static Storage getInstance() throws StorageNotInitialized {
        if (storage == null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (storage==null){
                throw new StorageNotInitialized(
                    "You need to call Storage.init(context) " +
                    "before any other call to the android-streams library " +
                    "is executed. Like on your Activity's onCreate() method.");
            }
        }
        return storage;
    }

    //==================================== PROPERTIES ====================================

    public boolean saveProperty(String property, String value){
        return PropertiesTable.write(
                sqLiteHelper.getWritableDatabase(), property, value);
    }

    public String loadProperty(String property){
        return PropertiesTable.read(
                sqLiteHelper.getReadableDatabase(), property);
    }

    public boolean deleteProperty(String property){
        return PropertiesTable.delete(
                sqLiteHelper.getWritableDatabase(), property);
    }

    //===================================== TIME SYNC ====================================
    public boolean saveTimestamp(Timestamp timestamp){
        return TimestampsTable.write(
                sqLiteHelper.getWritableDatabase(), timestamp);
    }

    public Timestamp loadTimestamp(long bootNum){
        return TimestampsTable.read(
                sqLiteHelper.getReadableDatabase(), bootNum);
    }

    public Timestamp loadMaxBootNumTimestamp() {
        return TimestampsTable.loadMaxBootNumTimestamp(
                sqLiteHelper.getReadableDatabase());
    }

    public boolean deleteTimestamp(long bootNum){
        return TimestampsTable.delete(
                sqLiteHelper.getWritableDatabase(), bootNum);
    }

    //====================================== RECORD ======================================

    public boolean saveRecord(Record record){
        return RecordsTable.write(sqLiteHelper.getWritableDatabase(), record);
    }

    public Record loadRecord(String uuid, Class<? extends Record> type){
        return RecordsTable.read(sqLiteHelper.getReadableDatabase(), uuid, type);
    }

    public boolean deleteRecord(Record record) {
        return (record != null) && deleteRecord(record.getRecordUUID());
    }

    public boolean deleteRecord(String uuid){
        return RecordsTable.delete(sqLiteHelper.getReadableDatabase(), uuid);
    }

    //=============================== LOAD RECORDS =====================================

    public ArrayList<Record> loadAllRecords(Class<? extends Record> cls) {
        return RecordsTable.readAll(sqLiteHelper.getReadableDatabase(), cls);
    }

    public ArrayList<Record> loadRecordsSinceReboot(
            Class<? extends Record> cls, long bootNum){
        return RecordsTable.readAllSinceReboot(
                sqLiteHelper.getReadableDatabase(), cls, bootNum);
    }

    public ArrayList<Record> loadRecordsSinceBootWithoutEpoch(
            Class<? extends Record> cls, long bootNum) {
        return RecordsTable.readAllSinceRebootWithoutEpoch(
                sqLiteHelper.getReadableDatabase(), cls, bootNum);
    }

    public ArrayList<Record> loadRecordsNotAcked(Class<? extends Record> cls) {
        return RecordsTable.readAllNotAcked(
                sqLiteHelper.getReadableDatabase(), cls);
    }

    //=============================== DELETE RECORDS =====================================

    public boolean deleteAllRecords(Class<? extends Record> cls) {
        return RecordsTable.deleteAllRecords(
                sqLiteHelper.getWritableDatabase(), cls);
    }

    public ArrayList<Record> deleteRecordsSinceReboot(
            Class<? extends Record> cls, long bootNum){
        return RecordsTable.readAllSinceReboot(
                sqLiteHelper.getReadableDatabase(), cls, bootNum);
    }

    public boolean deleteAckedRecords(Class<? extends Record> cls) {
        return RecordsTable.deleteAckedRecords(
                sqLiteHelper.getWritableDatabase(), cls);
    }

    public void deleteAckedRecordsOlderThan(
            Class<? extends Record> cls, Timestamp timestamp) {
        //TODO: Add this functionality.
    }






}
