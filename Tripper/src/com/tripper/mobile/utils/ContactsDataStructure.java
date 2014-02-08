package com.tripper.mobile.utils;

public class ContactsDataStructure {

	private String name;
	private String phoneNumber;
	
	public ContactsDataStructure()
	{
		name=null;
		phoneNumber=null;
	}
	public ContactsDataStructure(String name, String phoneNumber)
	{
		this.name=name;
		this.phoneNumber=phoneNumber;
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
