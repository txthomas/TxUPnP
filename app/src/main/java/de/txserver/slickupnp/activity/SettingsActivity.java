package de.txserver.slickupnp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import de.txserver.slickupnp.R;

public class SettingsActivity extends AppCompatActivity  {

    private String TAG = SettingsActivity.class.getSimpleName();

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // das Fragment anzeigen
        getFragmentManager().beginTransaction()
                .replace(R.id.settingsContainer, new SettingsFragment())
                .commit();

        setActivityTitle(null);
    }

    private void setActivityTitle(String customText) {

        if (actionBar == null) {
            return;
        }

        if (customText == null) {
            actionBar.setTitle(getResources().getString(R.string.menuItem_settings));
        } else {
            actionBar.setTitle(customText);
        }
    }
}
