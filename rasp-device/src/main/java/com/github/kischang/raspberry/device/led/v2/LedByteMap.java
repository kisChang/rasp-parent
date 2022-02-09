package com.github.kischang.raspberry.device.led.v2;

import java.util.HashMap;
import java.util.Map;

/**
 *   6
 * 1   5
 *   0
 * 2   4
 *   3
 *
 * @author KisChang
 */
public class LedByteMap {

    private static final String LED_NONE = "11111111";
    public static final char NONE = '~';

    private static Map<String, Byte> map = new HashMap<String, Byte>();

    static {
        map.put(String.valueOf(NONE), fromString(LED_NONE));
        map.put("0", fromString("11000000"));
        map.put("1", fromString("11111001"));
        map.put("2", fromString("10100100"));
        map.put("3", fromString("10110000"));
        map.put("4", fromString("10011001"));
        map.put("5", fromString("10010010"));
        map.put("6", fromString("10000010"));
        map.put("7", fromString("11111000"));
        map.put("8", fromString("10000000"));
        map.put("9", fromString("10010000"));

        map.put("S", fromString("10010010"));
        map.put("a", fromString("10001000"));
        map.put("b", fromString("10000011"));
        map.put("c", fromString("10100111"));
        map.put("d", fromString("10100001"));
        map.put("E", fromString("10000110"));
        map.put("F", fromString("10001110"));
        map.put("R", fromString("10101111"));
        map.put("r", fromString("11010000"));
        map.put("o", fromString("10100011"));
        map.put("u", fromString("11100011"));
        map.put("n", fromString("10101011"));
        map.put("-", fromString("10111111"));

        map.put("A", fromString("10001000"));
        map.put("C", fromString("11000110"));
        map.put("D", fromString("11000000"));
        map.put("N", fromString("11001000"));
        map.put("V", fromString("11000001"));
    }

    public static byte get(char ch) {
        return get(String.valueOf(ch));
    }

    public static byte get(String valueOf) {
        return map.get(valueOf);
    }

    public static byte fromString(String bin) {
        int x = (Integer.parseInt(bin, 2));
        return (byte) (x & 0xFF);
    }

    public static boolean has(char ch) {
        return map.containsKey(String.valueOf(ch));
    }
}
