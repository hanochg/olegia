package com.tripper.mobile.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.FindCallback;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.tripper.mobile.R;
import com.tripper.mobile.TripperApplication;

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
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tripper.mobile.adapter.FilterCursorWrapper;
import com.tripper.mobile.adapter.ListViewContactsAdapter;
import com.tripper.mobile.utils.*;




public class FriendsList extends Activity implements
						LoaderManager.LoaderCallbacks<Cursor>
{
	ListView mSelectedContactsList;
	SimpleCursorAdapter mCursorAdapter;
	private ContactsAdapter mAdapter; // The main query adapter
	Context context;
	ImageButton contactsButton;
	AutoCompleteTextView actvContacts;
    String mSearchString=null;
    ListViewContactsAdapter mListViewContactsAdapter;
    private final int SPEECH_REQUEST_CODE = 10;
    private final int CONTACTLIST_REQUEST_CODE = 11;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.friends_list);		
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
        mAdapter = new ContactsAdapter(this);
		
		contactsButton=(ImageButton) findViewById(R.id.contactsButton);
		contactsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, Contacts_List.class);
				startActivityForResult(intent,CONTACTLIST_REQUEST_CODE);	
				
			}
		});
		
		//Gets AutoCompleteTextView
		actvContacts=(AutoCompleteTextView) findViewById(R.id.acContactName);
		
        // Gets the ListView from the View list of the parent activity
		mSelectedContactsList = (ListView) findViewById(R.id.lvContactList);
        
        mListViewContactsAdapter=
        		new ListViewContactsAdapter(this,R.layout.friends_list_row_item,ContactsListSingleton.getInstance().getDB());
        mSelectedContactsList.setAdapter(mListViewContactsAdapter);
        
        
        actvContacts.setAdapter(mAdapter);
        
		actvContacts.setOnItemClickListener(
				new OnItemClickListener() {

			@Override
		    public void onItemClick(
		            AdapterView<?> parent, View item, int position, long rowID) {
		        
				ContactDataStructure contact = new ContactDataStructure();
				
				// Gets the Cursor object currently bound to the ListView
		        final Cursor cursor = mAdapter.getCursor();

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
		        
		        ContactsListSingleton.getInstance().insertContact(contact);
		        mListViewContactsAdapter.notifyDataSetChanged();
		        actvContacts.setText("");
			}
		});
			

		actvContacts.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				  Log.d("PROGRAM!!", "Text Changed");
				  
              	  mSearchString=s.toString();
              	  
              	
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
	    	mAdapter.notifyDataSetChanged();
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
        		Queries.DisplayName_SELECTION,
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
			mAdapter.swapCursor(filterCursorWrapper);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Delete the reference to the existing Cursor
		if (Queries.LoaderManagerID == loader.getId())
			mAdapter.swapCursor(null);
	}


	
    @Override
	protected void onResume() {
		super.onResume();
		//mAdapter.notifyDataSetChanged();
		//Log.d("FriendsList","Resumed");
	}

	
	public void removeContactButtonClick(View v)
	{		
		Log.d("ON CLICK!!","ON CLICK!!");
		int position = mSelectedContactsList.getPositionForView((View) v.getParent());		
		ContactsListSingleton.getInstance().removeContactByIndex(position);
		mListViewContactsAdapter.notifyDataSetChanged();
	}
	
	/**
	 * This is a subclass of CursorAdapter that supports binding Cursor columns to a view layout.
	 * If those items are part of search results, the search string is marked by highlighting the
	 * query text. An {@link AlphabetIndexer} is used to allow quicker navigation up and down the
	 * ListView.
	 */
	
	
	private class ContactsAdapter extends CursorAdapter implements SectionIndexer {
	    private LayoutInflater mInflater; // Stores the layout inflater
	    private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
	    private TextAppearanceSpan highlightTextSpan; // Stores the highlight text appearance style

	    /**
	     * Instantiates a new Contacts Adapter.
	     * @param context A context that has access to the app's layout.
	     */
	    public ContactsAdapter(Context context) {
	        super(context, null, 0);

	        // Stores inflater for use later
	        mInflater = LayoutInflater.from(context);

	        // Loads a string containing the English alphabet. To fully localize the app, provide a
	        // strings.xml file in res/values-<x> directories, where <x> is a locale. In the file,
	        // define a string with android:name="alphabet" and contents set to all of the
	        // alphabetic characters in the language in their proper sort order, in upper case if
	        // applicable.
	        final String alphabet = context.getString(R.string.alphabet);

	        // Instantiates a new AlphabetIndexer bound to the column used to sort contact names.
	        // The cursor is left null, because it has not yet been retrieved.
	        mAlphabetIndexer = new AlphabetIndexer(null, Queries.SORT_KEY, alphabet);

	        // Defines a span for highlighting the part of a display name that matches the search
	        // string
	        highlightTextSpan = new TextAppearanceSpan(getApplicationContext(), R.style.searchTextHiglight);
	    }

	    /**
	     * Identifies the start of the search string in the display name column of a Cursor row.
	     * E.g. If displayName was "Adam" and search query (mSearchTerm) was "da" this would
	     * return 1.
	     *
	     * @param displayName The contact display name.
	     * @return The starting position of the search string in the display name, 0-based. The
	     * method returns -1 if the string is not found in the display name, or if the search
	     * string is empty or null.
	     */
	    private int indexOfSearchQuery(String displayName) {
	        if (!TextUtils.isEmpty(mSearchString)) {
	            return displayName.toLowerCase(Locale.getDefault()).indexOf(
	            		mSearchString.toLowerCase(Locale.getDefault()));
	        }
	        return -1;
	    }

	    /**
	     * Overrides newView() to inflate the list item views.
	     */
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

	    	        
	    	// Inflates the list item layout.
	        final View itemLayout =
	                mInflater.inflate(R.layout.contact_list_item, viewGroup, false);

	        // Creates a new ViewHolder in which to store handles to each view resource. This
	        // allows bindView() to retrieve stored references instead of calling findViewById for
	        // each instance of the layout.
	        final ViewHolder holder = new ViewHolder();
	        holder.contactsName = (TextView) itemLayout.findViewById(R.id.nameCL);
	        holder.contactsNumber = (TextView) itemLayout.findViewById(R.id.phoneNumCL);
	        holder.icon = (QuickContactBadge) itemLayout.findViewById(R.id.contactBadge);
	        holder.icon.setVisibility(View.GONE);	      
	        holder.checked = (CheckBox)itemLayout.findViewById(R.id.cbSelected);
	        holder.checked.setVisibility(View.GONE);	      

	        // Stores the resourceHolder instance in itemLayout. This makes resourceHolder
	        // available to bindView and other methods that receive a handle to the item view.
	        itemLayout.setTag(holder);

	        // Returns the item layout view
	        return itemLayout;
	    }

	    /**
	     * Binds data from the Cursor to the provided view.
	     */
	    @Override
	    public void bindView(View view, Context context, Cursor cursor) {
	        
	    	
	    	// Gets handles to individual view resources
	        final ViewHolder holder = (ViewHolder) view.getTag();

	        final String displayName = cursor.getString(Queries.DISPLAY_NAME);
	        
	        final String phoneNum = cursor.getString(Queries.PHONE_NUM);

	        final int startIndex = indexOfSearchQuery(displayName);

	        
	        if (startIndex == -1) {
	            // If the user didn't do a search, or the search string didn't match a display
	            // name, show the display name without highlighting
	            holder.contactsName.setText(displayName);
	            holder.contactsNumber.setText(phoneNum);

	        } else {
	            // If the search string matched the display name, applies a SpannableString to
	            // highlight the search string with the displayed display name

	            // Wraps the display name in the SpannableString
	            final SpannableString highlightedName = new SpannableString(displayName);

	            // Sets the span to start at the starting point of the match and end at "length"
	            // characters beyond the starting point
	            highlightedName.setSpan(highlightTextSpan, startIndex,
	                    startIndex + mSearchString.length(), 0);

	            // Binds the SpannableString to the display name View object
	            holder.contactsName.setText(highlightedName);
	            holder.contactsNumber.setText(phoneNum);

	        }

	    }

	    /**
	     * Overrides swapCursor to move the new Cursor into the AlphabetIndex as well as the
	     * CursorAdapter.
	     */
	    @Override
	    public Cursor swapCursor(Cursor newCursor) {
	        // Update the AlphabetIndexer with new cursor as well
	        mAlphabetIndexer.setCursor(newCursor);
	        return super.swapCursor(newCursor);
	    }

	    /**
	     * An override of getCount that simplifies accessing the Cursor. If the Cursor is null,
	     * getCount returns zero. As a result, no test for Cursor == null is needed.
	     */
	    @Override
	    public int getCount() {
	        if (getCursor() == null) {
	            return 0;
	        }
	        return super.getCount();
	    }

	    /**
	     * Defines the SectionIndexer.getSections() interface.
	     */
	    @Override
	    public Object[] getSections() {
	        return mAlphabetIndexer.getSections();
	    }

	    /**
	     * Defines the SectionIndexer.getPositionForSection() interface.
	     */
	    @Override
	    public int getPositionForSection(int i) {
	        if (getCursor() == null) {
	            return 0;
	        }
	        return mAlphabetIndexer.getPositionForSection(i);
	    }

	    /**
	     * Defines the SectionIndexer.getSectionForPosition() interface.
	     */
	    @Override
	    public int getSectionForPosition(int i) {
	        if (getCursor() == null) {
	            return 0;
	        }
	        return mAlphabetIndexer.getSectionForPosition(i);
	    }

	    /**
	     * A class that defines fields for each resource ID in the list item layout. This allows
	     * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
	     * calling findViewById in each iteration of bindView.
	     */
	    private class ViewHolder {
	        TextView contactsName;
	        TextView contactsNumber;
	        QuickContactBadge icon;
	        CheckBox checked;
	    }
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friends_list_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    switch(item.getItemId()){
	    case R.id.doneFL:
	    	
	    	ArrayList<ContactDataStructure> db=ContactsListSingleton.getInstance().getDB();    	
	    	if(db==null || db.isEmpty())
	    		return true;
	    	
	    	sendNotifications();
	    	this.finish();
	    	
	        return true;            
	    }
	    return false;
	}
	
	
	public void sendNotifications()
	{
		ParseQuery<ParseUser> query = ParseUser.getQuery();
			
    	ArrayList<String> phones = ContactsListSingleton.getInstance().getAllPhonesForParse();
    	query.whereContainedIn("username", phones); 
    	
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		pushQuery.whereMatchesQuery("user", query);
		 
		ParsePush push = new ParsePush();
		push.setQuery(pushQuery); 
		
		push.setExpirationTimeInterval(60*60*24);//one day till query is relevant
		
		JSONObject data = new JSONObject();		
	    try
	    {
	        data.put("alert", "Gomo Tripper from: " + ParseUser.getCurrentUser().getUsername());
	        data.put("action","com.tripper.Invite");
	        data.put("User", ParseUser.getCurrentUser().getUsername());
	    }
	    catch(JSONException x)
	    {
	    	Toast.makeText(getApplicationContext(), "The Program has Crushed.", Toast.LENGTH_LONG).show();
	    	throw new RuntimeException("Something wrong with JSON", x);
	    }
		push.setData(data);
		push.sendInBackground();
	}
	
	
	/*
	public void CheckForUsers()
	{
		ParseQuery<ParseUser> query = ParseUser.getQuery();
    	ArrayList<String> phones = ContactsListSingleton.getInstance().getAllPhonesForParse();
    	query.whereContainedIn("username", phones); 	
    	query.findInBackground(new FindCallback<ParseUser>() 
    	{
    	  public void done(List<ParseUser> objects, ParseException e)
    	  {
    	    if (e == null)
    	    {
    	        // The query was successful.
    	    } 
    	    else
    	    {
    	        // Something went wrong.
    	    }
    	  }
    	});		
	}*/
}
