package org.smartregister.brac.hnpp.utils;

import android.support.annotation.NonNull;

public class BaseLocation implements Comparable<BaseLocation>{
    public int id;
    public int parentId;
    public String name;

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(BaseLocation baseLocation) {
        return name.compareTo(baseLocation.name);
    }
}
