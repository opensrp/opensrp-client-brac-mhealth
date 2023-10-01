package org.smartregister.brac.hnpp.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.model.HHVisitDurationModel;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;
import java.util.List;

public class EventFetchIntentService extends IntentService {
    private static String serverVersion = "0";
    private static String EVENT_FETCH = "";
    public static final String SERVER_VERSION_EVENT = "server_version_event";
    public static final String EVENT_FETCH_STATUS = "event_fetch_status";
    private static final String TAG = "EventFetch";
    int retryCount = 0;

    List<Event> eventList = new ArrayList<>();

    public EventFetchIntentService() { super(TAG); }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public EventFetchIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        intent = new Intent(HnppConstants.ACTION_EVENT_FETCH);
        intent.putExtra(HnppConstants.EVENT_PROGRESS_STATUS, true);
        sendBroadcast(intent);

        fetchEvent();

        intent = new Intent(HnppConstants.ACTION_EVENT_FETCH);
        intent.putExtra(HnppConstants.EVENT_PROGRESS_STATUS, false);
        sendBroadcast(intent);
    }

    /**
     * fetch event list and process
     */
    void fetchEvent(){
        String village = getVillage();
        //getting server version from shared preference
        String lastId = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(SERVER_VERSION_EVENT);
        serverVersion = lastId;
        if(TextUtils.isEmpty(lastId)){
            serverVersion ="0";
        }

        EVENT_FETCH = "/rest/event/service-sync?serverVersion="+serverVersion+"&limit=250&villageIds="+village;

        JSONObject jsonObjectEvent = getEventList();
        if(jsonObjectEvent!=null){
            JSONArray eventArray = jsonObjectEvent.optJSONArray("events");

            //if retry is bigger or equal than default retry value then do nothing
            if(retryCount<CoreLibrary.getInstance().getSyncConfiguration().getSyncMaxRetries()){

                //if number of events is less than 0
                //that means, need to retry
                //then call fetchEvent function again to retry
                if(jsonObjectEvent.optInt("no_of_events")<0){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    fetchEvent();
                    return;
                }
            }

            //if eventArray length is zero then all data is up to date
            //processing parsed data here, on makeVisitLogPA function
            if(eventArray != null && eventArray.length()==0){
                CoreLibrary.getInstance().context().allSharedPreferences().savePreference(EVENT_FETCH_STATUS,"true");
                Log.v("EVENT_FETCH_EVENT",""+eventArray.length()+"  "+eventList.size());
                FormParser.makeVisitLogPA(eventList);
                eventList.clear();
                retryCount=0;
                return;
            };

            long maxServerVersion = 0;

            assert eventArray != null;
            if(eventArray.length() > 0){
                for (int i=0;i<eventArray.length();i++){
                    try {
                        Event event =  new Gson().fromJson(eventArray.getJSONObject(i).toString(), Event.class);
                        eventList.add(event);
                        long serverVersion = eventArray.getJSONObject(i).optLong("serverVersion");
                        Log.v("EVENT_FETCH_SERVER_VERSION",""+serverVersion);
                        if(serverVersion > maxServerVersion){
                            maxServerVersion = serverVersion;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            //SSLocationHelper.getInstance().updateModel();
            Log.v("EVENT_FETCH_COUNT",""+ jsonObjectEvent.optInt("no_of_events")+"   "+eventArray.length()+" "+maxServerVersion);
            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(SERVER_VERSION_EVENT,maxServerVersion+"");
            fetchEvent();

        }
    }

    /**
     * getting selected village for pa
     * @return village id string
     */
    private String getVillage() {
        ArrayList<String> getVillageList = SSLocationHelper.getInstance().getSelectedVillageId();
        StringBuilder vid = new StringBuilder();
        for(int i = 0; i< getVillageList.size() ; i++){
            vid.append(getVillageList.get(i)).append(",");
        }
        if(vid.length() > 0){
            vid = new StringBuilder(vid.substring(0, vid.length() - 1));
        }
        return vid.toString();
    }

    /**
     * getting event list from server
     * @return parsed json object or null if error occurred
     */
    private JSONObject getEventList(){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }

            String url;
            url = baseUrl + EVENT_FETCH;
            Log.v("EVENT_FETCH","getEventList>>url:"+url);
            Response resp = httpAgent.fetch(url);

            if (resp.isFailure()) {
                throw new NoHttpResponseException(EVENT_FETCH + " not returned data");
            }

            JSONObject jsonObject = new JSONObject((String) resp.payload());

            Log.v("EVENT_FETCH_PAYLOAD","getEventList>>url:"+jsonObject);

            return jsonObject;
        }catch (Exception e){
            Log.v("EVENT_FETCH_ERRRR","getEventList>>url:"+e.getMessage());
        }

        retryCount++;
        return null;

    }
}
