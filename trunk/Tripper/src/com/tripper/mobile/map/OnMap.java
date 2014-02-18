package com.tripper.mobile.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class OnMap extends Activity {

	//Map Route Vars
	private static final LatLng AMSTERDAM = new LatLng(52.37518, 4.895439);
	private static final LatLng PARIS = new LatLng(48.856132, 2.352448);
	private static final LatLng FRANKFURT = new LatLng(50.111772, 8.682632);
	private LatLngBounds latlngBounds;
	private Polyline newPolyline;
	private boolean isTravelingToParis = false;
	private int width, height;
	
	
	//Globals
	private GoogleMap googleMap;
	List<MyMarkers> markersList;
    int markerCounter=0;
    Context context;
    
	//Externals
	public static Address selectedAddress=null;
	
	private BroadcastReceiver mMessageReceiver;
	
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
	
		context=this;
		getScreenDimensions();
		
		mMessageReceiver = new BroadcastReceiver() {
			  @Override
			  public void onReceive(Context context, Intent intent) 
			  {
				  Log.e("onReceive","aaaaaaaaaaaaaaaaaaaaaaaa");
				  Toast.makeText(getApplicationContext(), "zzzzzzzzzzzzzzzzzzzzz", Toast.LENGTH_LONG).show();
			  }
		};
				
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
		selectedAddress=ContactsListSingleton.getSingleRouteAddress();
		
		if(selectedAddress!=null && selectedAddress.getLatitude()!=0 && 
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
			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(markersList.get(markerCounter).markerOptions.getPosition(), 14, 0, 0)),3000,null);
			//increment markers counter
			markerCounter++;
			
		}

		
		
		/*
		 * NAVIGATION
		 */
		//GOOGLE
		Button GoogleNavigate = (Button)findViewById(R.id.navigateGooG);
		GoogleNavigate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedAddress=ContactsListSingleton.getSingleRouteAddress();
				
				//GoogleMaps Navigation
				try{					
					//source location
				    double latitudeCurr = 31.2718;
				    double longitudeCurr = 34.78256;
					
				    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(
				    		"http://maps.google.com/maps?" + "saddr="+ latitudeCurr + "," + longitudeCurr + "&daddr="+selectedAddress.getLatitude()+","+selectedAddress.getLongitude()));
				    		//can enter a String address after saddr\daddr
				    intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
				    startActivityForResult(intent,5);
				}catch(Exception e)
				{
					Log.e("Google Navigate","Error: "+e.getMessage());
				}
		        
	
			}
		});		
		
		Button WazeNavigate = (Button)findViewById(R.id.navigateWaze);
		WazeNavigate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				/*
				selectedAddress=new Address(Locale.getDefault());
				selectedAddress.setLatitude(ContactsListSingleton.getInstance().singleCoordinates_lat);
				selectedAddress.setLongitude(ContactsListSingleton.getInstance().singleCoordinates_long);		        
				/*  WAZE API
				 *  search for address: 	waze://?q=<address search term>
				 *	center map to lat / lon: 	waze://?ll=<lat>,<lon>
				 *	set zoom (minimum is 6): 	waze://?z=<zoom>
				 */
				/*
		        //Waze Navigation
				try
				{
					//String url = "waze://?ll="+ selectedAddress.getLatitude()+","+selectedAddress.getLongitude();
					String url = "waze://?q=ביאליק 1 באר שבע";
				    Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
				    startActivityForResult(intent,6);
				}
				catch ( ActivityNotFoundException ex  )
				{
				  Intent intent =
				    new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
				  startActivity(intent);
				}	*/

				
			}
		});
		
		//selectedAddress=null;//RETURN THIS COMMENT
		
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==5)
			Toast.makeText(this, "GoogleMap "+resultCode, Toast.LENGTH_LONG).show();
		if(requestCode==6)
			Toast.makeText(this, "Waze "+resultCode, Toast.LENGTH_LONG).show();
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			//googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			
			googleMap.setMyLocationEnabled(true);
			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
				return;
			}
		}
	}
	
	/*
	 * MAP ROUTE FUNCTIONS
	 */
	
	public void navigateClick(View v)
	{
		if (!isTravelingToParis)
		{
			isTravelingToParis = true;
			findDirections( AMSTERDAM.latitude, AMSTERDAM.longitude,PARIS.latitude, PARIS.longitude, GMapV2Direction.MODE_DRIVING );
		}
		else
		{
			isTravelingToParis = false;
			findDirections( AMSTERDAM.latitude, AMSTERDAM.longitude, FRANKFURT.latitude, FRANKFURT.longitude, GMapV2Direction.MODE_DRIVING );  
		}
	}
	public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {
		PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);

		for(int i = 0 ; i < directionPoints.size() ; i++) 
		{          
			rectLine.add(directionPoints.get(i));
		}
		if (newPolyline != null)
		{
			newPolyline.remove();
		}
		newPolyline = googleMap.addPolyline(rectLine);
		if (isTravelingToParis)
		{
			latlngBounds = createLatLngBoundsObject(AMSTERDAM, PARIS);
	        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 150));
		}
		else
		{
			latlngBounds = createLatLngBoundsObject(AMSTERDAM, FRANKFURT);
	        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 150));
		}
		
	}
	
	private void getScreenDimensions()
	{
		Display display = getWindowManager().getDefaultDisplay();
		width = display.getWidth(); 
		height = display.getHeight(); 
	}
	
	private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
	{
		if (firstLocation != null && secondLocation != null)
		{
			LatLngBounds.Builder builder = new LatLngBounds.Builder();    
			builder.include(firstLocation).include(secondLocation);
			
			return builder.build();
		}
		return null;
	}
	
	public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
	{
		Map<String, String> map = new HashMap<String, String>();
		map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
		map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
		map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
		map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
		map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);
		
		GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
		asyncTask.execute(map);	
	}	
	@Override
	protected void onResume() {
		super.onResume();	
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("com.tripper.mobile.UPDATE"));
		
		
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
		
		//MAP ROUTE 
    	latlngBounds = createLatLngBoundsObject(AMSTERDAM, PARIS);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 150));

	}
	
	
	@Override
	protected void onPause()
	{
	    super.onPause();

	    if (mMessageReceiver != null) 
	    {
	    	LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
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
