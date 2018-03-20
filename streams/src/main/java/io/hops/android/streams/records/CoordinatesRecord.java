package io.hops.android.streams.records;

import org.json.JSONException;
import org.json.JSONObject;

import io.hops.android.streams.storage.SQLiteNotInitialized;

public class CoordinatesRecord extends Record{

    private double latitude;

    private double longitude;

    public CoordinatesRecord(double latitude, double longitude) throws SQLiteNotInitialized {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CoordinatesRecord(String coordinatesStr) throws SQLiteNotInitialized, JSONException {
        JSONObject json = new JSONObject(coordinatesStr);
        this.latitude = json.getDouble("latitude");
        this.longitude = json.getDouble("longitude");
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }


    @Override
    public String toString() {
        return "CoordinatesRecord{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
