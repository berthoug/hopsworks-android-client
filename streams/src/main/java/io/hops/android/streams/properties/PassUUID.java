package io.hops.android.streams.properties;

import java.util.UUID;

import io.hops.android.streams.storage.Storage;
import io.hops.android.streams.storage.StorageNotInitialized;

public class PassUUID {

    private static final String NAME = "passUUID";
    private static String value;
    private static final Object lock = new Object();

    public static String getValue() throws StorageNotInitialized {
        if (value == null) {
            synchronized (lock) {
                if (value == null) {
                    if (!load()){
                        generate();
                    }
                }
            }
        }
        return value;
    }

    public static void set(String value) throws StorageNotInitialized {
        synchronized (lock) {
            PassUUID.value = value;
            save();
        }
    }

    private static boolean load() throws StorageNotInitialized {
        value = Storage.getInstance().loadProperty(NAME);
        return (value != null);
    }

    private static void generate() throws StorageNotInitialized {
        value = UUID.randomUUID().toString();
        save();
    }

    private static void save() throws StorageNotInitialized {
        Storage.getInstance().saveProperty(NAME, value);
    }

}
