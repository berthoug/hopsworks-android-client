package io.hops.android.streams.hopsworks;


public class DeviceDTO {

    private String deviceUuid;

    private String password;

    private String alias;

    public DeviceDTO(String deviceUuid, String password, String alias) {
        this.deviceUuid = deviceUuid;
        this.password = password;
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
