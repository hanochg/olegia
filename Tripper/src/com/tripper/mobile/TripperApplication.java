package com.tripper.mobile;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;
import com.tripper.mobile.activity.SplashScreen;

public class TripperApplication extends android.app.Application {	
	
  public TripperApplication() {
  }

  
  
  @Override
  public void onCreate() {
    super.onCreate();

    Parse.initialize(this, "dWDyR9aVsulbZmhK9HHG5VhMyHmYpCIa7YMxSott", "2D6DdeglHFpi96NexPVIQS6E9jh8dAsbhonQeaDx"); 
    
	// Specify an Activity to handle all pushes by default.  
   
	//PushService.setDefaultPushCallback(this, com.tripper.mobile.activity.NotificationActivity.class);
  }
}