package com.tripper.mobile.utils;

import android.net.Uri;
import android.util.Log;

public class ContactDataStructure 
{

	 public enum eAppStatus {
		   notChecked,noApp,hasApp
		 }
	 public enum eAnswer {
		   notAnswered,no,ok
		 }
	
	private String name;
	private String phoneNumber;
	public String internationalPhoneNumber;
	private long id;
	private String lookupkey;
	private Uri uri;
	private eAppStatus appStatus=eAppStatus.notChecked;
	private eAnswer contactAnswer=eAnswer.notAnswered;
	private double longtitude, latitude;
	public Object Locker;

	
	public ContactDataStructure()
	{		
		name=null;
		phoneNumber=null;
		id=0;
		lookupkey=null;
		uri=null;
		longtitude=0;
		latitude=0;
		internationalPhoneNumber="";
	}
	public ContactDataStructure(String name, String phoneNumber,long id, String lookupkey,Uri uri)
	{
		this.name=name;
		this.phoneNumber=phoneNumber;
		this.id=id;
		this.lookupkey=lookupkey;
		this.uri=uri;		
		this.internationalPhoneNumber="";
	}
	
	public double getLongtitude() {
		return longtitude;
	}
	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getLookupkey() {
		return lookupkey;
	}
	public void setLookupkey(String lookupkey) {
		this.lookupkey = lookupkey;
	}
	public Uri getUri() {
		return uri;
	}
	public void setUri(Uri uri) {
		this.uri = uri;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {		
		this.phoneNumber=phoneNumber;

	}
	
	public String getInternationalPhoneNumber() {
		String number;
		synchronized(internationalPhoneNumber)
		{
			number=internationalPhoneNumber;
		}
		return number;
	}
	
	public void setInternationalPhoneNumber(String number) {
		this.internationalPhoneNumber=number;
	}
	public void UpdateAppStatus(eAppStatus appStatus)
	{
		this.appStatus = appStatus;
	}
	
	public eAppStatus getAppStatus()
	{
		return appStatus;
	}
	
	public eAnswer getContactAnswer() {
		
		return contactAnswer;
	}
	public void setContactAnswer(eAnswer contactAnswer) {
		Log.d("setContactAnswer","answer: " + contactAnswer.toString());
		this.contactAnswer = contactAnswer;
	}
	
	public String getChannelforParse(char channelPrefix) 
	{
		String tempString=getInternationalPhoneNumber();
		
		if(tempString.startsWith("+"))
			tempString= tempString.substring(1);
		
		return channelPrefix+tempString;
	}	
}




