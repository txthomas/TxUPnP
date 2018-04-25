package de.txserver.txupnp.activity

import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.PreferenceFragment

import de.txserver.txupnp.R
import de.txserver.txupnp.app.TxUPnP


class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)

        val appVersionText = TxUPnP.instance.versionName + " (build " + TxUPnP.instance.versionCode + ")"
        val aboutVersionText = findPreference("about_version") as EditTextPreference
        aboutVersionText.summary = TxUPnP.instance.getSharedPref().getString("about_version", appVersionText)
    }
}
