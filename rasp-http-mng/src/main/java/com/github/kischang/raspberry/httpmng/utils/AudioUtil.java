package com.github.kischang.raspberry.httpmng.utils;

import javazoom.jl.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.http.HttpUtils;
import org.tio.utils.hutool.ResourceUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 音频播放工具类
 * alsa tce
 *
 * @author KisChang
 * @date 2019-12-20
 */
public class AudioUtil {

    private static final Logger logger = LoggerFactory.getLogger(AudioUtil.class);

    /* window.open("http://tts.baidu.com/text2audio?lan=zh&ie=UTF-8&text="+encodeURI("系统启动中")) */
    //系统启动中
    public static final String startup = "startup";
    //系统已启动，请配置设备
    public static final String start_conf = "start_conf";
    //系统已启动，正在连接服务器
    public static final String start_connserv = "start_connserv";
    //连接服务器成功，系统启动完成
    public static final String start_ok = "start_ok";
    //连接网络失败，请重新配置
    public static final String start_netfail = "start_netfail";
    //连接服务器失败，请重新配置
    public static final String start_servfail = "start_servfail";
    //系统配置成功，正在重启
    public static final String start_confend = "start_confend";
    //系统已重置，正在重启
    public static final String reset = "reset";
    //系统已成功升级，重启设备即可运行新版程序
    public static final String upgrade_success = "upgrade_success";

    //系统已启动，请扫码绑定设备
    public static final String need_bind = "need_bind";
    //获取设备信息失败，请重启设备进行重试，如果一直提示此错误，请联系技术人员处理
    public static final String get_devinfo_err = "get_devinfo_err";
    //配置信息加载失败，系统已重置，请重新进行配置
    public static final String err_conf_load = "err_conf_load";
    //与服务器通讯失败，请确认您的配置无误，如果重复出现请联系技术人员处理
    public static final String err_exchange_server = "err_exchange_server";

    private static final Object lock = new Object();

    private static void playRun(InputStream in) {
        synchronized (lock) {   //全局阻塞播放
            Player player = null;
            try {
                player = new Player(in);
                player.play();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            } finally {
                closeInput(in);
                closePlayer(player);
            }
        }
    }

    public static void playInternal(String name) {
        System.out.println("audio_play: " + name);
        playRun(ResourceUtil.getResourceAsStream("classpath:sound" + File.separator + name + ".mp3"));
    }

    public static boolean playStrByBaidu(String str) {
        return playStr("http://tts.baidu.com/text2audio?lan=zh&ie=UTF-8&text=%s", str);
    }

    public static boolean playStr(String ttsUrlFormat, String str) {
        try {
            str = URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ignored) {
            return false;
        }
        try (InputStream in = HttpUtils
                .get(String.format(ttsUrlFormat, str))
                .body()
                .byteStream()) {
            playRun(in);
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return false;
    }

    private static void closePlayer(Player player) {
        if (player != null) {
            player.close();
        }
    }

    private static void closeInput(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        playStrByBaidu("系统启动中");
    }
}
