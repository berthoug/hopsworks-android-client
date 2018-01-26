package io.hops.android.streams.time;


import android.app.IntentService;
import android.content.Intent;

import io.hops.android.streams.storage.SQLiteNotInitialized;

public class TimerSyncService extends IntentService {

    private String host;
    private int timeout;

    public TimerSyncService(String ntpHost, int timeout){
        super(TimerSyncService.class.getName());
        this.host = ntpHost;
        this.timeout = timeout;
    }

    public TimerSyncService() {
        this("pool.ntp.org", 5000);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (Timer.getInstance().sync(host, timeout)){
                // TODO: Trigger recalculation of records with no epochMillis
            }
        } catch (SQLiteNotInitialized SQLiteNotInitialized) {
            SQLiteNotInitialized.printStackTrace();
        }
    }
}