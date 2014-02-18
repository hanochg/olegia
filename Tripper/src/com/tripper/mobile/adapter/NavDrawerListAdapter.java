package com.tripper.mobile.adapter;

 
import java.util.ArrayList;

import com.tripper.mobile.R;
import com.tripper.mobile.drawer.NavDrawerItem; 
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class NavDrawerListAdapter extends BaseAdapter {
     
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private ArrayList<ContactDataStructure> contactsDB;
    
    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
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
    	
    	if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }
          
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        
        curContact = getItem(position);
        Log.d("getView","answer: " + curContact.getContactAnswer().toString());
        switch (curContact.getContactAnswer())
        {
        case notAnswered:
        	imgIcon.setImageResource(R.drawable.question_mark);
        	break;
        case no:
        	imgIcon.setImageResource(R.drawable.red_circle);
        	break;
        case ok:
        	imgIcon.setImageResource(R.drawable.green_circle);
        	break;        
        }
        txtTitle.setText(curContact.getName());
         

        
        //DEPRECATED
        // check whether it set visible or not
        //if(navDrawerItems.get(position).getCounterVisibility()){
        //    txtCount.setText(navDrawerItems.get(position).getCount());
        //}else{
            // hide the counter view
        //    txtCount.setVisibility(View.GONE);
        //}
         
        return convertView;
    }
 
}
