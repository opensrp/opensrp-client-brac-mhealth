package org.smartregister.brac.hnpp.model;

import android.view.View;

public class ChildService {
    int tag;
    String eventType;
    int status = 3;

    View view;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}