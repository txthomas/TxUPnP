package de.txserver.txupnp.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.txserver.txupnp.R;

public class DrawerListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflator;
    private ArrayList<NavigationItem> navigationItems;

    public DrawerListAdapter(Context context, ArrayList<NavigationItem> navigationItems) {

        this.context = context;
        this.navigationItems = navigationItems;
        this.inflator = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return navigationItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navigationItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = inflator.inflate(R.layout.navigation_item, parent, false);
        }

        TextView titleView = (TextView) view.findViewById(R.id.navTitle);
        TextView subtitleView = (TextView) view.findViewById(R.id.navSubTitle);
        ImageView iconView = (ImageView) view.findViewById(R.id.navIcon);

        NavigationItem navigationItem = navigationItems.get(position);

        titleView.setText(navigationItem.getTitle());
        subtitleView.setText(navigationItem.getSubTitle());
        iconView.setImageResource(navigationItem.getIcon());

        return view;
    }
}
