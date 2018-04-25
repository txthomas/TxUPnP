package de.txserver.txupnp.upnp

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

import java.util.Stack

import de.txserver.txupnp.R
import de.txserver.txupnp.activity.MainActivity
import de.txserver.txupnp.helper.DeviceModel
import de.txserver.txupnp.helper.ItemModel
import de.txserver.txupnp.helper.MimeTypeMap
import org.fourthline.cling.model.meta.Service
import java.util.ArrayList


class ContentDirectoryBrowseHandler(private val mainActivity: MainActivity, private val callback: ContentDirectoryBrowseCallbacks) : ContentDirectoryBrowseCallbacks {

    private val TAG = ContentDirectoryBrowseHandler::class.java.simpleName;

    private val mListener: BrowseRegistryListener
    private var mService: AndroidUpnpService? = null
    private val mFolders = Stack<ItemModel>()
    private var mIsShowingDeviceList: Boolean = true
    private var mCurrentDevice: DeviceModel? = null
    private var serviceConnectionBound: Boolean
    private var isBrowsing: Boolean = false
    private var stopBrowsing: Boolean = false

    init {
        mListener = BrowseRegistryListener(mainActivity as Context, mainActivity, mService)
        serviceConnectionBound = false
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = service as AndroidUpnpService

            val tempService: AndroidUpnpService? = mService

            if (tempService != null) {
                tempService.registry.addListener(mListener)

                for (device in tempService.registry.devices)
                    mListener.deviceAdded(device)

                tempService.controlPoint?.search()
            }
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

                if (conDir != null) {
                    browse(mainActivity, this, mService!!, conDir, "0")
                }

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

                browse(mainActivity, this, mService!!, model.service, model.id)

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

    fun goBack(): Boolean {

        if (mFolders.empty()) {
            if (!mIsShowingDeviceList) {
                mIsShowingDeviceList = true
                mainActivity.onDisplayDevices()
                refreshDevices()
            } else {
                return true
            }
        } else {

            if (isBrowsing) {
                stopBrowsing = true
            } else {

                val item = mFolders.pop()
                browse(mainActivity, this, mService!!, item.service, item.container!!.parentID)
            }
        }

        return false
    }

    fun browseRequired(service: Service<*, *>, id: String, fromId: Long) {

        if (stopBrowsing) {
            stopBrowsing = false
            browseFinished()
            return
        }

        browse(mainActivity, this, mService!!, service, id, fromId)
    }

    fun browseFinished() {

        isBrowsing = false
        callback.setShowRefreshing(false)
    }

    private fun browse(context: Context, handler: ContentDirectoryBrowseHandler, androidUpnpService: AndroidUpnpService, service: Service<*, *>, id: String, firstResult: Long = 0L) {

        isBrowsing = true
        mService?.controlPoint?.execute(
                CustomContentBrowseActionCallback(context, handler, androidUpnpService, service, id, firstResult))
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

                browse(mainActivity, this, mService!!, item.service, item.id)
            } else {
                if (mCurrentDevice != null) {
                    val service = mCurrentDevice?.contentDirectory
                    if (service != null)
                        browse(mainActivity, this, mService!!, service, "0")
                }
            }
        }
    }

    fun bindServiceConnection() {

        serviceConnectionBound = true
        mainActivity.bindService(
                Intent(mainActivity, AndroidUpnpServiceImpl::class.java),
                serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindServiceConnection() {

        serviceConnectionBound = false
        mService?.registry?.removeListener(mListener)
        mainActivity.unbindService(serviceConnection)
    }

    fun isServiceConnectionBound() : Boolean {

        return serviceConnectionBound
    }

    override fun setShowRefreshing(show: Boolean) {
        callback.setShowRefreshing(show)
    }

    override fun onDisplayDevices() {
        callback.onDisplayDevices()
    }

    override fun onDisplayDirectories() {
        callback.onDisplayDirectories()
    }

    override fun onDisplayItems(items: ArrayList<ItemModel>) {

        if (stopBrowsing) return
        callback.onDisplayItems(items)
    }

    override fun onDisplayAddItems(items: ArrayList<ItemModel>) {

        if (stopBrowsing) return
        callback.onDisplayAddItems(items)
    }

    override fun onDisplayItemsError(error: String) {
        callback.onDisplayItemsError(error)
    }

    override fun onDeviceAdded(device: DeviceModel) {
        callback.onDeviceAdded(device)
    }

    override fun onDeviceRemoved(device: DeviceModel) {
        callback.onDeviceRemoved(device)
    }
}
