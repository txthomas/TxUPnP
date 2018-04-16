package de.txserver.slickupnp.upnp

import java.util.ArrayList

import de.txserver.slickupnp.helper.DeviceModel
import de.txserver.slickupnp.helper.ItemModel

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
