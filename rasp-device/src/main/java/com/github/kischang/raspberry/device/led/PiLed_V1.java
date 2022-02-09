package com.github.kischang.raspberry.device.led;


import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Led 显示封装
 *
 * @author KisChang
 * @version 1.0
 */
public class PiLed_V1 extends PiLedAbstract {

    private GpioPinDigitalOutput DIO;
    private GpioPinDigitalOutput RCLK;
    private GpioPinDigitalOutput SCLK;
    private GpioController gpio;

    private boolean isRun = true;

    private volatile String[] arr = new String[]{"0", "0", "0", "0", "0", "0", "0", "0"};

    public PiLed_V1(GpioController gpio, int dio, int rclk, int sclk) {
        this(gpio
                , RaspiPin.getPinByAddress(dio)
                , RaspiPin.getPinByAddress(rclk)
                , RaspiPin.getPinByAddress(sclk)
        );
    }
    public PiLed_V1(GpioController gpio, Pin dio, Pin rclk, Pin sclk) {
        super(gpio, dio, rclk, sclk);
        //启动状态更新
        new Thread(() -> {
            while (isRun) {
                displayStr(arr);
            }
        }).start();
    }

    public String[] getArr() {
        return arr;
    }

    public void setArr(String[] arr) {
        this.arr = arr;
    }

    @Override
    public void display(String str) {
        String[] arr = new String[]{
                String.valueOf(str.charAt(0))
                , String.valueOf(str.charAt(1))
                , String.valueOf(str.charAt(2))
                , String.valueOf(str.charAt(3))
                , String.valueOf(str.charAt(4))
                , String.valueOf(str.charAt(5))
                , String.valueOf(str.charAt(6))
                , String.valueOf(str.charAt(7))
        };
        setArr(arr);
    }

    @Override
    public void display(int index, String ledStr) {
        //忽略index
        this.display(ledStr);
    }

    private void displayStr(String[] arr){
        displayStr(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5], arr[6], arr[7]);
    }

    private void displayStr(String d7, String d6, String d5, String d4, String d3, String d2, String d1, String d0) {
        displayOnce(d7, (byte) 128);
        displayOnce(d6, (byte) 64);
        displayOnce(d5, (byte) 32);
        displayOnce(d4, (byte) 16);
        displayOnce(d3, (byte) 8);
        displayOnce(d2, (byte) 4);
        displayOnce(d1, (byte) 2);
        displayOnce(d0, (byte) 1);
    }

    private void displayOnce(String str, byte index){
        Byte ledOut = LedMap.get(str);
        if (ledOut == null){
            return;
        }
        LED_OUT(ledOut);
        LED_OUT(index);
        RCLK.low();
        RCLK.high();
        sleep(2);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {}
    }



    private void LED_OUT(byte X) {
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

    public void destroy(){
        if (isRun){
            isRun = false;
            //关闭全部
            displayStr("OFF", "OFF", "OFF", "OFF", "OFF", "OFF", "OFF", "OFF");
            gpio.shutdown();
            gpio.unprovisionPin(DIO);
            gpio.unprovisionPin(SCLK);
            gpio.unprovisionPin(RCLK);
        }
    }
}
