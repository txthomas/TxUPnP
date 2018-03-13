package de.txserver.slickupnp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import de.txserver.slickupnp.R;
import de.txserver.slickupnp.app.SlickUPnP;


public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        String appVersionText = SlickUPnP.getInstance().getVersionName() + " (build " + SlickUPnP.getInstance().getVersionCode() + ")";
        EditTextPreference aboutVersionText =  (EditTextPreference) findPreference("about_version");
        aboutVersionText.setSummary(SlickUPnP.getInstance().getSharedPref().getString("about_version","appVersionText"));
    }
}
