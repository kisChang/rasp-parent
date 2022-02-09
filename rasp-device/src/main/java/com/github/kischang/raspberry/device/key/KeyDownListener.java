package com.github.kischang.raspberry.device.key;

import com.pi4j.io.gpio.PinState;

/**
 * 树莓派按键监听空实现
 *
 * @author KisChang
 * @version 1.0
 */
public class KeyDownListener extends AbstractKeyDownListener {

    public KeyDownListener() {
    }

    public KeyDownListener(long delay) {
        super(delay);
    }

    public KeyDownListener(long delay, PinState defState) {
        super(delay, defState);
    }

    @Override
    protected void onKeyDown() {

    }

    @Override
    protected void onKeyUp() {

    }

    @Override
    public void onClick() {

    }

    @Override
    public void onLongClick() {

    }

}
