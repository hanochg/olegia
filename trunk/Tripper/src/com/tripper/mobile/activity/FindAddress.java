package com.tripper.mobile.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tripper.mobile.R;
import com.tripper.mobile.utils.*;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
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
	private AsyncGeocode AsyncTasker;
	private MessageHandler msgHandler;
	private Message message;
	private ListView listView;
	private EditText addressSearch;
	private ArrayAdapter<Address> listViewAdapter;
	private double longitude,latitude;
	private ArrayList<Address> addressDB;
	private Context context;
	private Locale GeoCodeLocale;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.find_address_menu, menu);
		return true;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_address);
		GeoCodeLocale = new Locale("iw"); //change to "en" or default
		context=this;
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		listView = (ListView)findViewById(R.id.addressList);
		
		//Define Async
		AsyncTasker = new AsyncGeocode(this);
		longitude=latitude=-1;

		//Define Message
		msgHandler=new MessageHandler(this);

		//Define AutoComplete Adapter
		//autoCompleteAdapter = new ArrayAdapterNoFilter(this, android.R.layout.simple_dropdown_item_1line);//android.R.layout.simple_dropdown_item_1line);
		//autoCompleteAdapter.setNotifyOnChange(false);
		addressDB = new ArrayList<Address>();
		//Define ListAdapter
		listViewAdapter=new AddressAdapter(this, android.R.layout.simple_dropdown_item_1line,addressDB);
		listViewAdapter.setNotifyOnChange(false);
		
		addressSearch=(EditText) findViewById(R.id.etAddressSearch);
		
		
		// Show soft keyboard automatically
		addressSearch.requestFocus();
		getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		
		//Define AutoComplete TextView	    
		addressSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				final String value = s.toString();

				if (!"".equals(value) && (value.length() >= THRESHOLD))
				{	
					//clean the message queue
					msgHandler.removeMessages(MESSAGE_TYPE);

					//create a new message and send it
					message=msgHandler.obtainMessage(MESSAGE_TYPE);
					Bundle bundle=new Bundle();
					bundle.putString("Value", value);
					message.setData(bundle);
					msgHandler.sendMessageDelayed(message, TIME_INTERVAL_THRESHOLD);
				}			
				else
					listViewAdapter.clear();

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
			@Override//AdapterView<?> arg0, View arg1,int arg2, long arg3
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				Log.d("App!!!","onItemSelected");
				if (position < listViewAdapter.getCount()) 
				{
					selectedAddress = listViewAdapter.getItem(position);
					longitude=selectedAddress.getLongitude();
					latitude=selectedAddress.getLatitude();
					Toast.makeText(context, selectedAddress.getAddressLine(1)+ "," +selectedAddress.getAddressLine(0)+","+selectedAddress.getAddressLine(2), Toast.LENGTH_SHORT).show();
				}
				//Address data = addressDB.get(position); 
				//String addressStr = data.getAddressLine(1)+ "," +data.getAddressLine(0)+","+data.getAddressLine(2);
				//addressSearch.setText(addressStr);
				//TODO
			}
		});
	
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.nextFA:
	    	Intent intent = new Intent(this,FriendsList.class);
	    	startActivity(intent);	
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private  void notifyResult(String value,Context context) {
		Log.d("App!!!","notifyResult");
		try{
			AsyncTasker = new AsyncGeocode(context);
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
				String searchJSONString = ("yoseftal haifa").replaceAll(" ", "+");
				getLatLongFromAddress(searchJSONString);
				GeoResultsList = new Geocoder(context,GeoCodeLocale).getFromLocationName(value, GEOCODE_MAX_RESULTS);				

				Log.d("App!!!","after Geocode - list size: "+GeoResultsList.size());
			} catch (Exception ex) {
				Log.e("AsyncGeocode","Geocode Error:"+ex.getMessage());
			}
			return null;
		}
	    @Override
	    protected void onPostExecute(Void unused)
	    {				    		    
	    	Log.d("AsyncGeocode","OnPostExcecute");
	    	super.onPostExecute(unused);
	    	if(GeoResultsList!=null && GeoResultsList.size()!=0)
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
	private class MessageHandler extends Handler {

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

	public void getLatLongFromAddress(String youraddress) {
	    Address address=new Address(GeoCodeLocale);
		
		String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
	                  youraddress + "&sensor=false&language=he";
	    HttpGet httpGet = new HttpGet(uri);
	    HttpClient client = new DefaultHttpClient();
	    HttpResponse response;
	    StringBuilder stringBuilder = new StringBuilder();

	    try {
	        response = client.execute(httpGet);
	        HttpEntity entity = response.getEntity();
	        InputStream stream = entity.getContent();
	        int b;
	        while ((b = stream.read()) != -1) {
	            stringBuilder.append((char) b);
	        }
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    JSONObject jsonObject = new JSONObject();
	    try {
	        jsonObject = new JSONObject(stringBuilder.toString());

	        Double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
	        		.getJSONObject("geometry").getJSONObject("location")
	        		.getDouble("lng");

	        Double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
	            .getJSONObject("geometry").getJSONObject("location")
	            .getDouble("lat");
	        int length=((JSONArray)jsonObject.get("results")).length();
	        
	        
	        Log.d("latitude", String.valueOf(lat));
	        Log.d("longitude", String.valueOf(lng));
	        Log.d("longitude", String.valueOf(length));
	    } catch (JSONException e) {
	        e.printStackTrace();
	    }

	}

}
