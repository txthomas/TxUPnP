package de.txserver.slickupnp.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager

import de.txserver.slickupnp.R
import de.txserver.slickupnp.app.SlickUPnP


class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)

        val appVersionText = (SlickUPnP.instance?.versionName ?: "NaN") + " (build " + (SlickUPnP.instance?.versionCode
                ?: "NaN") + ")"
        val aboutVersionText = findPreference("about_version") as EditTextPreference
        aboutVersionText.summary = SlickUPnP.instance?.getSharedPref()?.getString("about_version", appVersionText) ?: "null"
    }
}
