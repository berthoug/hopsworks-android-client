package io.hops.android.streams.hopsworks;

import com.google.gson.Gson;

import java.util.List;

import io.hops.android.streams.records.Record;

public class TopicRecordsDTO {

    private String topic;

    private List<Record> records;

    public TopicRecordsDTO(String topic) {
        this.topic = topic;
    }

    public TopicRecordsDTO(String topic, List<Record> records) {
        this.topic = topic;
        this.records = records;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public final String toJson(){
        return new Gson().toJson(this);
    }
}
