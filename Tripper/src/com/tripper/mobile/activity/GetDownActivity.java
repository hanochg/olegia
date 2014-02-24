package com.tripper.mobile.activity;

import com.tripper.mobile.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;

public class GetDownActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setFinishOnTouchOutside(false);
		setContentView(R.layout.get_down_screen);
	}
	
	
	public void OnBtnGotItClick(View view)
	{	
		finish();
	}
	
}
