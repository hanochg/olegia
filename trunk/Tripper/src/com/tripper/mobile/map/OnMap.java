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
import com.tripper.mobile.adapter.NavDrawerListAdapter;
import com.tripper.mobile.drawer.NavDrawerItem;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
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
 
    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
 
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter navDrawerListAdapter;

	/*
	 * END DRAWER VARIABLES
	 */
    
	//Globals
	private GoogleMap googleMap;
	List<MyMarkers> markersList;
    int markerCounter=0;
    Context context;
    
	//Externals
	public static Address selectedAddress=null;
	
	private BroadcastReceiver mMessageReceiver;
	
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
				  navDrawerListAdapter.notifyDataSetChanged();
				  Toast.makeText(getApplicationContext(), "zzzzzzzzzzzzzzzzzzzzz", Toast.LENGTH_LONG).show();
			  }
		};
				
		//initialize markers
		markersList = new ArrayList<MyMarkers>();
		
		// Initializing Map
		try {
			initilizeMap();

		} catch (Exception e) {
			Log.e("OnMap","Error Initializing Map: "+e.getMessage());
		}
		
		//defining click on marker on the map
		googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {			
			@Override
			public boolean onMarkerClick(Marker arg0) {
				Log.d("MARKER","ON MARKER CLICK");
				for (MyMarkers myMarkers : markersList) {
					if(myMarkers.marker.equals(arg0))					
					{
						Toast.makeText(getBaseContext(), myMarkers.markerStreet, Toast.LENGTH_SHORT).show();
						break;
					}
				}
				return false;
			}
		});
		
		//Get single route details
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

		InitializeDrawer();
		
		selectedAddress=null;
		
	}
	
	
	private void InitializeDrawer()
	{
        mTitle = mDrawerTitle = getTitle();
        
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
 
        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
 
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
 
        navDrawerItems = new ArrayList<NavDrawerItem>();
 
        // adding nav drawer items to array
        // Home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Find People
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Photos
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        // Communities, Will add a counter here
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, "22"));
        // Pages
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        // What's hot, We  will add a counter here
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "50+"));
         
 
        // Recycle the typed array
        navMenuIcons.recycle();
 
        // setting the nav drawer list adapter
        navDrawerListAdapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
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
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    /**
     * Displaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
    	// update the main content by replacing fragments
    	Fragment fragment = null;
    	switch (position) {
    	case 0:
    		//fragment = new HomeFragment();
    		break;
    	case 1:
    		//fragment = new FindPeopleFragment();
    		break;
    	case 2:
    		//fragment = new PhotosFragment();
    		break;
    	case 3:
    		//fragment = new CommunityFragment();
    		break;
    	case 4:
    		//fragment = new PagesFragment();
    		break;
    	case 5:
    		//fragment = new WhatsHotFragment();
    		break;

    	default:
    		break;
    	}

    	// update selected item and title, then close the drawer
    	//mDrawerList.setItemChecked(position, true);
    	//mDrawerList.setSelection(position);
    	//setTitle(navMenuTitles[position]);
    	mDrawerLayout.closeDrawer(mDrawerList);

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
        case R.id.action_settings:
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
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
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
					markersList.get(markerIndex).markerStreet=addressString;	
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


