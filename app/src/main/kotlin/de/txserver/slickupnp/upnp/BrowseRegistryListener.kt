package de.txserver.slickupnp.upnp

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry

import de.txserver.slickupnp.R
import de.txserver.slickupnp.app.SlickUPnP
import de.txserver.slickupnp.helper.DeviceModel

class BrowseRegistryListener(private val context: Context, private val callbacks: ContentDirectoryBrowseCallbacks, private val androidUpnpService: AndroidUpnpService?) : DefaultRegistryListener() {

    var TAG = BrowseRegistryListener::class.java.simpleName
    private val prefs: SharedPreferences

    init {
        prefs = SlickUPnP.instance.getSharedPref()
    }

    override fun remoteDeviceDiscoveryStarted(registry: Registry?, device: RemoteDevice?) {
        deviceAdded(device)
    }

    override fun remoteDeviceDiscoveryFailed(registry: Registry?, device: RemoteDevice?, ex: Exception?) {
        deviceRemoved(device)
    }

    override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
        deviceAdded(device)
    }

    override fun remoteDeviceRemoved(registry: Registry, device: RemoteDevice) {
        deviceRemoved(device)
    }

    override fun localDeviceAdded(registry: Registry, device: LocalDevice) {
        deviceAdded(device)
    }

    override fun localDeviceRemoved(registry: Registry, device: LocalDevice) {
        deviceRemoved(device)
    }

    fun deviceAdded(device: Device<*, *, *>?) {

        val deviceModel = DeviceModel(R.drawable.ic_server_network_black_24px, device)

        val conDir = deviceModel.contentDirectory
        if (conDir != null) {

            if (prefs.getBoolean("settings_validate_devices", false)) {
                if (device!!.isFullyHydrated)
                    androidUpnpService?.controlPoint?.execute(
                            CustomContentBrowseTestCallback(callbacks, device, conDir))
            } else {
                callbacks.onDeviceAdded(deviceModel)
            }
        }
    }

    fun deviceRemoved(device: Device<*, *, *>?) {

        callbacks.onDeviceRemoved(DeviceModel(R.drawable.ic_server_network_black_24px, device))
    }
}
