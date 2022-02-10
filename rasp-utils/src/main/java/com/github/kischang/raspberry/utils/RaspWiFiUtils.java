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
                "ps aux | grep wpa_supplicant | awk '{print $1}' | xargs kill -9 \n" +
                "ps aux | grep udhcpc | awk '{print $1}' | xargs kill -9 \n" +
                // 写入配置文件
                "wpa_passphrase %s '%s' > %s\n" +
                "rm -rf /var/run/udhcpc.eth0.pid \n" +
                "rm -rf /var/run/udhcpc.wlan0.pid \n" +
                // 重启
                "rc-service networking restart \n"
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
        wpa_passphrase cpvs 'cpvs@2017' > /home/tc/wpa_supplicant.conf // 生成配置文件
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
                "udhcpc -b -R -p -p /var/run/udhcpc.wlan0.pid -i %s -x hostname:${HOST_NAME} \n"
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
        connWiFi("cpvs", "cpvs@2017");
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
		/*String content =
            "          Cell 07 - Address: EC:6C:9F:09:2E:1A\n" +
            "                    Channel:8\n" +
            "                    Frequency:2.447 GHz (Channel 8)\n" +
            "                    Quality=37/70  Signal level=-73 dBm  \n" +
            "                    Encryption key:on\n" +
            "                    ESSID:\"Volans_2E1A\"\n" +
            "                    Bit Rates:1 Mb/s; 2 Mb/s; 5.5 Mb/s; 11 Mb/s; 9 Mb/s\n" +
            "                              18 Mb/s; 36 Mb/s; 54 Mb/s\n" +
            "                    Bit Rates:6 Mb/s; 12 Mb/s; 24 Mb/s; 48 Mb/s\n" +
            "                    Mode:Master\n" +
            "                    Extra:tsf=0000000000000000\n" +
            "                    Extra: Last beacon: 70ms ago\n" +
            "                    IE: Unknown: 000B566F6C616E735F32453141\n" +
            "                    IE: Unknown: 010882848B961224486C\n" +
            "                    IE: Unknown: 030108\n" +
            "                    IE: Unknown: 2A0104\n" +
            "                    IE: Unknown: 32040C183060\n" +
            "                    IE: Unknown: 2D1AEE1117FFFF0000010000000000000000000000000C0000000000\n" +
            "                    IE: Unknown: 3D1608050600000000000000000000000000000000000000\n" +
            "                    IE: Unknown: 3E0100\n" +
            "                    IE: IEEE 802.11i/WPA2 Version 1\n" +
            "                        Group Cipher : CCMP\n" +
            "                        Pairwise Ciphers (1) : CCMP\n" +
            "                        Authentication Suites (1) : PSK\n" +
            "                    IE: Unknown: DD180050F2020101000003A4000027A4000042435E0062322F00\n" +
            "                    IE: Unknown: 0B0501001F127A\n" +
            "                    IE: Unknown: 7F0101\n" +
            "                    IE: Unknown: DD07000C4307000000\n" +
            "                    IE: Unknown: 0706434E20010D10\n" +
            "                    IE: Unknown: DD1E00904C33EE1117FFFF0000010000000000000000000000000C0000000000\n" +
            "                    IE: Unknown: DD1A00904C3408050600000000000000000000000000000000000000\n" +
            "          Cell 08 - Address: 8C:A6:DF:50:72:9D\n" +
            "                    Channel:11\n" +
            "                    Frequency:2.462 GHz (Channel 11)\n" +
            "                    Quality=37/70  Signal level=-73 dBm  \n" +
            "                    Encryption key:on\n" +
            "                    ESSID:\"8160\"\n" +
            "                    Bit Rates:1 Mb/s; 2 Mb/s; 5.5 Mb/s; 11 Mb/s; 6 Mb/s\n" +
            "                              9 Mb/s; 12 Mb/s; 18 Mb/s\n" +
            "                    Bit Rates:24 Mb/s; 36 Mb/s; 48 Mb/s; 54 Mb/s\n" +
            "                    Mode:Master\n" +
            "                    Extra:tsf=0000000000000000\n" +
            "                    Extra: Last beacon: 70ms ago\n" +
            "                    IE: Unknown: 000438313630\n" +
            "                    IE: Unknown: 010882848B960C121824\n" +
            "                    IE: Unknown: 03010B\n" +
            "                    IE: Unknown: 2A0100\n" +
            "                    IE: IEEE 802.11i/WPA2 Version 1\n" +
            "                        Group Cipher : CCMP\n" +
            "                        Pairwise Ciphers (1) : CCMP\n" +
            "                        Authentication Suites (1) : PSK\n" +
            "                    IE: Unknown: 32043048606C\n" +
            "                    IE: Unknown: 2D1AEE111BFFFFFF0000000000000000000100000000000000000000\n" +
            "                    IE: Unknown: 3D160B0F0000000000000000000000000000000000000000\n" +
            "                    IE: Unknown: 7F080000000000000040\n" +
            "                    IE: WPA Version 1\n" +
            "                        Group Cipher : CCMP\n" +
            "                        Pairwise Ciphers (1) : CCMP\n" +
            "                        Authentication Suites (1) : PSK\n" +
            "                    IE: Unknown: DD180050F2020101800003A4000027A4000042435E0062322F00\n" +
            "                    IE: Unknown: DD0900037F01010000FF7F\n" +
            "          Cell 09 - Address: BC:46:99:A6:F4:66\n" +
            "                    Channel:11\n" +
            "                    Frequency:2.462 GHz (Channel 11)\n" +
            "                    Quality=30/70  Signal level=-80 dBm  \n" +
            "                    Encryption key:on\n" +
            "                    ESSID:\"TB-SmartFarm-1\"\n" +
            "                    Bit Rates:1 Mb/s; 2 Mb/s; 5.5 Mb/s; 11 Mb/s; 6 Mb/s\n" +
            "                              9 Mb/s; 12 Mb/s; 18 Mb/s\n" +
            "                    Bit Rates:24 Mb/s; 36 Mb/s; 48 Mb/s; 54 Mb/s\n" +
            "                    Mode:Master\n" +
            "                    Extra:tsf=0000000000000000\n" +
            "                    Extra: Last beacon: 70ms ago\n" +
            "                    IE: Unknown: 000E54422D536D6172744661726D2D31\n" +
            "                    IE: Unknown: 010882848B960C121824\n" +
            "                    IE: Unknown: 03010B\n" +
            "                    IE: Unknown: 2A0100\n" +
            "                    IE: Unknown: 32043048606C\n" +
            "                    IE: Unknown: 2D1A6E1003FFFF000000000000000000000000000000000000000000\n" +
            "                    IE: Unknown: 3D160B070200000000000000000000000000000000000000\n" +
            "                    IE: IEEE 802.11i/WPA2 Version 1\n" +
            "                        Group Cipher : CCMP\n" +
            "                        Pairwise Ciphers (1) : CCMP\n" +
            "                        Authentication Suites (1) : PSK\n" +
            "                    IE: WPA Version 1\n" +
            "                        Group Cipher : CCMP\n" +
            "                        Pairwise Ciphers (1) : CCMP\n" +
            "                        Authentication Suites (1) : PSK\n" +
            "                    IE: Unknown: DD180050F2020101000003A4000027A4000042435E0062322F00\n" +
            "                    IE: Unknown: DD05000AEB0100\n" +
            "          Cell 10 - Address: 0C:DA:41:EC:7F:F0\n" +
            "                    Channel:11\n" +
            "                    Frequency:2.462 GHz (Channel 11)\n" +
            "                    Quality=38/70  Signal level=-72 dBm  \n" +
            "                    Encryption key:on\n" +
            "                    ESSID:\"cpvs\"\n" +
            "                    Bit Rates:1 Mb/s; 2 Mb/s; 5.5 Mb/s; 6 Mb/s; 9 Mb/s\n" +
            "                              11 Mb/s; 12 Mb/s; 18 Mb/s\n" +
            "                    Bit Rates:24 Mb/s; 36 Mb/s; 48 Mb/s; 54 Mb/s\n" +
            "                    Mode:Master\n" +
            "                    Extra:tsf=0000000000000000\n" +
            "                    Extra: Last beacon: 70ms ago\n" +
            "                    IE: Unknown: 000463707673\n" +
            "                    IE: Unknown: 010882848B0C12961824\n" +
            "                    IE: Unknown: 03010B\n" +
            "                    IE: Unknown: 0706434E49010D14\n" +
            "                    IE: Unknown: 2A0100\n" +
            "                    IE: IEEE 802.11i/WPA2 Version 1\n" +
            "                        Group Cipher : CCMP\n" +
            "                        Pairwise Ciphers (1) : CCMP\n" +
            "                        Authentication Suites (1) : PSK\n" +
            "                    IE: Unknown: 32043048606C\n" +
            "                    IE: Unknown: 2D1AED1103FFFF000000000000000000000000000000000000000000\n" +
            "                    IE: Unknown: 3D160B080400000000000000000000000000000000000000\n" +
            "                    IE: Unknown: DD180050F2020101810003A4000027A4000042435E0062322F00\n" +
            "                    IE: Unknown: DD1E00904C33ED1103FFFF000000000000000000000000000000000000000000\n" +
            "                    IE: Unknown: DD1A00904C340B080400000000000000000000000000000000000000";*/
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
