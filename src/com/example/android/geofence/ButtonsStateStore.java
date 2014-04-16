package com.example.android.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class ButtonsStateStore {

	private final SharedPreferences mPrefs;
	
	
	public ButtonsStateStore(Context context){
		
		mPrefs=context.getSharedPreferences("ButtonValues", Context.MODE_PRIVATE);
	}
	
	public void setButtonState(String id,boolean value){
		
		Editor editor=mPrefs.edit();
		
		editor.putBoolean(id, value);
		editor.commit();
	}
	
	public boolean getButtonState(String id){
		
		return mPrefs.getBoolean(id, false);
	}
	
	public void clearButtonsState(){
		
		Editor editor=mPrefs.edit();
		editor.clear();
		editor.commit();
		Log.d("clearButtonState", "button states cleared");
	}
}
