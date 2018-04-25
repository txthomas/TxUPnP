package de.txserver.txupnp.app

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.util.Log

import de.txserver.txupnp.R

class TxUPnP : Application() {

    var TAG = TxUPnP::class.java.simpleName

    private var sharedPref: SharedPreferences? = null

    init {
        instance = this
    }

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

        registerActivityLifecycleCallbacks(ApplicationLifecycleManager())

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)
    }

    fun getSharedPref(): SharedPreferences {

        return (sharedPref ?: PreferenceManager.getDefaultSharedPreferences(this))
    }

    companion object {
        @get:Synchronized
        lateinit var instance: TxUPnP
            private set
    }
}
