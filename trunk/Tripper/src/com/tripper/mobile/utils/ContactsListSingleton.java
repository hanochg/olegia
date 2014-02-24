package com.tripper.mobile.utils;

import java.util.ArrayList;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.parse.CountCallback;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.tripper.mobile.SettingsActivity;
import com.tripper.mobile.adapter.FriendsSelectedAdapter;
import com.tripper.mobile.utils.ContactDataStructure.eAnswer;
import com.tripper.mobile.utils.ContactDataStructure.eAppStatus;
import com.tripper.mobile.utils.Queries.Net.ChannelMode;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ContactsListSingleton 
{
	//public enum AppMode{SINGLE_DESTINATION,MULTI_DESTINATION,NOTIFICATION };
	//public AppMode APP_MODE;


	static private ArrayList<ContactDataStructure> db=null;
	static private ContactsListSingleton instance=null;

	private Address singleRouteCoordinates;
	private AsyncPhoneConverter asyncPhoneConverter;
	private String CountryTwoLetters;
	private String LanguageFromSettings;
	private boolean GlobalPreferenceAllowSMS=false;
	private double RadiusSingleFromSettings;

	public boolean isGlobalPreferenceAllowSMS() {
		return GlobalPreferenceAllowSMS;
	}

	public void setGlobalPreferenceAllowSMS(boolean globalPreferenceAllowSMS) {
		GlobalPreferenceAllowSMS = globalPreferenceAllowSMS;
	}

	public String getLanguageFromSettings() {
		return LanguageFromSettings;
	}

	public void setLanguageFromSettings(String languageFromSettings) {
		LanguageFromSettings = languageFromSettings;
	}

	public String getCountryTwoLetters() {
		return CountryTwoLetters;
	}

	public void setCountryTwoLetters(String countryTwoLetters) {
		CountryTwoLetters = countryTwoLetters;
	}

	public void setDefaultSettingsFromContex( Context context)
	{	
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String countryTwoLetters = sharedPref.getString(SettingsActivity.location_list,"");
		String languageFromSettings = sharedPref.getString(SettingsActivity.language_list,"");
		boolean allowSMS = sharedPref.getBoolean(SettingsActivity.pref_key_sms_allow,true);
		double radiusSingleRoute = (double)sharedPref.getFloat(SettingsActivity.default_radius_text_single,0);
		CountryTwoLetters = countryTwoLetters;
		LanguageFromSettings = languageFromSettings;
		GlobalPreferenceAllowSMS=allowSMS;
		RadiusSingleFromSettings=radiusSingleRoute;
	}


	public double getRadiusSingleFromSettings() {
		return RadiusSingleFromSettings;
	}

	public void setRadiusSingleFromSettings(double radiusSingleFromSettings) {
		RadiusSingleFromSettings = radiusSingleFromSettings;
	}


	public FriendsSelectedAdapter mFriendsSelectedAdapter;

	public void close()
	{
		db.clear();
		db=null;
		instance=null;
	}

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

		if (instance == null){
			synchronized(ContactsListSingleton.class)
			{
				if (instance == null)
					instance = new ContactsListSingleton();
			}
		}

		return instance;
	}

	public ArrayList<ContactDataStructure> getDB() 
	{
		return db; 

	}

	public synchronized void insertContact(final ContactDataStructure contact,FriendsSelectedAdapter mFriendsSelectedAdapter,Context context) 
	{

		if(db!=null)
		{
			//check if already contain the value
			if(indexOf(contact.getPhoneNumber())!=(-1))	
				return;
			//if its not already contained in the list
			db.add(contact);


			asyncPhoneConverter= new AsyncPhoneConverter(contact,mFriendsSelectedAdapter,context);
			asyncPhoneConverter.execute();

		}
		else
			Log.e("ContactsListSingelton","DB Not created before insertContact");
	}

	public synchronized void setContactLocation(String phone, double lon, double lat)
	{
		int index = indexOf(phone);
		if(index!=(-1))
		{
			db.get(index).setLongitude(lon);
			db.get(index).setLatitude(lat);	
			db.get(index).setContactAnswer(eAnswer.manual);
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

	public synchronized void removeContactByPhoneNum(String phone) 
	{
		if(db!=null)
		{
			int indexNum = indexOf(phone);
			if(indexNum!=(-1))
				db.remove(indexNum);
		}			
		else
			Log.e("ContactsListSingelton","DB Not created before removeContactByPhoneNum");
	}

	public ContactDataStructure findContactByPhoneNum(String phone)
	{
		if(db!=null)
		{
			//check if already contain the value
			for(int i=0 ; i<db.size() ; i++)
			{
				if(db.get(i).getInternationalPhoneNumber().equals(phone))
					return db.get(i);
			}			
		}
		return null; 
	}	


	public synchronized void removeContactByIndex(int index) 
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

	public ArrayList<String> getAllChannelsForParse(char channelPrefix)
	{
		ArrayList<String> phones = null; 
		if(db!=null && !db.isEmpty())
		{
			phones = new ArrayList<String>();			
			ContactDataStructure tempContact=null;

			for(int i=0 ; i<db.size() ; i++)
			{
				tempContact=db.get(i);
				if(channelPrefix==ChannelMode.INVITATION)
				{
					//When sending first massege 
					if(tempContact.getAppStatus() != eAppStatus.noApp && (tempContact.getContactAnswer()==eAnswer.none))
					{
						phones.add(tempContact.getChannelforParse(channelPrefix));
						tempContact.setContactAnswer(eAnswer.notAnswered);
					}
				}
				else if(channelPrefix==ChannelMode.LONERIDER)
				{
					//When got to point
					if(tempContact.getAppStatus() == eAppStatus.hasApp && (tempContact.getContactAnswer()==eAnswer.single))
					{
						phones.add(tempContact.getChannelforParse(channelPrefix));
						tempContact.setContactAnswer(eAnswer.singleWithMessage);
					}
				}
			}
		}	
		return phones;
	}


	private class AsyncPhoneConverter extends AsyncTask<Void, Void, Void>
	{


		PhoneNumber converedNumber;
		String phone;
		ContactDataStructure contact;
		String result=null;
		FriendsSelectedAdapter mFriendsSelectedAdapter;
		Context context;

		AsyncPhoneConverter(ContactDataStructure contact,FriendsSelectedAdapter mFriendsSelectedAdapter, Context context) 
		{
			super();
			this.contact = contact;
			converedNumber=null;
			this.phone = contact.getPhoneNumber();
			this.mFriendsSelectedAdapter=mFriendsSelectedAdapter;
			this.context=context;
		}

		@Override
		protected Void doInBackground(Void... none) {
			synchronized(contact.internationalPhoneNumber)
			{
				PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
				try {				
					converedNumber = phoneUtil.parse(phone, CountryTwoLetters);
					result=phoneUtil.format(converedNumber, PhoneNumberFormat.E164);
				} catch (Exception e) {
					Log.e("insertContact","NumberParseException was thrown: " + e.toString());
					result = null;
				}

				if(result==null || result.equals(""))
					contact.setInternationalPhoneNumber(phone.replace("-", ""));
				else
					contact.setInternationalPhoneNumber(result);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			Log.d("onPostExecute","inFunction");
			String number="";
			//*Parse*// 
			ParseQuery<ParseUser> query = ParseUser.getQuery();

			synchronized(contact)
			{
				if(contact!=null)
					number= contact.getInternationalPhoneNumber();				
			}
			query.whereEqualTo("username",number); 

			query.countInBackground(new CountCallback() 
			{
				public void done(int count, ParseException e) 
				{  
					synchronized(contact)
					{
						Log.d("countInBackground","done in synchronized");
						if (e == null && contact!=null)
						{
							Log.d("countInBackground","done in if");
							if(count!=0)
							{
								Log.d("countInBackground","done update to hasApp");
								contact.UpdateAppStatus(eAppStatus.hasApp);
							}
							else
							{
								Log.d("countInBackground","done update to noApp");
								contact.UpdateAppStatus(eAppStatus.noApp);
							}

							if(mFriendsSelectedAdapter!=null)
								mFriendsSelectedAdapter.notifyDataSetChanged();
							Intent intent = new Intent("com.tripper.mobile.UPDATE");	
							LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
						} 
						else if (contact!=null)
						{
							// The request failed,connection error
						}
					}

				}
			}); 
		}				
	}

}