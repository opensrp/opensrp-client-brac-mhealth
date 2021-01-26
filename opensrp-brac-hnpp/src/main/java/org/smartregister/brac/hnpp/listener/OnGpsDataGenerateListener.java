package org.smartregister.brac.hnpp.listener;

public interface OnGpsDataGenerateListener {
    void showProgressBar(int message);
    void hideProgressBar();
    void onGpsData(long latitude, long longitude);
}
