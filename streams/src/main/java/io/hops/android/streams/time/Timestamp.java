package io.hops.android.streams.time;

import android.support.annotation.NonNull;

public class Timestamp implements Cloneable, Comparable<Timestamp>{

    private long bootNum;

    private long bootMillis;

    private long epochMillis;

    public Timestamp(long  bootNum, long bootMillis, long epochMillis){
        this.bootNum = bootNum;
        this.bootMillis = bootMillis;
        this.epochMillis = epochMillis;
    }

    public long getBootNum(){
        return bootNum;
    }

    public long getBootMillis(){
        return bootMillis;
    }

    public long getEpochMillis() {
        return epochMillis;
    }

    public boolean isComplete(){
        return (bootNum >= 0) && (bootMillis >= 0) && (epochMillis >= 0);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int compareTo(@NonNull Timestamp other) {
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
