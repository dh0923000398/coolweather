package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
public class ChooseAreaActivity extends Activity{
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	/**
	 * 市列表
	 */
	private List<City> cityList;
	/**
	 * 县列表
	 */
	private List<County> countieList;
	/**
	 * 选中的省份
	 */
	private Province selectProvince;
	/**
	 * 选中的城市
	 */
	private City selectCity;
	/**
	 * 县区
	 */
	private County selectCounty;
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if(currentLevel == LEVEL_PROVINCE){
					selectProvince = provinceList.get(index);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selectCity = cityList.get(index);
					queryCounties();
				}
			}

		});
		Log.i("CoolWeather", "准备加载省级数据................");
		queryProvinces();//加载省级数据
	}
	/**
	 * 查询省,优先从数据看查询,没有再去服务器上查询
	 */
	private void queryProvinces() {
		// TODO Auto-generated method stub
		Log.i("CoolWeather", "开始加载省级数据................");
		provinceList = coolWeatherDB.loadProvinces();
		Log.i("CoolWeather", "provinceList的长度:"+provinceList.size());
		if(provinceList.size() > 0){
			dataList.clear();
			for (Province province : provinceList) {
				//把省份的名字放进去
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else{
			Log.i("CoolWeather", "准备调用服务器方法查询省级信息................");
			queryFromServer(null,"province");
		}
		
	}
	
	/**
	 * 查询城市
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectProvince.getId());
		if(cityList.size() > 0){
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectProvince.getProvinceCode(), "city");
		}
	}
	/**
	 * 查询县区
	 */
	private void queryCounties() {
		Log.i("CoolWeather", "queryCounties(查询县区开始............");
		countieList = coolWeatherDB.loadCounties(selectCity.getId());
		if(countieList.size() > 0){
			dataList.clear();
			for (County county : countieList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else{
			queryFromServer(selectCity.getCityCode(), "county");
		}
	}
	private void queryFromServer(final String code, final String type) {
		Log.i("CoolWeather", "开始调用queryFromServer方法===================");
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city" + code +".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		Log.i("CoolWeather", "address==================="+address);
		//显示进度条
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				Log.i("CoolWeather", "开始发送请求方法................");
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvinceResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result = Utility.handleCityResponse(coolWeatherDB, response, selectProvince.getId());
				}else if("county".equals(type)){
					result = Utility.handCountyResponse(coolWeatherDB, response, selectCity.getId());
				}
				if(result){
					//通过runOnUIThread方法返回主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//g关闭进度条
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT);
					}
				});
			}
		});
	}
	/**
	 * 显示进度条
	 */
	private void showProgressDialog() {
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * 关闭进度条
	 */
	private void closeProgressDialog() {
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	/**
	 * 重写back时间
	 */
	@Override
	public void onBackPressed() {
		if(currentLevel == LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel == LEVEL_CITY){
			queryProvinces();
		}else {
			finish();
		}
	}
	
}
