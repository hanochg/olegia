package com.tripper.mobile.activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import com.tripper.mobile.R;
import com.tripper.mobile.adapter.AddressAdapter;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioButton;
import android.widget.Toast;

public class FindAddress extends Activity {

	//Constants
	private static final int GEOCODE_MAX_RESULTS=10;
	private static final int MESSAGE_TYPE=1;
	private static final int THRESHOLD=3;
	private static final int TIME_INTERVAL_THRESHOLD=300;

	//Externals
	public static Address selectedAddress;

	//Globals
	private static AsyncGeocode AsyncTasker;
	private MessageHandler msgHandler;
	private Message message;
	private ListView listView;
	private EditText addressSearch;
	private AddressAdapter listViewAdapter;
	private ArrayList<Address> addressDB;
	private Context activityContext;
	private final int SPEECH_REQUEST_CODE = 10;
	private RadioButton lastCheckedRadioButton=null;
	
	
	
	private Locale GeoCodeLocale= new Locale("iw");//change to "en" or default 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.find_address_menu, menu);
		return true;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK)
	    {
	        // Populate the wordsList with the String values the recognition engine thought it heard
	        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	        addressSearch.setText(matches.get(0));
	    }
	}
	public void speechActivation(View view)
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me the address...");
		startActivityForResult(intent, SPEECH_REQUEST_CODE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_address);
		
		activityContext=this;
		AsyncTasker = new AsyncGeocode(activityContext);
		
		
		addressSearch=(EditText) findViewById(R.id.etAddressSearch);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		listView = (ListView)findViewById(R.id.addressList);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		
		//Define Async
		AsyncTasker = new AsyncGeocode(this);

		//Define Message
		msgHandler=new MessageHandler(this);

		//Define AutoComplete Adapter
		//autoCompleteAdapter = new ArrayAdapterNoFilter(this, android.R.layout.simple_dropdown_item_1line);//android.R.layout.simple_dropdown_item_1line);
		//autoCompleteAdapter.setNotifyOnChange(false);
		addressDB = new ArrayList<Address>();
		
		//Define ListAdapter
		listViewAdapter=new AddressAdapter(this, R.layout.find_address_row_item,addressDB);
		listViewAdapter.setNotifyOnChange(false);	
		
		// Show soft keyboard automatically
		addressSearch.requestFocus();
		getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		
		//Define AutoComplete TextView	    
		addressSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				final String value = s.toString();
				
				//reset list selection
				listViewAdapter.setLastCheckPosition(-1);
				lastCheckedRadioButton=null;
				
				if (!"".equals(value) && (value.length() >= THRESHOLD))
				{	
					//clean the message queue
					msgHandler.removeMessages(MESSAGE_TYPE);
					//get new async tasker for the request
					AsyncTasker = new AsyncGeocode(activityContext);
					//create a new message and send it
					message=msgHandler.obtainMessage(MESSAGE_TYPE);
					Bundle bundle=new Bundle();
					bundle.putString("Value", value);
					message.setData(bundle);
					msgHandler.sendMessageDelayed(message, TIME_INTERVAL_THRESHOLD);
				}			
				else
				{
					AsyncTasker.cancel(true);
					msgHandler.removeMessages(MESSAGE_TYPE);
					listViewAdapter.clear();
				}
					

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {


			}
		});
		
		listView.setAdapter(listViewAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{				
				if(lastCheckedRadioButton!=null)
					lastCheckedRadioButton.setChecked(false);
				
				lastCheckedRadioButton=(RadioButton) view.findViewById(R.id.addressRadioButton);
				lastCheckedRadioButton.setChecked(true);
				listViewAdapter.setLastCheckPosition(position);
				selectedAddress=addressDB.get(position);

				Log.d("App!!!","onItemSelected");

			}
		});
	
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.nextFA:
			switch (ContactsListSingleton.getInstance().APP_MODE)
			{
			case SINGLE_DESTINATION: //MainActivity				
				//get selected address
				ContactsListSingleton.setSingleRouteAddress(selectedAddress);

				Toast.makeText(activityContext, selectedAddress.getAddressLine(1)+ "," +selectedAddress.getAddressLine(0)+","+selectedAddress.getAddressLine(2), Toast.LENGTH_SHORT).show();

				//launch FriendsList						
				Intent intent = new Intent(activityContext, FriendsList.class);
				startActivity(intent);
				break;
			case MULTI_DESTINATION:	//Manual

				//get number from intent
				String phone="";

				//get selected address
				//selectedAddress = listViewAdapter.getItem(position);
				
				//get longitude and latitude and send it to user's data
				ContactsListSingleton.getInstance().setContactLocation(
						phone, selectedAddress.getLongitude(),selectedAddress.getLatitude());
				break;
			case NOTIFICATION:	//Notification
				
				break;					
			}

	    	
	
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private static void notifyResult(String value,Context context) {
		Log.d("App!!!","notifyResult");
		try{
			//AsyncTasker = new AsyncGeocode(context);
			AsyncTasker.execute(value);			
		}catch(Exception ex){
			Log.d("Error#@$#$",ex.getMessage());
		}
	}

	//Class extends AsyncTask for defining the AsyncTask function 
	private class AsyncGeocode extends AsyncTask<String, Void, Void> {
		private Context context;
		List<Address> GeoResultsList;
		
		public AsyncGeocode(Context context) {
			super();
			this.context = context;
		}

		//will run in the created thread
		protected Void doInBackground(String... search) 
		{
			
			Log.d("App!!!","doInBackground-begin");
			
			String value = search[0];
			try {
				
				if(!Geocoder.isPresent())
				{					
					Log.e("AsyncGeocode","Geocoder is not present! {Geocoder.isPresent()==false}");
					return null;
				}
				GeoResultsList = new Geocoder(context,GeoCodeLocale).getFromLocationName(value, GEOCODE_MAX_RESULTS);				
				
				Log.d("App!!!","after Geocode - list size: "+GeoResultsList.size());
			} catch (Exception ex) {				
				Log.e("AsyncGeocode","Geocode Error:"+ex.getMessage());
				Log.e("AsyncGeocode","Trying Geocode via HTTP request");
				GeoResultsList = geocodingViaHTTPRequest(value);
				
			}
			return null;
		}
	    @Override
	    protected void onPostExecute(Void unused)
	    {				    		    
	    	Log.d("AsyncGeocode","OnPostExcecute");
	    	super.onPostExecute(unused);

	    	if(GeoResultsList==null)
	    		{
	    			Toast.makeText(activityContext,"Connection problem occured, Check your internet connectivity",Toast.LENGTH_LONG).show();
	    			return;
	    		}
	    	if(GeoResultsList.size()!=0)
	    	{	    	
	    		Log.d("AsyncGeocode","OnPostExcecute-valid changes");
	    		listViewAdapter.clear();
				for (Address address : GeoResultsList)      								    
					listViewAdapter.add(address);					

	    		listViewAdapter.notifyDataSetChanged();
	    	}
	    	else
	    		Log.d("AsyncGeocode","OnPostExcecute-zero changes");

	    	Log.d("AsyncGeocode","OnPostExcecute");
	    }
	}//End Class AsyncTask


	//HANDLER 
	//Will handle the messages and the delay between them
	private static class MessageHandler extends Handler {

		private Context context;

		public MessageHandler(Context context) {
			this.context = context;
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_TYPE) 
			{
				String value = msg.getData().getString("Value");
				notifyResult(value,context);
			}
		}
	}//End class Handler

	public ArrayList<Address> geocodingViaHTTPRequest(String youraddress) {
	    ArrayList<Address> addressList= new ArrayList<Address>();
	    Address address;
	    
	    String addressString = youraddress.replaceAll(" ", "+");
	    
		String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
				addressString + "&sensor=false&language=" + GeoCodeLocale.getLanguage();
	    HttpGet httpGet = new HttpGet(uri);
	    HttpClient client = new DefaultHttpClient();
	    HttpResponse response;
	    StringBuilder stringBuilder = new StringBuilder();

	    //Create connection and get response part
	    try {
	        response = client.execute(httpGet);
	        HttpEntity entity = response.getEntity();
	        InputStream stream = entity.getContent();
	        int b;
	        while ((b = stream.read()) != -1) {
	            stringBuilder.append((char) b);
	        }
	    } catch (Exception e) {
	        Log.e("getLatLongFromAddress","connection Error occured: "+e.getMessage());
	        return null;
	    }

	    //JSON PART
	    JSONObject jsonObject = new JSONObject();
	    try {
	        jsonObject = new JSONObject(stringBuilder.toString());
	        
    		String StreetNumber="";
    		String addressType="";
    		String currentAddress="";
	        
	        int resultSize=((JSONArray)jsonObject.get("results")).length();
	        
	        for(int i=0;i<resultSize && i<GEOCODE_MAX_RESULTS;i++)
	        {
	    		address=new Address(GeoCodeLocale);
	    		
	    		address.setLongitude(((JSONArray)jsonObject.get("results")).getJSONObject(i)
	    				.getJSONObject("geometry").getJSONObject("location")
	    				.getDouble("lng"));
	    		
	    		address.setLatitude(((JSONArray)jsonObject.get("results")).getJSONObject(i)
	    				.getJSONObject("geometry").getJSONObject("location")
	    				.getDouble("lat"));
	    		
	    		int addressComponentsSize = ((JSONArray)jsonObject.get("results")).getJSONObject(i)
	    				.getJSONArray("address_components").length();
	    		
	    		StreetNumber="";
	    		addressType="";
	    		currentAddress="";
	    		
	    		for(int k=0;k<addressComponentsSize;k++)
	    		{
	    			addressType =((JSONArray)jsonObject.get("results")).getJSONObject(i)
    	    				.getJSONArray("address_components").getJSONObject(k).getJSONArray("types").getString(0);
	    			
	    			currentAddress=((JSONArray)jsonObject.get("results")).getJSONObject(i)
    	    				.getJSONArray("address_components").getJSONObject(k).getString("long_name");	    			
	    			
	    			if(addressType.equals("street_number"))
	    			{
	    				StreetNumber=currentAddress;
	    				continue;
	    			}
	    			else if(addressType.equals("route") && !StreetNumber.equals(""))
	    			{
	    				currentAddress=currentAddress + " " + StreetNumber;
	    			}

	    			//new string and the {getBytes("ISO-8859-1"), "UTF-8"} shit for supporting Hebrew on the results
	    			address.setAddressLine((addressComponentsSize-k)-1,new String(
	    					currentAddress.getBytes("ISO-8859-1"), "UTF-8"));
	    		}
	    		addressList.add(address);
	        }
	        
	    } catch (Exception e) {
	    	Log.e("getLatLongFromAddress","JSON Error occured: "+e.getMessage());
	    	return null;
	    }
	    return addressList;
	}

}
