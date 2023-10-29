package org.smartregister.unicef.mis.utils;

import android.support.annotation.NonNull;

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
