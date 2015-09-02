package com.M.map;

import java.util.ArrayList;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BusReminder extends Activity implements
		OnGetPoiSearchResultListener {

	// 搜索相关
	private PoiSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	// 公交线路查询
	Button chooseFrom = null;
	Button chooseDestination = null;
	Button nowcity = null;
	Button start = null; // 开启到站提醒。
	Button stopRemind = null;
	EditText busNumber;
	TextView text = null;
	TextView cityshow, fromshow, destshow;
	TextView nowStation, nowStationName;
	EditText limitSet;
	GetBusLine busLineSearch;
	// 公交站点
	public static ArrayList<String> busStations = new ArrayList<String>();
	public static ArrayList<String> busStops = new ArrayList<String>();
	private ArrayList<String> nearbyStations = new ArrayList<String>();
	private int maxSize = 501;
	private int vis[] = new int[maxSize];// 用于标记搜索中，哪些附近的站点在所乘车的公交路线中。
	private int passed[] = new int[maxSize];// 用于标记经过的站点
	private int maxDest;// 记录公交路线中最远的站点编号（能搜索到的站点中）
	private int searchRadius = 200;
	private int fromNo = -1,fromNo1=-1; // 表示起始站点在公交路线中的编号
	private int destNo = -1,destNo1=-1; // 表示目标站点在公交路线中的编号。
	private int lastNo;// 记录最近一次经过的公交站点
	private int nextNo;// 下一次预计要到达的站点
	private int limits = 2;// 用于目的站与当前站距离多少的时候提醒。
	private boolean isFromStation = false; // 用于标记是否单击的按钮是选择起始站
	private boolean locCity = false; // 用于标记是否单击的是定位当前城市
	/** 手动检测线程同步 */
	public static boolean getStations;
	// 定位相关：
	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor = "bd09ll";
	private LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;
	private boolean ischosen = false; // 用户是否选择了目标地点
	private boolean remindStart = false;
	private boolean playing = false;// 标记音乐是否在播放
	private String cityname="济南", destname;
	String busNo = "119";
	// private Thread thread=new Thread(new moniter());
	private Vibrator mVibrator; // 震动
	private MediaPlayer mMediaPlayer;

	Thread thread;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		// SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_busreminder);

		mSearch = PoiSearch.newInstance();
		mSearch.setOnGetPoiSearchResultListener(this);

		mVibrator = (Vibrator) getApplicationContext().getSystemService(
				Service.VIBRATOR_SERVICE);
		mMediaPlayer = new MediaPlayer();

		busLineSearch = new GetBusLine();

		text = (TextView) findViewById(R.id.textView1);
		getStations = false;
		cityshow = (TextView) findViewById(R.id.cityshow);
		destshow = (TextView) findViewById(R.id.destshow);
		fromshow = (TextView) findViewById(R.id.fromshow);
		nowStation = (TextView) findViewById(R.id.nowStation);
		nowStationName = (TextView) findViewById(R.id.nowStationName);
		busNumber = (EditText) findViewById(R.id.busNumber);
		limitSet=(EditText)findViewById(R.id.limitsValue);
		// busNumber.setText("119");
		// 定位相关：
		mLocationClient = new LocationClient(this.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);

		// 定位当前城市
		nowcity = (Button) findViewById(R.id.nowcity);
		nowcity.setOnClickListener(new CurrentCityListener());

		start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new RemindListener());

		chooseDestination = (Button) findViewById(R.id.chooseDestination);
		chooseDestination.setOnClickListener(new ChooseDestListener());

		chooseFrom = (Button) findViewById(R.id.chooseFrom);
		chooseFrom.setOnClickListener(new ChooseFromListener());

		stopRemind = (Button) findViewById(R.id.stopRemind);
		stopRemind.setOnClickListener(new StopRemindListener());
		
//		mSearch.searchInCity(new PoiCitySearchOption().city("济南").keyword("119"));
//		PoiNearbySearchOption option1=new PoiNearbySearchOption().keyword("公交站").location(new LatLng(36.667689, 117.143811)).radius(1000);
//		mSearch.searchNearby(option1);
	}

	/**
	 * 转到公交站选择界面
	 */
	public void startline() {
		Intent intent = new Intent(BusReminder.this, StationsList.class);
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("list", busStations);
		intent.putExtras(bundle);
		BusReminder.this.startActivityForResult(intent, 1);
	}

	@Override
	/**
	 * intent的第一个类，即MainActivity通过重写onActivityResult，
	 * 来获取第二个类StationsList的返回值
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
			super.onActivityResult(requestCode, resultCode, data);
//System.out.println(resultCode + data.getStringExtra("theIdselected"));
//			text.setText("" + resultCode);
			String res = data.getStringExtra("theIdSelected");
//			text.append(res + "\n");
//			text.append("size:" + busStations.size());
			int num = Integer.parseInt(res);
			if (isFromStation) {
				fromNo = num;
				fromshow.setText(busStations.get(num));
				nowStationName.setText(busStations.get(num));
			} else {
				destNo = num;
				destshow.setText(busStations.get(num)); // 站的索引从1开始的
			}
			for (int i = 0; i < maxSize; i++)
				passed[i] = 0;
	}

	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
	}

	protected void onStop() {
		super.onStop();
		mLocationClient.stop();
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	/**
	 * 搜索附近的公交站点，获取搜索结果
	 * 并且判断哪些在公交路线上
	 * 根据合理算法，选择下一个最佳站点
	 */
	public void onGetPoiResult(PoiResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			searchRadius +=200;
			if (searchRadius >600 ) {
				searchRadius = 200;
			}
			Toast.makeText(BusReminder.this, "搜索不到附近的公交站点，继续搜索",
					Toast.LENGTH_SHORT).show();
			// System.out.println("no result");
			return;
		}
		nearbyStations.clear();
		for (int i = 0; i < maxSize; i++)
			vis[i] = 0;
		for (PoiInfo poi : result.getAllPoi()) {
			if (poi.type == PoiInfo.POITYPE.BUS_STATION) {
				nearbyStations.add(poi.name);
				// System.out.println(poi.name+"name");
			}
		}
		stationJudge();

	}

	public void stationJudge() {
		int m = nearbyStations.size();
		int n = busStops.size();
		maxDest = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (nearbyStations.get(i).equals(busStops.get(j))) {
					vis[j] = 1;
					maxDest = Math.max(maxDest, j);
					break;
				}
			}
		}
		if (vis[nextNo] == 1) {
			searchRadius = 200;
			lastNo = nextNo;
			nextNo++;
			nowStationName.setText(busStops.get(lastNo));
		} else {
			searchRadius += 200;
			if (searchRadius >600) {
				searchRadius = 200;
			}
		}
		if (destNo1 - lastNo <= limits) {
			// testdebug.append(""+destNo+" "+lastNo+";");
			// mVibrator.vibrate(5000);
			// Toast.makeText(BusReminder.this, "震动提醒",
			// Toast.LENGTH_SHORT).show();
			playMusic();
		}
	}

	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);// 设置定位模式
		option.setCoorType(tempcoor);// 返回的定位结果是百度经纬度，默认值gcj02
		int span = 5000;
		option.setIsNeedAddress(true);
		option.setScanSpan(span);// 设置发起定位请求的间隔时间为5000ms
		mLocationClient.setLocOption(option);
	}

	/**
	 * 用于播放提醒时的音乐和震动
	 */
	private void playMusic() {
		try {
			int position1 = MainActivity.music.musics;
			if (position1 == 0)
				mMediaPlayer = MediaPlayer.create(BusReminder.this, R.raw.big);
			if (position1 == 1)
				mMediaPlayer = MediaPlayer.create(BusReminder.this,
						R.raw.clubbin);
			if (position1 == 2)
				mMediaPlayer = MediaPlayer.create(BusReminder.this,
						R.raw.fallingstar);
			if (position1 == 3)
				mMediaPlayer = MediaPlayer.create(BusReminder.this,
						R.raw.onlyhuman);
			if (position1 == 4)
				mMediaPlayer = MediaPlayer.create(BusReminder.this,
						R.raw.whiteflag);
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			}
			if (!playing) {
				playing = true;
				mMediaPlayer.prepare();
				int timeLength = MainActivity.music.times;
				if (MainActivity.music.ways != 2) {
					mMediaPlayer.start();
				}
				if (MainActivity.music.ways == 1) {
					mVibrator.vibrate(timeLength);
					Toast.makeText(BusReminder.this, "震动提醒", Toast.LENGTH_SHORT)
							.show();
				}
				// thread=new Thread(new Player());
				// thread.start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//System.out.println("error");
		}
	}

	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// Receive Location
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			System.out.println(location.getLatitude());
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			System.out.println(location.getLongitude());
			sb.append(location.getLongitude());

			if (!locCity) {
				PoiNearbySearchOption option1 = new PoiNearbySearchOption()
						.keyword("公交站")
						.location(
								new LatLng(location.getLatitude(), location
										.getLongitude())).radius(searchRadius);
				mSearch.searchNearby(option1);
			}

//			sb.append("\nradius : ");
//			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
//				sb.append("\nsatellite : ");
//				sb.append(location.getSatelliteNumber());
				sb.append("\ndirection : ");
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				// 运营商信息
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
			}
			if(location.getCity()!=null){
			cityname=location.getCity();
			cityshow.setText(location.getCity());
			}
			sb.append(location.getStreet() + location.getStreetNumber());
			text.setText("");
			text.append(sb.toString());
			Log.i("BaiduLocationApiDem", sb.toString());
		}
	}

	/**
	 * 定位当前所在城市
	 * 
	 * @author xiasuochen
	 * 
	 */
	private class CurrentCityListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			InitLocation();
			if (mLocationClient == null) {
				return;
			}
			if (mLocationClient.isStarted()) {
				nowcity.setText("定位当前城市");
				mLocationClient.stop();
			} else {
				nowcity.setText("停止定位城市");
				locCity = true;
				mLocationClient.start();

				/*
				 * 当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。调用requestLocation(
				 * )后，每隔设定的时间，定位SDK就会进行一次定位。如果定位SDK根据定位依据发现位置没有发生变化，就不会发起网络请求，
				 * 返回上一次定位的结果；如果发现位置改变，就进行网络请求进行定位，得到新的定位结果。
				 * 定时定位时，调用一次requestLocation，会定时监听到定位结果。
				 */
				mLocationClient.requestLocation();
			}

		}
	}

	/**
	 * 开启到站提醒功能
	 * 
	 * @author xiasuochen
	 * 
	 */
	private class RemindListener implements OnClickListener {
		public void onClick(View v) {
			locCity = false;
			if (fromNo == -1 || destNo == -1) {
				Toast.makeText(BusReminder.this, "请选择好起始站和目标站",
						Toast.LENGTH_SHORT).show();
			} else {
				String str=limitSet.getText().toString();
				if(!str.equals("")){
					limits=Integer.parseInt(str);
					if(limits<0)
						limits=0;
				}
				InitLocation();
				if (mLocationClient == null) {
					return;
				}
				if (mLocationClient.isStarted()) {
					start.setText("开始提醒");
					mLocationClient.stop();
				} else {
					start.setText("终止提醒");
					
					busStops.clear();
					if (destNo < fromNo) {
						// 将公交路线站点列表倒转一下
						destNo1 = busStations.size() - destNo-1;
						fromNo1 = busStations.size() - fromNo-1;
						int size = busStations.size();
						for (int i = size - 1; i >= 0; i--) {
							busStops.add(busStations.get(i));
						}
					} else {
						destNo1=destNo;
						fromNo1=fromNo;
						busStops = busStations;
					}
					lastNo = fromNo1;
					nextNo = fromNo1 + 1;
					nowStationName.setText(busStops.get(lastNo));
					mLocationClient.start();
					// remindStart=true; //开始到站定位时，设置为true.
					/*
					 * 当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。调用requestLocation(
					 * )后，每隔设定的时间，定位SDK就会进行一次定位。
					 * 如果定位SDK根据定位依据发现位置没有发生变化，就不会发起网络请求，
					 * 返回上一次定位的结果；如果发现位置改变，就进行网络请求进行定位，得到新的定位结果。
					 * 定时定位时，调用一次requestLocation，会定时监听到定位结果。
					 */
					mLocationClient.requestLocation();
				}
			}
		}
	}

	/**
	 * 选择目的站点
	 * 
	 * @author xiasuochen
	 * 
	 */
	private class ChooseDestListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// busLineSearch=new GetBusLine();
			busNo = busNumber.getText().toString();
			if (busNo.equals("")) {
				Toast.makeText(BusReminder.this, "请输入公交车号", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			Toast.makeText(BusReminder.this, "加载中，耐心等待", Toast.LENGTH_SHORT)
					.show();
			busLineSearch.start(BusReminder.this, cityname, busNo);
			// while(!MainActivity.getStations){SystemClock.sleep(500);};
			// if(MainActivity.getStations==true){
			isFromStation = false;
			new Thread(new moniter()).start();
			// }else{System.out.println("still");}
		}
	}

	/**
	 * 选择起始站点
	 * 
	 * @author xiasuochen
	 * 
	 */
	private class ChooseFromListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// busLineSearch=new GetBusLine();
			busNo = busNumber.getText().toString();
			if (busNo.equals("")) {
				Toast.makeText(BusReminder.this, "请输入公交车号", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			Toast.makeText(BusReminder.this, "加载中，耐心等待", Toast.LENGTH_SHORT).show();
			busLineSearch.start(BusReminder.this, cityname, busNo);
			// while(!MainActivity.getStations){SystemClock.sleep(500);};
			// if(MainActivity.getStations==true){
			isFromStation = true;

			new Thread(new moniter()).start();
			// }else{System.out.println("still");}
		}
	}

	private class StopRemindListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mVibrator.cancel();
			playing = false;
		}

	}

	/**
	 * 用线程来控制播放音乐的时间长度
	 * 
	 * @author xiasuochen
	 * 
	 */
//	private class Player implements Runnable {
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			int timeLength = MainActivity.music.times;
//			// if(MainActivity.music.ways!=2){
//			// mMediaPlayer.start();
//			// }
//			// if(MainActivity.music.ways==1){
//			// mVibrator.vibrate(timeLength);
//			// Toast.makeText(BusReminder.this, "震动提醒",
//			// Toast.LENGTH_SHORT).show();
//			// }
//			try {
//				Thread.sleep(timeLength);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			mMediaPlayer.stop();
//			mMediaPlayer.release();
//			mVibrator.cancel();
//			playing = false;
//
//		}
//
//	}

	private class moniter implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!BusReminder.getStations) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			startline();
		}

	}

}