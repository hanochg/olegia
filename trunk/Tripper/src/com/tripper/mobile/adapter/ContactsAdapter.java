package com.tripper.mobile.adapter;

import java.util.Locale;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.QuickContactBadge;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.Queries;

public class ContactsAdapter extends CursorAdapter implements SectionIndexer {
    private LayoutInflater mInflater; // Stores the layout inflater
    private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
    private TextAppearanceSpan highlightTextSpan; // Stores the highlight text appearance style
    private String mSearchTerm="";
    
    public void setSearchTerm(String s){
    	mSearchTerm=s;
    }
    
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
        highlightTextSpan = new TextAppearanceSpan(context, R.style.searchTextHiglight);
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
        if (!TextUtils.isEmpty(mSearchTerm)) {
            return displayName.toLowerCase(Locale.getDefault()).indexOf(
                    mSearchTerm.toLowerCase(Locale.getDefault()));
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
        holder.checked = (CheckBox) itemLayout.findViewById(R.id.cbSelected);
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

        // For Android 3.0 and later, gets the thumbnail image Uri from the current Cursor row.
        // For platforms earlier than 3.0, this isn't necessary, because the thumbnail is
        // generated from the other fields in the row.

        final String photoUri = cursor.getString(Queries.PHOTO_THUMBNAIL_DATA);
        final String displayName = cursor.getString(Queries.DISPLAY_NAME);	        
        final String phoneNum = cursor.getString(Queries.PHONE_NUM);
        //need this items for creating a new contact in the DB
        final long contactID = cursor.getLong(Queries.ID);
        final String lookupKey = cursor.getString(Queries.LOOKUP_KEY);
        final Uri uri = Contacts.getLookupUri(
                cursor.getLong(Queries.ID),
                cursor.getString(Queries.LOOKUP_KEY));
        
        int startIndex = indexOfSearchQuery(displayName);
        
        if (startIndex == -1) {
            // If the user didn't do a search, or the search string didn't match a display
            // name, show the display name without highlighting
            holder.contactsName.setText(displayName);
            
        } else {
            // If the search string matched the display name, applies a SpannableString to
            // highlight the search string with the displayed display name

            // Wraps the display name in the SpannableString
            final SpannableString highlightedName = new SpannableString(displayName);

            // Sets the span to start at the starting point of the match and end at "length"
            // characters beyond the starting point
            highlightedName.setSpan(highlightTextSpan, startIndex,
                    startIndex + mSearchTerm.length(), 0);

            // Binds the SpannableString to the display name View object
            holder.contactsName.setText(highlightedName);
        }
        
        //assign phone number to user in list
        holder.contactsNumber.setText(phoneNum);
        
        //mark if contact selected
        holder.checked.setOnCheckedChangeListener(null);
        if(ContactsListSingleton.getInstance().indexOf(phoneNum)!=(-1))
        	holder.checked.setChecked(true);
        else
        	holder.checked.setChecked(false);

        
        
        // Processes the QuickContactBadge. A QuickContactBadge first appears as a contact's
        // thumbnail image with styling that indicates it can be touched for additional
        // information. When the user clicks the image, the badge expands into a dialog box
        // containing the contact's details and icons for the built-in apps that can handle
        // each detail type.

        // Generates the contact lookup Uri
        final Uri contactUri = Contacts.getLookupUri(
                cursor.getLong(Queries.ID),
                cursor.getString(Queries.LOOKUP_KEY));

        // Binds the contact's lookup Uri to the QuickContactBadge
        //String photo = cursor.getString(Queries.PHOTO_THUMBNAIL_DATA);
        //Long id  = cursor.getLong(Queries.ID);
        //String lookup = cursor.getString(Queries.LOOKUP_KEY);
        holder.icon.assignContactUri(contactUri);

        // Loads the thumbnail image pointed to by photoUri into the QuickContactBadge in a
        // background worker thread
     //## mImageLoader.loadImage(photoUri, holder.icon);
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
