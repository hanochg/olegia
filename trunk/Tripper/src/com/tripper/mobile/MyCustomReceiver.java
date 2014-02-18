package com.tripper.mobile;

import org.json.JSONException;
import org.json.JSONObject;

import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactDataStructure.eAnswer;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyCustomReceiver extends BroadcastReceiver 
{
	public class ChannelMode
	{	
        public static final char INVITATION='a';
        public static final char ANSWER='b';
        public static final char GETDOWN='c';
	}
	
	public void onReceive(Context context, Intent intent)
	{
		char channel = intent.getExtras().getString("com.parse.Channel").charAt(0);
		
		JSONObject json;
		
	    try
	    {    	
	    	json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
	    }
	    catch (JSONException x) 
	    {
	    	throw new RuntimeException("Something wrong with JSON", x);
	    }
		
		switch(channel)
		{
			case ChannelMode.ANSWER:
				ANSWERHandler(json,context);
		        return ;  	
		}
	}
	
	public void ANSWERHandler(JSONObject json,Context context)
	{
		String answer="";
		String user="";
		ContactDataStructure contact;
		
		try
	    {    	
			answer=json.getString("answer");
			user=json.getString("user");
			
			contact=ContactsListSingleton.getInstance().findContactByPhoneNum(user);
			if (contact==null)
				return;
			
	    }
	    catch (JSONException x) 
	    {
	    	Log.e("MyCustomReceiver","ANSWERHandler-JNSON");
	    	return;
	    }
		
		if(answer=="ok")
		{	

			try
		    {    	
				double latitude=json.getDouble("latitude");
				double longtitude=json.getDouble("longitude");
				contact.setLatitude(latitude);
				contact.setLongtitude(longtitude);
				contact.setContactAnswer(eAnswer.ok);
		    }
		    catch (JSONException x) 
		    {
		    	Log.e("MyCustomReceiver","ANSWERHandler-ok-JNSON");
		    	return;
		    }

		}	
		else if (answer=="no")
		{
			contact.setContactAnswer(eAnswer.no);
		}
		
		Intent intent = new Intent("com.tripper.mobile.UPDATE");	
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		return;
	}
	
	
}
