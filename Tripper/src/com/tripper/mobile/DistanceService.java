package com.tripper.mobile;


import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParsePush;
import com.parse.ParseUser;
import com.tripper.mobile.activity.OnMap;
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.ContactDataStructure.eAnswer;
import com.tripper.mobile.utils.ContactDataStructure.eAppStatus;
import com.tripper.mobile.utils.Queries.Extra;
import com.tripper.mobile.utils.Queries.Net;
import com.tripper.mobile.utils.Queries.Net.ChannelMode;
import com.tripper.mobile.utils.Queries.Net.Messeges;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.gsm.SmsManager;
import android.util.Log;

public class DistanceService extends Service implements LocationListener {

	private LocationManager locationManager=null;
	private Notification note;
	private PendingIntent pi;
	private TextToSpeech ttobj;
	private int APP_MODE=-1;
	ArrayList<ContactDataStructure> db;
	Location targetlocation;

	LocationManager lm;

	public DistanceService() {}

	private final IBinder mBinder = new myBinder();

	public class myBinder extends Binder {
		public DistanceService getService() {
			// Return this instance of LocalService so clients can call public methods
			return DistanceService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	long starttime;


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		db=ContactsListSingleton.getInstance().getDB();
		APP_MODE = intent.getExtras().getInt(Extra.APP_MODE);
		locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		setNotification();
		InitializeSpeech();
		starttime=System.currentTimeMillis();


		return START_STICKY;
	}

	public void startFineLocations()
	{
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
	}
	public void startPassiveLocations()
	{
		locationManager.removeUpdates(this);
		locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 500, 0, this);
	}

	public void onLocationChanged(Location mylocation) 
	{
		boolean endFlag=true;
		/*
		Log.e( mylocation.getProvider(), "h");
		Log.e(Long.toString((System.currentTimeMillis()-starttime)/1000), Float.toString(mylocation.getAccuracy()));
		 */
		if(mylocation.getAccuracy()<30 && db!=null && !db.isEmpty()&& mylocation!=null )
		{

			if(APP_MODE==Extra.MULTI_DESTINATION)
			{
				ContactDataStructure contact;
				try
				{
					for (int i=0;i<db.size();i++)
					{
						contact=db.get(i);

						targetlocation = new Location(mylocation);
						targetlocation.setLatitude(contact.getLatitude());
						targetlocation.setLongitude(contact.getLongitude());
						if(	contact.getContactAnswer()!=eAnswer.messageSent && contact.getRadius()> mylocation.distanceTo(targetlocation))
						{
							if((contact.getContactAnswer()==eAnswer.ok ||  contact.getContactAnswer()==eAnswer.manual) &&
									contact.getAppStatus()==eAppStatus.hasApp)
							{
								sendGetDownMessage(contact.getInternationalPhoneNumber());
								speechAndUpdateAfterMessege(contact);
							}			
							else if(ContactsListSingleton.getInstance().isGlobalPreferenceAllowSMS() && contact.isAllowSMS() &&
									contact.getContactAnswer()==eAnswer.manual && contact.getAppStatus()==eAppStatus.noApp)
							{
								sendSMS(contact.getInternationalPhoneNumber());
								speechAndUpdateAfterMessege(contact);							
							}		

						}
						if(contact.getContactAnswer()!= eAnswer.messageSent )
						{
							endFlag=false;
						}
					}//for contacts


				}//try
				catch(Exception e)
				{
				}


			}
			else if(APP_MODE==Extra.SINGLE_DESTINATION)
			{

				targetlocation = new Location("me");
				Address singleRouteCoordinates = ContactsListSingleton.getSingleRouteAddress();
				targetlocation.setLatitude(singleRouteCoordinates.getLatitude());
				targetlocation.setLongitude(singleRouteCoordinates.getLongitude());	

				if(mylocation.distanceTo(targetlocation)< ContactsListSingleton.getInstance().getRadiusSingleFromSettings())
				{
					note.tickerText="Messages were sent";
					startForeground(1337, note);

					sendGotToPlace();

					ContactDataStructure contact;
					try
					{
						for (int i=0;i<db.size();i++)
						{

							contact=db.get(i);
							if(ContactsListSingleton.getInstance().isGlobalPreferenceAllowSMS() && contact.isAllowSMS() && 
									contact.getContactAnswer()==eAnswer.single && contact.getAppStatus()==eAppStatus.noApp)
							{
								sendSMS(contact.getInternationalPhoneNumber());
								contact.setContactAnswer(eAnswer.singleWithMessage);
							}


						}
					}
					catch(Exception e)
					{
					}
				}
				
				ContactDataStructure contact;
				for (int i=0;i<db.size();i++)
				{
					contact=db.get(i);
					if(contact.getContactAnswer()!= eAnswer.singleWithMessage )
					{
						endFlag=false;
					}
				}
			}	//SINGLE_DESTINATION
			if( endFlag==true)
			{
				myClose();
			}
		}//main if
	}
	public void onProviderEnabled(String s){}

	public void onProviderDisabled(String s) {}
	public void onStatusChanged(String s, int i, Bundle b){}


	public void speechAndUpdateAfterMessege(ContactDataStructure contact)
	{
		contact.setContactAnswer(eAnswer.messageSent);

		note.tickerText="Message to get down was sent to "+ contact.getName();
		startForeground(1337, note);

		//SEND UPDATE TO DRAWER
		Intent updateIntent = new Intent("com.tripper.mobile.UPDATE");	
		LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);

		//talk!
		ttobj.speak("arrival message sent ." , TextToSpeech.QUEUE_FLUSH, null);

		//MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.get_down);
		//mPlayer.start();
	}



	private void InitializeSpeech()
	{
		ttobj=new TextToSpeech(getApplicationContext(), 
				new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if(status != TextToSpeech.ERROR)
				{
					ttobj.setLanguage(Locale.UK);
				}				
			}
		});
	}



	@SuppressWarnings("deprecation")
	private void  setNotification()
	{	
		Intent intent=new Intent(this, OnMap.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(Extra.APP_MODE, APP_MODE);
		pi=PendingIntent.getActivity(this, 231,intent, 0);

		note=new Notification(R.drawable.ic_stat_icon,"New Trip started.",System.currentTimeMillis());
		note.flags|=Notification.FLAG_NO_CLEAR;
		note.setLatestEventInfo(this, "Tripper","Have a nice Trip!",pi);

		startForeground(1337, note);

		/* NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
	    builder.setTicker("New Trip started.").setContentTitle( "Tripper").setContentText("Have a nice Trip!")
	            .setWhen(System.currentTimeMillis()).setAutoCancel(false)
	            .setOngoing(true).setContentIntent(pi);
	    note = builder.build();
	   // note.flags |= Notification.FLAG_NO_CLEAR;
	    //startForeground(1337, note);
	    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1337, note);*/
	}



	public void sendGetDownMessage(String targetNumber)
	{	 
		ParsePush push = new ParsePush();
		push.setChannel(Net.PhoneToChannel(ChannelMode.GETDOWN,targetNumber)); 

		push.setExpirationTimeInterval(60*60*24);//one day till query is relevant

		JSONObject data = new JSONObject();		
		try
		{
			data.put("alert", Messeges.GETDOWN);
			data.put(Net.USER, ParseUser.getCurrentUser().getUsername());

		}
		catch(JSONException x)
		{
			throw new RuntimeException("Something wrong with JSON", x);
		}
		push.setData(data);
		push.sendInBackground();
	}



	public void sendGotToPlace()
	{	 
		ParsePush push = new ParsePush();
		ArrayList<String> phones = ContactsListSingleton.getInstance().getAllChannelsForParse(ChannelMode.LONERIDER);
		push.setChannels(phones);

		push.setExpirationTimeInterval(60*60*24);//one day till query is relevant

		JSONObject data = new JSONObject();		
		try
		{
			data.put("alert", Messeges.GOTTOPLACE ); // ParseUser.getCurrentUser().getUsername());
			data.put(Net.USER, ParseUser.getCurrentUser().getUsername());

		}
		catch(JSONException x)
		{
			throw new RuntimeException("Something wrong with JSON", x);
		}
		push.setData(data);
		push.sendInBackground();
	}

	public void sendSMS(String phoneNumber)
	{	 
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, "Got to Place", null, null);
	}


	@Override
	public void onDestroy() 
	{	
		super.onDestroy();	
		myClose();
	}
	private void myClose()
	{
		locationManager.removeUpdates(this);
		if(ttobj !=null){
			ttobj.stop();
			ttobj.shutdown();
		}
		stopForeground(true);
		stopSelf();
	}

}