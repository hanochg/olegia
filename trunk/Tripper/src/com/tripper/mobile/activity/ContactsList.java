package com.tripper.mobile.activity;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import com.tripper.mobile.BuildConfig;
import com.tripper.mobile.R;
import com.tripper.mobile.SettingsActivity;
import com.tripper.mobile.adapter.ContactsAdapter;
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.ImageLoader;
import com.tripper.mobile.utils.Queries;
import com.tripper.mobile.utils.ContactDataStructure.eAnswer;
import com.tripper.mobile.utils.Queries.Extra;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;

//import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.ListFragment;

//import android.support.v4.content.CursorLoader;
//import android.support.v4.content.Loader;
//import android.support.v4.widget.CursorAdapter;

import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsList extends Activity implements
					 LoaderManager.LoaderCallbacks<Cursor>
{
    // Defines a tag for identifying log entries {LOG.D TAG}
    private static final String TAG = "ContactsList";

    private ContactsAdapter mAdapter; // The main query adapter
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread
    private String mSearchTerm; // Stores the current search query term
    private EditText searchEditText;
    private ListView listView;
    private TextView mEmptyView;
    private Context context;
	private BroadcastReceiver mMessageReceiver;
    private final int SPEECH_REQUEST_CODE = 10;
    //private final int CONTACTLIST_REQUEST_CODE = 11;
    private int APP_MODE;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list);
		context=this;
		
		APP_MODE = getIntent().getExtras().getInt(Queries.Extra.APP_MODE);
				
        // Create the main contacts adapter
        mAdapter = new ContactsAdapter(this);
        getLoaderManager().initLoader(Queries.LoaderManagerID, null, this);
        
        listView = (ListView)findViewById(R.id.CL_list);
        searchEditText=(EditText)findViewById(R.id.etSearchCL);
        mEmptyView = (TextView) findViewById(R.id.noResults);
        
        searchEditText.addTextChangedListener(new TextWatcher() {        				
			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				  Log.d("PROGRAM!!", "Text Changed");
				  
              	  mSearchTerm=s.toString();
              	  mAdapter.setSearchTerm(mSearchTerm);              	
                  // restart the loader
                  getLoaderManager().restartLoader(Queries.LoaderManagerID, null, ContactsList.this);				
			}

			@Override
			public void afterTextChanged(Editable arg0) {	
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {	
			}
			
		});
        
        //##
        /*
         * An ImageLoader object loads and resizes an image in the background and binds it to the
         * QuickContactBadge in each item layout of the ListView. ImageLoader implements memory
         * caching for each image, which substantially improves refreshes of the ListView as the
         * user scrolls through it.
         *
         * To learn more about downloading images asynchronously and caching the results, read the
         * Android training class Displaying Bitmaps Efficiently.
		*/
        mImageLoader = new ImageLoader(this, getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                // This gets called in a background thread and passed the data from
                // ImageLoader.loadImage().
                return loadContactPhotoThumbnail((String) data, getImageSize());
            }
        };

        // Set a placeholder loading image for the image loader
        mImageLoader.setLoadingImage(R.drawable.ic_contact_picture_holo_light);
        
        
        // Add a cache to the image loader
        //mImageLoader.addImageCache(null, 0.1f);
        

        // Set up ListView, assign adapter and set some listeners. The adapter was previously
        // created in onCreate().
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		        Log.d("CLICK","CLICK!!!!!");
		        
				// Gets the Cursor object currently bound to the ListView
		        final Cursor cursor = mAdapter.getCursor();

		        // Moves to the Cursor row corresponding to the ListView item that was clicked
		        cursor.moveToPosition(position);

		        // Creates a contact lookup Uri from contact ID and lookup_key
		        final String displayName = cursor.getString(Queries.DISPLAY_NAME);	        
		        final String phoneNum = cursor.getString(Queries.PHONE_NUM);
		        //need this items for creating a new contact in the DB
		        final long contactID = cursor.getLong(Queries.ID);
		        final String lookupKey = cursor.getString(Queries.LOOKUP_KEY);
		        final Uri uri = Contacts.getLookupUri(
		                cursor.getLong(Queries.ID),
		                cursor.getString(Queries.LOOKUP_KEY));

		        CheckBox checked = (CheckBox) (((ViewGroup)v).getChildAt(3));
		        if(!checked.isChecked())
		        {
		        	checked.setChecked(true);
		        	ContactDataStructure contact = new ContactDataStructure(context);
		        	contact.setId(contactID);
		        	contact.setLookupkey(lookupKey);
		        	contact.setName(displayName);
		        	contact.setPhoneNumber(phoneNum);
		        	contact.setUri(uri);
			        if(APP_MODE==Extra.SINGLE_DESTINATION)
			        	contact.setContactAnswer(eAnswer.single);
			        
		        	ContactsListSingleton.getInstance().insertContact(contact,ContactsListSingleton.getInstance().mFriendsSelectedAdapter,getApplicationContext());
		        }
		        else
		        {
		        	checked.setChecked(false);
		        	ContactsListSingleton.getInstance().removeContactByPhoneNum(phoneNum);
		        }
				
				
			}
		});
        
        
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause image loader to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageLoader.setPauseWork(true);
                } else {
                    mImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {}
        });
          
        
		mMessageReceiver = new BroadcastReceiver() {
			  @Override
			  public void onReceive(Context context, Intent intent) 
			  {
				  String intentAction=intent.getAction();
				  if(intentAction.equals("com.tripper.mobile.EXIT"))
				  {
					  Log.d("onReceive","EXIT");
					  finish();
				  }
			  }
		};
    }

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK)
	    {
	        // Populate the wordsList with the String values the recognition engine thought it heard
	        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	        searchEditText.setText(matches.get(0));
	    }
	}
	public void speechActivation(View view)
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me the contact name...");
		startActivityForResult(intent, SPEECH_REQUEST_CODE);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_list_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
	    case R.id.doneCL:
	    	intent = new Intent(this, FriendsList.class);
	    	setResult(Activity.RESULT_OK,intent);
	    	finish();
	        return true; 
	    case R.id.SettingsCL:
	    	intent = new Intent(this, SettingsActivity.class);
	    	startActivity(intent);
	    	return true;
	    case android.R.id.home:
	    	finish();
	    	
	    }
	    return false;
	}
    
    @Override
    public void onPause() {
        super.onPause();
        // In the case onPause() is called during a fling the image loader is
        // un-paused to let any remaining background work complete.
        mImageLoader.setPauseWork(false);
    }
    

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			
        // If this is the loader for finding contacts in the Contacts Provider
        // (the only one supported)
        if (id == Queries.LoaderManagerID) {
			Uri contentUri;
			if (!TextUtils.isEmpty(mSearchTerm)) 
			{
				contentUri = Uri.withAppendedPath(
				    			Queries.CONTENT_FILTERED_URI,          						    	
								Uri.encode(mSearchTerm));
			}
			else
			{
				contentUri = Queries.CONTENT_URI;	
			}

            // Returns a new CursorLoader for querying the Contacts table. No arguments are used
            // for the selection clause. The search string is either encoded onto the content URI,
            // or no contacts search string is used. The other search criteria are constants. See
            // the ContactsQuery interface.
            return new CursorLoader(this,
                    contentUri,
                    Queries.PROJECTION_WITH_BADGE,
                    Queries.SELECTION_DISPLAY_NAME,
                    null,
                    Queries.SORT_ORDER);
        }

        Log.e(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // This swaps the new cursor into the adapter.
        if (loader.getId() == Queries.LoaderManagerID) {
        	if(!data.isClosed())
        		mAdapter.swapCursor(data);
			else
				Log.e("ContactsList","mAdapter-closed cursor error");
        	
            if(data.getCount()!=0)
            	mEmptyView.setVisibility(View.GONE);
            else if (data.getCount()==0)
            	mEmptyView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mSearchTerm)) {
                // Selects the first item in results, unless this fragment has
                // been restored from a saved state (like orientation change)
                // in which case it selects the previously selected search item.
                //*if (data != null && data.moveToPosition(mPreviouslySelectedSearchItem)) {
                    // Creates the content Uri for the previously selected contact by appending the
                    // contact's ID to the Contacts table content Uri
                   //* final Uri uri = Uri.withAppendedPath(
                    //*        Contacts.CONTENT_URI, String.valueOf(data.getLong(ContactsQuery.ID)));
                    //*getListView().setItemChecked(mPreviouslySelectedSearchItem, true);
               //* } else {
                    // No results, clear selection.
                  //*  onSelectionCleared();
                //*}
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == Queries.LoaderManagerID) {
            // When the loader is being reset, clear the cursor from the adapter. This allows the
            // cursor resources to be freed.
            mAdapter.swapCursor(null);
        }
    }

    /**
     * Gets the preferred height for each item in the ListView, in pixels, after accounting for
     * screen density. ImageLoader uses this value to resize thumbnail images to match the ListView
     * item height.
     *
     * @return The preferred height in pixels, based on the current theme.
     */
    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        // Resolve list item preferred height theme attribute into typedValue
        this.getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);

        // Create a new DisplayMetrics object
        final DisplayMetrics metrics = new android.util.DisplayMetrics();

        // Populate the DisplayMetrics
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Return theme value based on DisplayMetrics
        return (int) typedValue.getDimension(metrics);
    }

    
    


	/**
	 * This is a subclass of CursorAdapter that supports binding Cursor columns to a view layout.
	 * If those items are part of search results, the search string is marked by highlighting the
	 * query text. An {@link AlphabetIndexer} is used to allow quicker navigation up and down the
	 * ListView.
	 */

	


/**
 * This interface must be implemented by any activity that loads this fragment. When an
 * interaction occurs, such as touching an item from the ListView, these callbacks will
 * be invoked to communicate the event back to the activity.
 */
public interface OnContactsInteractionListener {
    /**
     * Called when a contact is selected from the ListView.
     * @param contactUri The contact Uri.
     */
    public void onContactSelected(Uri contactUri);

    /**
     * Called when the ListView selection is cleared like when
     * a contact search is taking place or is finishing.
     */
    public void onSelectionCleared();
}


/**
 * Decodes and scales a contact's image from a file pointed to by a Uri in the contact's data,
 * and returns the result as a Bitmap. The column that contains the Uri varies according to the
 * platform version.
 *
 * @param photoData For platforms prior to Android 3.0, provide the Contact._ID column value.
 *                  For Android 3.0 and later, provide the Contact.PHOTO_THUMBNAIL_URI value.
 * @param imageSize The desired target width and height of the output image in pixels.
 * @return A Bitmap containing the contact's image, resized to fit the provided image size. If
 * no thumbnail exists, returns null.
 */
private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {



    // Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
    // ContentResolver can return an AssetFileDescriptor for the file.
    AssetFileDescriptor afd = null;

    // This "try" block catches an Exception if the file descriptor returned from the Contacts
    // Provider doesn't point to an existing file.
    try {
        Uri thumbUri;
        // If Android 3.0 or later, converts the Uri passed as a string to a Uri object.
        if (Queries.hasHoneycomb()) {
            thumbUri = Uri.parse(photoData);
        } else {
            // For versions prior to Android 3.0, appends the string argument to the content
            // Uri for the Contacts table.
            final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, photoData);

            // Appends the content Uri for the Contacts.Photo table to the previously
            // constructed contact Uri to yield a content URI for the thumbnail image
            thumbUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);
        }
        // Retrieves a file descriptor from the Contacts Provider. To learn more about this
        // feature, read the reference documentation for
        // ContentResolver#openAssetFileDescriptor.
        afd = this.getContentResolver().openAssetFileDescriptor(thumbUri, "r");

        // Gets a FileDescriptor from the AssetFileDescriptor. A BitmapFactory object can
        // decode the contents of a file pointed to by a FileDescriptor into a Bitmap.
        FileDescriptor fileDescriptor = afd.getFileDescriptor();

        if (fileDescriptor != null) {
            // Decodes a Bitmap from the image pointed to by the FileDescriptor, and scales it
            // to the specified width and height
            return ImageLoader.decodeSampledBitmapFromDescriptor(
                    fileDescriptor, imageSize, imageSize);
        }
    } catch (FileNotFoundException e) {
        // If the file pointed to by the thumbnail URI doesn't exist, or the file can't be
        // opened in "read" mode, ContentResolver.openAssetFileDescriptor throws a
        // FileNotFoundException.
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Contact photo thumbnail not found for contact " + photoData
                    + ": " + e.toString());
        }
    } finally {
        // If an AssetFileDescriptor was returned, try to close it
        if (afd != null) {
            try {
                afd.close();
            } catch (IOException e) {
                // Closing a file descriptor might cause an IOException if the file is
                // already closed. Nothing extra is needed to handle this.
            }
        }
    }

    // If the decoding failed, returns null
    return null;
}

}
