package com.github.kischang.raspberry.device.utils;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * 树莓派工具类
 *
 * @author KisChang
 * @version 1.0
 */
public class RaspberryPiUtils {

    public static interface KeyParse {
        void onPress();
    }

    public static void onPress(int ioAddr, KeyParse keyParse) {
        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalInput resetPin = gpio.provisionDigitalInputPin(
                RaspiPin.getPinByAddress(ioAddr), PinPullResistance.PULL_DOWN);
        resetPin.setShutdownOptions(true);
        resetPin.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                keyParse.onPress();
            }
        });
        if (gpio.getState(resetPin) == PinState.HIGH) { //已经按下了
            keyParse.onPress();
        }
    }

    public static GpioPinDigitalInput getKeyGpio(GpioController gpio, int address) {
        GpioPinDigitalInput inTemp = gpio.provisionDigitalInputPin(RaspiPin.getPinByAddress(address), PinPullResistance.PULL_UP);
        inTemp.setMode(PinMode.DIGITAL_INPUT);
        //消除抖动
        inTemp.setDebounce(50);
        return inTemp;
    }

    public static void initAllIn(GpioController gpio, int... diId) {
        for (int id : diId){
            RaspberryPiUtils.getKeyGpio(gpio, id);
        }
    }

    public static void closeAllOut(GpioController gpio, int... doId) {
        for (int id : doId){
            GpioPinDigitalOutput op = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(id));
            RaspberryPiUtils.close(op);
            RaspberryPiUtils.shut(gpio, op);
        }
    }

    /*---控制输出模式---*/
    public static void open(GpioPinDigitalOutput op) {
        if (getDoMode() == 0){
            op.high();
        }else {
            op.low();
        }
    }

    public static void close(GpioPinDigitalOutput op) {
        if (getDoMode() == 0){
            op.low();
        }else {
            op.high();
        }
    }

    //默认模式
    private static int DO_MODE = -1;
    private synchronized static int getDoMode() {
        return DO_MODE;
    }
    public static void setDoMode(int doMode) {
        DO_MODE = doMode;
    }

    public static void shut(GpioController gpio, GpioPinDigitalOutput op) {
        gpio.unprovisionPin(op);
    }

}
