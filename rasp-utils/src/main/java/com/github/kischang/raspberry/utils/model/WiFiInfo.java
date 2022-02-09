package com.github.kischang.raspberry.utils.model;

import java.util.Objects;

/**
 * @author KisChang
 */
public class WiFiInfo implements java.io.Serializable {

    private String name;
    private String ssid;
    private String mac;
    private String frequency;
    private String quality;
    private String signal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WiFiInfo that = (WiFiInfo) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(ssid, that.ssid) &&
                Objects.equals(mac, that.mac) &&
                Objects.equals(frequency, that.frequency) &&
                Objects.equals(quality, that.quality) &&
                Objects.equals(signal, that.signal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ssid, mac, frequency, quality, signal);
    }

    @Override
    public String toString() {
        return "WiFiInfo{" +
                "name='" + name + '\'' +
                ", ssid='" + ssid + '\'' +
                ", mac='" + mac + '\'' +
                ", frequency='" + frequency + '\'' +
                ", quality='" + quality + '\'' +
                ", signal='" + signal + '\'' +
                '}';
    }
}
