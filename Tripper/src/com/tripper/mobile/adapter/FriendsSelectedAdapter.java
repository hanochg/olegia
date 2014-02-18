package com.tripper.mobile.adapter;

import java.util.ArrayList;
import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class FriendsSelectedAdapter extends ArrayAdapter<ContactDataStructure>{


	Context context; 
	int layoutResourceId;    
	ArrayList<ContactDataStructure> data = null;
	private LayoutInflater mInflater; 	
	
	public FriendsSelectedAdapter(Context context, int layoutResourceId,ArrayList<ContactDataStructure> data) {
		super(context, layoutResourceId, data);		  
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		this.mInflater = LayoutInflater.from(context);						
	}

	public View getView(final int position, View convertView, ViewGroup parent) {		
		
		ViewHolder holder = null;		       

		if (convertView == null) {

			//item_list
			convertView = mInflater.inflate(layoutResourceId,parent, false);

			holder = new ViewHolder();

			//fill the views
	        holder.contactsName = (TextView) convertView.findViewById(R.id.nameFL);
	        holder.contactsNumber = (TextView) convertView.findViewById(R.id.phoneNumFL);
	        convertView.setTag(holder);	
		} 
		else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		


		holder.contactsName.setText(data.get(position).getName());
		holder.contactsNumber.setText(data.get(position).getPhoneNumber());
		return convertView;
	}

	class ViewHolder {		
        TextView contactsName;
        TextView contactsNumber;
        QuickContactBadge icon;
		
	}
}
