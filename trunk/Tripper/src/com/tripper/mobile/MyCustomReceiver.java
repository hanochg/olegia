package com.tripper.mobile;

import org.json.JSONException;
import org.json.JSONObject;

import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactDataStructure.eAnswer;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.Queries.Net;
import com.tripper.mobile.utils.Queries.Net.ChannelMode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyCustomReceiver extends BroadcastReceiver 
{

	
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
			answer=json.getString(Net.ANSWER);
			user=json.getString(Net.USER);
			
			contact=ContactsListSingleton.getInstance().findContactByPhoneNum(user);
			if (contact==null)
				return;
			
	    }
	    catch (JSONException x) 
	    {
	    	Log.e("MyCustomReceiver","ANSWERHandler-JNSON");
	    	return;
	    }
		
		if(answer.equals(Net.AnswerIsOK))
		{	
			try
		    {    	
				double latitude=json.getDouble(Net.LATITUDE);
				double longtitude=json.getDouble(Net.LONGITUDE);
				contact.setLatitude(latitude);
				contact.setLongitude(longtitude);
				contact.setContactAnswer(eAnswer.ok);
		    }
		    catch (JSONException x) 
		    {
		    	Log.e("MyCustomReceiver","ANSWERHandler-ok-JNSON");
		    	return;
		    }

		}	
		else if (answer.equals(Net.AnswerIsNo))
		{
			contact.setContactAnswer(eAnswer.no);
		}
		
		Intent intent = new Intent("com.tripper.mobile.UPDATE");	
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		return;
	}
	
	
}
