package de.txserver.txupnp.upnp

import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.contentdirectory.callback.Browse
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.SortCriterion

import de.txserver.txupnp.R
import de.txserver.txupnp.activity.MainActivity
import de.txserver.txupnp.helper.DeviceModel

class CustomContentBrowseTestCallback(private val callbacks: ContentDirectoryBrowseCallbacks, private val device: Device<*, *, *>, private val service: Service<*, *>) : Browse(service, "0", BrowseFlag.DIRECT_CHILDREN, "*", 0, 99999L, SortCriterion(true, "dc:title")) {

    override fun received(actionInvocation: ActionInvocation<*>, didl: DIDLContent) {
        callbacks.onDeviceAdded(DeviceModel(R.drawable.ic_server_network_black_24px, device))
    }

    override fun updateStatus(status: Browse.Status) {

    }

    override fun failure(actionInvocation: ActionInvocation<*>, upnpResponse: UpnpResponse, s: String) {

    }
}
