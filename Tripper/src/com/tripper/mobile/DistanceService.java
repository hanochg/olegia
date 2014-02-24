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
import com.tripper.mobile.utils.Queries.Extra;
import com.tripper.mobile.utils.Queries.Net;
import com.tripper.mobile.utils.Queries.Net.ChannelMode;
import com.tripper.mobile.utils.Queries.Net.Messeges;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.telephony.gsm.SmsManager;




public class DistanceService extends IntentService  
{

	private LocationManager locationManager=null;
	private Notification note;
	private PendingIntent pi;
	private TextToSpeech ttobj;
	private int APP_MODE=-1;

	public DistanceService() {
		super("DistanceService");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onHandleIntent(Intent intent) 
	{			
		boolean flag;
		Location mylocation=null;
		Location targetlocation;
		ArrayList<ContactDataStructure> db=ContactsListSingleton.getInstance().getDB();
		APP_MODE = intent.getExtras().getInt(Extra.APP_MODE);

		setNotification();		
		InitializeSpeech();

		//long endTime = System.currentTimeMillis() + 120000;
		while (db!=null && !db.isEmpty()) 
		{
			flag=false;

			mylocation = getLastKnownLocation();

			if(mylocation!=null && db!=null)
			{

				if(APP_MODE==Extra.MULTI_DESTINATION)
				{
					ContactDataStructure contact;
					synchronized(db)
					{
						for (int i=0;i<db.size();i++)
						{
							contact=db.get(i);

							targetlocation = new Location(mylocation);
							targetlocation.setLatitude(contact.getLatitude());
							targetlocation.setLongitude(contact.getLongitude());

							if(contact.isSelected()==true)
							{
								note.setLatestEventInfo(this, "Tripper", "On the way to "+ contact.getName(), pi);
								startForeground(1337, note);
								flag=true;

							}

							if(contact.getContactAnswer()==eAnswer.ok &&  contact.getRadius()> mylocation.distanceTo(targetlocation))
							{
								note.tickerText="Message to get down was sent to "+ contact.getName();
								startForeground(1337, note);
								sendGetDownMessage(contact.getInternationalPhoneNumber());
								//db.remove(i);
								ttobj.speak("message was sent ." , TextToSpeech.QUEUE_FLUSH, null);
							}					
						}//for contacts
					}//synchronized

					if(flag==false)
					{
						note.setLatestEventInfo(this, "Tripper","Have a nice Trip!",pi);
						startForeground(1337, note);
					}

				}
				else if(APP_MODE==Extra.SINGLE_DESTINATION)
				{

					targetlocation = new Location("me");
					Address singleRouteCoordinates = ContactsListSingleton.getSingleRouteAddress();
					targetlocation.setLatitude(singleRouteCoordinates.getLatitude());
					targetlocation.setLongitude(singleRouteCoordinates.getLongitude());	

					if(mylocation.distanceTo(targetlocation)< 100)
					{
						note.tickerText="Messages were sent";
						startForeground(1337, note);
						
						sendGotToPlace();

						ContactDataStructure contact;
						synchronized(db)
						{
							for (int i=0;i<db.size();i++)
							{
								
								contact=db.get(i);
								if(contact.isAllowSMS() && contact.getContactAnswer()==eAnswer.single)
								{
									sendSMS(contact.internationalPhoneNumber);
									contact.setContactAnswer(eAnswer.singleWithMessage);
								}
							}
						}
						break;
					}
				}

			}//While

			try 
			{
				Thread.sleep(800);
			}
			catch (Exception e)
			{

			}			
		}
		if(ttobj !=null){
			ttobj.stop();
			ttobj.shutdown();
		}

		stopSelf();
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
		pi=PendingIntent.getActivity(this, 0,intent, 0);

		note=new Notification(R.drawable.icon,"New Trip started.",System.currentTimeMillis());
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



	private Location getLastKnownLocation()
	{ 
		Location l1=null; 	
		Location l2=null; 
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
		{
			l1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
		}
		if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			l2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		if (l1 == null && l2==null) 
			return null;
		else if(l1 == null)
			return l2;
		else if(l2 == null)
			return l1;
		else if(l1.getAccuracy() < l2.getAccuracy())
			return l1;
		else
			return l2;
	}


	@Override
	public void onDestroy()
	{
		super.onDestroy();
		stopForeground(true);
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
}