package io.hops.android.streams.hopsworks;


public class AckRecordDTO {

    private boolean ack;

    public AckRecordDTO() {
    }

    public AckRecordDTO(boolean ack) {
        this.ack = ack;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }
}