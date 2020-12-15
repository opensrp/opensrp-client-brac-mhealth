package org.smartregister.brac.hnpp.utils;

import android.support.annotation.NonNull;

public class BaseLocation {
    public int id;
    public String name;

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
