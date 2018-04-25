package de.txserver.txupnp.app

import android.app.Activity
import android.app.Application
import android.os.Bundle

class ApplicationLifecycleManager : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        foregroundActivityCount++
    }

    override fun onActivityPaused(activity: Activity) {
        foregroundActivityCount--
    }


    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        visibleActivityCount++
    }

    override fun onActivityStopped(activity: Activity) {
        visibleActivityCount--
    }

    companion object {

        private val TAG = ApplicationLifecycleManager::class.java.simpleName

        /** Manages the state of opened vs closed activities, should be 0 or 1.
         * It will be 2 if this value is checked between activity B onStart() and
         * activity A onStop().
         * It could be greater if the top activities are not fullscreen or have
         * transparent backgrounds.
         */
        private var visibleActivityCount = 0

        /** Manages the state of opened vs closed activities, should be 0 or 1
         * because only one can be in foreground at a time. It will be 2 if this
         * value is checked between activity B onResume() and activity A onPause().
         */
        private var foregroundActivityCount = 0

        /** Returns true if app has foreground  */
        val isAppInForeground: Boolean
            get() = foregroundActivityCount > 0

        /** Returns true if any activity of app is visible (or device is sleep when
         * an activity was visible)  */
        val isAppVisible: Boolean
            get() = visibleActivityCount > 0
    }
}
