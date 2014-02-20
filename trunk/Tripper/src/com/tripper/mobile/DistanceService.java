package com.tripper.mobile;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParsePush;
import com.parse.ParseUser;
import com.tripper.mobile.activity.OnMap;
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.ContactDataStructure.eAnswer;
import com.tripper.mobile.utils.Queries.Net;
import com.tripper.mobile.utils.Queries.Net.ChannelMode;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;




public class DistanceService extends IntentService  
{

	private LocationManager locationManager=null;
	Notification note;
	PendingIntent pi;

	public DistanceService() {
		super("DistanceService");
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{	
		setNotification();
		
		Location mylocation=null;
		ArrayList<ContactDataStructure> db=ContactsListSingleton.getInstance().getDB();
		Location targetlocation;

		long endTime = System.currentTimeMillis() + 40000;
		while (System.currentTimeMillis() < endTime) 
		{

			mylocation = getLastKnownLocation();

			//Log.e( "Place  " , Float.toString(mylocation.getAccuracy()));

			if(mylocation!=null && db!=null)
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
							
						}
												
						if(contact.getContactAnswer()==eAnswer.ok &&  contact.getRadius()> mylocation.distanceTo(targetlocation))
						{
							note.tickerText="Message down was sent to "+ contact.getName();
							//note.setLatestEventInfo(this, "aaaa", "zzzzzzzzz", pi);
							startForeground(1337, note);
							sendGetDownMessage(contact.getName());
							db.remove(contact.getId());
						}
						
					}
				}
			}

			try 
			{
				Thread.sleep(1000);
			}
			catch (Exception e)
			{

			}			
			//note.tickerText="sdasdasdasd";
			//note.setLatestEventInfo(this, "aaaa", "zzzzzzzzz", pi);
			//startForeground(1337, note);
		}

		stopSelf();
	}


	@SuppressWarnings("deprecation")
	private void  setNotification()
	{	
		note=new Notification(R.drawable.ic_launcher,"Checking Radius?",System.currentTimeMillis());
		Intent i=new Intent(this, OnMap.class);


		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
				Intent.FLAG_ACTIVITY_SINGLE_TOP);

		pi=PendingIntent.getActivity(this, 0,i, 0);

		note.setLatestEventInfo(this, "Tripper","Now Tripping: \"Ummmm, Nothing\"",pi);
		note.flags|=Notification.FLAG_NO_CLEAR;
		startForeground(1337, note);

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
			data.put("alert", "Get Down: " + ParseUser.getCurrentUser().getUsername());
			data.put(Net.USER, ParseUser.getCurrentUser().getUsername());

		}
		catch(JSONException x)
		{
			throw new RuntimeException("Something wrong with JSON", x);
		}
		push.setData(data);
		push.sendInBackground();
	}

}