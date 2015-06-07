package com.coolweather.app.util;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import android.R.bool;
import android.text.TextUtils;

public class Utility {
	/**
	 * 
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB,String response){
		if(!TextUtils.isEmpty(response)){
			//把省份分割开
			String[] allProvince = response.split(",");
			if(allProvince != null && allProvince.length > 0){
				for (String p : allProvince) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					//将解析出来的数据存储到Provice表总
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB,String response,
			int provinceId){
		if(!TextUtils.isEmpty(response)){
			//把城市分割开
			String[] allCitys = response.split(",");
			if(allCitys != null && allCitys.length > 0){
				for (String c : allCitys) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					//将解析出来的数据存储到Provice表总
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * 把县城分割开
	 * @param coolWeatherDB
	 * @param response
	 * @param cityId
	 * @return
	 */
	public synchronized static boolean handCountyResponse(CoolWeatherDB coolWeatherDB,String response,
			int cityId){
		if(!TextUtils.isEmpty(response)){
			//分割
			String[] allCounties = response.split(",");
			//判断allcountied
			if(allCounties != null && allCounties.length > 0){
				for (String c : allCounties) {
					//对每个县区进行切割
					String[] array = c.split("\\|");
					County county = new County();
					county.setCityId(cityId);
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
