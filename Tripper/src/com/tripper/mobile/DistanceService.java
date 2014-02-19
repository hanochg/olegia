package com.tripper.mobile;

import java.util.ArrayList;

import com.tripper.mobile.activity.OnMap;
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactsListSingleton;

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

	public DistanceService() {
		super("DistanceService");
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{	
		setNotification();
		long endTime = System.currentTimeMillis() + 20000;
		Location mylocation=null;
		ArrayList<ContactDataStructure> db=ContactsListSingleton.getInstance().getDB();
		Location targetlocation;
		
		
		while (System.currentTimeMillis() < endTime) 
		{

			mylocation = getLastKnownLocation();

			Log.e( "Place  " , Float.toString(mylocation.getAccuracy()));

			if(mylocation!=null && db!=null)
			{
				for (int i=0;i<db.size();i++)
				{
					targetlocation = new Location(mylocation);
					targetlocation.setLatitude(db.get(i).getLatitude());
					targetlocation.setLongitude(db.get(i).getLongitude());

					if(db.get(i).getRadius()> mylocation.distanceTo(targetlocation))
					{
						///
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
		}

		stopSelf();
	}


	@SuppressWarnings("deprecation")
	private void  setNotification()
	{	
		Notification note=new Notification(R.drawable.ic_launcher,"Checking Radius?",System.currentTimeMillis());
		Intent i=new Intent(this, OnMap.class);


		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
				Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pi=PendingIntent.getActivity(this, 0,i, 0);

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
}