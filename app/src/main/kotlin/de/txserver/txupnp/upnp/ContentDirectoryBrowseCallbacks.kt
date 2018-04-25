package de.txserver.txupnp.upnp

import java.util.ArrayList

import de.txserver.txupnp.helper.DeviceModel
import de.txserver.txupnp.helper.ItemModel

interface ContentDirectoryBrowseCallbacks {

    fun setShowRefreshing(show: Boolean)
    fun onDisplayDevices()
    fun onDisplayDirectories()
    fun onDisplayItems(items: ArrayList<ItemModel>)
    fun onDisplayAddItems(items: ArrayList<ItemModel>)
    fun onDisplayItemsError(error: String)
    fun onDeviceAdded(device: DeviceModel)
    fun onDeviceRemoved(device: DeviceModel)
}
