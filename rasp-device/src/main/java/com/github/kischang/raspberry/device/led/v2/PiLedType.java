package com.github.kischang.raspberry.device.led.v2;

/**
 * @author KisChang
 */
public enum PiLedType {

    led4(4)
    ,led8(8)
    ;
    private int value;

    PiLedType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
