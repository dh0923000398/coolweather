package com.coolweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import android.util.Log;

public class HttpUtil {
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
		new Thread(new Runnable() {
			public void run() {
				Log.i("CoolWeather", "进入HttpUtil中............");
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setReadTimeout(8000);
					connection.setConnectTimeout(8000);
					InputStream inputStream = connection.getInputStream();
					//l流转换,字节转字符
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					StringBuilder response = new StringBuilder();
					while((line = reader.readLine()) != null){
						response.append(line);
					}
					Log.i("CoolWeather", "查询的数据............"+response.toString());
					if(listener != null){
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					if(listener != null){
						listener.onError(e);
					}
				}finally{
					if(connection != null){
						connection.disconnect();
					}
				}
			}
		}).start();
	}
}
