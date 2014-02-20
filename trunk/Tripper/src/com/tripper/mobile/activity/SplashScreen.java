package com.tripper.mobile.activity;


import java.util.Timer;
import java.util.TimerTask;

import com.parse.ParseUser;
import com.tripper.mobile.R;
import com.tripper.mobile.utils.Queries.Extra;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class SplashScreen extends Activity {
	
	public static Activity splashActivity;
		
	private ParseUser currentUser=null;	
	private String mPhoneNumber="";
	
	private boolean networkConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		splashActivity=this;

		setContentView(R.layout.splash_screen);

		Timer myTimer = new Timer();
		myTimer.schedule(new TimerTask()
		{
			public void run() 
			{
				
				if (!networkConnection)	//Exit, connection error.(to Exit the timer thread)
				{
					splashActivity.finish(); 
					return;
				}
				
				Intent intent;
				
				if (currentUser != null) 
				{
					intent = new Intent(splashActivity, MainActivity.class);					
				}
				else
				{
					intent = new Intent(splashActivity, LoginActivity.class);
					intent.putExtra(Extra.PHONE, mPhoneNumber);
				}
				startActivity(intent);	
								
				//if (!networkConnection)
				//{
				//	intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
				//	startActivity(intent);
				//}
			}
		},getResources().getInteger(R.integer.splash_screen_timeout)); 	
			
		networkConnection=isNetworkAvailable();
		
		if (!networkConnection)
		{
			Toast.makeText(getApplicationContext(), "You must be connected to the internet", Toast.LENGTH_LONG).show();
			return; //Exit, connection error.
		}
		
		//ParseUser.logOut();
		currentUser = ParseUser.getCurrentUser();	
		
		if(currentUser==null)
		{
			try{
			mPhoneNumber = ((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).getLine1Number();
			if(mPhoneNumber.isEmpty())
			{
				AccountManager am = AccountManager.get(this);
				Account[] accounts = am.getAccountsByType("com.whatsapp");
			
				if(accounts.length!=0)
				{
					mPhoneNumber="+" + accounts[0].name;
				}
			}	
			}catch(Exception e){
				Log.e("SPLASH","NUMBER ERROR");
			}
			
			}
		}
	
	private boolean isNetworkAvailable()
	{
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	protected void onPause() {	
		// TODO Auto-generated method stub
		super.onPause();
	}


	@Override
	protected void onDestroy() {
		splashActivity=null;
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}

}
