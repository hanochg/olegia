package com.tripper.mobile.utils;

import java.util.ArrayList;
import java.util.Iterator;

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
		ContactDataStructure tempContact=null;
		
		if(db!=null)
		{
			//check if already contain the value
			if(contains(contact.getPhoneNumber()))	
				return;

			//if its not already contained in the list
			db.add(contact);
		}
		else
			Log.e("ContactDataStructure","DB Not created before insertContact");
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
	
	public void removeContact(ContactDataStructure contact) 
	{
		if(db!=null)
		{
			db.remove(contact);
		}			
		else
			Log.e("ContactDataStructure","DB Not created before removeContact");
	}
	

	
}