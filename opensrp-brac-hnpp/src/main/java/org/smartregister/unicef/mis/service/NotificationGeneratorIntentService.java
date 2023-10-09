package org.smartregister.unicef.mis.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;

import java.util.Calendar;

import static org.smartregister.unicef.mis.utils.HnppConstants.insertAtNotificationTable;

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
        if(HnppConstants.isNeedToShowStockEndPopup()){
            StringBuilder nameCountBuilder = HnppDBUtils.getStockEnd();
            Log.v("PROCESS_NOTI","nameCountBuilder.length()"+nameCountBuilder.length());
            if(nameCountBuilder!=null && nameCountBuilder.length()>0){

                try{
                    insertAtNotificationTable(getString(R.string.menu_end_stock),nameCountBuilder.toString());
                    Intent intent = new Intent(HnppConstants.ACTION_STOCK_END);
                    intent.putExtra(HnppConstants.EXTRA_STOCK_END, nameCountBuilder.toString());
                    sendBroadcast(intent);
                }catch (Exception e){

                }


            }
        }


    }
    private void processEDDThisMonth(){
        Calendar calendar = Calendar.getInstance();
        if(calendar.get(Calendar.DAY_OF_MONTH) ==1){
            if(HnppConstants.isNeedToShowEDDPopup()){
                StringBuilder nameCountBuilder = HnppDBUtils.getEddThisMonth();
                if(nameCountBuilder!=null && nameCountBuilder.length()>0){
                    try{
                        insertAtNotificationTable(getString(R.string.menu_edd_this_month),nameCountBuilder.toString());
                        Intent intent = new Intent(HnppConstants.ACTION_EDD);
                        intent.putExtra(HnppConstants.EXTRA_EDD, nameCountBuilder.toString());
                        sendBroadcast(intent);
                    }catch (Exception e){

                    }

                }
            }

        }
    }

}
