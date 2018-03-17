package de.txserver.slickupnp.upnp;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

import java.util.Stack;

import de.txserver.slickupnp.R;
import de.txserver.slickupnp.activity.MainActivity;
import de.txserver.slickupnp.helper.DeviceModel;
import de.txserver.slickupnp.helper.ItemModel;
import de.txserver.slickupnp.helper.MimeTypeMap;


public class ContentDirectoryBrowseHandler {

    private MainActivity mainActivity;

    private BrowseRegistryListener mListener;
    private AndroidUpnpService mService;
    private Stack<ItemModel> mFolders = new Stack<ItemModel>();
    private Boolean mIsShowingDeviceList = true;
    private DeviceModel mCurrentDevice = null;

    public ContentDirectoryBrowseHandler(MainActivity mainActivity) {

        this.mainActivity = mainActivity;
        mListener = new BrowseRegistryListener(mainActivity, mainActivity, mService);
    }

    public void navigateTo(Object model) {
        if (model instanceof DeviceModel) {

            DeviceModel deviceModel = (DeviceModel)model;
            Device device = deviceModel.getDevice();

            if (device.isFullyHydrated()) {
                Service conDir = deviceModel.getContentDirectory();

                if (conDir != null)
                    mService.getControlPoint().execute(
                            new CustomContentBrowseActionCallback(mainActivity, mainActivity, mService, conDir, "0"));

                if (mainActivity != null)
                    mainActivity.onDisplayDirectories();

                mIsShowingDeviceList = false;

                mCurrentDevice = deviceModel;
            } else {
                Toast.makeText(mainActivity, R.string.info_still_loading, Toast.LENGTH_SHORT)
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
                        new CustomContentBrowseActionCallback(mainActivity, mainActivity, mService, item.getService(),
                                item.getId()));

            } else {
                mainActivity.setShowRefreshing(false);
                try {
                    Uri uri = Uri.parse(item.getUrl());
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String type = mime.getMimeTypeFromUrl(uri.toString());
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, type);
                    mainActivity.startActivity(intent);
                } catch(NullPointerException ex) {
                    Toast.makeText(mainActivity, R.string.info_could_not_start_activity, Toast.LENGTH_SHORT)
                            .show();
                } catch(ActivityNotFoundException ex) {
                    Toast.makeText(mainActivity, R.string.info_no_handler, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    public Boolean goBack() {
        if (mFolders.empty()) {
            if (!mIsShowingDeviceList) {
                mIsShowingDeviceList = true;
                mainActivity.onDisplayDevices();
                refreshDevices();
            } else {
                return true;
            }
        } else {
            ItemModel item = mFolders.pop();

            mService.getControlPoint().execute(
                    new CustomContentBrowseActionCallback(mainActivity, mainActivity, mService, item.getService(),
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
            mainActivity.onDisplayDevices();

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
                        new CustomContentBrowseActionCallback(mainActivity, mainActivity, mService, item.getService(),
                                item.getId()));
            } else {
                if (mCurrentDevice != null) {
                    Service service = mCurrentDevice.getContentDirectory();
                    if (service != null)
                        mService.getControlPoint().execute(
                                new CustomContentBrowseActionCallback(mainActivity, mainActivity, mService, service, "0"));
                }
            }
        }
    }

    public Boolean bindServiceConnection() {
        if (mainActivity == null)
            return false;

        mainActivity.bindService(
                new Intent(mainActivity, AndroidUpnpServiceImpl.class),
                serviceConnection, Context.BIND_AUTO_CREATE);

        return true;
    }

    public Boolean unbindServiceConnection() {
        if (mService != null)
            mService.getRegistry().removeListener(mListener);

        if (mainActivity == null)
            return false;

        mainActivity.unbindService(serviceConnection);
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
}
