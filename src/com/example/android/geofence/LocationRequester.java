package com.example.android.geofence;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class LocationRequester implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{

	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	
	//storage for a referrence to the calling client
	private final Activity mActivity;
	
	
	
	
	public LocationRequester(Activity activityContext){
		
		mLocationRequest=LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(UPDATE_INTERVAL); //5 seconds
		
		mActivity=activityContext;
		mLocationClient=new LocationClient(mActivity, this, this);
	}
	
	/***********************************PUBLIC METHODS CALLED BY ACTIVITY**********************************/

	public LatLng getLocation(){
		
		if(mLocationClient==null){
			Log.e("getLocation", "LocationClient not connected (is null)");
			return null;
		}
		Location lastLocation=mLocationClient.getLastLocation();
		LatLng currentLocation=new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
		return currentLocation;
	}
	
	public Address getAddress(){
		
		if(mLocationClient==null){
			Log.e("getAddress", "LocationClient not connected (is null)");
			return null;
		}
		
		Location currentLocation=mLocationClient.getLastLocation();
		
		Geocoder geocoder=new Geocoder(mActivity,Locale.getDefault());
		List<Address>addresses=null;
		try{
			addresses=geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
		}catch(IOException exception1){
			
			Log.e("getAddress","problem in retrieving addresses with geocoder");
			exception1.printStackTrace();
		}catch(IllegalArgumentException exception2){
			
			Log.e("getAddress","illegal argument exception");
		}
		
		if(addresses!=null && addresses.size()>0){
				return addresses.get(0);
		}else{
			Log.d("getAddress","no address found");
			return null;
		}
		
	}
	
	
	public void requestConnection(){
		
		getLocationClient().connect();
	}
	
	//get the current location client or create a new one if necessary
	private GooglePlayServicesClient getLocationClient(){
		
		if(mLocationClient==null){
			
			mLocationClient.connect();
		}
		
		return mLocationClient;
	}
	
	public void requestDisconnection(){
		
		getLocationClient().disconnect();
	}
	
	public void startPeriodicUpdates(){
		
		if(mLocationClient==null){
			Log.e("startPeriodicUpdates", "LocationClient not connected (is null)");
			return;
		}
		
		mLocationClient.requestLocationUpdates(mLocationRequest, (LocationListener) mActivity);
		Log.d("startPeriodicUpdates", "periodic updates request");
	}
	
	public void stopPeriodicUpdates(){
		
		if(mLocationClient==null){
			Log.e("stopPeriodicUpdates", "LocationClient not connected (is null)");
			return;
		}
		
		mLocationClient.removeLocationUpdates((LocationListener) mActivity);
		Log.d("stopPeriodicUpdates", "periodic updates stopped");
	}
	
	
	/****************************IMPLEMENTED INTERFACE METHODS ******************************/
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		
		if(arg0.hasResolution()){
			
			try{
				arg0.startResolutionForResult(mActivity, GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			}catch(SendIntentException e){
				e.printStackTrace();
			}
		}
		else{	//no resolution available
			Intent errorBroadcastIntent=new Intent(GeofenceUtils.ACTION_CONNECTION_ERROR);
			errorBroadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
								.putExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE, arg0.getErrorCode());
			LocalBroadcastManager.getInstance(mActivity).sendBroadcast(errorBroadcastIntent);
			
		}
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		
		Log.d("onConnected-LocationRequester", "LocationClient connected");
		
	}

	@Override
	public void onDisconnected() {
		
		Log.d("onDisconnected-LocationRequester", "LocationClient disconnected");
		mLocationClient=null;
		
	}
	
	
}
