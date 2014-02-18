package com.tripper.mobile.activity;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactsListSingleton;

public class MainActivity extends FragmentActivity  {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);	
		SplashScreen.splashActivity.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ContactsListSingleton.getInstance().close();
	}

	public void OnBtnMultipleDestinationClick(View view)
	{	
		//define single destination mode:
		ContactsListSingleton.getInstance().APP_MODE=ContactsListSingleton.AppMode.MULTI_DESTINATION;
		/*
		//open Intent		
		Intent intent = new Intent(this, NavigationActivity.class);
		startActivity(intent);
		*/	
		
		//open Intent		
		Intent intent = new Intent(this, FriendsList.class);
		startActivity(intent);
		
	}
	
	public void OnBtnSingleDestinationClick(View view)
	{	
		//define single destination mode:
		ContactsListSingleton.getInstance().APP_MODE=ContactsListSingleton.AppMode.SINGLE_DESTINATION;
		
		//open Intent
		Intent intent = new Intent(this, FindAddress.class);
		startActivity(intent);     
	}
	
    public void onFinishEditDialog(String inputText) {
        Toast.makeText(this, "Hi, " + inputText, Toast.LENGTH_SHORT).show();
    }
}

