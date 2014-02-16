package com.tripper.mobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationActivity extends Activity {

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
		 
		 if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		 else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))  //don't have both
		 {
			 mylocationClicked=false;
			 Toast.makeText(getApplicationContext(), "Please turn on the GPS and try again", Toast.LENGTH_LONG).show();
			 return;
		 }
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
	
	/*		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.	
		getMenuInflater().inflate(R.menu.notification, menu);			
		return true;
	}*/

}



