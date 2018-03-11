package de.txserver.slickupnp.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import de.txserver.slickupnp.R;

public class SlickUPnP extends Application {

    public String TAG = SlickUPnP.class.getSimpleName();
    private static SlickUPnP mInstance;

    private SharedPreferences sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        registerActivityLifecycleCallbacks(new ApplicationLifecycleManager());

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
    }

    public static synchronized SlickUPnP getInstance() {
        return mInstance;
    }

    public SharedPreferences getSharedPref() {

        if (sharedPref == null) {

            sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        }

        return sharedPref;
    }

    public int getVersionCode() {

        PackageManager pm = getPackageManager();

        try {

            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);

            return pi.versionCode;

        } catch (PackageManager.NameNotFoundException e) {

            Log.e(TAG, e.getMessage());
        }

        return 0;
    }

    public String getVersionName() {

        PackageManager pm = getPackageManager();

        try {

            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);

            return pi.versionName;

        } catch (PackageManager.NameNotFoundException e) {

            Log.e(TAG, e.getMessage());
        }

        return "0";
    }
}
