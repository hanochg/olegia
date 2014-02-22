package com.tripper.mobile.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;

import com.tripper.mobile.R;
import com.tripper.mobile.SettingsActivity;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.Queries;

public class MainActivity extends Activity
				{

	private BroadcastReceiver mMessageReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);	
		if(SplashScreen.splashActivity!=null)
			SplashScreen.splashActivity.finish();
		
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

		//reading Settings				
		ContactsListSingleton.getInstance().setRegionalSettingsFromContex(this);
		
		mMessageReceiver = new BroadcastReceiver() {
			  @Override
			  public void onReceive(Context context, Intent intent) 
			  {
				  String intentAction=intent.getAction();
				  if(intentAction.equals("com.tripper.mobile.EXIT"))
				  {
					  Log.d("onReceive","EXIT");
					  finish();
				  }
			  }
		};
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
	    case R.id.SettingsMain:	
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ContactsListSingleton.getInstance().close();
	}

	public void OnBtnMultipleDestinationClick(View view)
	{	
		//open Intent		
		Intent intent = new Intent(this, FriendsList.class);
		
		//define single destination mode:
		intent.putExtra(Queries.Extra.APP_MODE,Queries.Extra.MULTI_DESTINATION);
		startActivity(intent);
		
	}
	
	public void OnBtnSingleDestinationClick(View view)
	{	
		//open Intent
		Intent intent = new Intent(this, FindAddress.class);
		
		//define single destination mode:
		intent.putExtra(Queries.Extra.APP_MODE,Queries.Extra.SINGLE_DESTINATION);
		startActivity(intent);     
	}




	@Override
	protected void onResume() {
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("com.tripper.mobile.EXIT"));
		super.onResume();
	}




    
}

