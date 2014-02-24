package com.tripper.mobile.utils;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;
import com.tripper.mobile.SettingsActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class ContactDataStructure 
{

	 public enum eAppStatus {
		   notChecked,noApp,hasApp
		 }
	 public enum eAnswer {
		   notAnswered,none,no,ok,manual,single,singleWithMessage,messageSent
		 }
	
	private String name;
	private String phoneNumber;
	public String internationalPhoneNumber;
	private long id;
	private String lookupkey;
	private Uri uri;
	private eAppStatus appStatus=eAppStatus.notChecked;
	private eAnswer contactAnswer=eAnswer.none;
	private double longitude, latitude;
	private double radius;
	private Marker marker; 
	private Circle radiusOnMap;
	private boolean isSelected=false;
	private boolean allowSMS=false;


	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public ContactDataStructure(Context context)
	{		
		//reading Settings
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String radiusStringFromSettings = sharedPref.getString(SettingsActivity.default_radius_text_multi, "");
		float radiusFromSettings = Float.parseFloat(radiusStringFromSettings);
		
		name=null;
		phoneNumber=null;
		id=0;
		lookupkey=null;
		uri=null;
		longitude=-1;
		latitude=-1;
		internationalPhoneNumber="";
		radius=radiusFromSettings;
		marker=null;
		radiusOnMap=null;
		allowSMS=false;
		
	}
	public ContactDataStructure(String name, String phoneNumber,long id, String lookupkey,Uri uri,Context context)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		float radiusFromSettings = sharedPref.getFloat(SettingsActivity.default_radius_text_multi, 0);
		
		this.radius=radiusFromSettings;
		this.name=name;
		this.phoneNumber=phoneNumber;
		this.id=id;
		this.lookupkey=lookupkey;
		this.uri=uri;		
		this.longitude=-1;
		this.latitude=-1;
		this.internationalPhoneNumber="";
		this.marker=null;
		this.radiusOnMap=null;
		allowSMS=false;
	}
	
	public boolean isAllowSMS() {
		return allowSMS;
	}
	public void setAllowSMS(boolean allowSMS) {
		this.allowSMS = allowSMS;
	}
	public Circle getRadiusOnMap() {
		return radiusOnMap;
	}
	public void setRadiusOnMap(Circle radiusOnMap) {
		this.radiusOnMap = radiusOnMap;
	}
	
	public Marker getMarker() {
		return marker;
	}
	public void setMarker(Marker marker) {
		this.marker = marker;
	}
	
	public double getRadius() {
		return radius;
	}
	public void setRadius(double radius) {
		this.radius = radius;
		radiusOnMap.setRadius(radius);
	}
	
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
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
		Log.d("setContactAnswer!!","answer: " + contactAnswer.toString());
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




