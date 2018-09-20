package io.hops.android.streams.records;

import org.json.JSONException;
import org.json.JSONObject;

import io.hops.android.streams.storage.SQLiteNotInitialized;

public class IntrabodyRecord extends Record {

    private int dvcId;
    private int channelId;
    private String value;
    private int unit;
    private long time;

    public IntrabodyRecord(String record) throws SQLiteNotInitialized, JSONException {
        super();
        JSONObject json = new JSONObject(record);
        this.dvcId = json.getInt("dvcId");
        this.channelId = json.getInt("channelId");
        this.value = json.getString("value");
        this.unit = json.getInt("unit");
        this.time =  json.getLong("time");
    }

    public IntrabodyRecord(int dvcId, int channelId, String value, int unit, long time) throws SQLiteNotInitialized {
        this.dvcId = dvcId;
        this.channelId = channelId;
        this.value = value;
        this.unit = unit;
        this.time = time;
    }

    @Override
    public String toString() {
        return "IntrabodyRecord{" +
                "dvcId=" + dvcId +
                ", channelId=" + channelId +
                ", value=" + value +
                ", unit=" + unit +
                ", time=" + time +
                '}';
    }
}
