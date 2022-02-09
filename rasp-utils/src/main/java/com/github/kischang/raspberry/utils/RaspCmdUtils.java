package com.github.kischang.raspberry.utils;

import com.github.kischang.raspberry.utils.cmd.CommandDaemon;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author KisChang
 */
public class RaspCmdUtils {

    public static boolean DAEMON_OUT_PRINT = false;

    /** 硬件序列号 */
    public static String getHardSerial(){
        //CPU 硬件序列号
        String rv = runCmdOnce("cat /proc/cpuinfo");
        int sInd = rv.indexOf("Serial");
        if (sInd == -1){
            return null;
        }
        rv = rv.substring(sInd);
        rv = rv.substring(rv.indexOf(":") + 1).trim().substring(8);

        //eth0 的MAC地址
        String mac = runCmdOnce("cat /sys/class/net/eth0/address").trim();
        mac = mac.replaceAll(":", "");
        return rv + mac;
    }

    /** 硬件版本 */
    public static String getHardModel(){
        return runCmdOnce("cat /proc/device-tree/model").trim();
    }

    /** 系统版本 */
    public static String getOsVersion(){
        return runCmdOnce("uname -r").trim();
    }

    /** 系统完整信息 */
    public static String getOsDesc(){
        return runCmdOnce("cat /proc/version").trim();
    }

    public static CommandDaemon runCmdDaemon(String cmd, String name){
        CommandDaemon cd = new CommandDaemon(cmd, name);
        cd.startDaemon();
        return cd;
    }

    public static String runCmdOnce(String cmd){
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            String err = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
            System.err.println(err);
            return IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String reboot(){
        return runCmdOnce("reboot");
    }

    public static String rmRf(String file){
        return runCmdOnce("rm -rf " + file);
    }

}
