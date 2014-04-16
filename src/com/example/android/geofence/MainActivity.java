/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.geofence;

import android.R.string;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.geofence.GeofenceUtils.REMOVE_TYPE;
import com.example.android.geofence.GeofenceUtils.REQUEST_TYPE;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * UI handler for the Location Services Geofence sample app.
 * Allow input of latitude, longitude, and radius for two geofences.
 * When registering geofences, check input and then send the geofences to Location Services.
 * Also allow removing either one of or both of the geofences.
 * The menu allows you to clear the screen or delete the geofences stored in persistent memory.
 */
public class MainActivity extends FragmentActivity implements com.google.android.gms.location.LocationListener {
    /*
     * Use to set an expiration time for a geofence. After this amount
     * of time Location Services will stop tracking the geofence.
     * Remember to unregister a geofence when you're finished with it.
     * Otherwise, your app will use up battery. To continue monitoring
     * a geofence indefinitely, set the expiration time to
     * Geofence#NEVER_EXPIRE.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 1;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;

    // Store the current request
    private REQUEST_TYPE mRequestType;

    // Store the current type of removal
    private REMOVE_TYPE mRemoveType;

    // Persistent storage for geofences
    private SimpleGeofenceStore mPrefs;

    // Store a list of geofences to add
    List<Geofence> mGeofencesToAdd;

    // Add geofences handler
    private GeofenceRequester mGeofenceRequester;
    // Remove geofences handler
    private GeofenceRemover mGeofenceRemover;
    
    private LocationRequester mLocationRequester;
    
    private Button mStartButton;
    private Button mStopButton;
    private Button mManualAddButton;
    
    private boolean newGeofence;
    
    
    private GoogleMap map;
        
    private TextView mActiveGeofencesText;
    
    private SimpleGeofence mUIGeofence;
    private SimpleGeofence mOfficeGeofence;
    private List<SimpleGeofence> simpleGeofences;
    
    private ButtonsStateStore mButtonState;

    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private GeofenceSampleReceiver mBroadcastReceiver;

    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;


    /********************************MANAGING ACTIVITY LIFECYCLE**************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new GeofenceSampleReceiver();

        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
        
        //Action for receiving geofence data manually inserted
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_INSERTED);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // Instantiate a new geofence storage area
        mPrefs = new SimpleGeofenceStore(this);
        
        // Instantiate the current List of geofences
        mGeofencesToAdd=new ArrayList<Geofence>();
        
        simpleGeofences=new LinkedList<SimpleGeofence>();

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(this);

        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(this);
        
        mLocationRequester= new LocationRequester(this);
        
        mButtonState=new ButtonsStateStore(this);

        // Attach to the main UI
        setContentView(R.layout.activity_main);

        
        // Get handles to the Geofence editor fields in the UI
        mStartButton=(Button)findViewById(R.id.register);
        mStopButton=(Button)findViewById(R.id.unregister_by_pending_intent);
        mManualAddButton=(Button)findViewById(R.id.manual_add);
        mActiveGeofencesText=(TextView)findViewById(R.id.active_geofences);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        
        newGeofence=true;
        
        //by default desabled
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
        saveButtonStates();
        
        //REFRESH GEOFENCES
        mPrefs.clearStoredGeofences();
        loadServerGeofences();
        
      
    }
    
        
    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver to receive status updates
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
        /*
         * Get existing geofences from the latitude, longitude, and
         * radius values stored in SharedPreferences. If no values
         * exist, null is returned.
         */
        simpleGeofences=mPrefs.getGeofences();
        logStoredGeofences();
        if(servicesConnected()){
        	mLocationRequester.requestConnection();
        	mLocationRequester.startPeriodicUpdates();
        }else{
        	Log.e("onResume", "services not connected!");
        }
        
        //resuming button states
        resumeButtonStates();
        
    }
      
    @Override
	protected void onPause() {
    	if(servicesConnected()){
    		mLocationRequester.stopPeriodicUpdates();
        	mLocationRequester.requestDisconnection();
        }else{
        	Log.e("onPause", "services not connected!");
        }

    	saveButtonStates();    	
		super.onPause();
	}
    


	/********************************************HANDLE SERVICES RELATED PROBLEMS****************************/

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * GeofenceRemover and GeofenceRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     * calls
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to add geofences
                        if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);

                            // Restart the process of adding the current geofences
                            mGeofenceRequester.addGeofences(mGeofencesToAdd);

                        // If the request was to remove geofences
                        } else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType ){

                            // Toggle the removal flag and send a new removal request
                            mGeofenceRemover.setInProgressFlag(false);

                            // If the removal was by Intent
                            if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

                                // Restart the removal of all geofences for the PendingIntent
                                mGeofenceRemover.removeGeofencesByIntent(
                                    mGeofenceRequester.getRequestPendingIntent());

                            // If the removal was by a List of geofence IDs
                            } /*else {

                                // Restart the removal of the geofence list
                                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
                            }*/
                        }
                    break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               Log.d(GeofenceUtils.APPTAG,
                       getString(R.string.unknown_activity_request_code, requestCode));

               break;
        }
    }



    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            Log.d(GeofenceUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;

        // Google Play services was not available for some reason
        } else {

            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), GeofenceUtils.APPTAG);
            }
            return false;
        }
    }
    
    /*************************HANDLE UI BUTTONS****************************************/

    /**
     * Called when the user clicks the "STOP GEOFENCES" button
     *
     */
    public void onUnregisterByPendingIntentClicked(View view) {
        /*
         * Remove all geofences set by this app. To do this, get the
         * PendingIntent that was added when the geofences were added
         * and use it as an argument to removeGeofences(). The removal
         * happens asynchronously; Location Services calls
         * onRemoveGeofencesByPendingIntentResult() (implemented in
         * the current Activity) when the removal is done
         */

        /*
         * Record the removal as remove by Intent. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
        // Record the type of removal
        mRemoveType = GeofenceUtils.REMOVE_TYPE.INTENT;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }

        // Try to make a removal request
        try {
        /*
         * Remove the geofences represented by the currently-active PendingIntent. If the
         * PendingIntent was removed for some reason, re-create it; since it's always
         * created with FLAG_UPDATE_CURRENT, an identical PendingIntent is always created.
         */
        mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.remove_geofences_already_requested_error,
                        Toast.LENGTH_LONG).show();
        }

    }

    
    //START GEOFENCING
    
    public void onRegisterClicked(View view) {
    	
    	if(newGeofence){
    		/*
             * Record the request as an ADD. If a connection error occurs,
             * the app can automatically restart the add request if Google Play services
             * can fix the error
             */
            mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

            /*
             * Check for Google Play services. Do this after
             * setting the request type. If connecting to Google Play services
             * fails, onActivityResult is eventually called, and it needs to
             * know what type of request was in progress.
             */
            if (!servicesConnected()) {

                return;
            }
            
            for(SimpleGeofence s:simpleGeofences){
            	mGeofencesToAdd.add(s.toGeofence());
            }
            
            // Start the request. Fail if there's already a request in progress
            try {
                // Try to add geofences
                mGeofenceRequester.addGeofences(mGeofencesToAdd);
            } catch (UnsupportedOperationException e) {
                // Notify user that previous request hasn't finished.
                Toast.makeText(this, R.string.add_geofences_already_requested_error,
                            Toast.LENGTH_LONG).show();
            }  		
    	}
    	else{
    		Log.d("onRegisterClicked","newGeofence flag false");
    	}

        
    }
    
    public void onManualAddClicked(View v){
    	
    	Intent intent=new Intent(this, ManualInsertActivity.class);
    	startActivity(intent);
    	
    	
    }
    
    
    
    /********************************OTHER METHODS*******************************************/

    private void showActiveGeofencesUI(){
    	
    	if(simpleGeofences!=null && !simpleGeofences.isEmpty()){
    		mActiveGeofencesText.setText(simpleGeofences.toString());
    	}
    	else mActiveGeofencesText.setText(R.string.no_geofences_in_list);
    }
    
    private void logStoredGeofences(){
    	
    	if(simpleGeofences!=null && !simpleGeofences.isEmpty()){
    		Log.d("StoredGeofences", simpleGeofences.toString());
    	}
    	else Log.d("StoredGeofences", "NO STORED GEOFENCES");
    	
    }
        

    private void loadServerGeofences(){
    	
    	String geofenceID=mPrefs.getNewID();
    	double latitudeOffice=45.065288;
    	double longitudeOffice=7.657679;
    	float radiusOffice=100;
    	
    	mOfficeGeofence=new SimpleGeofence(geofenceID,latitudeOffice,longitudeOffice,radiusOffice,GEOFENCE_EXPIRATION_IN_MILLISECONDS,Geofence.GEOFENCE_TRANSITION_ENTER);   	
    	mPrefs.setGeofence(geofenceID, mOfficeGeofence);
    	simpleGeofences.add(mOfficeGeofence);
    	
    	mGeofencesToAdd.add(mOfficeGeofence.toGeofence());
    }
    
    private void saveButtonStates(){
    	
    	//saving button states
    	
    	mButtonState.setButtonState(GeofenceUtils.KEY_START_BUTTON,mStartButton.isEnabled());
    	mButtonState.setButtonState(GeofenceUtils.KEY_STOP_BUTTON,mStopButton.isEnabled());
    	mButtonState.setButtonState(GeofenceUtils.KEY_MANUAL_ADD_BUTTON, mManualAddButton.isEnabled());
    	mButtonState.setButtonState(GeofenceUtils.KEY_NEW_GEOFENCE, newGeofence);
    	
    }
    
    private void resumeButtonStates(){
    	
    	//restore button states
    	
    	mStartButton.setEnabled(mButtonState.getButtonState(GeofenceUtils.KEY_START_BUTTON));
    	mStopButton.setEnabled(mButtonState.getButtonState(GeofenceUtils.KEY_STOP_BUTTON));
    	mManualAddButton.setEnabled(mButtonState.getButtonState(GeofenceUtils.KEY_MANUAL_ADD_BUTTON));
    	newGeofence=mButtonState.getButtonState(GeofenceUtils.KEY_NEW_GEOFENCE);
    	
    }
    

    /******************************IMPLEMENTED INTERFACE METHODS***************************/
    @Override
	public void onLocationChanged(Location location) {
		
    	LatLng currentLocation=new LatLng(location.getLatitude(), location.getLongitude());
    	
    	Marker locationMarker=map.addMarker(new MarkerOptions()
    	.position(currentLocation));
    	
    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
		
	}
    
    /*******************************INNER BROADCAST RECEIVER*********************************/
    
    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceSampleReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            
            String action = intent.getAction();
       
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);
            
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                    ||
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);
            
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);
          
            } else if(TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_INSERTED)){
            	
            	handleGeofenceManualAdd(context,intent);
            }            
            else {
                Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         */
        private void handleGeofenceStatus(Context context, Intent intent) {
        	String action = intent.getAction();
        	
        	if(TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)){
        		Toast toast=Toast.makeText(context, R.string.all_geofences_added, Toast.LENGTH_SHORT);
        		toast.show();
        		mStopButton.setEnabled(true);
        		newGeofence=false;
        		mGeofencesToAdd.clear();
        		showActiveGeofencesUI();
        		Log.d("handleGeofenceStatus", "all geofences added");
        		
        		
        	}else{ //geofences REMOVED
        		Toast toast=Toast.makeText(context, R.string.all_geofences_removed, Toast.LENGTH_SHORT);
        		toast.show();
        		//erase active geofences
        		mActiveGeofencesText.setText(R.string.no_geofences_in_list);
        		mStopButton.setEnabled(false);
        		
        	}
        }
        /*
         * Handling geofence manual inserction
         */
        private void handleGeofenceManualAdd(Context context, Intent intent){
        	
        	Log.d("handleGeofenceManualAdd","retrieving extra from intent");
        	
        	String ID=mPrefs.getNewID();
        	double lat1=intent.getDoubleExtra(GeofenceUtils.EXTRA_GEOFENCE_LATITUDE, GeofenceUtils.INVALID_DOUBLE_VALUE);
        	double lng1=intent.getDoubleExtra(GeofenceUtils.EXTRA_GEOFENCE_LONGITUDE, GeofenceUtils.INVALID_DOUBLE_VALUE);
        	float rad1=intent.getFloatExtra(GeofenceUtils.EXTRA_GEOFENCE_RADIUS, GeofenceUtils.INVALID_FLOAT_VALUE);
        	
        	if(lat1!=GeofenceUtils.INVALID_DOUBLE_VALUE && lng1!=GeofenceUtils.INVALID_DOUBLE_VALUE && rad1!=GeofenceUtils.INVALID_FLOAT_VALUE){
        		
        		mUIGeofence=new SimpleGeofence(ID,lat1,lng1,rad1,GEOFENCE_EXPIRATION_IN_MILLISECONDS,Geofence.GEOFENCE_TRANSITION_ENTER);
        		mPrefs.setGeofence(ID, mUIGeofence);
        		
        		Log.d("handleGeofenceManualAdd", "Geofence with ID:"+ID);
        		simpleGeofences.add(mUIGeofence);
        		newGeofence=true;
        		logStoredGeofences();
        	}
        }

        /**
         * Report geofence transitions to the UI
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
        	Log.d("handleGeofenceTransition", "geofence transition happened");
        	
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
  
    
/******************************************ERROR DIALOG FRAGMENT****************************************/
    
    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

	
}
