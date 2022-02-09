package com.github.kischang.raspberry.device.led;

import com.pi4j.io.gpio.*;

/**
 * @author KisChang
 */
public abstract class PiLedAbstract {

   protected GpioPinDigitalOutput DIO;
   protected GpioPinDigitalOutput RCLK;
   protected GpioPinDigitalOutput SCLK;
   protected GpioController gpio;

    public PiLedAbstract(GpioController gpio, int dio, int rclk, int sclk) {
        this(gpio
                , RaspiPin.getPinByAddress(dio)
                , RaspiPin.getPinByAddress(rclk)
                , RaspiPin.getPinByAddress(sclk)
                );
    }

    public PiLedAbstract(GpioController gpio, Pin dio, Pin rclk, Pin sclk) {
        this.gpio = gpio;
        this.DIO =  this.gpio.provisionDigitalOutputPin(dio, PinState.HIGH);
        this.RCLK = this.gpio.provisionDigitalOutputPin(rclk, PinState.HIGH);
        this.SCLK = this.gpio.provisionDigitalOutputPin(sclk, PinState.HIGH);
    }

    //默认输出位置输出
    public abstract void display(String ledStr);

    public abstract void display(int index, String ledStr);

    public abstract void destroy();
}
