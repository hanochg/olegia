package com.tripper.mobile.activity;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParsePush;
import com.parse.ParseUser;
import com.tripper.mobile.R;
import com.tripper.mobile.SettingsActivity;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import com.tripper.mobile.adapter.FilterCursorWrapper;
import com.tripper.mobile.adapter.FriendsAutoCompleteAdapter;
import com.tripper.mobile.adapter.FriendsSelectedAdapter;
import com.tripper.mobile.utils.*;
import com.tripper.mobile.utils.Queries.Net;
import com.tripper.mobile.utils.Queries.Net.ChannelMode;

public class FriendsList extends Activity implements
						LoaderManager.LoaderCallbacks<Cursor>
{
	ListView mSelectedContactsList;
	SimpleCursorAdapter mCursorAdapter;
	private FriendsAutoCompleteAdapter mAutoCompleteAdapter; // The main query adapter
	Context context;
	ImageButton contactsButton;
	AutoCompleteTextView actvContacts;
    String mSearchString=null;
    FriendsSelectedAdapter mFriendsSelectedAdapter;
    private final int SPEECH_REQUEST_CODE = 10;
    private final int CONTACTLIST_REQUEST_CODE = 11;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.friends_list);		
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
        mAutoCompleteAdapter = new FriendsAutoCompleteAdapter(this);
		
		contactsButton=(ImageButton) findViewById(R.id.contactsButton);
		contactsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, ContactsList.class);
				startActivityForResult(intent,CONTACTLIST_REQUEST_CODE);	
				
			}
		});
		
		//Gets AutoCompleteTextView
		actvContacts=(AutoCompleteTextView) findViewById(R.id.acContactName);
		
        // Gets the ListView from the View list of the parent activity
		mSelectedContactsList = (ListView) findViewById(R.id.lvContactList);
        
        mFriendsSelectedAdapter=
        		new FriendsSelectedAdapter(this,R.layout.friends_list_row_item,ContactsListSingleton.getInstance().getDB());
        mSelectedContactsList.setAdapter(mFriendsSelectedAdapter);
        
        ContactsListSingleton.getInstance().mFriendsSelectedAdapter=mFriendsSelectedAdapter;
        
        actvContacts.setAdapter(mAutoCompleteAdapter);
        
		actvContacts.setOnItemClickListener(
				new OnItemClickListener() {

			@Override
		    public void onItemClick(
		            AdapterView<?> parent, View item, int position, long rowID) {
		        
				ContactDataStructure contact = new ContactDataStructure(context);
				
				// Gets the Cursor object currently bound to the ListView
		        final Cursor cursor = mAutoCompleteAdapter.getCursor();

		        // Moves to the Cursor row corresponding to the ListView item that was clicked
		        cursor.moveToPosition(position);

		        // Creates a contact lookup Uri from contact ID and lookup_key
		        final Uri uri = Contacts.getLookupUri(
		                cursor.getLong(Queries.ID),
		                cursor.getString(Queries.LOOKUP_KEY));
		        		        
		        contact.setId(cursor.getLong(Queries.ID));
		        contact.setLookupkey(cursor.getString(Queries.LOOKUP_KEY));
		        contact.setName(cursor.getString(Queries.DISPLAY_NAME));
		        contact.setPhoneNumber(cursor.getString(Queries.PHONE_NUM));
		        contact.setUri(uri);
		        
		        ContactsListSingleton.getInstance().insertContact(contact,mFriendsSelectedAdapter);
		        
		        mFriendsSelectedAdapter.notifyDataSetChanged();
		        actvContacts.setText("");
			}
		});
			

		actvContacts.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				  Log.d("PROGRAM!!", "Text Changed");
				  
              	  mSearchString=s.toString();
              	  mAutoCompleteAdapter.setSearchString(mSearchString);
              	
                  // restart the loader
                  getLoaderManager().restartLoader(Queries.LoaderManagerID, null, FriendsList.this);
            }
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
        // Initializes the loader
        getLoaderManager().initLoader(Queries.LoaderManagerID, null,  this);
      }
	
	//##Google speech##
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK)
	    {
	        // Populate the wordsList with the String values the recognition engine thought it heard
	        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	        actvContacts.setText(matches.get(0));
	    }
	    if(requestCode == CONTACTLIST_REQUEST_CODE && resultCode == RESULT_OK)
	    {
	    	mFriendsSelectedAdapter.notifyDataSetChanged();
	    	Log.d("onActivityResult","CONTACTLIST_REQUEST_CODE");
	    }
	}
	public void speechActivation(View view)
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me the contact name...");
		startActivityForResult(intent, SPEECH_REQUEST_CODE);
	}
    

	//##Implements LoaderManager.LoaderCallbacks<Cursor>##
	
@Override
public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) { 
		CursorLoader cur=null;
	if (Queries.LoaderManagerID == loaderId)
    {
			Uri FilteredUri;
			if (!TextUtils.isEmpty(mSearchString))
				FilteredUri = Uri.withAppendedPath(
				    			Queries.CONTENT_FILTERED_URI,          						    	
								Uri.encode(mSearchString));
			else
				FilteredUri = Queries.CONTENT_URI;	
			
			cur = new CursorLoader(
        		context,
        		FilteredUri,
        		Queries.PROJECTION_WITH_BADGE,
        		Queries.SELECTION_DISPLAY_NAME,
        		null,
                Queries.SORT_ORDER
                );
			
			Log.d("PROGRAMM!", "onCreateLoader - succeed to return cur");
        return cur;        			        		
    }
	Log.e("PROGRAMM!", "onCreateLoader - incorrect ID provided (" + loaderId + ")");
	return cur;
}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {		        
		// Put the result Cursor in the adapter for the ListView
		if (Queries.LoaderManagerID == loader.getId())
		{
			FilterCursorWrapper filterCursorWrapper = new FilterCursorWrapper(cursor, true,0);
			mAutoCompleteAdapter.swapCursor(filterCursorWrapper);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Delete the reference to the existing Cursor
		if (Queries.LoaderManagerID == loader.getId())
			mAutoCompleteAdapter.swapCursor(null);
	}


	
    @Override
	protected void onResume() {
		super.onResume();
		mFriendsSelectedAdapter.notifyDataSetChanged();
		//Log.d("FriendsList","Resumed");
	}

	
	public void removeContactButtonClick(View v)
	{		
		int position = mSelectedContactsList.getPositionForView((View) v.getParent());		
		ContactsListSingleton.getInstance().removeContactByIndex(position);
		mFriendsSelectedAdapter.notifyDataSetChanged();
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friends_list_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
	    case R.id.doneFL:	    	
	    	ArrayList<ContactDataStructure> db=ContactsListSingleton.getInstance().getDB();    	
	    	if(db==null || db.isEmpty())
	    	{
	    		Toast.makeText(this, "Please add contacts.", Toast.LENGTH_LONG).show();
	    		return true;    	
	    	}
	    	intent = new Intent(this, OnMap.class);	
	    	startActivity(intent);
	    	
	    	sendNotifications();
	        return true;     
		case R.id.SettingsFL:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
	    }
	    return false;
	}
	
	
	public void sendNotifications()
	{	 
		ParsePush push = new ParsePush();
		ArrayList<String> phones = ContactsListSingleton.getInstance().getAllChannelsForParse(ChannelMode.INVITATION);
		push.setChannels(phones);
		
		push.setExpirationTimeInterval(60*60*24);//one day till query is relevant
		
		JSONObject data = new JSONObject();		
	    try
	    {
	    		data.put("alert", "Gooomo Tripper from: " + ParseUser.getCurrentUser().getUsername());
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
