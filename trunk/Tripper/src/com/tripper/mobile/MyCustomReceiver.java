package com.tripper.mobile;

import org.json.JSONException;
import org.json.JSONObject;

import com.tripper.mobile.map.OnMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
		
	    try
	    {    	
	    	 JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
	    }
	    catch (JSONException x) 
	    {
	    	throw new RuntimeException("Something wrong with JSON", x);
	    }
		
		switch(channel)
		{
			case ChannelMode.ANSWER:
		    	Intent intent2 = new Intent(context, OnMap.class);	
		    	context.startActivity(intent2);
		        return ;  	
		}
	}
	
}
