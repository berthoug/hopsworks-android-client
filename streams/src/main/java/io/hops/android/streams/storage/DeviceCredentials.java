package io.hops.android.streams.storage;

import java.util.UUID;

public class DeviceCredentials {

    /**
     * Retrieves the deviceUUID from the SQLite Database.
     * If there is no deviceUUID stored it auto-generates one and saves it in the SQLite Database.
     * @return The deviceUUID
     * @throws SQLiteNotInitialized when the SQLite Database has not been initialized.
     */
    public static String getDeviceUUID() throws SQLiteNotInitialized {
        String deviceUuid = PropertiesTable.read("deviceUUID");
        if (deviceUuid == null){
            PropertiesTable.write("deviceUUID", UUID.randomUUID().toString());
            deviceUuid = PropertiesTable.read("deviceUUID");
        }
        return deviceUuid;
    }

    /**
     * Retrieves the device's password from the SQLite Database.
     * If there is no password stored it auto-generates one and saves it in the SQLite Database.
     * @return The password
     * @throws SQLiteNotInitialized when the SQLite Database has not been initialized.
     */
    public static String getPassword() throws SQLiteNotInitialized {
        String password = PropertiesTable.read("password");
        if (password == null){
            PropertiesTable.write("password", UUID.randomUUID().toString());
            password = PropertiesTable.read("password");
        }
        return password;
    }

}
