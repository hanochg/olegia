package com.tripper.mobile.activity;


import java.util.Timer;
import java.util.TimerTask;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.tripper.mobile.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class SplashScreen extends Activity {

	private Intent intent;
	public static Activity splashActivity;
	ParseUser currentUser=null;
	Boolean flag=false;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		splashActivity=this;

		setContentView(R.layout.splash_screen);
		Parse.initialize(this, "dWDyR9aVsulbZmhK9HHG5VhMyHmYpCIa7YMxSott", "2D6DdeglHFpi96NexPVIQS6E9jh8dAsbhonQeaDx"); 

		//ParseUser.logOut();
		currentUser = ParseUser.getCurrentUser();

		if (currentUser == null) 
		{
			ParseUser.logInInBackground("ab@jhkhjkgkga", "oooo", new LogInCallback() 
			{
				public void done(ParseUser user, ParseException e) 
				{
					flag=true;
					if (user != null) 
					{
						currentUser=user;
					} 
					else
					{
						// Signup failed. Look at the ParseException to see what happened.
					}
				}
			});	
		}
		else
		{
			flag=true;
		}
		//////
		Timer myTimer = new Timer();
		myTimer.schedule(new TimerTask(){
			public void run() 
			{
				while(!flag);

				if (currentUser != null) 
				{
					intent = new Intent(splashActivity, MainActivity.class);
				}
				else
				{

					intent = new Intent(splashActivity, LoginActivity.class);
				}
				startActivity(intent);	
			}
		},getResources().getInteger(R.integer.splash_screen_timeout)); 	
	}

	@Override
	protected void onPause() {	
		// TODO Auto-generated method stub
		super.onPause();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}

}
