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
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	/**
	 * ���б�
	 */
	private List<City> cityList;
	/**
	 * ���б�
	 */
	private List<County> countieList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectProvince;
	/**
	 * ѡ�еĳ���
	 */
	private City selectCity;
	/**
	 * ����
	 */
	private County selectCounty;
	/**
	 * ��ǰѡ�еļ���
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
		Log.i("CoolWeather", "׼������ʡ������................");
		queryProvinces();//����ʡ������
	}
	/**
	 * ��ѯʡ,���ȴ����ݿ���ѯ,û����ȥ�������ϲ�ѯ
	 */
	private void queryProvinces() {
		// TODO Auto-generated method stub
		Log.i("CoolWeather", "��ʼ����ʡ������................");
		provinceList = coolWeatherDB.loadProvinces();
		Log.i("CoolWeather", "provinceList�ĳ���:"+provinceList.size());
		if(provinceList.size() > 0){
			dataList.clear();
			for (Province province : provinceList) {
				//��ʡ�ݵ����ַŽ�ȥ
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		}else{
			Log.i("CoolWeather", "׼�����÷�����������ѯʡ����Ϣ................");
			queryFromServer(null,"province");
		}
		
	}
	
	/**
	 * ��ѯ����
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
	 * ��ѯ����
	 */
	private void queryCounties() {
		Log.i("CoolWeather", "queryCounties(��ѯ������ʼ............");
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
		Log.i("CoolWeather", "��ʼ����queryFromServer����===================");
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city" + code +".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		Log.i("CoolWeather", "address==================="+address);
		//��ʾ������
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				Log.i("CoolWeather", "��ʼ�������󷽷�................");
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvinceResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result = Utility.handleCityResponse(coolWeatherDB, response, selectProvince.getId());
				}else if("county".equals(type)){
					result = Utility.handCountyResponse(coolWeatherDB, response, selectCity.getId());
				}
				if(result){
					//ͨ��runOnUIThread�����������̴߳����߼�
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//g�رս�����
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
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT);
					}
				});
			}
		});
	}
	/**
	 * ��ʾ������
	 */
	private void showProgressDialog() {
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * �رս�����
	 */
	private void closeProgressDialog() {
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	/**
	 * ��дbackʱ��
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
