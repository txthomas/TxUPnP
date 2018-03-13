package de.txserver.slickupnp.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import de.txserver.slickupnp.R;
import de.txserver.slickupnp.app.SlickUPnP;

public class AboutActivity extends AppCompatActivity {

    private String TAG = AboutActivity.class.getSimpleName();

    ActionBar actionBar;

    private TextView aboutVersionTextView;
    private TextView aboutDeveloperTextView;
    private TextView aboutWebsiteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        setActivityTitle();

        String appVersionText = getResources().getString(R.string.app_name) + ": " + SlickUPnP.getInstance().getVersionName() + " (build " + SlickUPnP.getInstance().getVersionCode() + ")";
        aboutVersionTextView  = (TextView) findViewById(R.id.aboutVersion_text);
        aboutVersionTextView.setText(appVersionText);

        String developerText = getResources().getString(R.string.about_developer) + ": " + getResources().getString(R.string.about_developer_name);
        aboutDeveloperTextView = (TextView) findViewById(R.id.aboutDeveloper_text);
        aboutDeveloperTextView.setText(developerText);

        String websiteText = getResources().getString(R.string.about_website) + ": " + getResources().getString(R.string.about_website_url);
        aboutWebsiteTextView = (TextView) findViewById(R.id.aboutWebsite_text);
        aboutWebsiteTextView.setText(websiteText);
    }

    private AppCompatActivity getActivity() {

        return this;
    }

    private void setActivityTitle() {

        this.setTitle(getResources().getString(R.string.menuItem_about));
    }
}
