package org.smartregister.chw.core.model;

public class NavigationOption {

    private int ResourceID;
    private int ResourceActiveID;
    private int TitleID;
    private String MenuTitle;
    private long RegisterCount;
    private boolean expanded;
    private boolean isNeedToExpand = false;
    private NavigationSubModel navigationSubModel;

    public NavigationOption(int resourceID, int resourceActiveID, int titleID, String menuTitle, long registerCount) {
        ResourceID = resourceID;
        ResourceActiveID = resourceActiveID;
        TitleID = titleID;
        MenuTitle = menuTitle;
        RegisterCount = registerCount;
    }

    public void setNavigationSubModel(NavigationSubModel navigationSubModel) {
        this.navigationSubModel = navigationSubModel;
    }

    public NavigationSubModel getNavigationSubModel() {
        return navigationSubModel;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setNeedToExpand(boolean needToExpand) {
        isNeedToExpand = needToExpand;
    }

    public boolean isNeedToExpand() {
        return isNeedToExpand;
    }

    public int getResourceID() {
        return ResourceID;
    }

    public void setResourceID(int resourceID) {
        ResourceID = resourceID;
    }

    public int getResourceActiveID() {
        return ResourceActiveID;
    }

    public void setResourceActiveID(int resourceActiveID) {
        ResourceActiveID = resourceActiveID;
    }

    public int getTitleID() {
        return TitleID;
    }

    public void setTitleID(int titleID) {
        TitleID = titleID;
    }

    public String getMenuTitle() {
        return MenuTitle;
    }

    public void setMenuTitle(String menuTitle) {
        MenuTitle = menuTitle;
    }

    public long getRegisterCount() {
        return RegisterCount;
    }

    public void setRegisterCount(long registerCount) {
        RegisterCount = registerCount;
    }

}
