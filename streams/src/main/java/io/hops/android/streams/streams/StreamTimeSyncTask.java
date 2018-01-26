package io.hops.android.streams.streams;


import java.util.ArrayList;

import io.hops.android.streams.records.Record;
import io.hops.android.streams.storage.RecordsTable;
import io.hops.android.streams.storage.StorageNotInitialized;
import io.hops.android.streams.time.Timer;
import io.hops.android.streams.time.Timestamp;

public class StreamTimeSyncTask implements Runnable{

    private Class<? extends Record> cls;

    public StreamTimeSyncTask(Class<? extends Record> cls){
        this.cls = cls;
    }

    @Override
    public void run() {
        try {
            Timer timer = Timer.getInstance();
            if (timer.sync("pool.ntp.org", 5000)) {
                Timestamp tRef= timer.getClonedReferenceTimestamp();
                ArrayList<Record> records =
                        RecordsTable.readAllSinceRebootWithoutEpoch(cls, tRef.getBootNum());
                for (Record record : records){
                    Timestamp tRec = record.getTimestamp();
                    if (tRef.getBootNum() == tRec.getBootNum()){
                        long correctedEpoch = tRef.getEpochMillis() -
                                tRec.getBootMillis() + tRec.getBootMillis();
                        record.setEpochMillis(correctedEpoch);
                        record.save();
                    }
                }
            }
        } catch (StorageNotInitialized storageNotInitialized) {
            storageNotInitialized.printStackTrace();
        }
    }
}
