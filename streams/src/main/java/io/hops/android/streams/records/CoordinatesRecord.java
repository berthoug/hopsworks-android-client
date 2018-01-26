package io.hops.android.streams.records;

import io.hops.android.streams.storage.SQLiteNotInitialized;

public class CoordinatesRecord extends Record{

    private double latitude;

    private double longitude;

    public CoordinatesRecord(double latitude, double longitude) throws SQLiteNotInitialized {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

}
