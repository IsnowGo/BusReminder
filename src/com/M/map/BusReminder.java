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

	// �������
	private PoiSearch mSearch = null; // ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��
	// ������·��ѯ
	Button chooseFrom = null;
	Button chooseDestination = null;
	Button nowcity = null;
	Button start = null; // ������վ���ѡ�
	Button stopRemind = null;
	EditText busNumber;
	TextView text = null;
	TextView cityshow, fromshow, destshow;
	TextView nowStation, nowStationName;
	EditText limitSet;
	GetBusLine busLineSearch;
	// ����վ��
	public static ArrayList<String> busStations = new ArrayList<String>();
	public static ArrayList<String> busStops = new ArrayList<String>();
	private ArrayList<String> nearbyStations = new ArrayList<String>();
	private int maxSize = 501;
	private int vis[] = new int[maxSize];// ���ڱ�������У���Щ������վ�������˳��Ĺ���·���С�
	private int passed[] = new int[maxSize];// ���ڱ�Ǿ�����վ��
	private int maxDest;// ��¼����·������Զ��վ���ţ�����������վ���У�
	private int searchRadius = 200;
	private int fromNo = -1,fromNo1=-1; // ��ʾ��ʼվ���ڹ���·���еı��
	private int destNo = -1,destNo1=-1; // ��ʾĿ��վ���ڹ���·���еı�š�
	private int lastNo;// ��¼���һ�ξ����Ĺ���վ��
	private int nextNo;// ��һ��Ԥ��Ҫ�����վ��
	private int limits = 2;// ����Ŀ��վ�뵱ǰվ������ٵ�ʱ�����ѡ�
	private boolean isFromStation = false; // ���ڱ���Ƿ񵥻��İ�ť��ѡ����ʼվ
	private boolean locCity = false; // ���ڱ���Ƿ񵥻����Ƕ�λ��ǰ����
	/** �ֶ�����߳�ͬ�� */
	public static boolean getStations;
	// ��λ��أ�
	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor = "bd09ll";
	private LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;
	private boolean ischosen = false; // �û��Ƿ�ѡ����Ŀ��ص�
	private boolean remindStart = false;
	private boolean playing = false;// ��������Ƿ��ڲ���
	private String cityname="����", destname;
	String busNo = "119";
	// private Thread thread=new Thread(new moniter());
	private Vibrator mVibrator; // ��
	private MediaPlayer mMediaPlayer;

	Thread thread;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// ��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext
		// ע��÷���Ҫ��setContentView����֮ǰʵ��
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
		// ��λ��أ�
		mLocationClient = new LocationClient(this.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);

		// ��λ��ǰ����
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
		
//		mSearch.searchInCity(new PoiCitySearchOption().city("����").keyword("119"));
//		PoiNearbySearchOption option1=new PoiNearbySearchOption().keyword("����վ").location(new LatLng(36.667689, 117.143811)).radius(1000);
//		mSearch.searchNearby(option1);
	}

	/**
	 * ת������վѡ�����
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
	 * intent�ĵ�һ���࣬��MainActivityͨ����дonActivityResult��
	 * ����ȡ�ڶ�����StationsList�ķ���ֵ
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
				destshow.setText(busStations.get(num)); // վ��������1��ʼ��
			}
			for (int i = 0; i < maxSize; i++)
				passed[i] = 0;
	}

	protected void onDestroy() {
		super.onDestroy();
		// ��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
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
	 * ���������Ĺ���վ�㣬��ȡ�������
	 * �����ж���Щ�ڹ���·����
	 * ���ݺ����㷨��ѡ����һ�����վ��
	 */
	public void onGetPoiResult(PoiResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			searchRadius +=200;
			if (searchRadius >600 ) {
				searchRadius = 200;
			}
			Toast.makeText(BusReminder.this, "�������������Ĺ���վ�㣬��������",
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
			// Toast.makeText(BusReminder.this, "������",
			// Toast.LENGTH_SHORT).show();
			playMusic();
		}
	}

	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);// ���ö�λģʽ
		option.setCoorType(tempcoor);// ���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
		int span = 5000;
		option.setIsNeedAddress(true);
		option.setScanSpan(span);// ���÷���λ����ļ��ʱ��Ϊ5000ms
		mLocationClient.setLocOption(option);
	}

	/**
	 * ���ڲ�������ʱ�����ֺ���
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
					Toast.makeText(BusReminder.this, "������", Toast.LENGTH_SHORT)
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
						.keyword("����վ")
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
				// ��Ӫ����Ϣ
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
	 * ��λ��ǰ���ڳ���
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
				nowcity.setText("��λ��ǰ����");
				mLocationClient.stop();
			} else {
				nowcity.setText("ֹͣ��λ����");
				locCity = true;
				mLocationClient.start();

				/*
				 * �����������ֵ���ڵ���1000��ms��ʱ����λSDK�ڲ�ʹ�ö�ʱ��λģʽ������requestLocation(
				 * )��ÿ���趨��ʱ�䣬��λSDK�ͻ����һ�ζ�λ�������λSDK���ݶ�λ���ݷ���λ��û�з����仯���Ͳ��ᷢ����������
				 * ������һ�ζ�λ�Ľ�����������λ�øı䣬�ͽ�������������ж�λ���õ��µĶ�λ�����
				 * ��ʱ��λʱ������һ��requestLocation���ᶨʱ��������λ�����
				 */
				mLocationClient.requestLocation();
			}

		}
	}

	/**
	 * ������վ���ѹ���
	 * 
	 * @author xiasuochen
	 * 
	 */
	private class RemindListener implements OnClickListener {
		public void onClick(View v) {
			locCity = false;
			if (fromNo == -1 || destNo == -1) {
				Toast.makeText(BusReminder.this, "��ѡ�����ʼվ��Ŀ��վ",
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
					start.setText("��ʼ����");
					mLocationClient.stop();
				} else {
					start.setText("��ֹ����");
					
					busStops.clear();
					if (destNo < fromNo) {
						// ������·��վ���б�תһ��
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
					// remindStart=true; //��ʼ��վ��λʱ������Ϊtrue.
					/*
					 * �����������ֵ���ڵ���1000��ms��ʱ����λSDK�ڲ�ʹ�ö�ʱ��λģʽ������requestLocation(
					 * )��ÿ���趨��ʱ�䣬��λSDK�ͻ����һ�ζ�λ��
					 * �����λSDK���ݶ�λ���ݷ���λ��û�з����仯���Ͳ��ᷢ����������
					 * ������һ�ζ�λ�Ľ�����������λ�øı䣬�ͽ�������������ж�λ���õ��µĶ�λ�����
					 * ��ʱ��λʱ������һ��requestLocation���ᶨʱ��������λ�����
					 */
					mLocationClient.requestLocation();
				}
			}
		}
	}

	/**
	 * ѡ��Ŀ��վ��
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
				Toast.makeText(BusReminder.this, "�����빫������", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			Toast.makeText(BusReminder.this, "�����У����ĵȴ�", Toast.LENGTH_SHORT)
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
	 * ѡ����ʼվ��
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
				Toast.makeText(BusReminder.this, "�����빫������", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			Toast.makeText(BusReminder.this, "�����У����ĵȴ�", Toast.LENGTH_SHORT).show();
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
	 * ���߳������Ʋ������ֵ�ʱ�䳤��
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
//			// Toast.makeText(BusReminder.this, "������",
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