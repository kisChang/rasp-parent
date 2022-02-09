package com.github.kischang.raspberry.utils.model;

import com.github.kischang.raspberry.utils.RaspCmdUtils;

import java.util.Objects;

/**
 * @author KisChang
 */
public class RaspHardInfo implements java.io.Serializable {

    private String hardSerial;
    private String hardModel;
    private String osVersion;
    private String osDesc;

    public RaspHardInfo() {
    }

    public static RaspHardInfo hasp_rasp = null;
    public static RaspHardInfo getByRasp(){
        if (hasp_rasp == null){
            hasp_rasp = new RaspHardInfo();
            hasp_rasp.setHardSerial(RaspCmdUtils.getHardSerial());
            hasp_rasp.setHardModel(RaspCmdUtils.getHardModel());
            hasp_rasp.setOsVersion(RaspCmdUtils.getOsVersion());
            hasp_rasp.setOsDesc(RaspCmdUtils.getOsDesc());
        }
        return hasp_rasp;
    }

    public static void main(String[] args) {
        System.out.println(getByRasp());
    }

    public String getHardSerial() {
        return hardSerial;
    }

    public void setHardSerial(String hardSerial) {
        this.hardSerial = hardSerial;
    }

    public String getHardModel() {
        return hardModel;
    }

    public void setHardModel(String hardModel) {
        this.hardModel = hardModel;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsDesc() {
        return osDesc;
    }

    public void setOsDesc(String osDesc) {
        this.osDesc = osDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaspHardInfo hardInfo = (RaspHardInfo) o;
        return Objects.equals(hardSerial, hardInfo.hardSerial) &&
                Objects.equals(hardModel, hardInfo.hardModel) &&
                Objects.equals(osVersion, hardInfo.osVersion) &&
                Objects.equals(osDesc, hardInfo.osDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hardSerial, hardModel, osVersion, osDesc);
    }

    @Override
    public String toString() {
        return "HardInfo{" +
                "hardSerial='" + hardSerial + '\'' +
                ", hardModel='" + hardModel + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", osDesc='" + osDesc + '\'' +
                '}';
    }
}
