package com.tripper.mobile.activity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
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
import com.tripper.mobile.utils.Queries;
import com.tripper.mobile.utils.Queries.Extra;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.KeyEvent;
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
    int markerCounter=0;
    private Context context;
    private Marker singleRouteMarker=null;
    private int APP_MODE=-1;
    
	//Externals
	public static Address selectedAddress=null;
	
	private BroadcastReceiver mMessageReceiver;
	
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.on_map);
	
		
		APP_MODE = getIntent().getExtras().getInt(Extra.APP_MODE);
		context=this;
		getScreenDimensions();
		
		
	    Intent i=new Intent(this, DistanceService.class);
	    i.putExtra(Extra.APP_MODE, APP_MODE);
	    startService(i);

		
		mMessageReceiver = new BroadcastReceiver() {
			  @Override
			  public void onReceive(Context context, Intent intent) 
			  {
				  String intentAction=intent.getAction();
				  if(intentAction.equals("com.tripper.mobile.MESSAGE"))
				  {
					  Log.d("onReceive","RECEIVED!!");
					  navDrawerListAdapter.notifyDataSetChanged();
					  addContactsMarkers();
					  String user = intent.getExtras().getString(Extra.USERNAME);
					  Toast.makeText(getApplicationContext(), "Message Received From "+ user +"!", Toast.LENGTH_LONG).show();					  
				  }
				  else if(intentAction.equals("com.tripper.mobile.UPDATE"))
				  {
					  Log.d("onReceive","List update received");
					  navDrawerListAdapter.notifyDataSetChanged();
				  }
			  }
		};

		
		//Initialize the Drawer
		InitializeDrawer();
		
	}

	private void setMapView(LatLng dest)
	{
		Location source = getLastKnownLocation();
		if (source != null)
		{
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
		    
			builder.include(new LatLng(source.getLatitude(), source.getLongitude()));
			builder.include(dest);

			LatLngBounds bounds = builder.build();
			
			int padding = 300; // offset from edges of the map in pixels
			CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

			try{
				googleMap.animateCamera(cu,1500,null);
			}catch(Exception e){
				Log.e("setMapView-MAP","error in animateCamera: "+e.getMessage());
			}

		}
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	
	    	new AlertDialog.Builder(this)
	        .setTitle("Exit Map")
	        .setMessage("Exiting the map will end the trip.\nAre you sure you wish to exit?")
	        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) { 
	                //clear the singleton
	            	ContactsListSingleton.getInstance().close();
	            	ContactsListSingleton.getInstance().setDefaultSettingsFromContex(getApplicationContext());
	            	finish();
	            }
	         })
	        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) { 	                
	            	return;
	            }
	         })
	         .show(); 
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
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
			
			//Make Marker
			MarkerOptions markerOptions = new MarkerOptions().
					position(latLng).
					title("Destination!!").
					icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_flag1));
			singleRouteMarker=googleMap.addMarker(markerOptions);
			
			//Make Circle on Marker
			ContactsListSingleton.getInstance().setSingleRouteCircle(
			googleMap.addCircle(new CircleOptions()
		     .center(latLng)
		     .radius(ContactsListSingleton.getInstance().getRadiusSingleFromSettings())
		     .strokeColor(Color.DKGRAY)
		     .strokeWidth(2)
		     .fillColor(Color.argb(50, Color.red(Color.DKGRAY), Color.green(Color.DKGRAY), Color.blue(Color.DKGRAY)))));

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
        navDrawerListAdapter = new NavDrawerListAdapter(this);
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
                navDrawerListAdapter.notifyDataSetChanged();
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }
 
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                navDrawerListAdapter.notifyDataSetChanged();
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.openDrawer(mDrawerList);
    
	}
	
    
    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements
            				ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
        		long id) {
        	ContactDataStructure currentContact = ContactsListSingleton.getInstance().getDB().get(position);
        	boolean currentSelection=currentContact.isSelected();
        	
        	if(!currentSelection)
        		setMapView(new LatLng(currentContact.getLatitude(), currentContact.getLongitude()));


        	currentContact.setSelected(!currentSelection);
        	navDrawerListAdapter.notifyDataSetChanged();
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
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("com.tripper.mobile.MESSAGE"));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("com.tripper.mobile.EXIT"));

		
		googleMap=null;
		
		// Initializing Map
		initilizeMap();
		
		Location source = getLastKnownLocation();
		if (source != null)
			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(source.getLatitude(),source.getLongitude()) , 14, 0, 0)),3000,null);
		
		addContactsMarkers();

		navDrawerListAdapter.notifyDataSetChanged();


	}
	
	private Location getLastKnownLocation()
	{ 
		Location l1=null; 	
		Location l2=null; 
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
		{
			l1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
		}
		if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			l2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		if (l1 == null && l2==null) 
			return null;
		else if(l1 == null)
			return l2;
		else if(l2 == null)
			return l1;
		else if(l1.getAccuracy() < l2.getAccuracy())
			return l1;
		else
			return l2;
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

	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		selectedAddress=null;
	    Intent i=new Intent(this, DistanceService.class);
	    stopService(i);
		
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
        Intent intent;
        // Handle action bar actions click
        switch (item.getItemId()) {
        case R.id.SettingsOM:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);			
			break;
        case R.id.exitOM:        	
        	finish();
        	//intent = new Intent("com.tripper.mobile.EXIT");	
    		//LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        	break;
        case R.id.editContacts:
            intent = new Intent(this,FriendsList.class);
            intent.putExtra(Queries.Extra.APP_MODE,Queries.Extra.ON_MAP);            
            startActivity(intent);
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
    
    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) 
    {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.SettingsOM).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
}	
	


