package io.hops.android.streams.time;

import android.os.SystemClock;

import io.hops.android.streams.storage.Storage;
import io.hops.android.streams.storage.StorageNotInitialized;

public class Timer{

    private static Timestamp referenceTimestamp;
    private static final Object lock = new Object();
    private static Timer timer = null;

    private Timer(){}

    public static Timer getInstance() throws StorageNotInitialized {
        if (timer == null) {
            synchronized (lock) {
                if (timer == null) {
                    timer = new Timer();
                    referenceTimestamp = Storage.getInstance().loadMaxBootNumTimestamp();
                    if(referenceTimestamp == null){
                        referenceTimestamp = new Timestamp(0, SystemClock.elapsedRealtime(), -1L);
                        Storage.getInstance().saveTimestamp(referenceTimestamp);
                    }
                }
            }
        }
        return timer;
    }

     void rebootHappened() throws StorageNotInitialized {
        long bootMillis = SystemClock.elapsedRealtime();
        synchronized (lock){
            referenceTimestamp = new Timestamp(referenceTimestamp.getBootNum()+1, bootMillis, -1L);
            Storage.getInstance().saveTimestamp(referenceTimestamp);
        }

    }

    public boolean sync(String host, int timeout) throws StorageNotInitialized {
        SntpClient client = new SntpClient();
        if (client.requestTime(host, timeout)) {
            long bootMillisNow = SystemClock.elapsedRealtime();
            long epochMillisNow = (
                    client.getNtpTime() - client.getNtpTimeReference() + bootMillisNow);

            synchronized(lock){
                referenceTimestamp = new Timestamp(
                        referenceTimestamp.getBootNum(), bootMillisNow, epochMillisNow);
                Storage.getInstance().saveTimestamp(referenceTimestamp);
            }
            return true;
        }
        return false;
    }

    public Timestamp getClonedReferenceTimestamp(){
        try {
            return (Timestamp) referenceTimestamp.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Timestamp getTimestamp(){
        long bootMillis = SystemClock.elapsedRealtime();
        Timestamp refTime = getClonedReferenceTimestamp();
        long epochMillis = -1L;
        if (refTime.isComplete()){
            epochMillis = refTime.getEpochMillis() - refTime.getBootMillis() + bootMillis;
        }
        return new Timestamp(refTime.getBootNum(), bootMillis, epochMillis);
    }

    public static Timestamp updateEpochMillis(
            Timestamp refTimestamp, Timestamp timestamp){
        if (timestamp != null && refTimestamp != null){
            if (refTimestamp.isComplete()){
                return new Timestamp(
                        timestamp.getBootNum(),
                        timestamp.getBootMillis(),
                        refTimestamp.getEpochMillis() - refTimestamp.getBootMillis() +
                                timestamp.getBootMillis());
            }
        }
        return null;
    }

}
