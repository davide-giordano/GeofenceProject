package com.example.android.geofence;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Build;

public class ManualInsertActivity extends Activity {
	
	private EditText mLatitude1;
	private EditText mLongitude1;
	private EditText mRadius1;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual_insert);

		mLatitude1=(EditText)findViewById(R.id.value_latitude_1);
		mLongitude1=(EditText)findViewById(R.id.value_longitude_1);
		mRadius1=(EditText)findViewById(R.id.value_radius_1);
		
	}
	
	/***************************UI BUTTON LISTENER***************************************/
	
	public void onAddGeofenceClicked(View v){
		
		if(!checkInputFields()){
    		Log.e("onAddGeofenceClicked", "incorrect input, try again");
    		mLatitude1.setText(GeofenceUtils.EMPTY_STRING);
        	mLongitude1.setText(GeofenceUtils.EMPTY_STRING);
        	mRadius1.setText(GeofenceUtils.EMPTY_STRING);
    		Toast toast=Toast.makeText(this, R.string.invalid_geofence, Toast.LENGTH_LONG);
    		toast.show();
    	}
		else{
			
			double lat1=Double.valueOf(mLatitude1.getText().toString());
			double lng1=Double.valueOf(mLongitude1.getText().toString());
			float rad1=Float.valueOf(mRadius1.getText().toString());
			
			//prepare intent with extra data
				//broadcast it to main activity
			
			Intent broadcastIntent=new Intent();
			
			Log.d("onAddGeofenceClicked", "preparing intent");
			
			broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_INSERTED)
						   .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
						   .putExtra(GeofenceUtils.EXTRA_GEOFENCE_LATITUDE, lat1)
						   .putExtra(GeofenceUtils.EXTRA_GEOFENCE_LONGITUDE, lng1)
						   .putExtra(GeofenceUtils.EXTRA_GEOFENCE_RADIUS, rad1);
			
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
			
			//closes activity
			this.finish();
		}
	}
	
	
	/***************************CHECK INPUT VALUES****************************************/
    private boolean checkInputFields(){
    	
    	boolean inputOK=true;
    	
    	//check for empty fields
    	
    	if(TextUtils.isEmpty(mLatitude1.getText())){
    		inputOK=false;
    		mLatitude1.setBackgroundColor(Color.RED);
    		Toast toast=Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG);
    		toast.show();
    	}else{
    		mLatitude1.setBackgroundColor(Color.BLACK);
    	}
    	if(TextUtils.isEmpty(mLongitude1.getText())){
    		inputOK=false;
    		mLongitude1.setBackgroundColor(Color.RED);
    		Toast toast=Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG);
    		toast.show();
    	}else{
    		mLongitude1.setBackgroundColor(Color.BLACK);
    	}
    	if(TextUtils.isEmpty(mRadius1.getText())){
    		inputOK=false;
    		mRadius1.setBackgroundColor(Color.RED);
    		Toast toast=Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG);
    		toast.show();
    	}else{
    		mRadius1.setBackgroundColor(Color.BLACK);
    	}
    	
    	
    	
    	//test if input values are in the correct range
    	
    	double lat1=Double.valueOf(mLatitude1.getText().toString());
    	double long1=Double.valueOf(mLongitude1.getText().toString());
    	float rad1=Float.valueOf(mRadius1.getText().toString());
    	
    	if((lat1<GeofenceUtils.MIN_LATITUDE) || (lat1>GeofenceUtils.MAX_LATITUDE)){
    		inputOK=false;
    		mLatitude1.setBackgroundColor(Color.RED);
    		Toast toast=Toast.makeText(this, R.string.geofence_input_error_latitude_invalid, Toast.LENGTH_LONG);
    		toast.show();
    	}else{
    		mLatitude1.setBackgroundColor(Color.BLACK);
    	}
    	if((long1<GeofenceUtils.MIN_LONGITUDE) || (long1>GeofenceUtils.MAX_LONGITUDE)){
    		inputOK=false;
    		mLongitude1.setBackgroundColor(Color.RED);
    		Toast toast=Toast.makeText(this, R.string.geofence_input_error_longitude_invalid, Toast.LENGTH_LONG);
    		toast.show();
    	}else{
    		mLongitude1.setBackgroundColor(Color.BLACK);
    	}
    	if(rad1<GeofenceUtils.MIN_RADIUS){
    		inputOK=false;
    		mRadius1.setBackgroundColor(Color.RED);
    		Toast toast=Toast.makeText(this, R.string.geofence_input_error_radius_invalid, Toast.LENGTH_LONG);
    		toast.show();
    	}else{
    		mRadius1.setBackgroundColor(Color.BLACK);
    	}
    	
    	
    	
    	return inputOK;
    }

	

}
