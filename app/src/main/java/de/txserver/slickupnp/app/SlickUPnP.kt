package de.txserver.slickupnp.app

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.preference.EditTextPreference
import android.preference.PreferenceManager
import android.util.Log

import de.txserver.slickupnp.R

class SlickUPnP : Application() {

    var TAG = SlickUPnP::class.java.simpleName

    private var sharedPref: SharedPreferences? = null

    val versionCode: Int
        get() {

            val pm = packageManager

            try {

                val pi = pm.getPackageInfo(packageName, 0)

                return pi.versionCode

            } catch (e: PackageManager.NameNotFoundException) {

                Log.e(TAG, e.message)
            }

            return 0
        }

    val versionName: String
        get() {

            val pm = packageManager

            try {

                val pi = pm.getPackageInfo(packageName, 0)

                return pi.versionName

            } catch (e: PackageManager.NameNotFoundException) {

                Log.e(TAG, e.message)
            }

            return "0"
        }

    override fun onCreate() {
        super.onCreate()
        instance = this

        registerActivityLifecycleCallbacks(ApplicationLifecycleManager())

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)
    }

    fun getSharedPref(): SharedPreferences {

        return (sharedPref ?: PreferenceManager.getDefaultSharedPreferences(this))
    }

    companion object {
        @get:Synchronized
        var instance: SlickUPnP? = null
            private set
    }
}
