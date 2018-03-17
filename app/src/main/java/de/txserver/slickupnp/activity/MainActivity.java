package de.txserver.slickupnp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.txserver.slickupnp.helper.DrawerListAdapter;
import de.txserver.slickupnp.helper.NavigationItem;
import de.txserver.slickupnp.upnp.ContentDirectoryBrowseCallbacks;
import de.txserver.slickupnp.R;
import de.txserver.slickupnp.helper.CustomListAdapter;
import de.txserver.slickupnp.helper.CustomListItem;
import de.txserver.slickupnp.helper.DeviceModel;
import de.txserver.slickupnp.helper.ItemModel;
import de.txserver.slickupnp.upnp.ContentDirectoryBrowseHandler;

public class MainActivity extends AppCompatActivity
                          implements ContentDirectoryBrowseCallbacks,
                          SharedPreferences.OnSharedPreferenceChangeListener {

    public String TAG = MainActivity.class.getSimpleName();

    private ActionBar actionBar;
    private ArrayList<NavigationItem> navigationItemArrayList;

    private DrawerLayout drawerLayout;
    private RelativeLayout drawerPane;
    private ActionBarDrawerToggle drawerToggle;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ListView listView;

    private ContentDirectoryBrowseHandler contentDirectoryBrowseHandler;
    private ArrayList<CustomListItem> mDeviceList;
    private ArrayAdapter<CustomListItem> mDeviceListAdapter;
    private ArrayList<CustomListItem> mItemList;
    private ArrayAdapter<CustomListItem> mItemListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        navigationItemArrayList = new ArrayList<>();
        navigationItemArrayList.add(new NavigationItem(getResources().getString(R.string.menuItem_settings), getResources().getString(R.string.menuItemText_settings), R.drawable.ic_settings_black_24px, R.layout.activity_settings));
        navigationItemArrayList.add(new NavigationItem(getResources().getString(R.string.menuItem_about), getResources().getString(R.string.menuItemText_about), R.drawable.ic_info_outline_black_24px, R.layout.activity_about));

        // Populate the Navigtion Drawer with options
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        drawerPane = (RelativeLayout)findViewById(R.id.drawerPane);

        ListView navigationListView = (ListView)findViewById(R.id.navList);
        DrawerListAdapter drawerListAdapter = new DrawerListAdapter(this, navigationItemArrayList);
        navigationListView.setAdapter(drawerListAdapter);
        navigationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshList();
                    }
                }
        );


        mDeviceList = new ArrayList<CustomListItem>();
        mDeviceListAdapter = new CustomListAdapter(this, mDeviceList);

        mItemList = new ArrayList<CustomListItem>();
        mItemListAdapter = new CustomListAdapter(this, mItemList);

        listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(mDeviceListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick (AdapterView < ? > adapter, View v,int position, long arg3){
                setShowRefreshing(true);
                contentDirectoryBrowseHandler.navigateTo(adapter.getItemAtPosition(position));
            }
        });

        if (contentDirectoryBrowseHandler == null) {
            contentDirectoryBrowseHandler = new ContentDirectoryBrowseHandler(this);
            contentDirectoryBrowseHandler.bindServiceConnection();
        } else {
            contentDirectoryBrowseHandler.refreshDevices();
            contentDirectoryBrowseHandler.refreshCurrent();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        final IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            switch (item.getItemId()) {
                case R.id.action_refresh:
//                    Toast.makeText(this, R.string.info_searching, Toast.LENGTH_SHORT).show();
                    refreshList();
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

        drawerToggle.syncState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        contentDirectoryBrowseHandler.unbindServiceConnection();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        refreshList();
    }

    private void setActivityTitle() {

        if (actionBar == null) {
            return;
        }

        actionBar.setTitle(getResources().getString(R.string.app_name));
    }

    private void selectItemFromDrawer(int position) {

        Intent intent = null;

        switch(navigationItemArrayList.get(position).getActivity()){

            case R.layout.activity_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.layout.activity_about:
                intent = new Intent(this, AboutActivity.class);
                break;
        }

        if (intent != null) {

            startActivity(intent);
        }

        // Close the drawer
        drawerLayout.closeDrawer(drawerPane);
    }

    private void unsetListSelection() {
        listView.setItemChecked(-1, true);
    }

    private void refreshList() {
        setShowRefreshing(true);
        contentDirectoryBrowseHandler.refreshCurrent();
    }

    @Override
    public void setShowRefreshing(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (contentDirectoryBrowseHandler.goBack())
            super.onBackPressed();
    }

    @Override
    public void onDisplayDevices() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unsetListSelection();
                mDeviceListAdapter.clear();
                listView.setAdapter(mDeviceListAdapter);
                setShowRefreshing(false);
            }
        });
    }

    @Override
    public void onDisplayDirectories() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unsetListSelection();
                mItemList.clear();
                mItemListAdapter.clear();
                listView.setAdapter(mItemListAdapter);
                setShowRefreshing(false);
            }
        });
    }

    @Override
    public void onDisplayItems(final ArrayList<ItemModel> items) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unsetListSelection();
                mItemList.clear();
                mItemList.addAll(items);
                mItemListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDisplayAddItems(final ArrayList<ItemModel> items) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mItemList.addAll(items);
                mItemListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDisplayItemsError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mItemListAdapter.clear();
                mItemListAdapter.add(new CustomListItem(
                        R.drawable.ic_alert_black_24px,
                        getResources().getString(R.string.info_errorlist_folders),
                        error));
            }
        });
    }

    @Override
    public void onDeviceAdded(final DeviceModel device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int position = mDeviceListAdapter.getPosition(device);
                if (position >= 0) {
                    mDeviceListAdapter.remove(device);
                    mDeviceListAdapter.insert(device, position);
                } else {
                    mDeviceListAdapter.add(device);
                }
            }
        });
    }

    @Override
    public void onDeviceRemoved(final DeviceModel device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceListAdapter.remove(device);
            }
        });
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);

                TextView wifi_warning = (TextView)findViewById(R.id.wifi_warning);

                switch (state) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        wifi_warning.setVisibility(View.GONE);

                        if (contentDirectoryBrowseHandler != null) {
                            contentDirectoryBrowseHandler.refreshDevices();
                            contentDirectoryBrowseHandler.refreshCurrent();
                        }
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        wifi_warning.setVisibility(View.VISIBLE);
                        mDeviceListAdapter.clear();
                        mItemListAdapter.clear();
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        wifi_warning.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    };
}