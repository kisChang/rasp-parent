package com.github.kischang.raspberry.device.key;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * 树莓派按键监听封装
 *
 * @author KisChang
 * @version 1.0
 */
public abstract class AbstractKeyDownListener implements GpioPinListenerDigital {

    //默认的输入Pin状态
    private PinState defState = PinState.HIGH;
    //默认长按延时(ms)
    private long delay = 300;

    //监听线程运行
    private boolean RUN = true;

    private boolean keyDown = false;
    private long keyDownTime = -1L;

    public AbstractKeyDownListener() {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                RUN = false;
            }
        });
        //长按监听
        new Thread(){
            @Override
            public void run() {
                while (RUN){
                    testLongClick();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                }
            }
        }.start();
    }

    public AbstractKeyDownListener(long delay) {
        this();
        this.delay = delay;
    }

    public AbstractKeyDownListener(final long delay, PinState defState) {
        this();
        this.defState = defState;
        this.delay = delay;
    }

    //测试长按
    private void testLongClick() {
        if (keyDown){
            if ( (System.currentTimeMillis() - keyDownTime) > delay ){
                clearKeyDownState();
                onLongClick();
            }
        }
    }

    private void clearKeyDownState() {
        keyDown = false;
        keyDownTime = -1L;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        PinState nowState = event.getState();
        if (nowState.getValue() != defState.getValue()){
            //按键按下
            onKeyDown();
            keyDown = true;
            keyDownTime = System.currentTimeMillis();
        }else {
            //按键抬起
            onKeyUp();
            if (keyDown){
                if ( (System.currentTimeMillis() - keyDownTime) > delay ){
                    clearKeyDownState();
                    onLongClick();
                }else {
                    onClick();
                }
            }
            clearKeyDownState();
        }

    }

    protected abstract void onKeyDown();

    protected abstract void onKeyUp();


    public abstract void onClick();

    public abstract void onLongClick();

}
