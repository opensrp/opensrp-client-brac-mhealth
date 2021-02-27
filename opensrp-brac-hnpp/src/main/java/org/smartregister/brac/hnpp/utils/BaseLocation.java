package org.smartregister.brac.hnpp.utils;

import androidx.annotation.NonNull;
public class BaseLocation {
    public int id;
    public int parentId;
    public String name;

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
