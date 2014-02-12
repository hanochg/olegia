package com.tripper.mobile.utils;

import java.util.ArrayList;
import java.util.List;

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
	
	public void insertContact(ContactDataStructure contact) 
	{
		if(db!=null)
		{
			//check if already contain the value
			if(contains(contact.getPhoneNumber()))	
				return;

			//if its not already contained in the list
			db.add(contact);
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
	
	public ArrayList<String> getAllPhones()
	{
		ArrayList<String> phones = new ArrayList<String>();
		if(db!=null)
		{
			for(int i=0 ; i<db.size() ; i++)
			{
				phones.add(db.get(i).getPhoneNumber().replace("-", ""));
			}
		}	
		return phones;
	}
	
	
}