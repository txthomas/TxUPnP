package de.txserver.slickupnp;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // das Fragment anzeigen
        getFragmentManager().beginTransaction()
                .replace(R.id.settingsContainer, new SettingsFragment())
                .commit();

    }
}
