package de.txserver.slickupnp.activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.txserver.slickupnp.R;


public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
