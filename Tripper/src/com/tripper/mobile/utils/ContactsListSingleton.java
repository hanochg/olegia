package com.tripper.mobile.utils;

import java.util.ArrayList;

import com.parse.CountCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.tripper.mobile.utils.ContactDataStructure.eAppStatus;

import android.util.Log;

public class ContactsListSingleton 
{
	static private ArrayList<ContactDataStructure> db=null;
	static private ContactsListSingleton instance=null;
	
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
			if(contains(contact.getPhoneNumber()))	
				return;
			//if its not already contained in the list
			db.add(contact);
			
			//*Parse*// 
			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.whereEqualTo("username", contact.getPhoneNumberforParse()); 
	    	
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
	
	public Boolean contains(String phone)
	{
		if(db!=null)
		{
			ContactDataStructure tempContact=null;
			//check if already contain the value
			for(int i=0 ; i<db.size() ; i++)
			{
				tempContact=db.get(i);
				if(tempContact.getPhoneNumber().equals(phone))
					return true;
			}			
		}
		return false; 
	}
	
	public void removeContactByPhoneNum(String phone) 
	{
		if(db!=null)
		{
			ContactDataStructure tempContact=null;
			//check if already contain the value
			for(int i=0 ; i<db.size() ; i++)
			{
				tempContact=db.get(i);
				if(tempContact.getPhoneNumber().equals(phone))
				{
					db.remove(i);
					return;
				}
			}
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
	
	public ArrayList<String> getAllPhonesForParse()
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
					phones.add(tempContact.getPhoneNumberforParse());
			}
		}	
		return phones;
	}
	
	
}