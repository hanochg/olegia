package com.tripper.mobile.adapter;


import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;

import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.ContactDataStructure.eAppStatus;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerListAdapter extends BaseAdapter {

	private boolean contactWiderMenu;
	private Context context;
	private ArrayList<ContactDataStructure> contactsDB;
	private TextView txtStatus;
	public NavDrawerListAdapter(Context context){
		this.context = context;
		contactsDB = ContactsListSingleton.getInstance().getDB();
	}

	@Override
	public int getCount() {
		return contactsDB.size();
	}

	@Override
	public ContactDataStructure getItem(int position) {      
		return contactsDB.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void radiusSet(View v)
	{
		Log.d("radiusSet","radiusSet");
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ContactDataStructure curContact=null;
		txtStatus=null;

		LayoutInflater mInflater = (LayoutInflater)
				context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		curContact = getItem(position);
		contactWiderMenu=curContact.isSelected();
		
		if(!contactWiderMenu)
			convertView = mInflater.inflate(R.layout.drawer_list_item_closed, null);
		else
		{
			convertView = mInflater.inflate(R.layout.drawer_list_item_replyed, null);			
			txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);			
		}
		TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
		txtTitle.setText(curContact.getName());
		

		

		ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);

		if (curContact.getAppStatus()==eAppStatus.noApp)
		{
			if(contactWiderMenu)
				setWiderMenuNoApp(convertView,curContact);		
				
			imgIcon.setImageResource(R.drawable.warning_sign);
		}			
		else
			switch (curContact.getContactAnswer())
			{
			case notAnswered:
				if(contactWiderMenu)
					setWiderMenuNoAnswer(convertView,curContact);				
				imgIcon.setImageResource(R.drawable.question_mark);
				break;
			case no:
				if(contactWiderMenu)
					setWiderMenuNo(convertView,curContact);
					
				imgIcon.setImageResource(R.drawable.red_circle);
				break;
			case ok:
				if(contactWiderMenu)
					setWiderMenuYes(convertView,curContact);
					
				imgIcon.setImageResource(R.drawable.green_circle);
				break;
			default:
				break;        
			}


		return convertView;
	}

	private void setWiderMenuYes(View convertView,ContactDataStructure curContact) {
		final ContactDataStructure contact=curContact;
		txtStatus.setText(context.getResources().getText(R.string.contact_response_status));
		
		Button replyButton = (Button) convertView.findViewById(R.id.radiusSet);
		replyButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				final EditText text = new EditText(context);
				text.setInputType(InputType.TYPE_CLASS_NUMBER);
		    	new AlertDialog.Builder(context)
		        .setTitle("Radius Value")
		        .setMessage("Enter Radius Value (In Meters)")
		        .setView(text)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) { 
		            	final String value = text.getText().toString();
		            	contact.setRadius(Double.parseDouble(value));
		            }
		         })
		        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) { 	                
		            	return;
		            }
		         })
		         .show(); 
			}
		});
		
		Button navButton = (Button) convertView.findViewById(R.id.navigation);
		navButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				
			}
		});
		
	}

	private void setWiderMenuNo(View convertView,ContactDataStructure curContact) {
		final ContactDataStructure contact=curContact;
		txtStatus.setText(context.getResources().getText(R.string.contact_deny_response_status));
		
	}

	private void setWiderMenuNoAnswer(View convertView, ContactDataStructure curContact) {
		txtStatus.setText(context.getResources().getText(R.string.contact_waiting_reply_status));
		

	}

	private void setWiderMenuNoApp(View convertView,ContactDataStructure curContact) {
		txtStatus.setText(context.getResources().getText(R.string.contact_no_app_status));
		
		final ContactDataStructure contact=curContact;


		
	}




}
