package com.tripper.mobile;

import com.tripper.mobile.utils.ContactsListSingleton;

import android.content.SharedPreferences;
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
	public static final String default_radius_text_multi="default_radius_text_multi"; 
	public static final String pref_key_sms_allow="pref_key_sms_allow"; 
	public static final String default_radius_text_single="default_radius_text_single";
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		//reference to the xml
		addPreferencesFromResource(R.xml.pref_general);
		
		//register to changes
		bindPreferenceSummaryToValue(findPreference(language_list));
		bindPreferenceSummaryToValue(findPreference(location_list));
		bindPreferenceSummaryToValue(findPreference(default_radius_text_multi));
		bindPreferenceSummaryToValue(findPreference(pref_key_sms_allow));
		bindPreferenceSummaryToValue(findPreference(default_radius_text_single));
		
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
				String languageSettings=value.toString();
				
				//used in ContactsListSingleton	
				ContactsListSingleton.getInstance().setLanguageFromSettings(languageSettings);

				return true;
			}
			if(preference.getKey().equals(location_list))
			{
				//used in ContactsListSingleton				
				String countryTwoLetters=value.toString();
				
				ContactsListSingleton.getInstance().setCountryTwoLetters(countryTwoLetters);
				return true;
			}
			if(preference.getKey().equals(default_radius_text_multi))
			{
				//used in ContactDataStructure
				return true;
			}
			if(preference.getKey().equals(default_radius_text_single))
			{
				double radius=Double.valueOf(value.toString());
				ContactsListSingleton.getInstance().setRadiusSingleFromSettings(radius);
				return true;
			}
			if(preference.getKey().equals(pref_key_sms_allow))
			{
				boolean allowSMS=Boolean.parseBoolean(value.toString());
				//will be used by service in OnMap
				ContactsListSingleton.getInstance().setGlobalPreferenceAllowSMS(allowSMS);
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
