package com.github.kischang.raspberry.device.led.v2;


/**
 * 对LED显示的字符串进行处理
 *
 * @author KisChang
 * @version 1.0
 */
public class LEDUtils {

    /**
     * 对显示字符长度进行处理
     */
    public static String parseStr(PiLedType type, String str) {
        return parseStr(type, str, true);
    }

    public static String parseStr(PiLedType type, String str, boolean pre) {
        int len = type.getValue() == PiLedType.led8.getValue() ? 8 : 4;
        if (str.length() > (len - 1)){
            //大于的处理方案
            if (pre){
                //获取前置 8 位
                String preStr = str.substring(0, len);
                int dc = countChar(preStr, '.');
                if (dc > 0){
                    if ((str.length() - len - dc) > 0){
                        return preStr + str.substring(len, len + dc);
                    }else {
                        return str;
                    }
                }else {
                    return preStr;
                }
            }else {
                //获取倒数 8 位
                int start = str.length() - len;
                String lastStr = str.substring(start);
                int dc = countChar(lastStr, '.');
                if (dc > 0){
                    //2. 如果里面包含. 截取原字符串  截取位置 之前位置
                    if (start - dc > 0){
                        return str.substring(start - dc, start) + lastStr;
                    }else {
                        return str;
                    }
                }else {
                    return lastStr;
                }
            }
        }else {
            return str;
        }
    }

    /**
     * 清除无法显示的字符
     */
    public static String cleanNoDis(String ledStr) {
        StringBuilder sb = new StringBuilder();
        for (char ch  : ledStr.toCharArray()){
            if (ch == '.' || LedByteMap.has(ch)){
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String prefix_len(String str, int len, char ch) {
        int dc = countChar(str, '.');
        if ( (str.length() - dc) < len ){
            int prefixSize = len - str.length() + countChar(str, '.');
            StringBuilder sb = new StringBuilder(String.valueOf(ch));
            for (int i = 1; i < prefixSize; i++) {
                sb.append(ch);
            }
            return sb.append(str).toString();
        }else {
            return str;
        }
    }


    private static int countChar(String str, char c) {
        if (isNullStr(str)){
            return 0;
        }
        int count = 0;
        for (char ch  : str.toCharArray()){
            if (ch == c){
                count ++;
            }
        }
        return count;
    }


    private static boolean isNullStr(String str){
        return str == null || "".equals(str);
    }

}
