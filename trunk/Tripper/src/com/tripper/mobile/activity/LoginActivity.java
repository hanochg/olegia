package com.tripper.mobile.activity;

import com.tripper.mobile.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	
	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_PHONE = "Phone Number";
	
	 private enum eConnectionStatus {
		   NoConnection, SighUp, PasswordError, SighIn
		 }
		
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for phone and password at the time of the login attempt.
	private String mPhoneNumber;
	private String mPassword;

	// UI references.
	private EditText mPhoneView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_screen);
		
		SplashScreen.splashActivity.finish();
		
		ParseAnalytics.trackAppOpened(getIntent()); //???
		
		mPhoneView = (EditText) findViewById(R.id.phone);
		mPhoneNumber=getIntent().getStringExtra(EXTRA_PHONE);
		mPhoneView.setText(mPhoneNumber);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid phone, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() 
	{
		if (mAuthTask != null)
		{
			return;
		}

		// Reset errors.
		mPhoneView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mPhoneNumber = mPhoneView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword))
		{
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} 
		else if (mPassword.length() < 4)
		{
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid phone address.
		if (TextUtils.isEmpty(mPhoneNumber)) 
		{
			mPhoneView.setError(getString(R.string.error_field_required));
			focusView = mPhoneView;
			cancel = true;
		} 
		else if( mPhoneNumber.length()<5)
		{
			mPhoneView.setError(getString(R.string.error_invalid_phone));
			focusView = mPhoneView;
			cancel = true;
		}

		if (cancel)
		{
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} 
		else
		{
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(this);
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) 
	{
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	
	
	//connect a server and sigh up  
		
	public class UserLoginTask extends AsyncTask<Void, Void, eConnectionStatus> {
		
		private Context context;
		
		public UserLoginTask(Context context) {
			super();
			this.context = context;
		}
		
		protected eConnectionStatus doInBackground(Void... params)
		{
			// TODO: attempt authentication against a network service.
			try
			{				
				ParseUser.logIn(mPhoneNumber, mPassword);
			} 
			catch (ParseException  e) 
			{
				switch (e.getCode())
				{
					case ParseException.OBJECT_NOT_FOUND: //no such user + password
						
						ParseUser user = new ParseUser();					
						user.setUsername(mPhoneNumber);
						user.setPassword(mPassword);
						
						try
						{		
							user.signUp();		
						} 
						catch (ParseException  e2) 
						{
							switch (e2.getCode())
							{
								case ParseException.USERNAME_TAKEN:
									return eConnectionStatus.PasswordError;	
								
								default:
									return eConnectionStatus.NoConnection;	
							}	
						}
						
						return eConnectionStatus.SighUp;	
						
					case ParseException.CONNECTION_FAILED:
						//can't connect server
						return eConnectionStatus.NoConnection;	
						
					default:
						return eConnectionStatus.NoConnection;	
				}//Switch					
			}//Catch
			
			return eConnectionStatus.SighIn;	//Log in success
		}

		@Override
		protected void onPostExecute(final eConnectionStatus success)
		{
			mAuthTask = null;
			showProgress(false);
			
			switch (success)
			{
				case SighIn: case SighUp:
					String message = (success==eConnectionStatus.SighIn)?"You have sign in successfully":"You have sign up successfully";
					
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
					finish();
					Intent intent = new Intent(context, MainActivity.class);
					startActivity(intent);	
					
				return;
								
				case NoConnection:
					Toast.makeText(getApplicationContext(), "Connection can't be established", Toast.LENGTH_LONG).show();
					finish();
					return;
					
				case PasswordError:
					mPasswordView
					.setError(getString(R.string.error_incorrect_password));
					mPasswordView.requestFocus();
				return;				
			} 				
		}

		@Override
		protected void onCancelled() 
		{
			mAuthTask = null;
			showProgress(false);
		}
	}
}
