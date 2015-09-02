package com.M.map;

import com.baidu.mapapi.SDKInitializer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity{
	private Button remind,musicset,exit;
	private Class targetClass=null;
	public static Music music;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 主Activity中，使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.main_activity);
		remind=(Button)findViewById(R.id.reminder);
		musicset=(Button)findViewById(R.id.music);
		exit=(Button)findViewById(R.id.exit);
		music=new Music();
		music.init();
	}
	
	public void onStart(){
		super.onStart();
		remind.setOnClickListener(new RemindListener());
		musicset.setOnClickListener(new MusicListener());
		exit.setOnClickListener(new ExitListener());
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
	protected void onStop(){
		super.onStop();
	}
	private class RemindListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			targetClass =BusReminder.class;
			if(targetClass!=null){
				Intent intent=new Intent(MainActivity.this,targetClass);
				startActivity(intent);
			}
		}
		
	}
	private class MusicListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			targetClass =MusicSetting.class;
			if(targetClass!=null){
				Intent intent=new Intent(MainActivity.this,targetClass);
				startActivity(intent);
			}
		}
	}
	private class ExitListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			System.exit(0);
		}
		
	}
}
