package com.tripper.mobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.tripper.mobile.R;
import com.tripper.mobile.adapter.FilterCursorWrapper;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.Queries;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationActivity extends Activity implements
								LoaderManager.LoaderCallbacks<Cursor>{

	private TextView tvMessage;
	private String phone="";
	private LocationManager locationManager=null;
	LocationListener locationListener;
	Boolean mylocationClicked=false;
	private Activity notificationActivity;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		notificationActivity=this;
		
		ContactsListSingleton.getInstance().APP_MODE=ContactsListSingleton.AppMode.NOTIFICATION;
		
		ParseAnalytics.trackAppOpened(getIntent());	
		
		try {
			JSONObject json = new JSONObject(getIntent().getExtras().getString("com.parse.Data"));
			phone= json.get("User").toString();
			
			
		} catch (JSONException e) {
			Toast.makeText(getApplicationContext(), "The Program has Crushed.", Toast.LENGTH_LONG).show();
			this.finish();
		}
		
		setContentView(R.layout.notification_screen);
		tvMessage = (TextView) findViewById(R.id.tvMessage);
		tvMessage.setText("User "+ phone + " is inviting you to the trip.");

		// Initializes the loader
        getLoaderManager().initLoader(Queries.LoaderManagerID_Notification, null,  this);
	}

	public void OnBtnMylocationClick(View view)
	{	
		if(mylocationClicked==true)
			return;	
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// Define a listener that responds to location updates
		locationListener = new LocationListener() 
		{
		    public void onLocationChanged(Location location) 
		    {
		    	if(location.getAccuracy()<50)
		    	{
		    		 locationManager.removeUpdates(this);										//not so working
		    		 Toast.makeText(getApplicationContext(), "Your location was sent back.", Toast.LENGTH_LONG).show();
		    		 answerHandler(getJSONDataMessage("ok",location));	
		    		 notificationActivity.finish();
		    		 
		    	}	
		    }
		    public void onStatusChanged(String provider, int status, Bundle extras) { }
		    public void onProviderEnabled(String provider) {}
		    public void onProviderDisabled(String provider) {}
		 };
		 
		 if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
			 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		 else 
		 {
			 mylocationClicked=false;
			 Toast.makeText(getApplicationContext(), "Please turn on the GPS and try again", Toast.LENGTH_LONG).show();
			 return;
		 }
		 
		 if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

		 mylocationClicked=true;			 
	}
	public void OnBtnNoThanksClick(View view)
	{	
		JSONObject data = getJSONDataMessage("no",null);
		answerHandler(data);
		Toast.makeText(getApplicationContext(), "Your answer was sent.", Toast.LENGTH_LONG).show();
		this.finish();
	}
	public void OnBtnProfileAddressClick(View view)
	{	
		
	}
	public void OnBtnEnterAdressClick(View view)
	{	
		Intent intent = new Intent(this, FindAddress.class);
		this.finish();
		startActivity(intent);
	}
	
	private JSONObject getJSONDataMessage(String answer,Location location)
	{
	    try
	    {
	        JSONObject data = new JSONObject();
	        data.put("action","com.tripper.mobile.Answer");
	        data.put("User", ParseUser.getCurrentUser().getUsername());
	        data.put("Answer", answer);
	        if(location!=null)
	        {
	        	data.put("Latitude",  location.getLongitude());
	        	data.put("Longitude",  location.getLongitude());
	        }
	        return data;
	    }
	    catch(JSONException x)
	    {
			Toast.makeText(getApplicationContext(), "The Program has Crushed.", Toast.LENGTH_LONG).show();
			throw new RuntimeException("Something wrong with JSON", x);
	    }
	}
	
	private void answerHandler(JSONObject data)
	{		 
		ParsePush push = new ParsePush();
		push.setChannel("b"+phone.substring(1)); 
		
		push.setExpirationTimeInterval(60*60*24);//one day, till query is relevant
		
		push.setData(data);
		push.sendInBackground();		
	}
	
    @Override
    protected void onPause() 
    {
    	super.onPause();
    	if(locationManager!=null)
    		locationManager.removeUpdates(locationListener);
    }
	
	//##Implements LoaderManager.LoaderCallbacks<Cursor>##
	
@Override
public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) { 
		CursorLoader cur=null;
	if (Queries.LoaderManagerID_Notification == loaderId)
    {
			Uri FilteredUri;
			if (!TextUtils.isEmpty(phone))
				FilteredUri = Uri.withAppendedPath(
				    			Queries.CONTENT_FILTERED_URI,          						    	
								Uri.encode(phone));
			else
				FilteredUri = Queries.CONTENT_URI;	
			
			cur = new CursorLoader(
				notificationActivity,
        		FilteredUri,
        		Queries.PROJECTION_FOR_NOTIFICATION,
        		Queries.SELECTION_DISPLAY_NAME,
        		null,
                Queries.SORT_ORDER
                );
			
			Log.d("PROGRAMM!", "onCreateLoader - succeed to return cur");
        return cur;        			        		
    }
	Log.e("PROGRAMM!", "onCreateLoader - incorrect ID provided (" + loaderId + ")");
	return cur;
}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {		        
		// Put the result Cursor in the adapter for the ListView
		if (Queries.LoaderManagerID_Notification == loader.getId())
		{
			if(cursor.getCount()!=0){
				cursor.moveToPosition(0);
				tvMessage.setText("User "+ cursor.getString(0) + " is inviting you to the trip.");
			}
				
			//FilterCursorWrapper filterCursorWrapper = new FilterCursorWrapper(cursor, true,0);
			//mAdapter.swapCursor(filterCursorWrapper);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Delete the reference to the existing Cursor
		//if (Queries.LoaderManagerID == loader.getId())
			//mAdapter.swapCursor(null);
	}

    
	/*		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.	
		getMenuInflater().inflate(R.menu.notification, menu);			
		return true;
	}*/

}



