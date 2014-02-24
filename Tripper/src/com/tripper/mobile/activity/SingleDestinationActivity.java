package com.tripper.mobile.activity;

import com.tripper.mobile.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;

public class SingleDestinationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setFinishOnTouchOutside(false);
		setContentView(R.layout.single_destanation_screen);
	}
	
	
	public void btnGotItSingleClick(View view)
	{	
		finish();
	}

}
