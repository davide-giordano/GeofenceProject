package com.example.android.geofence;

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
	
	

	public MyGeofencesArrayAdapter(Context context, int textViewResourceId,
			List<SimpleGeofence> objects) {
		super(context, textViewResourceId, objects);
		this.objects=objects;
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
			
			TextView mFirstLine=(TextView)convertView.findViewById(R.id.firstLine);
			TextView mSecondLine=(TextView)convertView.findViewById(R.id.secondLine);
			
			mFirstLine.setText("Geofence"+s.getId());
			mSecondLine.setText("Longitude:"+s.getLongitude()+" Latitude:"+s.getLatitude()+" Radius:"+s.getRadius());			
		}
		
		
		
		return convertView;
	}



	
	
}
