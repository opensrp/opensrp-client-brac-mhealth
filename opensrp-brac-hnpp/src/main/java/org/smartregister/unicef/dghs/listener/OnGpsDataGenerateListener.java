package org.smartregister.unicef.dghs.listener;

public interface OnGpsDataGenerateListener {
    void showProgressBar(int message);
    void hideProgress();
    void onGpsData(double latitude, double longitude);
    void onGpsDataNotFound();
}
