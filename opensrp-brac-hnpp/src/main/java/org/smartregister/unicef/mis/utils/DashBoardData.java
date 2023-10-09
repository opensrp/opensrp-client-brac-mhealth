package org.smartregister.unicef.mis.utils;

import java.io.Serializable;

public class DashBoardData implements Serializable {

    private int imageSource;
    private String title;
    private int count;
    private String eventType;
    private String ssName;
    public DashBoardData(){

    }

    public DashBoardData(String eventType, String title){
        this.eventType = eventType;
        this.title = title;
    }
    public void setSsName(String ssName) {
        this.ssName = ssName;
    }


    public String getSsName() {
        return ssName;
    }


    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getImageSource() {
        return imageSource;
    }

    public void setImageSource(int imageSource) {
        this.imageSource = imageSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
