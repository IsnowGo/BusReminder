package com.M.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.PoiInfo.POITYPE;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
/**@author find
 * ���൥��������ȡ������·����Ϊȫ�����У�������·ֻ��ȡһ��
 * */
public class GetBusLine implements OnGetBusLineSearchResultListener,OnGetPoiSearchResultListener{
	//����������·���
	private BusLineSearch mBusLineSearch=null;
	private BusLineResult route=null;
	private List<String> busLineIDList=null;
	
	//����poi���
	private PoiSearch mSearch=null;
	
	private Context context;
	private String city;
	
	/**����������
	 * @param city ��������
	 * @param keyword ������·������
	 * */
	public void start(Context context,String city,String keyword){
		mSearch=PoiSearch.newInstance();
		mSearch.setOnGetPoiSearchResultListener(this);
		mBusLineSearch=BusLineSearch.newInstance();
		mBusLineSearch.setOnGetBusLineSearchResultListener(this);
		busLineIDList=new ArrayList<String>();
		this.context=context;
		this.city=city;
		mSearch.searchInCity(new PoiCitySearchOption().city(city).keyword(keyword));
	}
	/**��ȡ������·����ϸ��Ϣ*/
	public void onGetBusLineResult(BusLineResult result) {
		if(result==null || result.error!=SearchResult.ERRORNO.NO_ERROR){
			Toast.makeText(context, "No answer", Toast.LENGTH_SHORT).show();
			return;
		}
		route=result;
		
		List<BusLineResult.BusStation>st=route.getStations();
		//����Ҫ��clear�����򹫽�·�߾ͻ��ۻ����б���
		BusReminder.busStations.clear();
		for(Iterator<BusLineResult.BusStation>i=st.iterator();i.hasNext();){
			BusReminder.busStations.add(i.next().getTitle());
		}
		BusReminder.getStations=true;
		
	}
	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**��ȡ������·��uid*/
	@Override
	public void onGetPoiResult(PoiResult result) {
		if(result==null||result.error!=SearchResult.ERRORNO.NO_ERROR){
			Toast.makeText(context, "No answer", Toast.LENGTH_SHORT).show();
			return;
		}
		busLineIDList.clear();
		for(PoiInfo poi:result.getAllPoi()){
			if(poi.type==POITYPE.BUS_LINE||poi.type==POITYPE.SUBWAY_LINE){
				busLineIDList.add(poi.uid);
			}
		}
		mBusLineSearch.searchBusLine(new BusLineSearchOption().city(city).uid(busLineIDList.get(0)));
	}
	
}
