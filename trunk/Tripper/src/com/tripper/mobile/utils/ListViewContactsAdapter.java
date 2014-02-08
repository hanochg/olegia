package com.tripper.mobile.utils;

import java.util.ArrayList;
import java.util.List;

import com.tripper.mobile.R;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class ListViewContactsAdapter extends ArrayAdapter<ContactDataStructure>{


	Context context; 
	int layoutResourceId;    
	ArrayList<ContactDataStructure> data = null;
	private LayoutInflater mInflater; 	

	public ListViewContactsAdapter(Context context, int layoutResourceId, ArrayList<ContactDataStructure> data) {
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
			convertView = mInflater.inflate(R.layout.contact_list_item, null);

			holder = new ViewHolder();

			//fill the views
	        holder.contactsName = (TextView) convertView.findViewById(R.id.nameCL);
	        holder.contactsNumber = (TextView) convertView.findViewById(R.id.phoneNumCL);
	        holder.icon = (QuickContactBadge) convertView.findViewById(R.id.contactBadge);
	        convertView.setTag(holder);	
	        //holder.icon.setVisibility(View.INVISIBLE);
		} 
		else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
			
			

		}
		String name = data.get(position).getName();
		name="blabla";
		holder.contactsName.setText(name);
		holder.contactsNumber.setText(data.get(position).getPhoneNumber());
		return convertView;
	}

	class ViewHolder {		
        TextView contactsName;
        TextView contactsNumber;
        QuickContactBadge icon;
		
	}
}
