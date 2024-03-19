package mpower.iot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import org.jetbrains.annotations.NotNull;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;


public class IOTActivity extends SecuredActivity {
    public static final String EXTRA_INTENT_DATA = "extra_result";
    public static final String EXTRA_DIASTOLIC = "extra_diastolic";
    public static final String EXTRA_SYSTOLIC = "extra_systolic";
    public static final String EXTRA_FASTING = "extra_fasting";
    public static final String EXTRA_RANDOM = "extra_random";
    public static final String EXTRA_HR = "extra_hr";
    public static final int IOT_REQUEST_CODE = 40000;
    private TextView bpMeasurementValue,fastingMeasurementValue,randomMeasurmentValue;
    private float systolic,diastolic,heartrate;
    private float fasting =0,random =0;
    private boolean fastingNoSelected,randomNoSelected = false;


    private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
    private static String[] PERMISSIONS_BLUETOOTH = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    public static void startIOTActivity(Activity activity,boolean bloodSugerTakenEnable){
        Intent intent = new Intent(activity,IOTActivity.class);
        intent.putExtra("blood_suger",bloodSugerTakenEnable);
        activity.startActivityForResult(new Intent(),IOT_REQUEST_CODE);
    }

    private void sendDataToActivity(){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_INTENT_DATA, "yes");
        intent.putExtra(EXTRA_DIASTOLIC, diastolic);
        intent.putExtra(EXTRA_SYSTOLIC, systolic);
        intent.putExtra(EXTRA_HR, heartrate);
        intent.putExtra(EXTRA_FASTING, fasting);
        intent.putExtra(EXTRA_RANDOM, random);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setUpIOTConfiguration() {
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_BLUETOOTH,
                    1
            );
        }else{
            initBluetoothHandler();
        }
    }


    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }

    private void initBluetoothHandler()
    {
        BluetoothHandler.getInstance(getApplicationContext());
    }

    @NotNull
    private BluetoothManager getBluetoothManager() {
        return Objects.requireNonNull((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE),"cannot get BluetoothManager");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(locationServiceStateReceiver);
        unregisterReceiver(bloodPressureDataReceiver);
//        unregisterReceiver(temperatureDataReceiver);
//        unregisterReceiver(heartRateDataReceiver);
//        unregisterReceiver(pulseOxDataReceiver);
//        unregisterReceiver(weightDataReceiver);
        unregisterReceiver(glucoseDataReceiver);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_iot);
        boolean bloodSuger = getIntent().getBooleanExtra("blood_suger",false);
        if(!bloodSuger){
            findViewById(R.id.fasting_ll).setVisibility(View.GONE);
            findViewById(R.id.random_ll).setVisibility(View.GONE);
        }
        bpMeasurementValue = (TextView) findViewById(R.id.bloodPressureValue);
        fastingMeasurementValue = (TextView) findViewById(R.id.fastingValue);
        randomMeasurmentValue = (TextView) findViewById(R.id.randomValue);
        findViewById(R.id.bp_yes_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.bp_panel).setVisibility(View.VISIBLE);
                setUpIOTConfiguration();
            }
        });
        findViewById(R.id.fasting_no_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.fasting_panel).setVisibility(View.GONE);
                fastingNoSelected = true;
            }
        });
        findViewById(R.id.fasting_yes_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.fasting_panel).setVisibility(View.VISIBLE);
                setUpIOTConfiguration();
            }
        });
        findViewById(R.id.random_yes_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.random_panel).setVisibility(View.VISIBLE);
                setUpIOTConfiguration();
            }
        });
        findViewById(R.id.random_no_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.random_panel).setVisibility(View.GONE);
                randomNoSelected = true;
            }
        });
        findViewById(R.id.next_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDataToActivity();
            }
        });
        registerReceiver(locationServiceStateReceiver, new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Context.RECEIVER_NOT_EXPORTED : 0 ;
            registerReceiver(bloodPressureDataReceiver, new IntentFilter( BluetoothHandler.MEASUREMENT_BLOODPRESSURE ), flags);
//            registerReceiver(temperatureDataReceiver, new IntentFilter( BluetoothHandler.MEASUREMENT_TEMPERATURE ), flags);
//            registerReceiver(heartRateDataReceiver, new IntentFilter( BluetoothHandler.MEASUREMENT_HEARTRATE ), flags);
//            registerReceiver(pulseOxDataReceiver, new IntentFilter( BluetoothHandler.MEASUREMENT_PULSE_OX ), flags);
//            registerReceiver(weightDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_WEIGHT), flags);
            registerReceiver(glucoseDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_GLUCOSE), flags);
        } else {
            registerReceiver(bloodPressureDataReceiver, new IntentFilter( BluetoothHandler.MEASUREMENT_BLOODPRESSURE ));
//            registerReceiver(temperatureDataReceiver, new IntentFilter( BluetoothHandler.MEASUREMENT_TEMPERATURE ));
//            registerReceiver(heartRateDataReceiver, new IntentFilter( BluetoothHandler.MEASUREMENT_HEARTRATE ));
//            registerReceiver(pulseOxDataReceiver, new IntentFilter( BluetoothHandler.MEASUREMENT_PULSE_OX ));
//            registerReceiver(weightDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_WEIGHT));
            registerReceiver(glucoseDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_GLUCOSE));
        }
    }

    @Override
    protected void onResumption() {

    }

    private final BroadcastReceiver locationServiceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(LocationManager.MODE_CHANGED_ACTION)) {
                boolean isEnabled = areLocationServicesEnabled();
                Timber.i("Location service state changed to: %s", isEnabled ? "on" : "off");
                checkPermissions();
            }
        }
    };

    private final BroadcastReceiver bloodPressureDataReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            BloodPressureMeasurement measurement = (BloodPressureMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_BLOODPRESSURE_EXTRA);
            if (measurement == null) return;
            systolic = measurement.getSystolic();
            diastolic = measurement.getDiastolic();
            heartrate = measurement.getPulseRate()==null?0:measurement.getPulseRate();
            bpMeasurementValue.setText("সিস্টোলিক(mmHg): "+systolic);
            bpMeasurementValue.append("\n");
            bpMeasurementValue.append("ডায়াস্টোলিক(mmHg) : "+diastolic);
            bpMeasurementValue.append("\n");
            bpMeasurementValue.append("হার্ট রেট: "+heartrate);
            findViewById(R.id.status_tv).setVisibility(View.GONE);
            //measurementValue.setText(String.format(Locale.ENGLISH, "%s\n\nfrom %s", measurement, peripheral.getName()));
        }
    };
//
//    private final BroadcastReceiver temperatureDataReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
//            TemperatureMeasurement measurement = (TemperatureMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_TEMPERATURE_EXTRA);
//            if (measurement == null) return;
//
//            measurementValue.setText(String.format(Locale.ENGLISH, "%s\n\nfrom %s", measurement, peripheral.getName()));
//        }
//    };
//
//    private final BroadcastReceiver heartRateDataReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            HeartRateMeasurement measurement = (HeartRateMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_HEARTRATE_EXTRA);
//            if (measurement == null) return;
//
//            measurementValue.setText(String.format(Locale.ENGLISH, "%d bpm", measurement.pulse));
//        }
//    };
//
//    private final BroadcastReceiver pulseOxDataReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
//            PulseOximeterContinuousMeasurement measurement = (PulseOximeterContinuousMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_PULSE_OX_EXTRA_CONTINUOUS);
//            if (measurement != null) {
//                measurementValue.setText(String.format(Locale.ENGLISH, "SpO2 %d%%,  Pulse %d bpm\n\nfrom %s", measurement.getSpO2(), measurement.getPulseRate(), peripheral.getName()));
//            }
//            PulseOximeterSpotMeasurement spotMeasurement = (PulseOximeterSpotMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_PULSE_OX_EXTRA_SPOT);
//            if (spotMeasurement != null) {
//                measurementValue.setText(String.format(Locale.ENGLISH, "%s\n\nfrom %s", spotMeasurement, peripheral.getName()));
//            }
//        }
//    };
//
//    private final BroadcastReceiver weightDataReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
//            WeightMeasurement measurement = (WeightMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_WEIGHT_EXTRA);
//            if (measurement != null) {
//                measurementValue.setText(String.format(Locale.ENGLISH, "%s\n\nfrom %s", measurement, peripheral.getName()));
//            }
//        }
//    };
//
    private final BroadcastReceiver glucoseDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            GlucoseMeasurement measurement = (GlucoseMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_GLUCOSE_EXTRA);

            Log.v("glucoseDataReceiver","glucoseDataReceiver>>>fasting:"+fasting+":fastingNoSelected:"+fastingNoSelected+":"+measurement.getValue());
        if (measurement != null) {
                if(fasting== 0 && !fastingNoSelected){
                    fasting = measurement.getValue();
                    fastingMeasurementValue.setText("ফাস্টিং (mmol/l): "+fasting);
                }else  if(random == 0 && !randomNoSelected ){
                    random = measurement.getValue();
                    randomMeasurmentValue.setText("ফাস্টিং (mmol/l): "+random);
                }
            }
        }
    };

    private BluetoothPeripheral getPeripheral(String peripheralAddress) {
        BluetoothCentralManager central = BluetoothHandler.getInstance(getApplicationContext()).central;
        return central.getPeripheral(peripheralAddress);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            if (missingPermissions.length > 0) {
                //blePermissionRequest.launch(missingPermissions);
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_BLUETOOTH,
                        1
                );
            } else {
                permissionsGranted();
            }
        }
    }

    private String[] getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String requiredPermission : requiredPermissions) {
                if (getApplicationContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
        }
        return missingPermissions.toArray(new String[0]);
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        } else return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    private void permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && targetSdkVersion < Build.VERSION_CODES.S) {
            if (checkLocationServices()) {
                initBluetoothHandler();
            }
        } else {
            initBluetoothHandler();
        }
    }

    private boolean areLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Timber.e("could not get location manager");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            return isGpsEnabled || isNetworkEnabled;
        }
    }

    private boolean checkLocationServices() {
        if (!areLocationServicesEnabled()) {
            new AlertDialog.Builder(IOTActivity.this)
                    .setTitle("Location services are not enabled")
                    .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }
}
