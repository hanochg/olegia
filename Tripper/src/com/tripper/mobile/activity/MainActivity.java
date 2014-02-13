package com.tripper.mobile.activity;



import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;

import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.tripper.mobile.R;

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
	
	public void OnBtnMultipleDestinationClick(View view)
	{	
		Intent intent = new Intent(this, FriendsList.class);
		intent.putExtra(getResources().getString(R.string.Choice),(long)getResources().getInteger(R.integer.MultipleDestination));
		startActivity(intent);	
	}
	
	public void OnBtnSingleDestinationClick(View view)
	{	
		Intent intent = new Intent(this, FindAddress.class);
		intent.putExtra(getResources().getString(R.string.Choice),(long)getResources().getInteger(R.integer.SingleDestination));
		startActivity(intent);     
	}
	
    public void onFinishEditDialog(String inputText) {
        Toast.makeText(this, "Hi, " + inputText, Toast.LENGTH_SHORT).show();
    }
}

