package com.tripper.mobile.activity;

import java.util.ArrayList;
import java.util.List;

import com.tripper.mobile.R;
import com.tripper.mobile.utils.*;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FindAddress extends Activity {

	//Constants
	private static final int GEOCODE_MAX_RESULTS=10;
	private static final int MESSAGE_TYPE=1;
	private static final int THRESHOLD=3;
	private static final int TIME_INTERVAL_THRESHOLD=300;

	//Externals
	public static Address selectedAddress;
	public static ProgressDialog pd;


	//Globals
	private AsyncGeocode AsyncTasker;
	private MessageHandler msgHandler;
	private Message message;
	private ListView listView;
	private AutoCompleteTextView actvAddress;
	private EditText addressSearch;
	//private List<Address> addressSuggestions;
	//private ArrayAdapter<String> autoCompleteAdapter;
	private ArrayAdapter<Address> listViewAdapter;
	private double longitude,latitude;


	/*Unknown
	public interface EditNameDialogListener {
		void onFinishEditDialog(String inputText);
	}*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.find_address, menu);
		return true;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_address);
		
		listView = (ListView)findViewById(R.id.addressList);
		
		//Define Async
		AsyncTasker = new AsyncGeocode(this);
		longitude=latitude=-1;

		//Define Message
		msgHandler=new MessageHandler(this);

		//Define AutoComplete Adapter
		//autoCompleteAdapter = new ArrayAdapterNoFilter(this, android.R.layout.simple_dropdown_item_1line);//android.R.layout.simple_dropdown_item_1line);
		//autoCompleteAdapter.setNotifyOnChange(false);

		//Define ListAdapter
		listViewAdapter=new AddressAdapter(this, android.R.layout.simple_dropdown_item_1line,new ArrayList<Address>());
		listViewAdapter.setNotifyOnChange(false);
		
		
		//listViewAdapter.addAll("hello","welcome");
		//View view = inflater.inflate(R.layout.enter_destination_dialog_fragment, container);
		//actvAddress = (AutoCompleteTextView) view.findViewById(R.id.atcvAddress);
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
				}
			}
		});


		//actvAddress.setThreshold(THRESHOLD);
		//actvAddress.setAdapter(listViewAdapter);


		//getDialog().setTitle(getResources().getString(R.string.Enter_Destination_dialog_title));



		//actvAddress.setOnEditorActionListener(this);

		//return view;		
	}

	//@Override
	/*
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			// Return input text to activity
			EditNameDialogListener activity = (EditNameDialogListener) this;
			activity.onFinishEditDialog(actvAddress.getText().toString());
			//this.dismiss();
			return true;
		}
		return false;
	}*/
	
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
					throw new Exception("Geocoder is not present! {Geocoder.isPresent()==false}");
				GeoResultsList = new Geocoder(context).getFromLocationName(value, GEOCODE_MAX_RESULTS);				

				Log.d("App!!!","after Geocode - list size: "+GeoResultsList.size());
			} catch (Exception ex) {
				Log.d("Error#@$#$",ex.getMessage());
			}
			return null;
		}
	    @Override
	    protected void onPostExecute(Void unused)
	    {				    		    	
	    	super.onPostExecute(unused);
	    	if(GeoResultsList.size()!=0)
	    	{
	    		Log.d("App!!!","OnPostExcecute-much change");
	    		listViewAdapter.clear();
				for (Address address : GeoResultsList) 
				{        			
					    
					listViewAdapter.add(address);					
				}	
	    		listViewAdapter.notifyDataSetChanged();
	    	}
	    	else
	    		Log.d("App!!!","OnPostExcecute-zero changes");
	    	
	    	Log.d("App!!!","OnPostExcecute");
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

}
