package com.tripper.mobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseAnalytics;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.ContactsListSingleton.AppMode;
import com.tripper.mobile.utils.Queries;
import com.tripper.mobile.utils.Queries.Extra;
import com.tripper.mobile.utils.Queries.Net;
import com.tripper.mobile.utils.Queries.Net.ChannelMode;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class NotificationActivity extends Activity implements
								LoaderManager.LoaderCallbacks<Cursor>{

	private TextView tvMessage;
	private String phone="";
	private LocationManager locationManager=null;
	private LocationListener locationListener;
	private Boolean mylocationClicked=false;
	private Activity notificationActivity;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification_screen);
		//getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		notificationActivity=this;
		
		ContactsListSingleton.getInstance().APP_MODE=ContactsListSingleton.AppMode.NOTIFICATION;
		
		ParseAnalytics.trackAppOpened(getIntent());	
		
		try {
			JSONObject json = new JSONObject(getIntent().getExtras().getString("com.parse.Data"));
			phone= json.get(Net.USER).toString();
			
			
		} catch (JSONException e) {
			Toast.makeText(getApplicationContext(), "The Program has Crushed.", Toast.LENGTH_LONG).show();
			this.finish();
		}
		

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
		    	if(location.getAccuracy()<100)
		    	{
		    		 progressDialog.dismiss();
		    		 locationManager.removeUpdates(locationListener);			//not so working
		    		 Toast.makeText(notificationActivity, "Your location was sent back.", Toast.LENGTH_LONG).show();
		    		 answerHandler(getJSONDataMessage(Net.AnswerIsOK,location.getLatitude(),location.getLongitude()),phone);	
		    		 notificationActivity.finish();
		    		 
		    	}	
		    }
		    public void onStatusChanged(String provider, int status, Bundle extras) { }
		    public void onProviderEnabled(String provider) {}
		    public void onProviderDisabled(String provider) {}
		 };
		 
		 
		 if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) 
		 {
			 mylocationClicked=false;
			 Toast.makeText(getApplicationContext(), "Please turn on the GPS on high accuracy and try again", Toast.LENGTH_LONG).show();
			 return;
		 }
		 else
		 {
			 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		 }
		 			
		 
		 progressDialog = new ProgressDialog(this);

		 progressDialog.setTitle("Retrieving GPS coordinates");
		 progressDialog.setMessage("Please wait.."); 
		 progressDialog.show();	 
		 
		 mylocationClicked=true;			 
	}
	
	public void OnBtnNoThanksClick(View view)
	{	
		JSONObject data = getJSONDataMessage(Net.AnswerIsNo,0,0);
		answerHandler(data,phone);
		Toast.makeText(notificationActivity, "Your answer was sent.", Toast.LENGTH_LONG).show();
		this.finish();
	}
	
	public void OnBtnEnterAdressClick(View view)
	{	
		ContactsListSingleton.getInstance().APP_MODE=AppMode.NOTIFICATION;
		Intent intent = new Intent(this, FindAddress.class);
		startActivityForResult(intent, Extra.NOTIFICATION_RESULTCODE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
	    if (requestCode == Extra.NOTIFICATION_RESULTCODE && resultCode == RESULT_OK)
	    {
	    	double  latitude =  intent.getDoubleExtra(Extra.LATITUDE,0);
	    	double  longitude = intent.getDoubleExtra(Extra.LONGITUDE,0);
	    	
	    	 answerHandler(getJSONDataMessage(Net.AnswerIsOK,latitude,longitude),phone);	
	    	
	    	Toast.makeText(notificationActivity, "Your answer was sent.", Toast.LENGTH_LONG).show();
			this.finish();
	    }
	}
	
	
	public static JSONObject getJSONDataMessage(String answer,double latitude,double longitude)
	{
	    try
	    {
	        JSONObject data = new JSONObject();
	        data.put("action","com.tripper.mobile.Answer");
	        data.put(Net.USER, ParseUser.getCurrentUser().getUsername());
	        data.put(Net.ANSWER, answer);
	        if(answer!=Net.AnswerIsNo)
	        {
	        	data.put(Net.LATITUDE,  latitude);
	        	data.put(Net.LONGITUDE, longitude);
	        }
	        return data;
	    }
	    catch(JSONException x)
	    {
			throw new RuntimeException("Something wrong with JSON", x);
	    }
	}
	
	public static void answerHandler(JSONObject data, String targetNumber)
	{		 
		ParsePush push = new ParsePush();
		push.setChannel(Net.PhoneToChannel(ChannelMode.ANSWER,targetNumber)); 
		
		push.setExpirationTimeInterval(60*60*24);//one day, till query is relevant
		
		push.setData(data);
		push.sendInBackground();		
	}
	
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();
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



