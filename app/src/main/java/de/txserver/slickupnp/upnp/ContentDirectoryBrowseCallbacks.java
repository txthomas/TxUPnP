package de.txserver.slickupnp.upnp;

import java.util.ArrayList;

import de.txserver.slickupnp.helper.DeviceModel;
import de.txserver.slickupnp.helper.ItemModel;

public interface ContentDirectoryBrowseCallbacks {

    void setShowRefreshing(boolean show);
    void onDisplayDevices();
    void onDisplayDirectories();
    void onDisplayItems(ArrayList<ItemModel> items);
    void onDisplayAddItems(ArrayList<ItemModel> items);
    void onDisplayItemsError(String error);
    void onDeviceAdded(DeviceModel device);
    void onDeviceRemoved(DeviceModel device);
}
