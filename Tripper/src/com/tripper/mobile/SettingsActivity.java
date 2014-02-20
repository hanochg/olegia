package com.tripper.mobile;

import com.tripper.mobile.activity.FindAddress;
import com.tripper.mobile.utils.ContactsListSingleton;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;


public class SettingsActivity extends PreferenceActivity {

	public static final String pref_key_location_language="pref_key_location_language"; 
	public static final String language_list="language_list"; 
	public static final String location_list="location_list"; 
	public static final String pref_key_default_radius="pref_key_default_radius"; 
	public static final String default_radius_text="default_radius_text"; 
	public static final String pref_key_sms_allow="pref_key_sms_allow"; 
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		//reference to the xml
		addPreferencesFromResource(R.xml.pref_general);
		
		//register to changes
		bindPreferenceSummaryToValue(findPreference(language_list));
		bindPreferenceSummaryToValue(findPreference(location_list));
		bindPreferenceSummaryToValue(findPreference(default_radius_text));
		bindPreferenceSummaryToValue(findPreference(pref_key_sms_allow));
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			Log.d("settings","OnPreferenceChangeListener");
			if(preference.getKey().equals(language_list))
			{
				//used in FindAddress


				return true;
			}
			if(preference.getKey().equals(location_list))
			{
				//used in ContactsListSingleton
				
				String countryTwoLetters=value.toString();
				
				ContactsListSingleton.getInstance().setCountryTwoLetters(countryTwoLetters);
				return true;
			}
			if(preference.getKey().equals(default_radius_text))
			{
				//used in ContactDataStructure
				return true;
			}
			if(preference.getKey().equals(pref_key_sms_allow))
			{
				//will be used by service in OnMap
				return true;
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
	}

	@Override
	protected void onPause() {
		super.onPause();

	}
	
	@Override
	protected void onResume() {
		super.onResume();

	}

}
