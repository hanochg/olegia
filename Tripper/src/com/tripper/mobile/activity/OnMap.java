package com.tripper.mobile.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tripper.mobile.DistanceService;
import com.tripper.mobile.R;
import com.tripper.mobile.SettingsActivity;
import com.tripper.mobile.adapter.NavDrawerListAdapter;
import com.tripper.mobile.map.*;
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
	
	
	/*
	 * DRAWER VARIABLES
	 */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // nav drawer title
    private CharSequence mDrawerTitle;
 
    // used to store app title
    private CharSequence mTitle;
 
    private NavDrawerListAdapter navDrawerListAdapter;

	/*
	 * END DRAWER VARIABLES
	 */
    
	//Globals
    private GoogleMap googleMap;
	List<MyMarkers> markersList;
    int markerCounter=0;
    Context context;
    Marker singleRouteMarker=null;
    
	//Externals
	public static Address selectedAddress=null;
	
	private BroadcastReceiver mMessageReceiver;
	
	//MyMarkers Struct (in java)
	private class MyMarkers
	{	
		public MarkerOptions markerOptions;
	    public Marker marker; 
	    
	    MyMarkers(MarkerOptions markerOptions,Marker marker,String marker_street)
	    {
	    	this.markerOptions=markerOptions;
	    	this.marker=marker;    	
	    }
	 };
	 
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.on_map);
	
		context=this;
		getScreenDimensions();
		
		
	    Intent i=new Intent(this, DistanceService.class);
	    startService(i);
		
		mMessageReceiver = new BroadcastReceiver() {
			  @Override
			  public void onReceive(Context context, Intent intent) 
			  {
				  Log.e("onReceive","RECEIVED!!");
				  navDrawerListAdapter.notifyDataSetChanged();
				  addContactsMarkers();
				  Toast.makeText(getApplicationContext(), "MSG RECEIVED!", Toast.LENGTH_LONG).show();
			  }
		};

		//initialize markers
		markersList = new ArrayList<MyMarkers>();

		// Initializing Map
		initilizeMap();
		
		//Initialize the Drawer
		InitializeDrawer();
		
	}
	
	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			try{
				googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
				//googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

				googleMap.setMyLocationEnabled(true);
			}
			catch(Exception e){
				Log.e("OnMap","Error Initializing Map: "+e.getMessage());
			}
			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			//defining click on marker on the map
			googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {			
				@Override
				public boolean onMarkerClick(Marker arg0) {
					return false;
				}
			});
			googleMap.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
			googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				
				@Override
				public void onInfoWindowClick(Marker arg0) {
					//Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
					
				}
			});
			
			//MARKERS
			addSingleRouteMarker();
			addContactsMarkers();
		}
	}
	
	private void addSingleRouteMarker()
	{
		//Get single route details
		selectedAddress=ContactsListSingleton.getSingleRouteAddress();

		if(selectedAddress!=null && selectedAddress.getLatitude()!=-1 && 
				selectedAddress.getLongitude()!=-1)
		{
			LatLng latLng=new LatLng(selectedAddress.getLatitude(),selectedAddress.getLongitude());
			MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Destination!!").icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_flag1));				
			singleRouteMarker=googleMap.addMarker(markerOptions);
		}
	}

	private void addContactsMarkers()
	{
		ArrayList<ContactDataStructure> contactsDB = ContactsListSingleton.getInstance().getDB();
		ContactDataStructure curContact;
		double curContactLong,curContactLat;
		
		for(int i=0 ; i<contactsDB.size() ; i++)
		{
			curContact=contactsDB.get(i);
			curContactLong=curContact.getLongitude();
			curContactLat=curContact.getLatitude();
			if(curContactLat!=-1 && curContactLong!=-1 && 
					curContact.getMarker()==null)
			{
				LatLng latLng=new LatLng(curContactLat,curContactLong);
				Log.d("ContactMarker","MarkerName: "+curContact.getName());
				MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(curContact.getName());
				curContact.setRadiusOnMap(googleMap.addCircle(new CircleOptions()
			     .center(latLng)
			     .radius(curContact.getRadius())
			     .strokeColor(Color.DKGRAY)
			     .strokeWidth(2)
			     //.fillColor(Color.BLUE)
			     .fillColor(Color.argb(50, Color.red(Color.DKGRAY), Color.green(Color.DKGRAY), Color.blue(Color.DKGRAY)))));
				curContact.setMarker(googleMap.addMarker(markerOptions));
				
			}
		}	
	}
	
	private void InitializeDrawer()
	{
        mTitle = mDrawerTitle = getTitle();
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
 
        // setting the nav drawer list adapter
        navDrawerListAdapter = new NavDrawerListAdapter(getApplicationContext());
        mDrawerList.setAdapter(navDrawerListAdapter);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        
        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
 
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }
 
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
 
    
	}
	
    
    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements
            				ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {

        	mDrawerLayout.closeDrawer(mDrawerList);
        	
        }
    }  

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==5)
			Toast.makeText(this, "GoogleMap "+resultCode, Toast.LENGTH_LONG).show();
		if(requestCode==6)
			Toast.makeText(this, "Waze "+resultCode, Toast.LENGTH_LONG).show();
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
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void getScreenDimensions()
	{
		 if (  Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 13 ) 
		 {
	         Display display = getWindowManager().getDefaultDisplay(); 
	         width = display.getWidth();
	         height = display.getHeight();
	     } 
		 else 
	     {
	          Display display = getWindowManager().getDefaultDisplay();
	          Point size = new Point();
	          display.getSize(size);
	          width = size.x;
	          height = size.y;
	     }        
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
		navDrawerListAdapter.notifyDataSetChanged();
/*
		//MAP ROUTE 
    	latlngBounds = createLatLngBoundsObject(AMSTERDAM, PARIS);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 150));
*/
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
	
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	/**------------------
	 * MENU Definitions
	 * ------------------*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.on_map_menu, menu);
	    return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
        case R.id.SettingsOM:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.SettingsOM).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
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
					//markersList.get(markerIndex).markerStreet=addressString;	
				}
				
			} catch (Exception ex) {
				Log.d("MAP Error#@$#$",ex.getMessage());
			}
			return null;
		}    
	}//End Class AsyncTask

}

/*
 * NAVIGATION

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
		}	

		
	}
}); */


