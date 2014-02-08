package com.tripper.mobile.utils;

import android.net.Uri;

public class ContactDataStructure {

	private String name;
	private String phoneNumber;
	private long id;
	private String lookupkey;
	private Uri uri;
	
	public ContactDataStructure()
	{
		name=null;
		phoneNumber=null;
		id=0;
		lookupkey=null;
		uri=null;
	}
	public ContactDataStructure(String name, String phoneNumber,long id, String lookupkey,Uri uri)
	{
		this.name=name;
		this.phoneNumber=phoneNumber;
		this.id=id;
		this.lookupkey=lookupkey;
		this.uri=uri;		
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
		this.phoneNumber = phoneNumber;
	}
}
