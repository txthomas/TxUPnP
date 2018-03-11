package de.txserver.slickupnp.upnp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import de.txserver.slickupnp.R;
import de.txserver.slickupnp.app.SlickUPnP;
import de.txserver.slickupnp.helper.DeviceModel;

public class BrowseRegistryListener extends DefaultRegistryListener {

    public String TAG = BrowseRegistryListener.class.getSimpleName();

    private Context context;
    private SharedPreferences prefs;

    private ContentDirectoryBrowseCallbacks callbacks;
    private AndroidUpnpService androidUpnpService;

    public BrowseRegistryListener(Context context, ContentDirectoryBrowseCallbacks callbacks, AndroidUpnpService androidUpnpService) {
        super();

        this.callbacks = callbacks;
        this.androidUpnpService = androidUpnpService;

        this.context = context;
        prefs = SlickUPnP.getInstance().getSharedPref();
    }

    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
        deviceRemoved(device);
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        deviceRemoved(device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        deviceAdded(device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        deviceRemoved(device);
    }

    public void deviceAdded(Device device) {

        DeviceModel deviceModel = new DeviceModel(R.drawable.ic_server_network_black_24px, device);

        Service conDir = deviceModel.getContentDirectory();
        if (conDir != null) {

            if (prefs.getBoolean("settings_validate_devices", false)) {
                if (device.isFullyHydrated())
                    androidUpnpService.getControlPoint().execute(
                            new CustomContentBrowseTestCallback(callbacks, device, conDir));
            } else {
                callbacks.onDeviceAdded(deviceModel);
            }
        }
    }

    public void deviceRemoved(Device device) {

        callbacks.onDeviceRemoved(new DeviceModel(R.drawable.ic_server_network_black_24px, device));
    }
}
