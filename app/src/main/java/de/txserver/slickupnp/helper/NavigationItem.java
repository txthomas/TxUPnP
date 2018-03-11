package de.txserver.slickupnp.helper;

public class NavigationItem {

    private String title, subTitle;
    private int icon, activity;

    public NavigationItem(String title, String subTitle, int icon, int activity) {

        this.title = title;
        this.subTitle = subTitle;
        this.icon = icon;
        this.activity = activity;
    }

    public String getTitle() {

        return title;
    }

    public String getSubTitle() {

        return subTitle;
    }

    public int getIcon() {

        return icon;
    }

    public int getActivity() {

        return activity;
    }
}
