package com.github.kischang.raspberry.utils;

import com.github.kischang.raspberry.utils.cmd.CommandDaemon;
import com.github.kischang.raspberry.utils.model.WiFiInfo;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * ubuntu: sudo apt install wireless-tools net-tools hostapd dnsmasq-base udhcpc
 *
 * @author KisChang
 */
public class RaspWiFiUtils {

    /**
     * 按系统配置文件写入并重启服务
     * @param ssid  wifi ssid
     * @param key   wifi密码
     * @param wpa_supplicantConfPath  系统配置文件路径
     * @return
     */
    public static boolean connWiFiBySys(String ssid, String key, String wpa_supplicantConfPath) {
        String shell_connWifi = String.format(
                "#!/bin/sh\n" +
                // 强行结束部分进程，确保后续重启没有问题
                "ps aux | grep wpa_supplicant | awk '{print $2}' | xargs kill -9 \n" +
                "ps aux | grep udhcpc.wlan0 | awk '{print $2}' | xargs kill -9 \n" +
                // 写入配置文件
                "wpa_passphrase %s '%s' > %s\n" +
                "rm -rf /var/run/udhcpc.wlan0.pid \n" +
                // 重启
                "rc-service networking restart \n" +
                "rc-service avahi-daemon restart \n"
                , ssid, key, wpa_supplicantConfPath);

        CommandDaemon testRv = runSh(shell_connWifi, "conn_wifi_sys");
        return true;
    }

    /**
     * 尝试连接WiFi
     * 依赖： iwconf dhcpcd
     * 命令：
        sudo ifconfig wlan0 up //启动
        WPADRV=$(cat /etc/sysconfig/wifi-wpadrv)
        wpa_passphrase wifiap '1234567' > /home/tc/wpa_supplicant.conf // 生成配置文件
        sudo wpa_supplicant -B -i wlan0 -c /home/tc/wpa_supplicant.conf -D $WPADRV //启动 -B 是后台运行

        ------dhcpcd 方案 （现用）
        sudo killall dhcpcd
        sudo dhcpcd wlan0 &  //dhcp到ip，后台运行

        -----udhcpc 方案

        状态检查：
        iwconfig wlan0， 是否存在ESSID
     *
     * @param ssid    WiFi的ESSID
     * @param key     WiFi密码
     * @param devName 设备名称
     * @param path    配置存储路径
     * @return true连接成功，false连接失败
     */
    public static boolean connWiFi(String ssid, String key, String devName, String path) {
            String shell_connWifi = String.format(
                "#!/bin/sh\n" +
                "ps aux | grep wpa_supplicant | awk '{print $1}' | xargs kill -9 \n" +
                "ps aux | grep udhcpc | awk '{print $1}' | xargs kill -9 \n" +
                "ifconfig %s down\n" +
                "sleep 2s \n" +
                "ifconfig %s up\n" +
                "WPADRV=nl80211\n" +
                "wpa_passphrase %s '%s' > %s/wpa_supplicant.conf\n" +
                "wpa_supplicant -B -i wlan0 -c %s/wpa_supplicant.conf -D ${WPADRV} \n" +
                "sleep 2s \n" + //等一下
                "if [ -f /var/run/udhcpc.wlan0.pid ]; then \n" +
                "  cat /var/run/udhcpc.wlan0.pid | xargs kill -9 \n" +
                "fi \n" +
                "rm -rf /var/run/udhcpc.wlan0.pid \n" +
                "HOST_NAME=$(hostname) \n" +
                "udhcpc -b -R -p -p /var/run/udhcpc.wlan0.pid -i %s -x hostname:${HOST_NAME} \n" +
                "rc-service avahi-daemon restart \n"
//                "udhcpc -n -i %s -x hostname ${HOST_NAME} -p /var/run/udhcpc.wlan0.pid \n"
                , devName, devName, ssid, key, path, path, devName);

        File testPath = new File(path);
        if (!testPath.exists()) {
            testPath.mkdirs();
        }

        CommandDaemon testRv = runSh(shell_connWifi, "conn_wifi");

        //等待连接WiFi
        try {
            Thread.sleep(7000);
        } catch (InterruptedException ignored) {}
        //2. 简单检查一下
        String rv = RaspCmdUtils.runCmdOnce(String.format("iwconfig %s", devName));
        return rv.contains(ssid);
    }

    public static boolean connWiFi(String ssid, String key) {
        return connWiFi(ssid, key
                , "wlan0"
                , Paths.get(System.getProperty("user.home"), ".conf").toString()
        );
    }

    public static void main(String[] args) {
        connWiFi("wifiap", "12345678");
    }

    public static boolean testNetwork(String server) {
        boolean reachable = false;
        try {
            InetAddress address = InetAddress.getByName(server);
            reachable = address.isReachable(5000);
        } catch (Exception ignored) {
        }
        return reachable;
    }
    public static boolean testNetwork() {
        return testNetwork("www.baidu.com");
    }


    /**
     * 扫描wifi
     * @return
     */
    public static List<WiFiInfo> scanWiFi() {
        String content = RaspCmdUtils.runCmdOnce("iwlist wlan0 scanning");
        return RaspWiFiUtils.parseContent(content);
    }

    /**
     * 处理 /usr/local/sbin/iwlist wlan0 scanning 的返回结果
     */
    public static List<WiFiInfo> parseContent(String content) {
        List<WiFiInfo> list = new LinkedList<>();
        for (String once : content.split("Cell")) {
            if (once.contains("ESSID")) {
                String[] lines = once.split("\n");
                WiFiInfo info = new WiFiInfo();
                info.setName(parseOnceLine(lines, 5, ":", 1, 1));
                info.setSsid(parseOnceLine(lines, 0, ":", 1, 0));
                info.setMac(parseOnceLine(lines, 0, ":", 1, 0));
                info.setFrequency(parseOnceLine(lines, 2, ":", 0, 0));
                info.setQuality(parseOnceLine(lines, 3, "=", 0, "Signal", 1));
                info.setSignal(parseOnceLine(lines, 3, "level=", 0, 1));
                list.add(info);
            }
        }
        return list;
    }

    private static String parseOnceLine(String[] lines, int lineIndex, String strIndex, int startAdd, int beforeEnd) {
        return parseOnceLine(lines, lineIndex, strIndex, startAdd, null, beforeEnd);
    }

    private static String parseOnceLine(String[] lines, int lineIndex, String strIndex, int startAdd, String end, int beforeEnd) {
        return lines[lineIndex].substring(
                lines[lineIndex].indexOf(strIndex) + strIndex.length() + startAdd
                , end == null
                        ? (lines[lineIndex].length() - beforeEnd)
                        : (lines[lineIndex].indexOf(end) - beforeEnd)
        );
    }

    public static final String shell_dnsmasq =
            "#!/bin/sh\n" +
            "\n" +
            "# user variables\n" +
            "lan_if=wlan0\n" +
            "ip_stem=192.168.10\n" +
            "\n" +
            "# set lan_if ip address\n" +
            "ifconfig $lan_if $ip_stem.1\n" +
            "\n" +
            "# create dnsmasq.conf\n" +
            "echo \"interface=wlan0\n" +
            "listen-address=$ip_stem.1\n" +
            "address=/*/$ip_stem.1\n" +
            "address=/#/$ip_stem.1\n" +
            "dhcp-range=$ip_stem.100,$ip_stem.200,255.255.255.0,24h\n" +
            "dhcp-option-force=option:router,$ip_stem.1\n" +
            "dhcp-option-force=option:dns-server,$ip_stem.1\n" +
            "dhcp-option-force=option:mtu,1500\n" +
            "dhcp-leasefile=/tmp/dnsmasq.leases\n" +
            "\" >/tmp/dnsmasq.conf\n" +
            "\n" +
            "# start dnsmasq\n" +
            "dnsmasq -C /tmp/dnsmasq.conf"
            ;

    private static final String shell_hostapd =
            "#!/bin/sh\n" +
            "# re-up wlan0\n" +
            "cleanup() {\n" +
            "      /etc/init.d/wpa_supplicant stop\n" +
            "      ps aux | grep wpa_supplicant | awk '{print $2}' | xargs kill -9\n" +
            "      ifconfig wlan0 down 2>/dev/null\n" +
            "      for k in $(ps | awk '/wlan0/{print $1}'); do kill ${k} 2>/dev/null; done\n" +
            "}\n" +
            "\n" +
            "cleanup\n" +
            "sleep 1\n" +
            "ifconfig wlan0 up\n" +
            "ifconfig wlan0 192.168.10.1\n" +
            "\n" +
            "# create hostapd.conf\n" +
            "echo \"interface=wlan0\n" +
            "country_code=CN\n" +
            "driver=nl80211\n" +
            "ssid=%s\n" +
            "channel=7\" >/tmp/hostapd.conf\n" +
            "\n" +
            "# start hostapd\n" +
            "hostapd /tmp/hostapd.conf"
            ;
    public static void startApMode(String ssid) {
        /*
        启动热点，SSID： ZK_FB_ABCD  pwd：开放
        sudo hostapd hostapd.conf

        启动dnsmasq，其中修改本机ip为192.168.10.1，并将所有dns解析到了此IP
        sudo dnsmasq.sh
        */
        CommandDaemon rv_hostapd = runSh(String.format(shell_hostapd, ssid), "hostapd");
        //System.out.println("hostapd >>" + rv.getOutStr());

        //执行dns
        CommandDaemon rv_dnsmasq = runSh(shell_dnsmasq, "dnsmasq");
        //System.out.println("dnsmasq >>" + rv.getOutStr());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }

    public static String randStr(int len) {
        return randStr("ABCDEFGHIJKLMNOPQRSTUVWXYZ", len);
    }
    public static String randStr(String tmp, int len) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(tmp.charAt(random.nextInt(tmp.length())));
        }
        return sb.toString();
    }

    private static CommandDaemon runSh(String bashShell, String name) {
        //写入文件
        try (OutputStream out = new FileOutputStream("/tmp/" + name + ".sh")){
            IOUtils.write(bashShell, out, StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
        //执行这些命令尝试连接
        CommandDaemon testRv = RaspCmdUtils.runCmdDaemon("/bin/sh /tmp/" + name + ".sh", name);
        //System.out.println(testRv.getOutStr());
        return testRv;
    }

}
