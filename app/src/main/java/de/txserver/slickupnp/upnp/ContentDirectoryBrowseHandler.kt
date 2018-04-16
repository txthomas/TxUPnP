package de.txserver.slickupnp.upnp

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.widget.Toast

import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service

import java.util.Stack

import de.txserver.slickupnp.R
import de.txserver.slickupnp.activity.MainActivity
import de.txserver.slickupnp.helper.DeviceModel
import de.txserver.slickupnp.helper.ItemModel
import de.txserver.slickupnp.helper.MimeTypeMap


class ContentDirectoryBrowseHandler(private val mainActivity: MainActivity) {

    private val mListener: BrowseRegistryListener
    private var mService: AndroidUpnpService? = null
    private val mFolders = Stack<ItemModel>()
    private var mIsShowingDeviceList: Boolean = true
    private var mCurrentDevice: DeviceModel? = null

    init {
        mListener = BrowseRegistryListener(mainActivity as Context, mainActivity, mService)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = service as AndroidUpnpService
            mService?.registry?.addListener(mListener)

            for (device in mService!!.registry.devices)
                mListener.deviceAdded(device)

            mService?.controlPoint?.search()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
        }
    }

    fun navigateTo(model: Any) {
        if (model is DeviceModel) {

            val device = model.device

            if (device.isFullyHydrated) {
                val conDir = model.contentDirectory

                if (conDir != null)
                    mService?.controlPoint?.execute(
                            CustomContentBrowseActionCallback(mainActivity, mainActivity, mService!!, conDir, "0"))

                mainActivity.onDisplayDirectories()

                mIsShowingDeviceList = false

                mCurrentDevice = model
            } else {
                Toast.makeText(mainActivity, R.string.info_still_loading, Toast.LENGTH_SHORT)
                        .show()
            }
        }

        if (model is ItemModel) {

            if (model.isContainer) {
                if (mFolders.isEmpty())
                    mFolders.push(model)
                else if (mFolders.peek().id != model.id)
                    mFolders.push(model)

                mService?.controlPoint?.execute(
                        CustomContentBrowseActionCallback(mainActivity, mainActivity, mService!!, model.service,
                                model.id))

            } else {
                mainActivity.setShowRefreshing(false)
                try {
                    val uri = Uri.parse(model.url)
                    val mime = MimeTypeMap.getSingleton()
                    val type = mime.getMimeTypeFromUrl(uri.toString())
                    val intent = Intent()
                    intent.action = android.content.Intent.ACTION_VIEW
                    intent.setDataAndType(uri, type)
                    mainActivity.startActivity(intent)
                } catch (ex: NullPointerException) {
                    Toast.makeText(mainActivity, R.string.info_could_not_start_activity, Toast.LENGTH_SHORT)
                            .show()
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(mainActivity, R.string.info_no_handler, Toast.LENGTH_SHORT)
                            .show()
                }

            }
        }
    }

    fun goBack(): Boolean? {
        if (mFolders.empty()) {
            if (!mIsShowingDeviceList) {
                mIsShowingDeviceList = true
                mainActivity.onDisplayDevices()
                refreshDevices()
            } else {
                return true
            }
        } else {
            val item = mFolders.pop()

            mService?.controlPoint?.execute(
                    CustomContentBrowseActionCallback(mainActivity, mainActivity, mService!!, item.service,
                            item.container!!.parentID))
        }

        return false
    }

    fun refreshDevices() {
        if (mService == null)
            return

        mService?.registry?.removeAllRemoteDevices()

        for (device in mService!!.registry.devices)
            mListener.deviceAdded(device)

        mService?.controlPoint?.search()
    }

    fun refreshCurrent() {
        if (mService == null)
            return

        if (mIsShowingDeviceList) {
            mainActivity.onDisplayDevices()

            mService?.registry?.removeAllRemoteDevices()

            for (device in mService!!.registry.devices)
                mListener.deviceAdded(device)

            mService?.controlPoint?.search()
        } else {
            if (!mFolders.empty()) {
                val item = mFolders.peek() ?: return

                mService?.controlPoint?.execute(
                        CustomContentBrowseActionCallback(mainActivity, mainActivity, mService!!, item.service,
                                item.id))
            } else {
                if (mCurrentDevice != null) {
                    val service = mCurrentDevice?.contentDirectory
                    if (service != null)
                        mService?.controlPoint?.execute(
                                CustomContentBrowseActionCallback(mainActivity, mainActivity, mService!!, service, "0"))
                }
            }
        }
    }

    fun bindServiceConnection(): Boolean? {
//        if (mainActivity == null)
//            return false

        mainActivity.bindService(
                Intent(mainActivity, AndroidUpnpServiceImpl::class.java),
                serviceConnection, Context.BIND_AUTO_CREATE)

        return true
    }

    fun unbindServiceConnection(): Boolean? {
        if (mService != null)
            mService?.registry?.removeListener(mListener)

//        if (mainActivity == null)
//            return false

        mainActivity.unbindService(serviceConnection)
        return true
    }
}
