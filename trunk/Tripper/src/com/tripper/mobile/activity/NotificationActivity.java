package com.tripper.mobile.activity;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.tripper.mobile.R;
import com.tripper.mobile.R.layout;
import com.tripper.mobile.R.menu;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.net.Uri;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationActivity extends Activity {

	private TextView tvMessage;
	private String phone="";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
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
			
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.	
		getMenuInflater().inflate(R.menu.notification, menu);			
		return true;
	}
	public void OnBtnMylocationClick(View view)
	{	
		
		/*
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        // Google Play services was not available for some reason
        } else {
        }
		
		*/
		
		
	}
	public void OnBtnNoThanksClick(View view)
	{	
		JSONObject data = getJSONDataMessage("No");
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
	
	private JSONObject getJSONDataMessage(String coordinates)
	{
	    try
	    {
	        JSONObject data = new JSONObject();
	        data.put("action","com.tripper.Answer");
	        data.put("User", phone);
	        data.put("Answer", coordinates);
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
		ParseQuery<ParseUser> query = ParseUser.getQuery();   	
		query.whereEqualTo("username", phone);  
    	
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		pushQuery.whereMatchesQuery("user", query);
		 
		ParsePush push = new ParsePush();
		push.setQuery(pushQuery); 
		
		push.setExpirationTimeInterval(60*60*24);//one day, till query is relevant
		
		push.setData(data);
		push.sendInBackground();		
	}

}



