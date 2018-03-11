package de.txserver.slickupnp.upnp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.net.URI;
import java.util.ArrayList;

import de.txserver.slickupnp.R;
import de.txserver.slickupnp.app.SlickUPnP;
import de.txserver.slickupnp.helper.ItemModel;
import de.txserver.slickupnp.helper.MimeTypeMap;

public class CustomContentBrowseActionCallback extends Browse {

    private Context context;
    private SharedPreferences prefs;

    private ContentDirectoryBrowseCallbacks callbacks;
    private AndroidUpnpService androidUpnpService;
    private Service service;
    private String id;
    private long firstResult;

    public CustomContentBrowseActionCallback(Context context, ContentDirectoryBrowseCallbacks callbacks, AndroidUpnpService androidUpnpService, Service service, String id) {
        this(context, callbacks, androidUpnpService, service, id, 0L);
    }

    public CustomContentBrowseActionCallback(Context context, ContentDirectoryBrowseCallbacks callbacks, AndroidUpnpService androidUpnpService, Service service, String id, long firstResult) {
        super(service, id, BrowseFlag.DIRECT_CHILDREN, "*", firstResult, 99999L,
                new SortCriterion(true, "dc:title"));

        this.callbacks = callbacks;
        this.androidUpnpService = androidUpnpService;
        this.service = service;
        this.id = id;
        this.firstResult = firstResult;

        this.context = context;
        prefs = SlickUPnP.getInstance().getSharedPref();
    }

    private ItemModel createItemModel(DIDLObject item) {

        ItemModel itemModel = new ItemModel(context.getResources(),
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
//                numberReturned = Long.parseLong(actionInvocation.getOutput("NumberReturned").toString());

            if (firstResult < 9999L && firstResult + items.size() < totalMatches) {

                androidUpnpService.getControlPoint().execute(
                        new CustomContentBrowseActionCallback(context, callbacks, androidUpnpService, service,
                                id, firstResult + items.size()));


            }

            if (firstResult == 0) {
                callbacks.onDisplayItems(items);
            } else {
                callbacks.onDisplayAddItems(items);
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
        callbacks.onDisplayItemsError(createDefaultFailureMessage(invocation, response));
    }
}
