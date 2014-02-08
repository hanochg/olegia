package com.tripper.mobile.utils;

import java.util.List;

import com.tripper.mobile.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import android.support.v4.app.DialogFragment;

public class EnterDestinationDialog extends DialogFragment implements OnEditorActionListener {

	//Constants
	private static final int GEOCODE_MAX_RESULTS=10;
	private static final int MESSAGE_TYPE=1;
	private static final int THRESHOLD=3;
	private static final int TIME_INTERVAL_THRESHOLD=300;

	//Externals
	public static Address autoCompleteSelectedAddress;
	public static ProgressDialog pd;


	//Globals
	private AsyncGeocode AsyncTasker;
	private MessageHandler msgHandler;
	private Message message;
	private AutoCompleteTextView actvAddress;
	private List<Address> autoCompleteSuggestionAddresses;
	private ArrayAdapter<String> autoCompleteAdapter;
	private double longitude,latitude;


	//Unknown
	public interface EditNameDialogListener {
		void onFinishEditDialog(String inputText);
	}


	// Empty constructor required for DialogFragment
	public EnterDestinationDialog() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//Define Async
		AsyncTasker = new AsyncGeocode(getActivity());
		longitude=latitude=-1;

		//Define Message
		msgHandler=new MessageHandler(getActivity());

		//Define AutoComplete Adapter
		//autoCompleteAdapter = new ArrayAdapterNoFilter(getActivity(), android.R.layout.simple_dropdown_item_1line);//android.R.layout.simple_dropdown_item_1line);
		autoCompleteAdapter.setNotifyOnChange(false);


		View view = inflater.inflate(R.layout.enter_destination_dialog_fragment, container);
		actvAddress = (AutoCompleteTextView) view.findViewById(R.id.atcvAddress);

		//Define AutoComplete TextView	    
		actvAddress.addTextChangedListener(new TextWatcher() {

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
					autoCompleteAdapter.clear();

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});


		//on auto complete selection click: 
		actvAddress.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) 
			{
				Log.d("App!!!","onItemSelected");
				if (arg2 < autoCompleteSuggestionAddresses.size()) 
				{
					autoCompleteSelectedAddress = autoCompleteSuggestionAddresses.get(arg2);
					longitude=autoCompleteSelectedAddress.getLongitude();
					latitude=autoCompleteSelectedAddress.getLatitude();
				}
			}
		});
		actvAddress.setThreshold(THRESHOLD);
		actvAddress.setAdapter(autoCompleteAdapter);


		getDialog().setTitle(getResources().getString(R.string.Enter_Destination_dialog_title));


		// Show soft keyboard automatically
		actvAddress.requestFocus();
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		actvAddress.setOnEditorActionListener(this);

		return view;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			// Return input text to activity
			EditNameDialogListener activity = (EditNameDialogListener) getActivity();
			activity.onFinishEditDialog(actvAddress.getText().toString());
			this.dismiss();
			return true;
		}
		return false;
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
				autoCompleteSuggestionAddresses = new Geocoder(context).getFromLocationName(value, GEOCODE_MAX_RESULTS);        						
				autoCompleteAdapter.clear();
				for (Address a : autoCompleteSuggestionAddresses) 
				{        			
					String temp = ""+ a.getAddressLine(2)+","+ a.getAddressLine(1)+","+a.getAddressLine(0);    
					autoCompleteAdapter.add(temp);
				}
				autoCompleteAdapter.notifyDataSetChanged();				
			} catch (Exception ex) {
				Log.d("Error#@$#$",ex.getMessage());
			}
			return null;
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