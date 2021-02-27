package org.smartregister.brac.hnpp.task;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;

import android.util.Log;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.listener.OnGpsDataGenerateListener;

import static android.content.Context.LOCATION_SERVICE;

public class GenerateLatitudeLongitudeTask extends AsyncTask<Void, Void, Void> {


    LocationManager locationManager;
    Location location;
    boolean isGPSEnable, isNetworkEnable;
    double latitude = 0.0, longitude = 0.0;
    OnGpsDataGenerateListener onGpsDataGenerateListener;
    Context context;

    public GenerateLatitudeLongitudeTask(OnGpsDataGenerateListener onGpsDataGenerateListener, Context context) {
        this.onGpsDataGenerateListener = onGpsDataGenerateListener;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onGpsDataGenerateListener.showProgressBar(R.string.gps_searching);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        locationManager = (LocationManager) HnppApplication.getInstance().getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable) {

        } else {

            if (isNetworkEnable) {
                location = null;
                if (locationManager != null) {


                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        Log.v("GPS_DATE","isNetworkEnable latitude:"+location.getLatitude() + ":longitude:"+location.getLongitude() + "");


                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }

            }


            if (isGPSEnable) {
                location = null;
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        Log.v("GPS_DATE","isGPSEnable latitude:"+location.getLatitude() + ":longitude:"+location.getLongitude() + "");

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        onGpsDataGenerateListener.hideProgress();
        if(latitude==0.0 && longitude == 0.0){
            onGpsDataGenerateListener.onGpsDataNotFound();
        }else {
            onGpsDataGenerateListener.onGpsData(latitude,longitude);
        }


    }
}
