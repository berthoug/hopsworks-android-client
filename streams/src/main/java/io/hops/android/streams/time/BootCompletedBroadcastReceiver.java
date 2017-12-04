package io.hops.android.streams.time;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.hops.android.streams.storage.Storage;
import io.hops.android.streams.storage.StorageNotInitialized;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //Intent pushIntent = new Intent(context, BackgroundService.class);
            //context.startService(pushIntent);
            //TODO: Consider moving the code below to a Background Service

            Storage.init(context);
            try {
                Timer.getInstance().rebootHappened();
            } catch (StorageNotInitialized e) {
                e.printStackTrace();
            }
        }
    }

}