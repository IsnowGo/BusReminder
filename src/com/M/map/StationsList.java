package com.M.map;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
/**This is used to show the bus  stations on  this line*/
public class StationsList extends ListActivity{
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle receiveBundle=this.getIntent().getExtras();
		ArrayList<String> strs=receiveBundle.getStringArrayList("list");
		ArrayAdapter<String> ada=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,strs);
		setListAdapter(ada);
		BusReminder.getStations=false;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Intent send=new Intent();
		send.putExtra("theIdSelected",""+id);
		StationsList.this.setResult(1, send);
		StationsList.this.finish();
	}
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Toast.makeText(StationsList.this, "您必须选择一个站点", Toast.LENGTH_SHORT).show();
			return false;
		}  
		return  super.onKeyDown(keyCode, event);     

	} 
}
