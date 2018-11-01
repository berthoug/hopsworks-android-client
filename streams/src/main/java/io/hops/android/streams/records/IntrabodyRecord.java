package io.hops.android.streams.records;

import java.util.Arrays;

import io.hops.android.streams.storage.SQLiteNotInitialized;

public class IntrabodyRecord extends Record {

    private float temp;
    private float humidity;
    private int gRPS ;
    private int[] gStrech;

    public IntrabodyRecord(String gloveRecord) throws SQLiteNotInitialized {
        String[] data = gloveRecord.split(",");
        this.temp = Float.parseFloat(data[0].trim());
        this.humidity = Float.parseFloat(data[1].trim());
        this.gRPS = Integer.parseInt(data[2].trim());
        this.gStrech = new int[]{Integer.parseInt(data[3].trim()), Integer.parseInt(data[4].trim()),
                Integer.parseInt(data[5].trim()), Integer.parseInt(data[6].trim())};
    }

    public float getTemp() {
        return temp;
    }

    public float getHumidity() {
        return humidity;
    }

    public int getgRPS() {
        return gRPS;
    }

    public int[] getgStrech() {
        return gStrech;
    }

    @Override
    public String toString() {
        return "IntrabodyRecord{" +
                "temp=" + temp +
                ", humidity=" + humidity +
                ", gRPS=" + gRPS +
                ", gStrech=" + Arrays.toString(gStrech) +
                '}';
    }
}
