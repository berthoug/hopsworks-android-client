package io.hops.android.streams.time;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimerSyncReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "io.hops.android.streams.threads.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, TimerSyncService.class));
    }

}