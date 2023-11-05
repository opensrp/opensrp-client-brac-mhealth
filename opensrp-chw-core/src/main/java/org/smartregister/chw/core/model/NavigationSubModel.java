package org.smartregister.chw.core.model;

public class NavigationSubModel {
    private int subTitleId;
    private int subCount;
    private String type;
    public NavigationSubModel(int subTitleId,int subCount,String type){
        this.subTitleId = subTitleId;
        this.subCount = subCount;
        this.type = type;
    }

    public int getSubTitle() {
        return subTitleId;
    }

    public void setSubTitle(int subTitle) {
        this.subTitleId = subTitle;
    }

    public int getSubCount() {
        return subCount;
    }

    public void setSubCount(int subCount) {
        this.subCount = subCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
