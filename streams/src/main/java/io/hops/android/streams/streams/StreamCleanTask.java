package io.hops.android.streams.streams;

import io.hops.android.streams.records.Record;
import io.hops.android.streams.storage.Storage;
import io.hops.android.streams.storage.StorageNotInitialized;


public class StreamCleanTask implements Runnable{


    private Class<? extends Record> cls;

    public StreamCleanTask(Class<? extends Record> cls){
        this.cls = cls;
    }

    @Override
    public void run() {
        try {
            Storage.getInstance().deleteAckedRecords(cls);
        } catch (StorageNotInitialized storageNotInitialized) {
            storageNotInitialized.printStackTrace();
        }

    }
}