package com.tripper.mobile.utils;

import java.util.ArrayList;

public class MyArrayList extends ArrayList<ContactsDataStructure>
{
	public MyArrayList()
	{
		super();
	}

	public StringBuilder numbersToQueryString() {
		StringBuilder string=new StringBuilder();
		for(int i=0;i<this.size();i++)
		{
			string.append(this.get(i).getPhoneNumber());
			if((i+1)!=this.size())
				string.append("");
		}
		return string;
	}

	
}