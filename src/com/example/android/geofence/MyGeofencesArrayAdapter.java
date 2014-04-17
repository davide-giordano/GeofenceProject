package com.example.android.geofence;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyGeofencesArrayAdapter extends ArrayAdapter<SimpleGeofence> {

	private final List<SimpleGeofence> objects;
	private DecimalFormat nF1;
	private DecimalFormat nF2;
	

	public MyGeofencesArrayAdapter(Context context, int textViewResourceId,
			List<SimpleGeofence> objects) {
		super(context, textViewResourceId, objects);
		this.objects=objects;
		nF1 = new DecimalFormat("#.000");
		nF2 = new DecimalFormat("#.0");
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		
		
		Log.d("getView", "doing get view");
		
		
		if(convertView==null){
			LayoutInflater inflater=(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView=inflater.inflate(R.layout.list_element, null);
		}
		
		
		//variable position refers to the position of the current object in the list
		
		SimpleGeofence s=objects.get(position);
		
		if(s!=null){
			
			TextView mGeofenceID=(TextView)convertView.findViewById(R.id.geofence_id_line);
			TextView mGeofenceDescription=(TextView)convertView.findViewById(R.id.geofence_description_line);
			
			
			mGeofenceID.setText("Geofence"+s.getId());
			mGeofenceDescription.setText("Longitude:"+nF1.format(s.getLongitude())+"  Latitude:"+nF1.format(s.getLatitude())+"  Radius:"+nF2.format(s.getRadius()));			
		}
		
		
		
		return convertView;
	}



	
	
}
