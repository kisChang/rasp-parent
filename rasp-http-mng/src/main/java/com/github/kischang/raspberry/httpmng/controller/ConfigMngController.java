package com.github.kischang.raspberry.httpmng.controller;

import com.github.kischang.raspberry.httpmng.model.ConfigInfo;
import com.github.kischang.raspberry.httpmng.utils.AudioUtil;
import com.github.kischang.raspberry.utils.RaspCmdUtils;
import com.github.kischang.raspberry.utils.RaspWiFiUtils;
import com.github.kischang.raspberry.utils.model.WiFiInfo;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.util.Resps;
import org.tio.utils.resp.RespVo;

import java.util.List;

@RequestPath
public class ConfigMngController {

	@RequestPath(value = "/404")
	public HttpResponse page404(HttpRequest request) throws Exception {
//		return Resps.html(request, "自定义的404");
		//访问到404的，就尝试回首页
		return Resps.redirect(request, "/");
	}

	@RequestPath(value = "/500")
	public HttpResponse page500(HttpRequest request) throws Exception {
		return Resps.html(request, "<div style='text-align: center;'>服务器运行异常，请稍后重试！</div>");
	}

	//自动登录的页面，会访问一个http://www.msftconnecttest.com/redirect，直接重定向到首页
	@RequestPath(value = "/redirect")
	public HttpResponse redirect(HttpRequest request) throws Exception {
		return Resps.redirect(request, "/");
	}

	/*基础功能*/

	//扫描wifi的结果
	@RequestPath(value = "/config/wifi_scan")
	public HttpResponse wifi_scan(HttpRequest request) throws Exception {
		List<WiFiInfo> wifiList = RaspWiFiUtils.scanWiFi();
		return Resps.json(request, RespVo.ok().msg("加载成功！").data(wifiList));
	}
	//保存配置
	@RequestPath(value = "/config/submit")
	public HttpResponse config_submit(ConfigInfo configInfo, HttpRequest request) throws Exception {
		System.out.println(configInfo);
		//TODO dev code
		/*AppStarter.conf.setProperty("conf_wifi_ssid", configInfo.getWifi_ssid());
		AppStarter.conf.setProperty("conf_wifi_pw", configInfo.getWifi_pw());
		AppStarter.conf.setProperty("conf_WorkServer", configInfo.getWorkServer());
		AppStarter.conf.setProperty("conf_WorkPort", configInfo.getWorkPort());
		AppStarter.conf.setProperty("is_run", true);
		//更新存储
		try (FileWriter fileWriter = new FileWriter(AppStarter.confFile)){
			AppStarter.conf.getLayout().save(AppStarter.conf, fileWriter);
		}
		*/

		/*
		if ("Client".equalsIgnoreCase(wifiSetting.getWifiMode())) {
            // 清空
            RaspCmdUtils.runCmdOnce("echo 'country=CN\n' > " + ManagerApp.WPACONF_PATH);
            // 连接至wifi
            RaspWiFiUtils.connWiFiBySys(
                    wifiSetting.getWifiSsid()
                    , wifiSetting.getWifiPw()
                    , ManagerApp.WPACONF_PATH);
            ManagerApp.disableApMode();
        }else {
            // 启用AP模式
            ManagerApp.enableApMode();
        }
		*/

		if (configInfo.isReboot()){
			AudioUtil.playInternal(AudioUtil.start_confend);
			new Thread(() -> {
				//稍等一下
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ignored) {}
				//重启
				RaspCmdUtils.runCmdOnce("reboot");
			}).start();
		}
		return Resps.json(request, RespVo.ok().msg("操作成功！"));
	}

}
