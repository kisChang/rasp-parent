package com.github.kischang.raspberry.httpmng.model;

/**
 * @author KisChang
 * @date 2019-12-19
 */
public class ConfigInfo implements java.io.Serializable {

    private boolean reboot;

    private String wifi_ssid;
    private String wifi_pw;
    private String workServer;
    private int workPort;

    public boolean isReboot() {
        return reboot;
    }

    public void setReboot(boolean reboot) {
        this.reboot = reboot;
    }

    public String getWifi_ssid() {
        return wifi_ssid;
    }

    public void setWifi_ssid(String wifi_ssid) {
        this.wifi_ssid = wifi_ssid;
    }

    public String getWifi_pw() {
        return wifi_pw;
    }

    public void setWifi_pw(String wifi_pw) {
        this.wifi_pw = wifi_pw;
    }

    public String getWorkServer() {
        return workServer;
    }

    public void setWorkServer(String workServer) {
        this.workServer = workServer;
    }

    public int getWorkPort() {
        return workPort;
    }

    public void setWorkPort(int workPort) {
        this.workPort = workPort;
    }

    @Override
    public String toString() {
        return "ConfigInfo{" +
                "reboot=" + reboot +
                ", wifi_ssid='" + wifi_ssid + '\'' +
                ", wifi_pw='" + wifi_pw + '\'' +
                ", workServer='" + workServer + '\'' +
                ", workPort=" + workPort +
                '}';
    }
}
