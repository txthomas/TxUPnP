package de.txserver.slickupnp.upnp;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;

import de.txserver.slickupnp.R;
import de.txserver.slickupnp.activity.MainActivity;
import de.txserver.slickupnp.helper.DeviceModel;

public class CustomContentBrowseTestCallback extends Browse {

    private ContentDirectoryBrowseCallbacks callbacks;
    private Device device;
    private Service service;

    public CustomContentBrowseTestCallback(ContentDirectoryBrowseCallbacks callbacks, Device device, Service service) {
        super(service, "0", BrowseFlag.DIRECT_CHILDREN, "*", 0, 99999L,
                new SortCriterion(true, "dc:title"));

        this.callbacks = callbacks;
        this.device = device;
        this.service = service;
    }

    @Override
    public void received(final ActionInvocation actionInvocation, final DIDLContent didl) {
        callbacks.onDeviceAdded(new DeviceModel(R.drawable.ic_server_network_black_24px, device));
    }

    @Override
    public void updateStatus(Status status) {

    }

    @Override
    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {

    }
}
