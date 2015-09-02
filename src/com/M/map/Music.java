package com.M.map;

public class Music {
	private String music;
	private String way;
	private String time;
	int musics;
	int ways;
	int times;
	public static String[] strdate = new String[3];
	public void init(){
		music=MusicSetting.music[2];
		musics=2;
		way=MusicSetting.way[1];
		ways=1;
		time=MusicSetting.time[0];
		times=MusicSetting.timeSecond[0]*1000;
		
	}
	public String getMusic() {
		return music;
	}

	public void setMusic(String music, int musics) {
		this.music = music;
		this.musics = musics;
	}

	public String getWay() {
		return way;
	}

	public void setWay(String way, int ways) {
		this.way = way;
		this.ways = ways;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time, int times) {
		this.time = time;
		this.times = MusicSetting.timeSecond[times]*1000;
	}
}
