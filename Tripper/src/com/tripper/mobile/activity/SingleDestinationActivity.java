package com.tripper.mobile.activity;

import com.tripper.mobile.R;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.view.View;

public class SingleDestinationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setFinishOnTouchOutside(false);
		setContentView(R.layout.single_destanation_screen);
		
		//Reset Settings values
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		//Writing global settings Settings				
		ContactsListSingleton.getInstance().setDefaultSettingsFromContex(this);
	}
	
	
	public void OnBtnbtnGotItSingleClick(View view)
	{	
		finish();
	}

}
