package com.github.kischang.raspberry.device.led.v2;

import com.github.kischang.raspberry.device.led.PiLedAbstract;
import com.pi4j.io.gpio.GpioController;

import java.util.Arrays;

/**
 * 新版LED
 *
 * @author KisChang
 */
public class PiLedMulti extends PiLedAbstract {

    //几个几位的led
    private int ledNum;
    private byte[] byteData;
    private PiLedType defLedType;

    public PiLedMulti(GpioController gpio, int dio, int rclk, int sclk
            , int ledNum, PiLedType defLedType) {
        super(gpio, dio, rclk, sclk);

        //初始化本地缓存
        this.byteData = new byte[defLedType.getValue() * ledNum];
        this.defLedType = defLedType;
        this.ledNum = ledNum;
    }


    public void display(String str){
        display(0, str);
    }

    public void display(int index, String str){
        display(index, this.defLedType, str);
    }

    public void display(int index, PiLedType ledType, String str){
        //index 起始位置以0开始
        if (index >= ledNum){
            return;
        }

        //去除无法显示的
        str = LEDUtils.cleanNoDis(str);
        //定位点
        int dianId = str.indexOf(".");
        if (dianId >= 0){
            str = str.replaceAll("\\.","");
        }

        //对字符串进行一次处理
        str = LEDUtils.parseStr(ledType, str);

        //补充长度
        str = LEDUtils.prefix_len(str, ledType.getValue(), LedByteMap.NONE);


        byte[] bytes;
        if (ledType.getValue() == PiLedType.led8.getValue()) {
            bytes = new byte[]{
                      LedByteMap.get(str.charAt(7))
                    , LedByteMap.get(str.charAt(6))
                    , LedByteMap.get(str.charAt(5))
                    , LedByteMap.get(str.charAt(4))
                    , LedByteMap.get(str.charAt(3))
                    , LedByteMap.get(str.charAt(2))
                    , LedByteMap.get(str.charAt(1))
                    , LedByteMap.get(str.charAt(0))
            };
        } else {
            bytes = new byte[]{
                     LedByteMap.get(str.charAt(3))
                    , LedByteMap.get(str.charAt(2))
                    , LedByteMap.get(str.charAt(1))
                    , LedByteMap.get(str.charAt(0))
            };
        }

        //修改点的位置
        if (dianId >= 0){
            dianId = ledType.getValue() - dianId;
            bytes[dianId] = (byte) (bytes[dianId] & LedByteMap.fromString("01111111"));
        }

        int startIndex = index * ledType.getValue();
        for (int ind = 0; ind < ledType.getValue(); ind ++){
            Arrays.fill(byteData
                    , startIndex + ind
                    , startIndex + ind + 1
                    , bytes[ind]
            );
        }

        shuffle();
    }


    private void shuffle() {
        for (byte bt : byteData){
            ledOut(bt);
            sleep(2);
        }

        rclk();
    }


    private void ledOut(byte X) {
        byte i;
        for (i = 8; i >= 1; i--) {
            if ((X & 0x80) != 0) {
                DIO.high();
            } else {
                DIO.low();
            }
            X <<= 1;
            SCLK.low();
            SCLK.high();
        }
    }

    private void rclk() {
        RCLK.low();
        RCLK.high();
    }


    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void destroy() {
        this.byteData = new byte[defLedType.getValue() * ledNum];
        shuffle();
    }

}
