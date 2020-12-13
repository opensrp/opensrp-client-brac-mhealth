package org.smartregister.brac.hnpp.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.StockData;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

import java.util.Calendar;

import static org.smartregister.brac.hnpp.utils.HnppConstants.insertAtNotificationTable;

public class NotificationGeneratorIntentService extends IntentService {

    private static final String TAG = "NotificationGeneratorIntentService";

    public NotificationGeneratorIntentService() { super(TAG); }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationGeneratorIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        processStockEndNotification();
        processEDDThisMonth();

    }
    private void processStockEndNotification(){
        Log.v("NOTIFICATION_JOB","processStockEndNotification");
        if(HnppConstants.isNeedToShowStockEndPopup()){
            Log.v("NOTIFICATION_JOB","is need");
            StringBuilder nameCountBuilder = HnppDBUtils.getStockEnd();
            Log.v("NOTIFICATION_JOB","processStockEndNotification:"+nameCountBuilder);
            if(nameCountBuilder!=null && nameCountBuilder.length()>0){


                insertAtNotificationTable(getString(R.string.menu_end_stock),nameCountBuilder.toString());
                Intent intent = new Intent(HnppConstants.ACTION_STOCK_END);
                intent.putExtra(HnppConstants.EXTRA_STOCK_END, nameCountBuilder.toString());
                sendBroadcast(intent);

            }
        }


    }
    private void processEDDThisMonth(){
        Calendar calendar = Calendar.getInstance();
        if(calendar.get(Calendar.DAY_OF_MONTH) ==1){
            if(HnppConstants.isNeedToShowEDDPopup()){
                StringBuilder nameCountBuilder = HnppDBUtils.getEddThisMonth();
                if(nameCountBuilder!=null && nameCountBuilder.length()>0){
                    insertAtNotificationTable(getString(R.string.menu_edd_this_month),nameCountBuilder.toString());
                    Intent intent = new Intent(HnppConstants.ACTION_EDD);
                    intent.putExtra(HnppConstants.EXTRA_EDD, nameCountBuilder.toString());
                    sendBroadcast(intent);
                }
            }

        }
    }

}
