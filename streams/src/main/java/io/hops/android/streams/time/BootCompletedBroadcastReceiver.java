package io.hops.android.streams.time;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.hops.android.streams.storage.SQLite;
import io.hops.android.streams.storage.SQLiteNotInitialized;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //TODO: Consider moving the code below to a Background Service
            SQLite.init(context);
            try {
                Timer.getInstance().rebootHappened();
            } catch (SQLiteNotInitialized e) {
                e.printStackTrace();
            }
        }
    }

}