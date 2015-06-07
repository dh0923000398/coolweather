package com.coolweather.app.util;

public interface HttpCallbackListener {

	void onError(Exception e);
	void onFinish(String response);
}
