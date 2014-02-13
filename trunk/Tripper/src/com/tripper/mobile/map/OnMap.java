package com.tripper.mobile.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class OnMap extends Activity {

	//Globals
	private GoogleMap googleMap;
	List<MyMarkers> markersList;
    int markerCounter=0;
    
	//Externals
	public static Address selectedAddress=null;
	
	private class contact
	{
		LatLng contactLocation;
		String locationString;
		boolean isReplied;
		boolean manuallyOverride;
		double radius;
	}
	
	
	//MyMarkers Struct (in java)
	private class MyMarkers
	{	
		public MarkerOptions markerOptions;
	    public Marker marker; 
	    public String markerStreet;
	    
	    MyMarkers(MarkerOptions markerOptions,Marker marker,String marker_street)
	    {
	    	this.markerOptions=markerOptions;
	    	this.marker=marker;
	    	this.markerStreet=marker_street;	    	
	    }
	 };
	 
	 public void showDialog(View view)
	 {
		 //Intent intent = new Intent(this,ShowDialog.class);
		 //startActivity(intent);
	 }
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.on_map);
		
		//initialize markers
		markersList = new ArrayList<MyMarkers>();
		
		// Loading map
		try {
			initilizeMap();

		} catch (Exception e) {
			Log.d("Error#@$#$",e.getMessage());
		}

		
		//defining Long click on map
		googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(LatLng arg0) {
				String longt,lat;
				
				longt=String.valueOf(arg0.longitude);
				lat=String.valueOf(arg0.latitude);
				
				//Defind and add the Marker to map and list.
				MarkerOptions markerOptions = new MarkerOptions().position(arg0).title(lat + ":" + longt);
				markersList.add(new MyMarkers(markerOptions,null,"Not detected yet or Not next to any street"));				
				markersList.get(markerCounter).marker=googleMap.addMarker(markerOptions);
				
				//launch Async Geocode query
				notifyResult(getBaseContext(),markerCounter);
				
				//increment Markers counter
				markerCounter++;
				
				//Toast
				Toast.makeText(getBaseContext(), "Marker Added", Toast.LENGTH_SHORT).show();
			}
		});
		
		//defining click on map actions
		googleMap.setOnMapClickListener(new OnMapClickListener() {			
			@Override
			public void onMapClick(LatLng arg0) {

			}
		});
		
		//defining click on marker on the map
		googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {			
			@Override
			public boolean onMarkerClick(Marker arg0) {
				Log.d("MARKER","ON MARKER CLICK");
				for (MyMarkers myMarkers : markersList) {
					if(myMarkers.marker.equals(arg0))					
					{
						Log.d("MARKER","IN IF");
						Toast.makeText(getBaseContext(), myMarkers.markerStreet, Toast.LENGTH_SHORT).show();
						break;
					}
				}
				return false;
			}
		});
		selectedAddress=new Address(Locale.getDefault());
		selectedAddress.setLatitude(ContactsListSingleton.getInstance().singleCoordinates_lat);
		selectedAddress.setLongitude(ContactsListSingleton.getInstance().singleCoordinates_long);
		
		if(selectedAddress.getLatitude()!=0 && 
				selectedAddress.getLongitude()!=0)
		{
			Toast.makeText(this, selectedAddress.getLatitude()+","+selectedAddress.getLongitude(), Toast.LENGTH_SHORT ).show();

			// create marker from previous form
			MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude())).title(selectedAddress.getLatitude() + ":" + selectedAddress.getLongitude());		

			markersList.add(new MyMarkers(markerOptions,null,"Not detected yet or Not next to any street"));

			// adding marker
			markersList.get(markerCounter).marker=googleMap.addMarker(markersList.get(markerCounter).markerOptions);

			//launch Async Geocode query
			notifyResult(this,markerCounter);

			//increment markers counter
			markerCounter++;
			
		}
		selectedAddress=null;
		
	}
	
	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			
			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
				return;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();		
		if(selectedAddress!=null)
		{
			Toast.makeText(this, selectedAddress.getLatitude()+","+selectedAddress.getLongitude(), Toast.LENGTH_SHORT ).show();

			// create marker from previous form
			MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude())).title(selectedAddress.getLatitude() + ":" + selectedAddress.getLongitude());		

			markersList.add(new MyMarkers(markerOptions,null,"Not detected yet or Not next to any street"));

			// adding marker
			markersList.get(markerCounter).marker=googleMap.addMarker(markersList.get(markerCounter).markerOptions);

			//launch Async Geocode query
			notifyResult(this,markerCounter);

			//increment markers counter
			markerCounter++;
			selectedAddress=null;
		}
	}
	
	//Launch Async Geocode
	private  void notifyResult(Context context,int markerIndex) {
		Log.d("App!!!","notifyResult");
		try{
			AsyncGeocode AsyncTasker = new AsyncGeocode(context,markerIndex);
			AsyncTasker.execute();			
		}catch(Exception ex){
			Log.d("AsyncTasker Error#@$#$",ex.getMessage());
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		selectedAddress=null;
	}

	
/**------------------
 * MENU Definitions
 * ------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.display_address, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.addLocation:
	    	    Intent intent = new Intent(this, AddMarker.class);	
	    	    startActivity(intent);
	            return true;	            	           
	        case R.id.clearAll:
	        	for (MyMarkers myMarkers : markersList) 
	        		myMarkers.marker.remove();
	        	markersList.clear();
	        	markerCounter=0;
	            return true;	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}*/
	/**------------------
	 * END MENU Definitions
	 * ------------------*/
	
	
	/**--------------------
	 * ASYNC GeoCode Class 
	 * --------------------*/
	//Class extends AsyncTask for defining the AsyncTask function 
	private class AsyncGeocode extends AsyncTask<String, Void, Void> {
		private Context context;
		private int markerIndex;

		public AsyncGeocode(Context context,int markerIndex) {
			super();
			this.context = context;
			this.markerIndex= markerIndex;
		}

		//will run in the created thread
		protected Void doInBackground(String... search) 
		{
			Log.d("MAP!!!","doInBackground-begin");
			MarkerOptions marker = markersList.get(markerIndex).markerOptions;
			LatLng cords = marker.getPosition();
			double lat = cords.latitude;
			double longt = cords.longitude;
			
			try {
				List<Address> addressList = new Geocoder(context).getFromLocation(lat,longt, 1);
				if(addressList!=null)
				{
					Address address= addressList.get(0);
					String addressString=address.getAddressLine(2)+","+ address.getAddressLine(1)+","+address.getAddressLine(0);
					markersList.get(markerIndex).markerStreet=addressString;	
				}
				
			} catch (Exception ex) {
				Log.d("MAP Error#@$#$",ex.getMessage());
			}
			return null;
		}    
	}//End Class AsyncTask
	


}
