package io.hops.android.streams.records;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.UUID;

import io.hops.android.streams.storage.DeviceCredentials;
import io.hops.android.streams.storage.RecordsTable;
import io.hops.android.streams.storage.StorageNotInitialized;
import io.hops.android.streams.time.Timer;
import io.hops.android.streams.time.Timestamp;

/**
 *  Limitation issue: It is assumed that a specific subclass of Record can be used for 1 stream
 *  only. If you want to reuse the specific sublcass in another stream you can't. You either have
 *  to extend the subclass itself or create a new subclass for the new stream.
 */
public abstract class Record implements Comparable<Record>{

    private transient long acked;

    private long bootNum;

    private long bootMillis;

    private long epochMillis;

    private String recordUUID;

    private String deviceUUID;


    public Record() throws StorageNotInitialized {
        Timestamp timestamp = Timer.getInstance().getTimestamp();
        this.deviceUUID = DeviceCredentials.getDeviceUUID();
        this.bootNum = timestamp.getBootNum();
        this.bootMillis = timestamp.getBootMillis();
        this.epochMillis = timestamp.getEpochMillis();
        this.recordUUID = UUID.randomUUID().toString();
        this.acked = 0;
    }

    public String getClassType(){
        return this.getClass().getName();
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public String getRecordUUID() {
        return recordUUID;
    }

    public boolean getAcked() {
        if (acked > 0){
            return true;
        }
        return false;
    }

    public void setAcked(boolean acked) {
        if (acked){
            this.acked = 1;
        }else{
            this.acked = 0;
        }
    }

    public long getBootNum() {
        return bootNum;
    }

    public long getBootMillis(){
        return bootMillis;
    }

    public long getEpochMillis() {
        return epochMillis;
    }

    public void setEpochMillis(long epochMillis) {
        this.epochMillis = epochMillis;
    }

    public final String toJson(){
        return new Gson().toJson(this);
    }

    public static Object fromJson(String json, Class type){
        return new Gson().fromJson(json, type);
    }

    public boolean save() throws StorageNotInitialized{
        return RecordsTable.write(this);
    }

    public boolean delete() throws StorageNotInitialized{
        return RecordsTable.delete(this.recordUUID);
    }

    public Timestamp getTimestamp(){
        return new Timestamp(this.bootNum, this.bootMillis, this.epochMillis);
    }

    @Override
    public int compareTo(@NonNull Record other) {
        if (this.bootNum < other.bootNum){
            return -1;
        }else if(this.bootNum > other.bootNum){
            return 1;
        }else{
            if (this.bootMillis < other.bootMillis){
                return -1;
            }else if(this.bootMillis > other.bootMillis){
                return 1;
            }else{
                return 0;
            }
        }
    }
}
