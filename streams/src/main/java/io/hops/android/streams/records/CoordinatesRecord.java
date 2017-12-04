package io.hops.android.streams.records;

import io.hops.android.streams.storage.StorageNotInitialized;

public class CoordinatesRecord extends Record{

    private double latitude;

    private double longitude;

    public CoordinatesRecord(double latitude, double longitude) throws StorageNotInitialized {
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
