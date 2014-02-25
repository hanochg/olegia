package com.tripper.mobile.adapter;

import java.util.List;
import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactsListSingleton;
import android.content.Context;
import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class AddressAdapter extends ArrayAdapter<Address>{


	Context context; 
	int layoutResourceId;    
	List<Address> data = null;
	private LayoutInflater mInflater;
	private int lastCheckedPosition=-1;

	public AddressAdapter(Context context, int layoutResourceId, List<Address> data) {
		super(context, layoutResourceId, data);		  
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		this.mInflater = LayoutInflater.from(context);						
	}

	public void setLastCheckPosition(int pos)
	{
		lastCheckedPosition=pos;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {		

		ViewHolder holder = null;		       

		if (convertView == null) {

			//item_list
			convertView = mInflater.inflate(layoutResourceId, null);

			holder = new ViewHolder();

			//fill the views
			holder.address = (TextView) convertView.findViewById(R.id.addressItem);
			holder.checked = (RadioButton)convertView.findViewById(R.id.addressRadioButton);
			convertView.setTag(holder);						
		} 
		else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();			
		}
		
		//reading Settings
		String languageFromSettings = ContactsListSingleton.getInstance().getLanguageFromSettings();
		//check if language set to hebrew
		if(languageFromSettings.equals(getContext().getResources().getString(R.string.HebrewLanguage)))
			holder.address.setText(data.get(position).getAddressLine(0)+ "," +data.get(position).getAddressLine(1)+","+data.get(position).getAddressLine(2));
		else if(languageFromSettings.equals(getContext().getResources().getString(R.string.EnglishLanguage)))
			holder.address.setText(data.get(position).getAddressLine(2)+ "," +data.get(position).getAddressLine(1)+","+data.get(position).getAddressLine(0));
		else
			Log.e("AddressAdapter","Language not recognized!");
		
		if(position==lastCheckedPosition)
			holder.checked.setChecked(true);
		else
			holder.checked.setChecked(false);
		
		
		return convertView;
	}

	class ViewHolder {		
		TextView address;
		RadioButton checked;
	}
}
