package org.smartregister.brac.hnpp.task;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.listener.OnGpsDataGenerateListener;
import org.smartregister.brac.hnpp.utils.HnppConstants;

import static android.content.Context.LOCATION_SERVICE;

public class GenerateGPSTask  implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private final double MIN_ACCURACY = 4d;
    double latitude = 0.0, longitude = 0.0;
    OnGpsDataGenerateListener onGpsDataGenerateListener;
    Context context;

    public GenerateGPSTask(OnGpsDataGenerateListener onGpsDataGenerateListener, Context context) {
        this.onGpsDataGenerateListener = onGpsDataGenerateListener;
        this.context = context;
        onGpsDataGenerateListener.showProgressBar(R.string.gps_searching);
        initGoogleApiClient();
    }

    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v("GPS_DATA","onConnected"+googleApiClient.isConnected());
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        updateUi();
    }

    @Override
    public void onConnectionSuspended(int i) {
        updateUi();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("GPS_DATA","onConnectionFailed");
        if(onGpsDataGenerateListener !=null){
            onGpsDataGenerateListener.hideProgress();
            onGpsDataGenerateListener.onGpsDataNotFound();
        }

        updateUi();

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (lastLocation != null && lastLocation.getAccuracy() <= MIN_ACCURACY) {
            updateLocationViews(lastLocation);
        }
    }
    protected void initGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        googleApiClient.connect();
    }

    private void disconnectGoogleApiClient() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }
    public void updateUi(){

        updateLocationViews(lastLocation);
    }
    private void updateLocationViews(Location location) {
        if(onGpsDataGenerateListener == null) return;
        if (location != null) {
            HnppConstants.GPS_ATTEMPT_COUNT = 0;
            location.getProvider();
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            onGpsDataGenerateListener.hideProgress();
            if(latitude==0.0 && longitude == 0.0){
                onGpsDataGenerateListener.onGpsDataNotFound();
            }else {
                onGpsDataGenerateListener.onGpsData(latitude,longitude);
                onGpsDataGenerateListener = null;
            }


        }else{
            HnppConstants.GPS_ATTEMPT_COUNT++;

            if( HnppConstants.GPS_ATTEMPT_COUNT >= HnppConstants.DEFAULT_GPS_ATTEMPT){
                HnppConstants.IS_MANDATORY_GPS = false;
            }
            onGpsDataGenerateListener.hideProgress();
            onGpsDataGenerateListener.onGpsDataNotFound();
            onGpsDataGenerateListener = null;
        }
        disconnectGoogleApiClient();
    }
}
