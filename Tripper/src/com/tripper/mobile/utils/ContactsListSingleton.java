package com.tripper.mobile.utils;

import java.util.ArrayList;

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
			db.add(contact);
		else
			Log.e("ContactDataStructure","DB Not created before insertContact");
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