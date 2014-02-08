package com.tripper.mobile.utils;


import com.tripper.mobile.R;
import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

public class Queries {
	
    public final static String SORT_ORDER =
    		hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
	
	public static final Uri CONTENT_URI = 
			ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	
	public static final Uri CONTENT_FILTERED_URI = 
			ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI;
	
    /*public static final String[] PROJECTION =
        {
    	ContactsContract.CommonDataKinds.Phone._ID,
    	ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
    	ContactsContract.CommonDataKinds.Phone.NUMBER,
    	ContactsContract_DISPLAY_NAME()        		
        };*/
    
    public static final String[] PROJECTION_WITH_BADGE =
        {
    	ContactsContract.CommonDataKinds.Phone._ID,
    	ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
    	ContactsContract_DISPLAY_NAME(),
    	Queries.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI : ContactsContract.CommonDataKinds.Phone._ID,    			
    	SORT_ORDER,
    	ContactsContract.CommonDataKinds.Phone.NUMBER    	
        };
    
    // The query column numbers which map to each value in the projection
    public final static int ID = 0;
    public final static int LOOKUP_KEY = 1;
    public final static int DISPLAY_NAME = 2;
    public final static int PHOTO_THUMBNAIL_DATA = 3;
    public final static int SORT_KEY = 4;
    public final static int PHONE_NUM = 5;
    
    public final static String DisplayName_SELECTION =
            (ContactsContract_DISPLAY_NAME()) +
            "<>''" +" AND " + ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + "=1"  
            	   +" AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1" ;
            	   
    
    public final static int LoaderManagerID = 1;

    public final static String[] FROM_COLUMNS = 
    {
    	ContactsContract_DISPLAY_NAME(),
        ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    public final static int[] TO_VIEWS_IDS = {
           R.id.contactName,
           R.id.contactNumber
    };
    
    public final static int[] TO_VIEWS_IDS2 = {
        R.id.nameCL,
        R.id.phoneNumCL
 };
    /**
     * Uses static final constants to detect if the device's platform version is Gingerbread or
     * later.
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb or
     * later.
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    public static String ContactsContract_DISPLAY_NAME()
    {
    	if(hasHoneycomb()) 
    		return ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY;
    	else
    		return ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
    }
    
    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb MR1 or
     * later.
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }
    
    
    /**
     * Uses static final constants to detect if the device's platform version is ICS or
     * later.
     */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
    
    /**
     * Enables strict mode. This should only be called when debugging the application and is useful
     * for finding some potential bugs or best practice violations.
     */
    @TargetApi(11)
    public static void enableStrictMode() {
        // Strict mode is only available on gingerbread or later
        if (hasGingerbread()) {

            // Enable all thread strict mode policies
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Enable all VM strict mode policies
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Honeycomb introduced some additional strict mode features
            if (hasHoneycomb()) {
                // Flash screen when thread policy is violated
                threadPolicyBuilder.penaltyFlashScreen();
                // For each activity class, set an instance limit of 1. Any more instances and
                // there could be a memory leak.
                //vmPolicyBuilder
                     //   .setClassInstanceLimit(ContactsListActivity.class, 1)
                      //  .setClassInstanceLimit(ContactDetailActivity.class, 1);
            }

            // Use builders to enable strict mode policies
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }
}
