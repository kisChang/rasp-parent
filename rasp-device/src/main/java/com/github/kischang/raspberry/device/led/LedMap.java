package com.github.kischang.raspberry.device.led;

import java.util.HashMap;
import java.util.Map;

/**
 * LED 灯映射
 *
 * @author KisChang
 * @version 1.0
 */
public class LedMap {

    private static String LED_f = "10111111";
    private static String LED_0 = "11000000";
    private static String LED_1 = "11111001";
    private static String LED_2 = "10100100";
    private static String LED_3 = "10110000";
    private static String LED_4 = "10011001";
    private static String LED_5 = "10010010";
    private static String LED_6 = "10000010";
    private static String LED_7 = "11111000";
    private static String LED_8 = "10000000";
    private static String LED_9 = "10010000";

    private static Map<String, Byte> map = new HashMap<String, Byte>(){{
        put(String.valueOf(0)  , fromString(LED_0));
        put(String.valueOf(1)  , fromString(LED_1));
        put(String.valueOf(2)  , fromString(LED_2));
        put(String.valueOf(3)  , fromString(LED_3));
        put(String.valueOf(4)  , fromString(LED_4));
        put(String.valueOf(5)  , fromString(LED_5));
        put(String.valueOf(6)  , fromString(LED_6));
        put(String.valueOf(7)  , fromString(LED_7));
        put(String.valueOf(8)  , fromString(LED_8));
        put(String.valueOf(9)  , fromString(LED_9));
        put("-"                , fromString(LED_f));
        put("~"                , fromString("11111111"));
        put("R"                , fromString("10001000"));
        put("S"                , fromString("10010010"));
        put("E"                , fromString("10000110"));
        put("r"                , fromString("10101111"));
        put("o"                , fromString("10100011"));
        put("u"                , fromString("11100011"));
        put("c"                , fromString("10100111"));
    }};


    private static byte fromString(String bin) {
        int x = (Integer.parseInt(bin, 2));
        return (byte) (x & 0xFF);
    }

    public static Byte get(String cont){
        return map.getOrDefault(cont, null);
    }

}
