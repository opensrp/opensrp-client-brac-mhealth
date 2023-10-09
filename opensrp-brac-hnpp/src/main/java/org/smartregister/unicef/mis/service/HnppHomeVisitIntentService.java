package org.smartregister.unicef.mis.service;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Calendar;
import java.util.List;

import static org.smartregister.chw.anc.util.NCUtils.getSyncHelper;

public class HnppHomeVisitIntentService{//} extends IntentService {

//    public HnppHomeVisitIntentService() {
//        super("HnppHomeVisitIntentService");
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        try {
//            processVisits();
//        } catch (Exception e) {
//            Timber.e(e);
//        }
//    }

    public static synchronized void processVisits() {
        Calendar calendar = Calendar.getInstance();
        VisitRepository visitRepository = AncLibrary.getInstance().visitRepository();
        List<Visit> visits = visitRepository.getAllUnSynced(calendar.getTime().getTime());
        for (Visit v : visits) {
            if (!v.getProcessed()) {

                // persist to db
                Event baseEvent = new Gson().fromJson(v.getPreProcessedJson(), Event.class);
                AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
                JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
                JSONObject eventJson = null;
                try {
                    eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                    getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
                    // process details
                    Log.v("FORUM_TEST","processVisits>>eventType:"+baseEvent.getEventType()+":visitId:"+v.getVisitId());

                    visitRepository.completeProcessing(v.getVisitId());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.v("FORUM_TEST","processVisits>>exception"+e.getMessage());
                }

            }
        }

        // process after all events are saved
       // NCUtils.startClientProcessing();

    }
}
