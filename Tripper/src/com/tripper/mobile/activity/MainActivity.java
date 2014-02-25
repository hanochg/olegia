package com.tripper.mobile.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.tripper.mobile.R;
import com.tripper.mobile.SettingsActivity;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.Queries;

public class MainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);	

		//Reset Settings values
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		//Writing global settings Settings				
		ContactsListSingleton.getInstance().setDefaultSettingsFromContex(this);
		
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
		super.onResume();
	}




    
}

