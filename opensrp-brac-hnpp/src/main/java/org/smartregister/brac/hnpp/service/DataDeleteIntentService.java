package org.smartregister.brac.hnpp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.R;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.domain.Response;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.service.HTTPAgent;
import org.smartregister.sync.intent.BaseSyncIntentService;

import java.text.MessageFormat;
import java.util.List;

/**
 * Created by keyman on 11/10/2017.
 */
public class DataDeleteIntentService extends IntentService {

    private Context context;
    private HTTPAgent httpAgent;
    private static final String HH = "households";
    private static final String MEMBER = "members";
    private static final String SERVER_VERSION = "serverVersion";
    private static final String DELETE_FETCH = "/rest/event/deleting?";
    private static final String TAG = "DataDeleteIntentService";
    public static final String LAST_SYNC_TIME = "delete_last_sync_time";
    public DataDeleteIntentService() {
        super("DataDeleteIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if(TextUtils.isEmpty(userName)){
                return;
            }
            String lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_TIME);
            if(TextUtils.isEmpty(lastSynTime)){
                lastSynTime ="0";
            }
            //testing
            String url = baseUrl + DELETE_FETCH + SERVER_VERSION+"="+lastSynTime;
            Log.v("DataDelete","getLocationList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            JSONObject results = new JSONObject((String) resp.payload());
            //{"serverVersion":1642325679000,"members":["59188799-39d7-4c3a-9a86-20f52e54506a","c4de7552-9c58-410c-9329-a520b2f9f0fb"],"households":["bd42029e-cba0-40f6-81a6-5c7a36cbc588","3d7e52af-3330-476a-8123-af7d5ab227ff"],"events":["1cc2f544-2b5a-4d77-9774-390f6b1b455e","87d240ff-aa37-4cef-b0ed-10d2ea1a7f3a","bbd878b1-cb98-4d31-ac00-5224d1327f04"]}
//            String test ="{\"serverVersion\":1642325679000,\"members\":[\"c1377ea8-ab6c-4b11-8bbd-6d36ce3cac54-test\",\"9d376051-a0b1-4a0c-8712-01167dc6cfd6-test\"],\"households\":[],\"events\":[\"0ea01098-41b6-4ecb-8c14-1fdefc2c0655-test\"]}";
//            JSONObject results = new JSONObject(test);

            SQLiteDatabase db = CoreChwApplication.getInstance().getRepository().getWritableDatabase();
            if (results.has(HH)){
                JSONArray validHHClient = results.getJSONArray(HH);
                StringBuilder builder = new StringBuilder();
                StringBuilder builderHH = new StringBuilder();
                for (int i = 0; i < validHHClient.length(); i++) {
                    String hhClientString = validHHClient.getString(i);
                    //delete client table
                    //delete ec_household
                    if(builder.toString().isEmpty()){
                        builder.append(" baseEntityId = '"+hhClientString+"'");
                        builderHH.append(" base_entity_id = '"+hhClientString+"'");
                    }else{
                        builder.append(" OR ");
                        builder.append(" baseEntityId = '"+hhClientString+"'");
                        builderHH.append(" OR ");
                        builderHH.append(" base_entity_id = '"+hhClientString+"'");
                    }

                }
                if(!TextUtils.isEmpty(builder.toString())){
                    String q = "delete from client where "+builder.toString();
                    Log.v("DATA_DELETE","q:"+q);
                    db.execSQL(q);
                }
                if(!TextUtils.isEmpty(builderHH.toString())){
                    String h = "delete from ec_family where "+builderHH.toString();
                    Log.v("DATA_DELETE","h:"+h);
                    db.execSQL(h);
                }
            }

            if (results.has(MEMBER)) {
                JSONArray validMemberEvents = results.getJSONArray(MEMBER);
                StringBuilder builderClient = new StringBuilder();
                StringBuilder builderMember = new StringBuilder();
                StringBuilder builderChild = new StringBuilder();
                for (int i = 0; i < validMemberEvents.length(); i++) {
                    String memberClientString = validMemberEvents.getString(i);
                    //delete client table
                    //delete ec_member,ec_child
                    if(builderClient.toString().isEmpty()){
                        builderClient.append(" baseEntityId = '"+memberClientString+"'");
                        builderMember.append(" base_entity_id = '"+memberClientString+"'");
                        builderChild.append(" base_entity_id = '"+memberClientString+"'");
                    }else{
                        builderClient.append(" OR ");
                        builderClient.append(" baseEntityId = '"+memberClientString+"'");
                        builderMember.append(" OR ");
                        builderMember.append(" base_entity_id = '"+memberClientString+"'");
                        builderChild.append(" OR ");
                        builderChild.append(" base_entity_id = '"+memberClientString+"'");
                    }
                }
                if(!TextUtils.isEmpty(builderClient.toString())){
                    String q = "delete from client where "+builderClient.toString();
                    Log.v("DATA_DELETE","q:"+q);
                    db.execSQL(q);
                }
                if(!TextUtils.isEmpty(builderMember.toString())){
                    String h = "delete from ec_family_member where "+builderMember.toString();
                    Log.v("DATA_DELETE","h:"+h);
                    db.execSQL(h);
                }
                if(!TextUtils.isEmpty(builderChild.toString())){
                    String h = "delete from ec_child where "+builderChild.toString();
                    Log.v("DATA_DELETE","h:"+h);
                    db.execSQL(h);
                }
            }
            if (results.has(AllConstants.KEY.EVENTS)) {
                JSONArray validEvents = results.getJSONArray(AllConstants.KEY.EVENTS);
                StringBuilder builderEvent = new StringBuilder();
                StringBuilder builderVisits = new StringBuilder();
                StringBuilder builderVisitLog = new StringBuilder();
                StringBuilder builderTargetAchievment = new StringBuilder();
                StringBuilder builderStockAchievment = new StringBuilder();
                for (int i = 0; i < validEvents.length(); i++) {
                    String formSubmissionId = validEvents.getString(i);
                    //delete event table
                    //delete visits,ec_visit_log,target

                    if(builderEvent.toString().isEmpty()){
                        builderEvent.append(" formSubmissionId = '"+formSubmissionId+"'");
                        builderVisits.append(" form_submission_id = '"+formSubmissionId+"'");
                        String visitId = HnppDBUtils.getVisitIdByFormSubmissionId(formSubmissionId);
                        builderVisitLog.append(" visit_id = '"+visitId+"'");
                        builderTargetAchievment.append(" form_submission_id = '"+formSubmissionId+"'");
                        builderStockAchievment.append(" form_submission_id = '"+formSubmissionId+"'");
                    }else{
                        builderEvent.append(" OR ");
                        builderEvent.append(" formSubmissionId = '"+formSubmissionId+"'");
                        builderVisits.append(" OR ");
                        builderVisits.append(" form_submission_id = '"+formSubmissionId+"'");
                        String visitId = HnppDBUtils.getVisitIdByFormSubmissionId(formSubmissionId);
                        builderVisitLog.append(" OR ");
                        builderVisitLog.append(" visit_id = '"+visitId+"'");
                        builderTargetAchievment.append(" OR ");
                        builderTargetAchievment.append(" form_submission_id = '"+formSubmissionId+"'");
                        builderStockAchievment.append(" OR ");
                        builderStockAchievment.append(" form_submission_id = '"+formSubmissionId+"'");
                    }
                }
                if(!TextUtils.isEmpty(builderEvent.toString())){
                    String q = "delete from event where "+builderEvent.toString();
                    Log.v("DATA_DELETE","q:"+q);
                    db.execSQL(q);
                }
                if(!TextUtils.isEmpty(builderVisits.toString())){
                    String v = "delete from visits where "+builderVisits.toString();
                    Log.v("DATA_DELETE","v:"+v);
                    db.execSQL(v);
                }
                if(!TextUtils.isEmpty(builderVisitLog.toString())){
                    String l = "delete from ec_visit_log where "+builderVisitLog.toString();
                    Log.v("DATA_DELETE","l:"+l);
                    db.execSQL(l);
                }
                if(!TextUtils.isEmpty(builderTargetAchievment.toString())){
                    String t = "delete from target_table where "+builderTargetAchievment.toString();
                    Log.v("DATA_DELETE","t:"+t);
                    db.execSQL(t);
                }
                if(!TextUtils.isEmpty(builderStockAchievment.toString())){
                    String s = "delete from stock_table where "+builderStockAchievment.toString();
                    Log.v("DATA_DELETE","s:"+s);
                    db.execSQL(s);
                }

            }
            if (results.has(SERVER_VERSION)){
                long serverVersion = results.getLong(SERVER_VERSION);
                if(serverVersion !=0){
                    CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_SYNC_TIME,serverVersion+"");
                }
                Log.v("DATA_DELETE","serverVersion:"+serverVersion);
            }

        } catch (Exception e) {
            Log.e("DATA_DELETE", "", e);
        }
    }


}
