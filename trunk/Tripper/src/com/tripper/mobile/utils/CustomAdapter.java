package com.tripper.mobile.utils;

import java.util.List;

import com.tripper.mobile.R;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<Address>{


	Context context; 
	int layoutResourceId;    
	List<Address> data = null;
	private LayoutInflater mInflater; 	

	public CustomAdapter(Context context, int layoutResourceId, List<Address> data) {
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
			convertView = mInflater.inflate(R.layout.list_single_item, null);

			holder = new ViewHolder();

			//fill the views
			holder.address = (TextView) convertView.findViewById(R.id.ListTextView);

			convertView.setTag(holder);						
		} 
		else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();			
		}

		holder.address.setText(data.get(position).getAddressLine(1)+ "," +data.get(position).getAddressLine(0)+","+data.get(position).getAddressLine(2));

		return convertView;
	}

	class ViewHolder {		
		TextView address;
	}
}
