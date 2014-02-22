package com.tripper.mobile.adapter;

 
import java.util.ArrayList;

import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactDataStructure;
import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.ContactDataStructure.eAppStatus;

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
    private ArrayList<ContactDataStructure> contactsDB;
    
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

    	curContact = getItem(position);

    	LayoutInflater mInflater = (LayoutInflater)
    			context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

    	if(curContact.isSelected())
    		convertView = mInflater.inflate(R.layout.drawer_list_item, null);
    	else
    		convertView = mInflater.inflate(R.layout.drawer_list_item_big, null);
    	
    	TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        txtTitle.setText(curContact.getName());
        
        //set image according to contact status
    	//if(curContact.isSelected())
    	//{
    		ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
    		
    		if (curContact.getAppStatus()==eAppStatus.noApp)
    			imgIcon.setImageResource(R.drawable.warning_sign);
    		else
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
			default:
				break;        
        	}
    	//}

        
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
