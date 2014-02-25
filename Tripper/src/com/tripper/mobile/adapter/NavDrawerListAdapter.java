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
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ContactDataStructure curContact=null;
		txtStatus=null;
		ImageView imgIcon=null;

		LayoutInflater mInflater = (LayoutInflater)
				context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		curContact = getItem(position);
		isContactWiderMenu=curContact.isSelected();
		Log.d("getView_NAV","User: " + curContact.getName() +"APP-"+curContact.getAppStatus()+" ANSWER- "+curContact.getContactAnswer());

		//SINGLE ROUTE SETTINGS
		if(curContact.getContactAnswer()==eAnswer.single ||
				curContact.getContactAnswer()==eAnswer.singleWithMessage)
		{
			Log.d("getView_NAV","In Single route settings");
			if (curContact.getAppStatus()==eAppStatus.noApp)
			{
				Log.d("getView_NAV","In Single route-NoApp");
				convertView = mInflater.inflate(R.layout.drawer_list_item_single_no_app, null);
				txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
				txtStatus.setText(context.getResources().getText(R.string.contact_no_app_status));
				setWiderMenuSingleRouteNoApp(convertView,curContact);
				imgIcon = (ImageView) convertView.findViewById(R.id.icon);
				imgIcon.setImageResource(R.drawable.warning_sign);
			}
			else
			{
				Log.d("getView_NAV","In Single route-HasApp");
				convertView = mInflater.inflate(R.layout.drawer_list_item_closed, null);
				imgIcon = (ImageView) convertView.findViewById(R.id.icon);
				imgIcon.setImageResource(R.drawable.ic_home);
			}	
		}
		//REGULAR CLOSED SETTINGS
		else if(!isContactWiderMenu)
		{
			Log.d("getView_NAV","In regular closed settings");
			convertView = mInflater.inflate(R.layout.drawer_list_item_closed, null);
			imgIcon = (ImageView) convertView.findViewById(R.id.icon);
		}		
		
		//NO APP SETTINGS
		if (curContact.getAppStatus()==eAppStatus.noApp && 
				curContact.getContactAnswer()!=eAnswer.single &&
						curContact.getContactAnswer()!=eAnswer.manual &&
								curContact.getContactAnswer()!=eAnswer.messageSent &&
									curContact.getContactAnswer()!=eAnswer.singleWithMessage)
		{
			Log.d("getView_NAV","In MultiRoute NoApp settings");
			if(isContactWiderMenu)
			{
				Log.d("getView_NAV","In MultiRoute NoApp settings-Wide");
				convertView = mInflater.inflate(R.layout.drawer_list_item_no_app, null);

				imgIcon = (ImageView) convertView.findViewById(R.id.icon);
			
				setWiderMenuNoApp(convertView,curContact);
				txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
				txtStatus.setText(context.getResources().getText(R.string.contact_no_app_status));
			}
			Log.d("getView_NAV","In MultiRoute NoApp settings-closed");
			imgIcon.setImageResource(R.drawable.warning_sign);
		}			
		else
			//HAVE APP + (SINGLE ROUTE or MANUAL or MESG SENT), THEN:
			switch (curContact.getContactAnswer())
			{
			//NOT ANSWERED SETTINGS 
			//APPSTATUS = NOT CHECKED (before checking app achieved) also gets in. 
			case notAnswered:				
				Log.d("getView_NAV","In NotAnswered settings");
				if(isContactWiderMenu)
				{
					Log.d("getView_NAV","In NotAnswered settings-wide");
					convertView = mInflater.inflate(R.layout.drawer_list_item_no_answer, null);
					
					imgIcon = (ImageView) convertView.findViewById(R.id.icon);
					
					setWiderMenuNoAnswer(convertView,curContact);
					txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
					txtStatus.setText(context.getResources().getText(R.string.contact_waiting_reply_status));
				}
				Log.d("getView_NAV","In NotAnswered settings-closed");
				imgIcon.setImageResource(R.drawable.question_mark);
				break;
			//ANSWERED NO! SETTINGS
			case no:
				Log.d("getView_NAV","In AnswerIsNo settings");
				if(isContactWiderMenu)
				{
					Log.d("getView_NAV","In AnswerIsNo settings-wide");
					
					//Same layout as No Answer
					convertView = mInflater.inflate(R.layout.drawer_list_item_no_answer, null);
					
					imgIcon = (ImageView) convertView.findViewById(R.id.icon);
					
					//Same options as No Answer
					setWiderMenuNoAnswer(convertView,curContact);
					txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
					txtStatus.setText(context.getResources().getText(R.string.contact_deny_response_status));
				}
				Log.d("getView_NAV","In AnswerIsNo settings-closed");
				imgIcon.setImageResource(R.drawable.red_circle);
				break;
			//ANSWERED YES SETTINGS
			case ok:
				Log.d("getView_NAV","In AnswerIsOK settings");
				if(isContactWiderMenu)
				{
					Log.d("getView_NAV","In AnswerIsOK settings-wide");
					convertView = mInflater.inflate(R.layout.drawer_list_item_replied, null);	
					
					imgIcon = (ImageView) convertView.findViewById(R.id.icon);
					
					setWiderMenuYes(convertView,curContact);
					txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
					txtStatus.setText(context.getResources().getText(R.string.contact_response_status));
				}
				Log.d("getView_NAV","In AnswerIsOK settings-closed");
				imgIcon.setImageResource(R.drawable.green_circle);
				break;
			//MANUAL ADDRESS ENTERED SETTINGS
			case manual:
				Log.d("getView_NAV","In ManualAddress settings");
				if(isContactWiderMenu)
				{
					Log.d("getView_NAV","In ManualAddress settings-wide");
					if(curContact.getAppStatus()==eAppStatus.hasApp)
					{
						convertView = mInflater.inflate(R.layout.drawer_list_item_replied, null);

						imgIcon = (ImageView) convertView.findViewById(R.id.icon);				
					}
					else if(curContact.getAppStatus()==eAppStatus.noApp)
					{
						convertView = mInflater.inflate(R.layout.drawer_list_item_manual_no_app, null);

						imgIcon = (ImageView) convertView.findViewById(R.id.icon);
					}
					setWiderMenuYes(convertView,curContact);
					txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
					txtStatus.setText(context.getResources().getText(R.string.contact_manual_status));
				}
				Log.d("getView_NAV","In ManualAddress settings-closed");
				imgIcon.setImageResource(R.drawable.green_circle);
				break;
			case messageSent:
				Log.d("getView_NAV","In messageSent settings");
				if(isContactWiderMenu)
				{
					Log.d("getView_NAV","In messageSent settings-wide");
					convertView = mInflater.inflate(R.layout.drawer_list_item_msg_sent, null);
					
					imgIcon = (ImageView) convertView.findViewById(R.id.icon);
					
					setWiderMenuMsgSent(convertView,curContact);
					txtStatus = (TextView) convertView.findViewById(R.id.contactStatus);
					txtStatus.setText(context.getResources().getText(R.string.contact_msg_sent_status));
				}
				Log.d("getView_NAV","In messageSent settings-closed");
				imgIcon.setImageResource(R.drawable.check);
				break;
			default:
				//Single route items can enter this part
				break;        
			}
		TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
		txtTitle.setText(curContact.getName());

		return convertView;
	}



	private void setWiderMenuMsgSent(View convertView,
			ContactDataStructure curContact) {
		
		final ContactDataStructure contact=curContact;
		
		final CheckBox allowSMSCheck = (CheckBox) convertView.findViewById(R.id.allowSMS);
		if(contact.getAppStatus()==eAppStatus.hasApp)
		{
			allowSMSCheck.setVisibility(View.GONE);
			/*RelativeLayout lowerLayout=(RelativeLayout)convertView.findViewById(R.id.drawerLowerLayout);
			LayoutParams params = lowerLayout.getLayoutParams();
			// Changes the height to the specified *DIP*
			params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 115, convertView.getResources().getDisplayMetrics());
			lowerLayout.setLayoutParams(params);*/
			
		}
		else
		{
		allowSMSCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(ContactsListSingleton.getInstance().isGlobalPreferenceAllowSMS())
					contact.setAllowSMS(isChecked);
				else
				{
					allowSMSCheck.setChecked(false);
					Toast.makeText(context, "App is not allowed to send SMS.\nYou can change it in Settings.", Toast.LENGTH_LONG).show();	
				}								
			}
		});
		}
		Button reSendGetDownButton = (Button) convertView.findViewById(R.id.resendMessage);
		reSendGetDownButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//TODO RESEND GET DOWN
				
			}
		});
		Button removeButton = (Button) convertView.findViewById(R.id.removeContact);
		removeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(ContactsListSingleton.getInstance().getDB().size()==1)
					Toast.makeText(context, "This is the last contact, cannot be removed.", Toast.LENGTH_LONG).show();
				else
				{
					new AlertDialog.Builder(context)
					.setTitle("Delete Contact")
					.setMessage("Are you sure you want to delete "+ contact.getName() +"?")
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							//Delete marker&circle if any
							if(contact.getRadiusOnMap()!=null)
								contact.getRadiusOnMap().remove();
							if(contact.getMarker()!=null)
								contact.getMarker().remove();
							
							//DELETE
							ContactsListSingleton.getInstance().removeContactByPhoneNum(contact.getPhoneNumber());
							
							//SEND UPDATE TO DRAWER
							Intent intent = new Intent("com.tripper.mobile.UPDATE");	
							LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 	                
							return;
						}
					})
					.show(); 

				}
			}
		});
		
	}

	private void setWiderMenuSingleRouteNoApp(View convertView,ContactDataStructure curContact) {
		final ContactDataStructure contact=curContact;
		
		final CheckBox allowSMSCheck = (CheckBox) convertView.findViewById(R.id.allowSMS);
		allowSMSCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(ContactsListSingleton.getInstance().isGlobalPreferenceAllowSMS())
					contact.setAllowSMS(isChecked);
				else
				{
					allowSMSCheck.setChecked(false);
					Toast.makeText(context, "App is not allowed to send SMS.\nYou can change it in Settings.", Toast.LENGTH_LONG).show();
				}
			}
		});

	}

	private void setWiderMenuYes(View convertView,ContactDataStructure curContact) {
		final ContactDataStructure contact=curContact;
		
		if(contact.getAppStatus()==eAppStatus.noApp)			
		{
			final CheckBox allowSMSCheck = (CheckBox) convertView.findViewById(R.id.allowSMS);
			allowSMSCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(ContactsListSingleton.getInstance().isGlobalPreferenceAllowSMS())
						contact.setAllowSMS(isChecked);
					else
					{
						allowSMSCheck.setChecked(false);
						Toast.makeText(context, "App is not allowed to send SMS.\nYou can change it in Settings.", Toast.LENGTH_LONG).show();	
					}								
				}
			});
		}
		
		
		Button setRadiusButton = (Button) convertView.findViewById(R.id.radiusSet);
		setRadiusButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final EditText text = new EditText(context);
				text.setText(String.valueOf(contact.getRadius()),TextView.BufferType.SPANNABLE);
				text.setInputType(InputType.TYPE_CLASS_NUMBER);
				text.selectAll();

				
				
				AlertDialog.Builder b = new AlertDialog.Builder(context)
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
				});
				AlertDialog dialog = b.create();
				dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				dialog.show();
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
				if(contact.getAppStatus()!=eAppStatus.notChecked)
				{
					sendNotification(contact);	
					Toast.makeText(context, "Request Sent Again!", Toast.LENGTH_LONG).show();
				}
				else
					Toast.makeText(context, "Please wait.\nStill checking for if contact has app.", Toast.LENGTH_LONG).show();
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

		final ContactDataStructure contact=curContact;
		
		final CheckBox allowSMSCheck = (CheckBox) convertView.findViewById(R.id.allowSMS);
		allowSMSCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(ContactsListSingleton.getInstance().isGlobalPreferenceAllowSMS())
					contact.setAllowSMS(isChecked);
				else
				{
					allowSMSCheck.setChecked(false);
					Toast.makeText(context, "App is not allowed to send SMS.\nYou can change it in Settings.", Toast.LENGTH_LONG).show();	
				}								
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
		Button removeButton = (Button) convertView.findViewById(R.id.removeContact);
		removeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(ContactsListSingleton.getInstance().getDB().size()==1)
					Toast.makeText(context, "This is the last contact, cannot be removed.", Toast.LENGTH_LONG).show();
				else
				{
					new AlertDialog.Builder(context)
					.setTitle("Delete Contact")
					.setMessage("Are you sure you want to delete "+ contact.getName() +"?")
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							//Delete marker&circle if any
							if(contact.getRadiusOnMap()!=null)
								contact.getRadiusOnMap().remove();
							if(contact.getMarker()!=null)
								contact.getMarker().remove();
							
							//DELETE
							ContactsListSingleton.getInstance().removeContactByPhoneNum(contact.getPhoneNumber());
							
							//SEND UPDATE TO DRAWER
							Intent intent = new Intent("com.tripper.mobile.UPDATE");	
							LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 	                
							return;
						}
					})
					.show(); 

				}
			}
		});
		
		
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
