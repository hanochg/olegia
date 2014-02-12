package com.tripper.mobile.activity;

import com.parse.ParseAnalytics;
import com.tripper.mobile.R;
import com.tripper.mobile.R.layout;
import com.tripper.mobile.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class NotificationActivity extends Activity {

	
	private TextView tvMessege;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		ParseAnalytics.trackAppOpened(getIntent());
		this.tvMessege = (TextView) findViewById(R.id.tvMessage);
		
		
		tvMessege.setText(getIntent().getExtras().getString("com.parse.Data"));
		
		setContentView(R.layout.notification_screen);
	}
		
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.	
		getMenuInflater().inflate(R.menu.notification, menu);			
		return true;
	}
	
	
	

}
