package com.github.kischang.raspberry.httpmng;

import com.github.kischang.raspberry.httpmng.init.HttpServerInit;
import org.tio.utils.jfinal.P;

public class HttpServerShowcaseStarter {

	public static void main(String[] args) throws Exception {
		P.use("app.properties");
		HttpServerInit.init();
	}

}
