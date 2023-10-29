//package org.smartregister.unicef.dghs.service;
//
//import android.app.IntentService;
//import android.content.Intent;
//import android.text.TextUtils;
//import android.util.Log;
//
//import org.apache.http.NoHttpResponseException;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.smartregister.CoreLibrary;
//import org.smartregister.unicef.dghs.HnppApplication;
//import org.smartregister.unicef.dghs.model.Survey;
//import org.smartregister.domain.Response;
//import org.smartregister.service.HTTPAgent;
//
//public class SurveyHistoryIntentService extends IntentService {
//    public static final String SURVEY_URL = "/rest/client/survey_history?";
//    public static final String LAST_SYNC_TIME = "last_survey_time";
//    public static final String TIME_STAMP = "time_stamp";
//    private static final String TAG = SurveyHistoryIntentService.class.getCanonicalName();
//
//    public SurveyHistoryIntentService() {
//        super("SurveyHistoryIntentService");
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        fetchRetry(0);
//    }
//    private void fetchRetry(int count){
//        try {
//
//            JSONArray historyArrayObj = fetchSurveyHistory();
//            if (historyArrayObj != null && historyArrayObj.length()>0) {
//                parseResponse(historyArrayObj);
//                fetchRetry(0);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage(), e);
////            try {
////                parseResponse(new JSONArray(IDUtils.hhids));
////            } catch (Exception e1) {
////                e1.printStackTrace();
////            }
//        }
//    }
//
//    private JSONArray fetchSurveyHistory() throws Exception {
//        HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
//        String baseUrl = CoreLibrary.getInstance().context().
//                configuration().dristhiBaseURL();
//        String endString = "/";
//        if (baseUrl.endsWith(endString)) {
//            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
//        }
//        String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
//        if(TextUtils.isEmpty(userName)){
//            return null;
//        }
//        String lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_TIME);
//        if(TextUtils.isEmpty(lastSynTime)){
//            lastSynTime ="0";
//        }
//        long lastTime = Long.parseLong(lastSynTime);
//        //testing
//        String url = baseUrl + SURVEY_URL + TIME_STAMP+"="+lastTime;
//        Log.v("SURVEY_HISTORY","getLocationList>>url:"+url);
//        Response resp = httpAgent.fetch(url);
//        if (resp.isFailure()) {
//            throw new NoHttpResponseException(url + " not returned data");
//        }
//        return new JSONArray((String) resp.payload());
//    }
//
//    private void parseResponse(JSONArray jsonArray) throws Exception {
//        //{"form_name":"hh_form","type":"hh/mm","date":"22-04-22 12:30","uuid":"327c0e24-54ea-4e1b-9692-8cc4f219e19b","base_entity_id":"327c0e24-54ea-4e1b-9692-8cc4f219e19b-test","form_id":"234567hjgjhg","time_stamp":231231244444}
//        long time_stamp = 0;
//        if (jsonArray != null && jsonArray.length() > 0) {
//            for (int i = 0; i < jsonArray.length(); i++) {
//
//                try{
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    String form_name = jsonObject.getString("form_name");
//                    String date_time = jsonObject.getString("date");
//                    String uuid = jsonObject.getString("uuid");
//                    try{
//                        time_stamp = jsonObject.getLong("time_stamp");
//                    }catch (Exception e){
//                        time_stamp = Long.parseLong(jsonObject.getString("time_stamp"));
//                    }
//                    String form_id = jsonObject.optString("form_id");
//                    String type = jsonObject.optString("type");
//                    String baseEntityId = jsonObject.optString("base_entity_id");
//                    Survey survey = new Survey();
//                    survey.formName = form_name;
//                    survey.formId = form_id;
//                    survey.uuid = uuid;
//                    survey.timestamp = time_stamp;
//                    survey.baseEntityId = baseEntityId;
//                    survey.dateTime = date_time;
//                    survey.type = type;
//
//                    HnppApplication.getSurveyHistoryRepository().addOrUpdate(survey,type);
//
//                }catch (Exception e){
//                    e.printStackTrace();
//
//                }
//
//            }
//        }
//        if(time_stamp > 0){
//            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_SYNC_TIME,time_stamp+"");
//        }
//    }
//
//}
