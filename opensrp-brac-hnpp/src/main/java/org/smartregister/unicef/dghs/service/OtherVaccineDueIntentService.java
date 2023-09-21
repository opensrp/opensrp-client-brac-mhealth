//
//
//package org.smartregister.unicef.dghs.service;
//
//import android.app.IntentService;
//import android.content.Intent;
//import android.database.Cursor;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.google.gson.Gson;
//
//import net.sqlcipher.database.SQLiteDatabase;
//
//import org.joda.time.DateTime;
//import org.joda.time.LocalDate;
//import org.joda.time.format.DateTimeFormat;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.smartregister.AllConstants;
//import org.smartregister.CoreLibrary;
//import org.smartregister.domain.Alert;
//import org.smartregister.domain.Response;
//import org.smartregister.family.util.JsonFormUtils;
//import org.smartregister.immunization.ImmunizationLibrary;
//import org.smartregister.immunization.domain.Vaccine;
//import org.smartregister.immunization.domain.VaccineSchedule;
//import org.smartregister.service.HTTPAgent;
//import org.smartregister.unicef.dghs.BuildConfig;
//import org.smartregister.unicef.dghs.HnppApplication;
//import org.smartregister.unicef.dghs.R;
//import org.smartregister.unicef.dghs.utils.HnppConstants;
//import org.smartregister.unicef.dghs.utils.OtherVaccineContentData;
//
//import java.text.MessageFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//public class OtherVaccineDueIntentService extends IntentService {
//    public OtherVaccineDueIntentService() {
//        super("OtherVaccineDueIntentService");
//    }
//    private final String TEST_BASE="362243b3-9c00-45eb-8fc5-778ff08db909-pros";
//
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        HnppConstants.processUnSyncData(0);
//
//    }
//
//    private void broadcastStatus(String message){
//        try{
//            Intent broadcastIntent = new Intent("VACCINE_OTHER");
//            broadcastIntent.putExtra("EXTRA_OTHER_UPDATE", message);
//            sendBroadcast(broadcastIntent);
//        }catch (Exception e){
//
//        }
//
//    }
//
//}
