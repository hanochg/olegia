package com.tripper.mobile.activity;

import java.util.Locale;

import com.tripper.mobile.R;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.tripper.mobile.utils.*;

public class FriendsList extends Activity {
	ListView mSelectedContactsList;
	SimpleCursorAdapter mCursorAdapter;
	private ContactsAdapter mAdapter; // The main query adapter
	Context context;
	ImageButton plusButton;
	AutoCompleteTextView actvContacts;
    String mSearchString=null;
    ListViewContactsAdapter mListViewContactsAdapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.friends_list_screen);		
		
        mAdapter = new ContactsAdapter(this);
		
		plusButton=(ImageButton) findViewById(R.id.plusButton);
		plusButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, Contacts_List.class);
				//intent.putExtra(getResources().getString(R.string.Choice),(long)getResources().getInteger(R.integer.MultipleDestination));
				startActivity(intent);	
				
			}
		});
		
		//Gets AutoCompleteTextView
		actvContacts=(AutoCompleteTextView) findViewById(R.id.acContactName);
		
        // Gets the ListView from the View list of the parent activity
		mSelectedContactsList = (ListView) findViewById(R.id.lvContactList);
        
        mListViewContactsAdapter=
        		new ListViewContactsAdapter(this,R.layout.friends_list_row_item,ContactsListSingleton.getInstance().getDB());
        mSelectedContactsList.setAdapter(mListViewContactsAdapter);
        
        // Gets a CursorAdapter
        /*mCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.contact_list_item,
                null,
                Queries.FROM_COLUMNS, Queries.TO_VIEWS_IDS2,
               	0);*/
        // Sets the adapter for the ListView
        //mContactsList.setAdapter(mAdapter);
        //actvContacts.setAdapter(mCursorAdapter);
        
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
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				  Log.d("PROGRAM!!", "Text Changed");
				  
              	  mSearchString=s.toString();
              	  
              	
                  // restart the loader
                  getLoaderManager().restartLoader(Queries.LoaderManagerID, null,  new LoaderCallbacks<Cursor>() {
  
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
          					//mAdapter.swapCursor(cursor);
          		        	FilterCursorWrapper filterCursorWrapper = new FilterCursorWrapper(cursor, true,0);
          		        	mAdapter.swapCursor(filterCursorWrapper);
          				}
          			}

          			@Override
          			public void onLoaderReset(Loader<Cursor> loader) {
          		        // Delete the reference to the existing Cursor
          				if (Queries.LoaderManagerID == loader.getId())
          					//mCursorAdapter.swapCursor(null);
          					mAdapter.swapCursor(null);
          			}
          		});
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		
		
		
        // Initializes the loader
        getLoaderManager().initLoader(Queries.LoaderManagerID, null,  new LoaderCallbacks<Cursor>() {

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
					//mAdapter.swapCursor(cursor);
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
		});
	}
	
	public void removeContactButtonClick(View v)
	{		
		Log.d("ON CLICK!!","ON CLICK!!");
		int position = mSelectedContactsList.getPositionForView((View) v.getParent());		
		ContactsListSingleton.getInstance().getDB().remove(position);
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
	    }
	}



}
