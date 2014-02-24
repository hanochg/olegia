package com.tripper.mobile.adapter;


import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;

import com.parse.ParsePush;
import com.parse.ParseUser;
import com.tripper.mobile.R;
import com.tripper.mobile.activity.FindAddress;
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactDataStructure.eAnswer;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.Queries;
import com.tripper.mobile.utils.ContactDataStructure.eAppStatus;
import com.tripper.mobile.utils.Queries.Extra;
import com.tripper.mobile.utils.Queries.Net;
import com.tripper.mobile.utils.Queries.Net.ChannelMode;
import com.tripper.mobile.utils.Queries.Net.Messeges;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerListAdapter extends BaseAdapter {

	private boolean isContactWiderMenu;
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
		ImageView imgIcon=null;

		LayoutInflater mInflater = (LayoutInflater)
				context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		curContact = getItem(position);
		isContactWiderMenu=curContact.isSelected();
		

		if(curContact.getContactAnswer()!=eAnswer.single ||
				curContact.getContactAnswer()!=eAnswer.singleWithMessage)
		{
			imgIcon = (ImageView) convertView.findViewById(R.id.icon);
			imgIcon.setImageResource(R.drawable.ic_home);

			if (curContact.getAppStatus()==eAppStatus.noApp)
			{
				convertView = mInflater.inflate(R.layout.drawer_list_item_replied, null);
				txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
				txtStatus.setText(context.getResources().getText(R.string.contact_no_app_status));
				setWiderMenuSingleRouteNoApp(convertView,curContact);
			}
			else
			{
				convertView = mInflater.inflate(R.layout.drawer_list_item_closed, null);
			}
			
		}
		else if(!isContactWiderMenu)
		{
			convertView = mInflater.inflate(R.layout.drawer_list_item_closed, null);
			imgIcon = (ImageView) convertView.findViewById(R.id.icon);
		}





		
		if (curContact.getAppStatus()==eAppStatus.noApp && 
				curContact.getContactAnswer()!=eAnswer.single &&
					curContact.getContactAnswer()!=eAnswer.singleWithMessage)
		{
			if(isContactWiderMenu)
			{
				convertView = mInflater.inflate(R.layout.drawer_list_item_no_app, null);

				imgIcon = (ImageView) convertView.findViewById(R.id.icon);
				
				setWiderMenuNoApp(convertView,curContact);
				txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
				txtStatus.setText(context.getResources().getText(R.string.contact_no_app_status));
			}
			imgIcon.setImageResource(R.drawable.warning_sign);
		}			
		else
			switch (curContact.getContactAnswer())
			{
			case notAnswered:
				if(isContactWiderMenu)
				{
					convertView = mInflater.inflate(R.layout.drawer_list_item_no_answer, null);
					
					imgIcon = (ImageView) convertView.findViewById(R.id.icon);
					
					setWiderMenuNoAnswer(convertView,curContact);
					txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
					txtStatus.setText(context.getResources().getText(R.string.contact_waiting_reply_status));
				}
				imgIcon.setImageResource(R.drawable.question_mark);
				break;
			case no:
				if(isContactWiderMenu)
				{
					convertView = mInflater.inflate(R.layout.drawer_list_item_no_app, null);
					
					imgIcon = (ImageView) convertView.findViewById(R.id.icon);
					
					//Same options as No Answer
					setWiderMenuNoAnswer(convertView,curContact);
					txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
					txtStatus.setText(context.getResources().getText(R.string.contact_deny_response_status));
				}
				imgIcon.setImageResource(R.drawable.red_circle);
				break;
			case ok:
				if(isContactWiderMenu)
				{
					convertView = mInflater.inflate(R.layout.drawer_list_item_replied, null);	
					
					imgIcon = (ImageView) convertView.findViewById(R.id.icon);
					
					setWiderMenuYes(convertView,curContact);
					txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
					txtStatus.setText(context.getResources().getText(R.string.contact_response_status));
				}
				imgIcon.setImageResource(R.drawable.green_circle);
				break;
			case manual:
				if(isContactWiderMenu)
				{
					convertView = mInflater.inflate(R.layout.drawer_list_item_replied, null);
					
					imgIcon = (ImageView) convertView.findViewById(R.id.icon);
					
					setWiderMenuYes(convertView,curContact);
					txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
					txtStatus.setText(context.getResources().getText(R.string.contact_manual_status));
				}
				imgIcon.setImageResource(R.drawable.green_circle);
				break;
			default:
				break;        
			}
		TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
		txtTitle.setText(curContact.getName());

		return convertView;
	}

	private void setWiderMenuSingleRouteNoApp(View convertView,ContactDataStructure curContact) {
		final ContactDataStructure contact=curContact;
		
		CheckBox allowSMSCheck = (CheckBox) convertView.findViewById(R.id.allowSMS);
		//if()

	}

	private void setWiderMenuYes(View convertView,ContactDataStructure curContact) {
		final ContactDataStructure contact=curContact;
		

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
			AlertDialog alert;
			@Override
			public void onClick(View v) {
				
				final CharSequence[] choiceList =
					{"Waze", "Google Maps","Other" };	
				int selected = -1; // does not select anything

				AlertDialog.Builder builder =
						new AlertDialog.Builder(context);
				builder.setTitle("Select Navigation Application");		             		             
				builder.setSingleChoiceItems(
						choiceList,
						selected,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,int which) 
							{
								
								switch(which)
								{
								case 0: 
									//Waze Navigation
									/*  WAZE API
									 *  search for address: 	waze://?q=<address search term>
									 *	center map to lat / lon: 	waze://?ll=<lat>,<lon>
									 *	set zoom (minimum is 6): 	waze://?z=<zoom>
									 */
									try
									{
										String url = "waze://?ll="+ contact.getLatitude()+","+contact.getLongitude()+"&navigate=yes";//waze://?ll=40.3560493,-105.4533494&navigate=yes;								
										Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
										context.startActivity(intent);
										alert.dismiss();
									}
									catch ( ActivityNotFoundException ex  )
									{
										Intent intent =
												new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
										context.startActivity(intent);
									}	
									return;
									
								case 1://GoogleMaps Navigation							
									try{															
									    //can enter a String address after saddr\daddr
									    Uri uri = Uri.parse("google.navigation:q="+contact.getLatitude()+","+contact.getLongitude());
									    //Uri uri = Uri.parse("http://maps.google.com/maps?" + "saddr="+ latitudeCurr + "," + longitudeCurr + "&daddr="+contact.getLatitude()+","+contact.getLongitude());
									    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
									    intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
									    context.startActivity(intent);
									}catch(Exception e)
									{
										Log.e("Google Navigate","Error: "+e.getMessage());
									}
									
									alert.dismiss();
									return;
								case 2:
									try{															
									    //can enter a String address after saddr\daddr
									    Uri uri = Uri.parse("google.navigation:q="+contact.getLatitude()+","+contact.getLongitude());
									    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
									    //intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
									    context.startActivity(intent);
									}catch(Exception e)
									{
										Log.e("Other Application Navigate","Error: "+e.getMessage());
									}									
									alert.dismiss();
									return;
								}

							}
						});
				alert = builder.create();
				alert.show();

			}
		});

	}


	private void setWiderMenuNoAnswer(View convertView, ContactDataStructure curContact) {
		
		final ContactDataStructure contact=curContact;
		
		Button reRequestButton = (Button) convertView.findViewById(R.id.reRequest);
		reRequestButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				sendNotification(contact);				
			}
		});
		
		Button manualLocationButton = (Button) convertView.findViewById(R.id.manualLocation);
		manualLocationButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, FindAddress.class);
				intent.putExtra(Extra.APP_MODE,Extra.MULTI_DESTINATION);
				intent.putExtra(Extra.MANUAL_ADDRESS,contact.getPhoneNumber());
				context.startActivity(intent);
				
			}
		});
	}

	private void setWiderMenuNoApp(View convertView,ContactDataStructure curContact) {
		txtStatus.setText(context.getResources().getText(R.string.contact_no_app_status));

		final ContactDataStructure contact=curContact;
	}

	public void sendNotification(ContactDataStructure curContact)
	{	 
		ParsePush push = new ParsePush();
		push.setChannel(Net.PhoneToChannel(ChannelMode.INVITATION,curContact.getInternationalPhoneNumber()));
		
		push.setExpirationTimeInterval(60*60*24);//one day till query is relevant
		
		JSONObject data = new JSONObject();		
	    try
	    {
	    		data.put("alert", Messeges.INVITATION ); // ParseUser.getCurrentUser().getUsername());
	    		data.put(Net.USER, ParseUser.getCurrentUser().getUsername());

	    }
	    catch(JSONException x)
	    {
	    	throw new RuntimeException("Something wrong with JSON", x);
	    }
		push.setData(data);
		push.sendInBackground();
	}



}
