package com.M.map;

import java.io.FileWriter;
import java.io.IOException;
import com.M.map.R;
import android.net.Uri;
import android.os.Bundle;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.app.Activity;
import android.content.Context;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.*;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MusicSetting extends Activity {
	public Music data = new Music();
	Button save;
//	Button stop;
	Button start;
	Spinner spinner1;
	Spinner spinner2;
	Spinner spinner3;
	private ArrayAdapter<String> adapter1;
	private ArrayAdapter<String> adapter2;
	private ArrayAdapter<String> adapter3;
	public MediaPlayer mMediaPlayer = null;
	private boolean flag=false;
	public static int position1 = 0;
	public static int position2 = 0;
	public static int position3 = 0;

	static final String[] way = { "响铃", "响铃+震动", "震动" };
	static final String[] time = { "15秒","30秒", "1分钟", "2分钟", "5分钟", "10分钟",
			 "20分钟" };
	static final String[] music = { "big", "clubbin", "fallingstar",
			"onlyhuman", "whiteflag" };
	static final int[] timeSecond={15,30,60,120,300,600,1200};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_musicsetting);
		// 初始化
		mMediaPlayer = new MediaPlayer();
		data.setMusic(music[0],0);
		data.setWay(way[0],0);
		data.setTime(time[0],0);
		spinner1=(Spinner)findViewById(R.id.spinner1);
		spinner2=(Spinner)findViewById(R.id.spinner2);
		spinner3=(Spinner)findViewById(R.id.spinner3);
		save = (Button) findViewById(R.id.save);
//		stop = (Button) findViewById(R.id.stopplay);
		start = (Button) findViewById(R.id.startplay);

		// 将可选内容与ArrayAdapter连接起来
		adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, music);
		adapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, way);
		adapter3 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, time);
		
		// 设置下拉列表的风格
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		spinner1.setAdapter(adapter1);
		spinner2.setAdapter(adapter2);
		spinner3.setAdapter(adapter3);
		
		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				data.setMusic(music[position],position);
				//playMusic("" + music[position]);
				position1 = position;
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});
		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				data.setWay(way[position],position);
				position2 = position;
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});
		spinner3.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				data.setTime(time[position],position);
				position3 = position;
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});
		
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(),
						"您选择的提醒音乐：" + music[position1], Toast.LENGTH_SHORT)
						.show();
				Toast.makeText(getApplicationContext(),
						"您选择的提醒方式：" + way[position2], Toast.LENGTH_SHORT)
						.show();
				Toast.makeText(getApplicationContext(),
						"您选择的提醒时间：" + time[position3], Toast.LENGTH_SHORT)
						.show();
				MainActivity.music=data;
				// System.out.println(way[position1]);
				// System.out.println(time[position2]);
				// System.out.println(music[position3]);
			}
		});
//		stop.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				stopMusic();
//			}
//		});
		start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(flag==false){
					flag=true;
					start.setText("停止播放");
					playMusic(music[position3]);
				}
				else{
					stopMusic();
					flag=false;
					start.setText("试听播放");
				}
			}
		});

		// 设置默认值
		spinner1.setVisibility(View.VISIBLE);
		spinner2.setVisibility(View.VISIBLE);
		spinner3.setVisibility(View.VISIBLE);
	}

	private void playMusic(String path) {
		try {

			if (position1 == 0)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this, R.raw.big);
			if (position1 == 1)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this,
						R.raw.clubbin);
			if (position1 == 2)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this,
						R.raw.fallingstar);
			if (position1 == 3)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this,
						R.raw.onlyhuman);
			if (position1 == 4)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this,
						R.raw.whiteflag);
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			}
			if(mMediaPlayer.isPlaying()){
				mMediaPlayer.stop();
			}
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error");
		}
	}

	private void stopMusic() {
		try{
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}
			//finish();
		}
		catch(Exception e){
			
		}
		
	}
}