package de.txserver.slickupnp;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.net.URI;
import java.util.ArrayList;
import java.util.Stack;

public class ContentDirectoryBrowseTaskFragment extends Fragment {

    static interface Callbacks {
        void onDisplayDevices();
        void onDisplayDirectories();
        void onDisplayItems(ArrayList<ItemModel> items);
        void onDisplayAddItems(ArrayList<ItemModel> items);
        void onDisplayItemsError(String error);
        void onDeviceAdded(DeviceModel device);
        void onDeviceRemoved(DeviceModel device);
    }

    private Callbacks mCallbacks;
    private BrowseRegistryListener mListener = new BrowseRegistryListener();
    private AndroidUpnpService mService;
    private Stack<ItemModel> mFolders = new Stack<ItemModel>();
    private Boolean mIsShowingDeviceList = true;
    private DeviceModel mCurrentDevice = null;
    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity == null)
            return;

        mActivity = activity;
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        bindServiceConnection();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindServiceConnection();
    }

    public void navigateTo(Object model) {
        if (model instanceof DeviceModel) {

            DeviceModel deviceModel = (DeviceModel)model;
            Device device = deviceModel.getDevice();

            if (device.isFullyHydrated()) {
                Service conDir = deviceModel.getContentDirectory();

                if (conDir != null)
                    mService.getControlPoint().execute(
                            new CustomContentBrowseActionCallback(mService, conDir, "0"));

                if (mCallbacks != null)
                    mCallbacks.onDisplayDirectories();

                mIsShowingDeviceList = false;

                mCurrentDevice = deviceModel;
            } else {
                Toast.makeText(mActivity, R.string.info_still_loading, Toast.LENGTH_SHORT)
                    .show();
            }
        }

        if (model instanceof ItemModel) {

            ItemModel item = (ItemModel)model;

            if (item.isContainer()) {
                if (mFolders.isEmpty())
                    mFolders.push(item);
                else
                    if (!mFolders.peek().getId().equals(item.getId()))
                        mFolders.push(item);

                mService.getControlPoint().execute(
                        new CustomContentBrowseActionCallback(mService, item.getService(),
                                item.getId()));

            } else {
                try {
                    Uri uri = Uri.parse(item.getUrl());
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String type = mime.getMimeTypeFromUrl(uri.toString());
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, type);
                    startActivity(intent);
                } catch(NullPointerException ex) {
                    Toast.makeText(mActivity, R.string.info_could_not_start_activity, Toast.LENGTH_SHORT)
                            .show();
                } catch(ActivityNotFoundException ex) {
                    Toast.makeText(mActivity, R.string.info_no_handler, Toast.LENGTH_SHORT)
                        .show();
                }
            }
        }
    }

    public Boolean goBack() {
        if (mFolders.empty()) {
            if (!mIsShowingDeviceList) {
                mIsShowingDeviceList = true;
                if (mCallbacks != null)
                    mCallbacks.onDisplayDevices();
            } else {
                return true;
            }
        } else {
            ItemModel item = mFolders.pop();

            mService.getControlPoint().execute(
                    new CustomContentBrowseActionCallback(mService, item.getService(),
                            item.getContainer().getParentID()));
        }

        return false;
    }

    public void refreshDevices() {
        if (mService == null)
            return;

        mService.getRegistry().removeAllRemoteDevices();

        for (Device device : mService.getRegistry().getDevices())
            mListener.deviceAdded(device);

        mService.getControlPoint().search();
    }

    public void refreshCurrent() {
        if (mService == null)
            return;

        if (mIsShowingDeviceList != null && mIsShowingDeviceList) {
            if (mCallbacks != null)
                mCallbacks.onDisplayDevices();

            mService.getRegistry().removeAllRemoteDevices();

            for (Device device : mService.getRegistry().getDevices())
                mListener.deviceAdded(device);

            mService.getControlPoint().search();
        } else {
            if (!mFolders.empty()) {
                ItemModel item = mFolders.peek();
                if (item == null)
                    return;

                mService.getControlPoint().execute(
                        new CustomContentBrowseActionCallback(mService, item.getService(),
                                item.getId()));
            } else {
                if (mCurrentDevice != null) {
                    Service service = mCurrentDevice.getContentDirectory();
                    if (service != null)
                        mService.getControlPoint().execute(
                            new CustomContentBrowseActionCallback(mService, service, "0"));
                }
            }
        }
    }

    private Boolean bindServiceConnection() {
        Context context = mActivity.getApplicationContext();
        if (context == null)
            return false;

        context.bindService(
            new Intent(mActivity, AndroidUpnpServiceImpl.class),
            serviceConnection, Context.BIND_AUTO_CREATE
        );

        return true;
    }

    private Boolean unbindServiceConnection() {
        if (mService != null)
            mService.getRegistry().removeListener(mListener);

        Context context = mActivity.getApplicationContext();
        if (context == null)
            return false;

        context.unbindService(serviceConnection);
        return true;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = (AndroidUpnpService) service;
            mService.getRegistry().addListener(mListener);

            for (Device device : mService.getRegistry().getDevices())
                mListener.deviceAdded(device);

            mService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    private class BrowseRegistryListener extends DefaultRegistryListener {
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
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(mActivity);
                if (prefs.getBoolean("settings_validate_devices", false)) {
                    if (device.isFullyHydrated())
                        mService.getControlPoint().execute(
                            new CustomContentBrowseTestCallback(device, conDir));
                } else {
                    if (mCallbacks != null)
                        mCallbacks.onDeviceAdded(deviceModel);
                }
            }
        }

        public void deviceRemoved(Device device) {
            if (mCallbacks != null)
                mCallbacks.onDeviceRemoved(new DeviceModel(R.drawable.ic_server_network_black_24px, device));
        }
    }

    private class CustomContentBrowseActionCallback extends Browse {
        private AndroidUpnpService androidUpnpService;
        private Service service;
        private String id;
        private long firstResult;

        public CustomContentBrowseActionCallback(AndroidUpnpService androidUpnpService, Service service, String id) {
            this(androidUpnpService, service, id, 0L);
        }

        public CustomContentBrowseActionCallback(AndroidUpnpService androidUpnpService, Service service, String id, long firstResult) {
            super(service, id, BrowseFlag.DIRECT_CHILDREN, "*", firstResult, 99999L,
                    new SortCriterion(true, "dc:title"));

            this.androidUpnpService = androidUpnpService;
            this.service = service;
            this.id = id;
            this.firstResult = firstResult;

            if (mCallbacks != null)
                mCallbacks.onDisplayDirectories();
        }

        private ItemModel createItemModel(DIDLObject item) {

            ItemModel itemModel = new ItemModel(getResources(),
                    R.drawable.ic_folder_black_24px, service, item);

            URI usableIcon = item.getFirstPropertyValue(DIDLObject.Property.UPNP.ICON.class);
            if (usableIcon == null || usableIcon.toString().isEmpty()) {
                usableIcon = item.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
            }
            if (usableIcon != null)
                itemModel.setIconUrl(usableIcon.toString());

            if (item instanceof Item) {
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(itemModel.getUrl()));

                if (mimeType.contains("video")) {
                    itemModel.setIcon(R.drawable.ic_file_video_black_24px);

                } else if (mimeType.contains("audio")) {
                    itemModel.setIcon(R.drawable.ic_file_music_black_24px);

                } else if (mimeType.contains("image")) {
                    itemModel.setIcon(R.drawable.ic_file_image_black_24px);

                } else {
                    itemModel.setIcon(R.drawable.ic_file_black_24px);
                }

                SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(mActivity);

                if (prefs.getBoolean("settings_hide_file_icons", false))
                    itemModel.setHideIcon(true);

                if (prefs.getBoolean("settings_show_extensions", false))
                    itemModel.setShowExtension(true);
            }

            return itemModel;
        }

        @Override
        public void received(final ActionInvocation actionInvocation, final DIDLContent didl) {

            ArrayList<ItemModel> items = new ArrayList<ItemModel>();
            Long totalMatches;
            Long numberReturned;

            try {
                for (Container childContainer : didl.getContainers())
                    items.add(createItemModel(childContainer));

                for (Item childItem : didl.getItems())
                    items.add(createItemModel(childItem));

                totalMatches = Long.parseLong(actionInvocation.getOutput("TotalMatches").toString());
                numberReturned = Long.parseLong(actionInvocation.getOutput("NumberReturned").toString());

                if (firstResult + items.size() < totalMatches) {

                    androidUpnpService.getControlPoint().execute(
                            new CustomContentBrowseActionCallback(androidUpnpService, service,
                                    id, firstResult + items.size()));

                    if (mCallbacks != null) {
                        if (firstResult == 0) {
                            mCallbacks.onDisplayItems(items);
                        } else {
                            mCallbacks.onDisplayAddItems(items);
                        }
                    }
                } else {

                    if (mCallbacks != null)
                        mCallbacks.onDisplayItems(items);
                }

            } catch (Exception ex) {
                actionInvocation.setFailure(new ActionException(
                        ErrorCode.ACTION_FAILED,
                        "Can't create list childs: " + ex, ex));
                failure(actionInvocation, null, ex.getMessage());
            }
        }

        @Override
        public void updateStatus(Status status) {

        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse response, String s) {
            if (mCallbacks != null)
                mCallbacks.onDisplayItemsError(createDefaultFailureMessage(invocation, response));
        }
    }

    private class CustomContentBrowseTestCallback extends Browse {
        private Device device;
        private Service service;

        public CustomContentBrowseTestCallback(Device device, Service service) {
            super(service, "0", BrowseFlag.DIRECT_CHILDREN, "*", 0, 99999L,
                    new SortCriterion(true, "dc:title"));

            this.device = device;
            this.service = service;
        }

        @Override
        public void received(final ActionInvocation actionInvocation, final DIDLContent didl) {
            if (mCallbacks != null)
                mCallbacks.onDeviceAdded(new DeviceModel(R.drawable.ic_server_network_black_24px, device));
        }

        @Override
        public void updateStatus(Status status) {

        }

        @Override
        public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {

        }
    }
}