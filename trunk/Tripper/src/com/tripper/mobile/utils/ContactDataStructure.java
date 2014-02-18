package com.tripper.mobile.utils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import android.net.Uri;
import android.os.AsyncTask;
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
	private String internationalPhoneNumber;
	private long id;
	private String lookupkey;
	private Uri uri;
	private eAppStatus appStatus=eAppStatus.notChecked;
	private eAnswer contactAnswer=eAnswer.notAnswered;
	private double longtitude, latitude;
	private String CountryTwoLetters="IL";
	private AsyncPhoneConverter asyncPhoneConverter;
	
	public ContactDataStructure()
	{
		name=null;
		phoneNumber=null;
		id=0;
		lookupkey=null;
		uri=null;
		longtitude=0;
		latitude=0;
	}
	public ContactDataStructure(String name, String phoneNumber,long id, String lookupkey,Uri uri)
	{
		this.name=name;
		this.phoneNumber=phoneNumber;
		this.id=id;
		this.lookupkey=lookupkey;
		this.uri=uri;		
		
		asyncPhoneConverter = new AsyncPhoneConverter(phoneNumber);
		asyncPhoneConverter.execute();

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
		asyncPhoneConverter = new AsyncPhoneConverter(phoneNumber);
		asyncPhoneConverter.execute();
	}
	
	public String getInternationalPhoneNumber() {
		return internationalPhoneNumber;
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
		this.contactAnswer = contactAnswer;
	}
	
	public String getPhoneNumberforParse() 
	{
		String tempString=phoneNumber;
		
		if(tempString.startsWith("0"))
			tempString=tempString.replaceFirst("0", "972");		
		else if(tempString.startsWith("+972"))
			tempString= tempString.substring(1);
		
		return tempString.replace("-", "");
	}	
		
	public String convertNumberToInternationalNumber(String phone)
	{
		PhoneNumber converedNumber=null;

		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		try {
			converedNumber = phoneUtil.parse(phone, CountryTwoLetters);
		} catch (Exception e) {
			Log.e("insertContact","NumberParseException was thrown: " + e.toString());
			return null;
		}
		return phoneUtil.format(converedNumber, PhoneNumberFormat.E164);
	}
	
	private class AsyncPhoneConverter extends AsyncTask<Void, Void, String>
	{
		PhoneNumber converedNumber;
		String phone;
		
		AsyncPhoneConverter(String phone) 
		{
			super();
			converedNumber=null;
			this.phone = phone;
        }
		
		@Override
		protected String doInBackground(Void... none) {
			
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			try {
				converedNumber = phoneUtil.parse(phone, CountryTwoLetters);
			} catch (Exception e) {
				Log.e("insertContact","NumberParseException was thrown: " + e.toString());
				return null;
			}
			return phoneUtil.format(converedNumber, PhoneNumberFormat.E164);
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result==null || result.equals(""))
				internationalPhoneNumber=phone;
			else
				internationalPhoneNumber=result;
		}
	}

}




