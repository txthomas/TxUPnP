package de.txserver.slickupnp.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ApplicationLifecycleManager implements Application.ActivityLifecycleCallbacks {

    private static String TAG = ApplicationLifecycleManager.class.getSimpleName();

    /** Manages the state of opened vs closed activities, should be 0 or 1.
     * It will be 2 if this value is checked between activity B onStart() and
     * activity A onStop().
     * It could be greater if the top activities are not fullscreen or have
     * transparent backgrounds.
     */
    private static int visibleActivityCount = 0;

    /** Manages the state of opened vs closed activities, should be 0 or 1
     * because only one can be in foreground at a time. It will be 2 if this
     * value is checked between activity B onResume() and activity A onPause().
     */
    private static int foregroundActivityCount = 0;

    /** Returns true if app has foreground */
    public static boolean isAppInForeground(){
        return foregroundActivityCount > 0;
    }

    /** Returns true if any activity of app is visible (or device is sleep when
     * an activity was visible) */
    public static boolean isAppVisible(){
        return visibleActivityCount > 0;
    }

    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    public void onActivityDestroyed(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        foregroundActivityCount ++;
    }

    public void onActivityPaused(Activity activity) {
        foregroundActivityCount --;
    }


    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityStarted(Activity activity) {
        visibleActivityCount ++;
    }

    public void onActivityStopped(Activity activity) {
        visibleActivityCount --;
    }
}
