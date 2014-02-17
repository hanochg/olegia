package com.tripper.mobile.utils;

import java.util.ArrayList;

import com.parse.CountCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.tripper.mobile.utils.ContactDataStructure.eAppStatus;

import android.content.Intent;
import android.location.Address;
import android.util.Log;

public class ContactsListSingleton 
{
	public enum AppMode{SINGLE_DESTINATION,MULTI_DESTINATION,NOTIFICATION };
	public AppMode APP_MODE;
	
	
	static private ArrayList<ContactDataStructure> db=null;
	static private ContactsListSingleton instance=null;
	private Address singleRouteCoordinates;

	
	public static void setSingleRouteAddress(Address address)
	{
			getInstance().singleRouteCoordinates=address;

	}
	
	public static Address getSingleRouteAddress()
	{
			return(getInstance().singleRouteCoordinates);

	}
	
	private ContactsListSingleton()
	{
		db=new ArrayList<ContactDataStructure>();
	}

	static public ContactsListSingleton getInstance()
	{
		if(instance==null)
			instance = new ContactsListSingleton();
		
		return instance;
	}
	
	public ArrayList<ContactDataStructure> getDB() 
	{
		return db; 
		
	}
	
	public void insertContact(final ContactDataStructure contact) 
	{
		if(db!=null)
		{
			//check if already contain the value
			if(indexOf(contact.getPhoneNumber())!=(-1))	
				return;
			//if its not already contained in the list
			db.add(contact);
			
			//*Parse*// 
			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.whereEqualTo("username","+"+ contact.getPhoneNumberforParse()); 
	    	
	    	query.countInBackground(new CountCallback() 
	    	{
	    		  public void done(int count, ParseException e) 
	    		  {  
	    		    if (e == null && contact!=null)
	    		    {
	    		      if(count!=0)
	    		    	  contact.UpdateAppStatus(eAppStatus.hasApp);
	    		      else
	    		    	  contact.UpdateAppStatus(eAppStatus.noApp);
	    		    } 
	    		    else if (contact!=null)
	    		    {
	    		      // The request failed,connection error
	    		    }
	    		  }
	    	}); 
		
			
		}
		else
			Log.e("ContactsListSingelton","DB Not created before insertContact");
	}
	
	public void setContactLocation(String phone, double lon, double lat)
	{
		int index = indexOf(phone);
		if(index!=(-1))
		{
			db.get(index).setLongtitude(lon);
			db.get(index).setLatitude(lat);			
		}
	}
	
	public int indexOf(String phone)
	{
		if(db!=null)
		{
			//check if already contain the value
			for(int i=0 ; i<db.size() ; i++)
			{
				if(db.get(i).getPhoneNumber().equals(phone))
					return i;
			}			
		}
		return (-1); 
	}
	
	public void removeContactByPhoneNum(String phone) 
	{
		if(db!=null)
		{
			int indexNum = indexOf(phone);
			if(indexNum!=(-1))
				db.get(indexNum);
		}			
		else
			Log.e("ContactsListSingelton","DB Not created before removeContactByPhoneNum");
	}
	
	public void removeContactByIndex(int index) 
	{
		if(db!=null)
		{
			try{
				db.remove(index);
			}
			catch (Exception e){
				Log.e("ContactsListSingelton","Error in removeContactByIndex: "+e.getMessage());
			}
		}
		else
				Log.e("ContactsListSingelton","DB Not created before removeContactByIndex");
	}
	
	public ArrayList<String> getAllChannelsForParse(String channelPrefix)
	{
		ArrayList<String> phones = null; 
		if(db!=null && !db.isEmpty())
		{
			phones = new ArrayList<String>();			
			ContactDataStructure tempContact=null;
			
			for(int i=0 ; i<db.size() ; i++)
			{
				tempContact=db.get(i);
				if(tempContact.getAppStatus() != ContactDataStructure.eAppStatus.noApp)
					phones.add(channelPrefix + tempContact.getPhoneNumberforParse());
			}
		}	
		return phones;
	}

	
}