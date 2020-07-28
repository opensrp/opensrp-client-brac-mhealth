package org.smartregister.brac.hnpp.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.VisitLog;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.util.AssetHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC1_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC2_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC3_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ELCO;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ENC_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.GIRL_PACKAGE;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.IYCF_PACKAGE;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.MEMBER_REFERRAL;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.NCD_PACKAGE;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PNC_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.WOMEN_PACKAGE;
import static org.smartregister.chw.anc.util.NCUtils.eventToVisit;
import static org.smartregister.util.JsonFormUtils.gson;

public class VisitLogIntentService extends IntentService {

    public VisitLogIntentService() {
        super("VisitLogService");
    }

    public List<Visit>  getANCRegistrationVisitsFromEvent(){
        List<Visit> v = new ArrayList<>();
        String query = "SELECT event.baseEntityId,event.eventId, event.json,event.eventType FROM event WHERE (event.eventType = 'ANC Registration' OR event.eventType = 'Pregnancy Outcome') AND event.eventId NOT IN (Select visits.visit_id from visits) AND event.json like '%form_name%'";
        Cursor cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String baseEntityId = cursor.getString(0);
                String eventId = cursor.getString(1);
                String json = cursor.getString(2);
                String eventType = cursor.getString(3);
                Event baseEvent = gson.fromJson(json, Event.class);

                try {
                    Visit visit = NCUtils.eventToVisit(baseEvent, eventId);
                    v.add(visit);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                cursor.moveToNext();
            }
            cursor.close();
        }
        return v;
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<String> visit_ids = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitIds();
        for (int i = 0; i < visit_ids.size(); i++) {
            List<Visit> v = AncLibrary.getInstance().visitRepository().getVisitsByVisitId(visit_ids.get(i));
            //getANCRegistrationVisitsFromEvent(v);
            for (Visit visit : v) {
                Log.v("PROCESS_CLIENT","start>>>>"+visit.getVisitType());
                if(isForumEvent(visit.getVisitType())){
                   saveForumData(visit);

                }else{

                    String eventJson = visit.getJson();
                    if (!StringUtils.isEmpty(eventJson)) {
                        try {

                            Event baseEvent = gson.fromJson(eventJson, Event.class);
                            String base_entity_id = baseEvent.getBaseEntityId();
                            HashMap<String,Object>form_details = getFormNamesFromEventObject(baseEvent);
                            ArrayList<String> encounter_types = (ArrayList<String>) form_details.get("form_name");
                            HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
                            final CommonPersonObjectClient client = new CommonPersonObjectClient(base_entity_id, details, "");
                            client.setColumnmaps(details);
                            for (String encounter_type : encounter_types) {
                                JSONObject form_object = loadFormFromAsset(encounter_type);
                                JSONObject stepOne = form_object.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                                for (int k = 0; k < jsonArray.length(); k++) {
                                    populateValuesForFormObject(client, jsonArray.getJSONObject(k));
                                }
                                VisitLog log = new VisitLog();
                                log.setVisitId(visit.getVisitId());
                                log.setVisitType(visit.getVisitType());
                                log.setBaseEntityId(base_entity_id);
                                if( HnppConstants.EVENT_TYPE.CHILD_REFERRAL.equalsIgnoreCase(encounter_type)){
                                    if(details.containsKey("cause_of_referral_child")&&!StringUtils.isEmpty(details.get("cause_of_referral_child"))){
                                        log.setReferReason(details.get("cause_of_referral_child"));

                                    }
                                    if(details.containsKey("place_of_referral")){
                                        log.setReferPlace( details.get("place_of_referral"));
                                    }

                                }else if( HnppConstants.EVENT_TYPE.WOMEN_REFERRAL.equalsIgnoreCase(encounter_type)){
                                    if(details.containsKey("cause_of_referral_woman")&&!StringUtils.isEmpty(details.get("cause_of_referral_woman"))){
                                        log.setReferReason(details.get("cause_of_referral_woman"));

                                    }
                                    if(details.containsKey("place_of_referral")){
                                        log.setReferPlace( details.get("place_of_referral"));
                                    }

                                }
                                else if( HnppConstants.EVENT_TYPE.MEMBER_REFERRAL.equalsIgnoreCase(encounter_type)){
                                    if(details.containsKey("cause_of_referral_all")&&!StringUtils.isEmpty(details.get("cause_of_referral_all"))){
                                        log.setReferReason(details.get("cause_of_referral_all"));

                                    }
                                    if(details.containsKey("place_of_referral")){
                                        log.setReferPlace( details.get("place_of_referral"));
                                    }

                                }
                                if(REFERREL_FOLLOWUP.equalsIgnoreCase(encounter_type)){
                                    String refer_reason = "";
                                    String place_of_refer = "";
                                    if(details.containsKey("caused_referred")&&!StringUtils.isEmpty(details.get("caused_referred"))){
                                        refer_reason = details.get("caused_referred");
                                    }

                                    log.setReferReason(refer_reason);

                                    if(details.containsKey("place_referred")){
                                        place_of_refer = details.get("place_of_referral");
                                    }
                                    log.setReferPlace(place_of_refer);


                                }
                                if(ELCO.equalsIgnoreCase(encounter_type)){
                                    if(details.containsKey("pregnancy_test_result")&&!StringUtils.isEmpty(details.get("pregnancy_test_result"))){
                                        log.setPregnantStatus(details.get("pregnancy_test_result"));
                                    }
                                }
                                if(ANC1_REGISTRATION.equalsIgnoreCase(encounter_type) || ANC2_REGISTRATION.equalsIgnoreCase(encounter_type)
                                        || ANC3_REGISTRATION.equalsIgnoreCase(encounter_type)){
                                    if(details.containsKey("brac_anc") && !StringUtils.isEmpty(details.get("brac_anc"))){
                                        String ancValue = details.get("brac_anc");
                                        Log.v("BRAC_ANC","ancValue:"+ancValue);
                                        String prevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(base_entity_id+"_BRAC_ANC");
                                        Log.v("BRAC_ANC","prevalue:"+prevalue);
                                        if(!TextUtils.isEmpty(prevalue)){
                                            int lastValue = Integer.parseInt(prevalue);
                                            int ancValueInt = Integer.parseInt(ancValue);
                                            Log.v("BRAC_ANC","lastValue:"+lastValue+":ancValueInt:"+ancValueInt);
                                            if(ancValueInt >= lastValue){
                                                Log.v("BRAC_ANC","updated ancValueInt:"+ancValueInt);

                                                FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_ANC",(ancValueInt+1)+"");
                                            }
                                        }else{
                                            FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_ANC",1+"");
                                        }
                                    }
                                }
                                if(PNC_REGISTRATION.equalsIgnoreCase(encounter_type)){
                                    if(details.containsKey("brac_pnc") && !StringUtils.isEmpty(details.get(""))){
                                        String ancValue = details.get("brac_pnc");
                                        String prevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(base_entity_id+"_BRAC_PNC");
                                        if(!TextUtils.isEmpty(prevalue)){
                                            int lastValue = Integer.parseInt(prevalue);
                                            int ancValueInt = Integer.parseInt(ancValue);
                                            if(ancValueInt >= lastValue){
                                                FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_PNC",(ancValueInt+1)+"");
                                            }
                                        }else{
                                            FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_PNC",1+"");
                                        }
                                    }
                                    if(details.containsKey("total_anc") && !StringUtils.isEmpty(details.get(""))){
                                        String ancValue = details.get("total_anc");
                                        if(!TextUtils.isEmpty(ancValue)){
                                            int count = Integer.parseInt(ancValue);
                                            FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_TOTAL_ANC",count+"");

                                        }
                                    }
                                }
                                if(ANC_REGISTRATION.equalsIgnoreCase(encounter_type)){
                                    FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_ANC",0+"");
                                    FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_PNC",0+"");

                                }
                                if(HOME_VISIT_FAMILY.equalsIgnoreCase(encounter_type)){
                                    log.setFamilyId(base_entity_id);
                                }else{
                                    log.setFamilyId(HnppDBUtils.getFamilyIdFromBaseEntityId(base_entity_id));
                                }
                                log.setVisitDate(visit.getDate().getTime());
                                log.setEventType(encounter_type);
                                log.setVisitJson(form_object.toString());
                                HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
                                if (HOME_VISIT_FAMILY.equalsIgnoreCase(encounter_type)){
                                    HnppApplication.getHNPPInstance().getHnppVisitLogRepository().updateFamilyLastHomeVisit(base_entity_id,String.valueOf(visit.getDate().getTime()));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
        }
        processAncregistration();
    }
    private boolean isForumEvent(String eventType){
        switch (eventType) {
            case HnppConstants.EVENT_TYPE.FORUM_CHILD:
            case HnppConstants.EVENT_TYPE.FORUM_WOMEN:
            case HnppConstants.EVENT_TYPE.FORUM_ADO:
            case HnppConstants.EVENT_TYPE.FORUM_NCD:
                return true;
            default:
                return false;
        }

    }
    private static synchronized void saveForumData(Visit visit) {
        Log.v("PROCESS_CLIENT","saveForumData visit event:"+visit.getVisitType());
        switch (visit.getVisitType()){
            case HnppConstants.EVENT_TYPE.FORUM_CHILD:
            case HnppConstants.EVENT_TYPE.FORUM_WOMEN:
            case HnppConstants.EVENT_TYPE.FORUM_ADO:
            case HnppConstants.EVENT_TYPE.FORUM_NCD:
                if (visit.getJson() != null) {
                    Log.v("PROCESS_CLIENT","processing>>>>"+visit.getVisitType());
                    Event baseEvent = gson.fromJson(visit.getJson(), Event.class);
                    List<Obs> obsList = baseEvent.getObs();
                    ForumDetails forumDetails = new ForumDetails();
                    for(Obs obs:obsList){
                        try{
                            Log.v("PROCESS_CLIENT","loop>>>>"+baseEvent.getEventType());
                            String key = obs.getFormSubmissionField();

                            Log.v("PROCESS_CLIENT","loop>>>>key:"+key);
                            if(key.equalsIgnoreCase("forumType")){
                                forumDetails.forumType = (String) obs.getValue();
                            }
                            if(key.equalsIgnoreCase("forumName")){
                                forumDetails.forumName = (String) obs.getValue();
                            }
                            if(key.equalsIgnoreCase("place")){
                                String jsonFromMap = gson.toJson(obs.getValue());
                                forumDetails.place = gson.fromJson(jsonFromMap, HHMemberProperty.class);
                            }
                            if(key.equalsIgnoreCase("participants")){
                                String jsonFromMap = gson.toJson(obs.getValue());
                                forumDetails.participants = gson.fromJson(jsonFromMap,  new TypeToken<ArrayList<HHMemberProperty>>() {
                                }.getType());
                            }
                            if(key.equalsIgnoreCase("noOfParticipant")){
                                forumDetails.noOfParticipant = (String) obs.getValue();
                            }
                            if(key.equalsIgnoreCase("forumDate")){
                                forumDetails.forumDate = (String) obs.getValue();
                            }
                            if(key.equalsIgnoreCase("ssName")){
                                forumDetails.ssName = (String) obs.getValue();
                            }
                            if(key.equalsIgnoreCase("villageName")){
                                forumDetails.villageName =  (String) obs.getValue();
                            }
                            if(key.equalsIgnoreCase("clusterName")){
                                forumDetails.clusterName =  (String) obs.getValue();
                            }
                            if(key.equalsIgnoreCase("noOfAdoTakeFiveFood")){
                                forumDetails.noOfAdoTakeFiveFood = (String) obs.getValue();
                            }
                            if(key.equalsIgnoreCase("noOfServiceTaken")){
                                forumDetails.noOfServiceTaken = (String) obs.getValue();
                            }
                            if(key.equalsIgnoreCase("sIndex")){
                                double d =  (Double) obs.getValue();
                                forumDetails.sIndex = (int)d;
                            }
                            if(key.equalsIgnoreCase("vIndex")){
                                double d =  (Double) obs.getValue();
                                forumDetails.vIndex =  (int) d;
                            }
                            if(key.equalsIgnoreCase("cIndex")){
                                double d =  (Double) obs.getValue();
                                forumDetails.cIndex =  (int) d;
                            }
                        }catch (Exception e){
                            Log.v("PROCESS_CLIENT","json exception>>>"+visit.getVisitType());
                            e.printStackTrace();
                        }

                    }
                    Log.v("PROCESS_CLIENT","done>>>>"+visit.getVisitType());
                    if(!TextUtils.isEmpty(forumDetails.forumName)){
                        VisitLog log = new VisitLog();
                        log.setVisitId(visit.getVisitId());
                        log.setVisitType(visit.getVisitType());
                        log.setBaseEntityId(visit.getBaseEntityId());
                        log.setVisitDate(visit.getDate().getTime());
                        log.setEventType(visit.getVisitType());
                        log.setVisitJson(gson.toJson(forumDetails));
                        log.setFamilyId(forumDetails.place.getBaseEntityId());
                        Log.v("PROCESS_CLIENT","add to visitlog>>>>"+visit.getVisitType());
                        HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
                    }

                }
                break;
            default:
                break;


        }

    }
    private void processAncregistration(){
        List<Visit> v = getANCRegistrationVisitsFromEvent();
        for (Visit visit : v) {
            String eventJson = visit.getJson();
            if (!StringUtils.isEmpty(eventJson)) {
                try {

                    Event baseEvent = gson.fromJson(eventJson, Event.class);
                    String base_entity_id = baseEvent.getBaseEntityId();
                    HashMap<String,Object>form_details = getFormNamesFromEventObject(baseEvent);
                    ArrayList<String> encounter_types = (ArrayList<String>) form_details.get("form_name");
                    HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
                    final CommonPersonObjectClient client = new CommonPersonObjectClient(base_entity_id, details, "");
                    client.setColumnmaps(details);
                    for (String encounter_type : encounter_types) {
                        JSONObject form_object = loadFormFromAsset(encounter_type);
                        JSONObject stepOne = form_object.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        for (int k = 0; k < jsonArray.length(); k++) {
                            populateValuesForFormObject(client, jsonArray.getJSONObject(k));
                        }
                        VisitLog log = new VisitLog();
                        log.setVisitId(visit.getVisitId());
                        log.setVisitType(visit.getVisitType());
                        log.setBaseEntityId(base_entity_id);

                        log.setVisitDate(visit.getDate().getTime());
                        log.setEventType(encounter_type);
                        log.setVisitJson(form_object.toString());
                        log.setFamilyId(HnppDBUtils.getFamilyIdFromBaseEntityId(base_entity_id));
                        HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void populateValuesForFormObject(CommonPersonObjectClient client, JSONObject jsonObject) {
        try {
            String value = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY),false);
            //spinner
            if (jsonObject.has("openmrs_choice_ids")) {
                JSONObject choiceObject = jsonObject.getJSONObject("openmrs_choice_ids");

                for (int i = 0; i < choiceObject.names().length(); i++) {
                    if (value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))) {
                        value = choiceObject.names().getString(i);
                    }
                }
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,value);
            }else if (jsonObject.has("options")) {
                if(jsonObject.getString("key").equalsIgnoreCase("hh_visit_members")){
                    JSONArray option_array = jsonObject.getJSONArray("options");
                    String[] strs = value.split(",");
                    if(strs.length == 0){

                    }else{
                        for(String name : strs){
                            JSONObject item = new JSONObject();
                            if(name.equalsIgnoreCase("chk_nobody")){

                                item.put("key","chk_nobody");
                                item.put("text","কাউকে পাওয়া যায়নি");
                                item.put("value",true);
                                item.put("openmrs_entity","concept");
                                item.put("openmrs_entity_id","chk_nobody");
                            }else{
                                item.put("key",name.replace(" ","_"));
                                item.put("text",name);
                                item.put("value",true);
                                item.put("openmrs_entity","concept");
                                item.put("openmrs_entity_id",name.replace(" ","_"));
                            }


                            option_array.put(item);
                        }
                    }

                }

                else{
                    JSONArray option_array = jsonObject.getJSONArray("options");
                    for (int i = 0; i < option_array.length(); i++) {
                        JSONObject option = option_array.getJSONObject(i);
                        if(jsonObject.getString("key").equalsIgnoreCase("preg_outcome")){
                            String[] strs = value.split(",");
                            for(String name : strs){
                                if (name.equalsIgnoreCase(option.optString("key"))) {
                                    option.put("value", "true");
                                }
                            }
                        }
                        else if (value.contains(option.optString("key"))) {
                            option.put("value", "true");
                        }
                    }
                }

            }
            else{
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, value);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject loadFormFromAsset(String encounter_type) {
        String form_name = "";
        if (ANC_PREGNANCY_HISTORY.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.PREGNANCY_HISTORY+".json";
        } else if (ANC_GENERAL_DISEASE.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.GENERAL_DISEASE+".json";
        } else if (ANC1_REGISTRATION.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.ANC1_FORM+".json";
        } else if (ANC2_REGISTRATION.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.ANC2_FORM+".json";
        } else if (ANC3_REGISTRATION.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.ANC3_FORM+".json";
        }else if (MEMBER_REFERRAL.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.MEMBER_REFERRAL+".json";
        }else if (HnppConstants.EVENT_TYPE.WOMEN_REFERRAL.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.WOMEN_REFERRAL+".json";
        }else if (HnppConstants.EVENT_TYPE.CHILD_REFERRAL.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.CHILD_REFERRAL+".json";
        }else if (PNC_REGISTRATION.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.PNC_FORM+".json";
        } else if (ELCO.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.ELCO+".json";
        }else if (NCD_PACKAGE.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.NCD_PACKAGE+".json";
        }else if (WOMEN_PACKAGE.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.WOMEN_PACKAGE+".json";
        }else if (GIRL_PACKAGE.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.GIRL_PACKAGE+".json";
        }else if (IYCF_PACKAGE.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.IYCF_PACKAGE+".json";
        }
        else if (ENC_REGISTRATION.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.ENC_REGISTRATION+".json";
        }
        else if (HOME_VISIT_FAMILY.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.HOME_VISIT_FAMILY+".json";
        }
        else if (REFERREL_FOLLOWUP.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP+".json";
        }
        else if (CHILD_FOLLOWUP.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.CHILD_FOLLOWUP+".json";
        }
        else if (ANC_REGISTRATION.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.ANC_FORM+".json";
        }else if (PREGNANCY_OUTCOME.equalsIgnoreCase(encounter_type)) {
            form_name = HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME+".json";
        }
        try {
            String jsonString = AssetHandler.readFileFromAssetsFolder("json.form/"+form_name, VisitLogIntentService.this);
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){

        }
        return new JSONObject();
    }

    public HashMap<String,Object> getFormNamesFromEventObject(Event baseEvent) {
        ArrayList<String> forms = new ArrayList<>();
        HashMap<String,Object>details = new HashMap<>();
        for (Obs o : baseEvent.getObs()) {
            if ("form_name".equalsIgnoreCase(o.getFormSubmissionField())) {
                forms.add(o.getFieldCode());
            }

            if(details.containsKey(o.getFormSubmissionField())) {
                details.put(o.getFormSubmissionField(),details.get(o.getFormSubmissionField())+","+o.getValue());
            } else {
                details.put(o.getFormSubmissionField(),o.getValue());
            }
        }
        HashMap<String,Object>form_details = new HashMap<>();
        form_details.put("form_name",forms);
        form_details.put("details",details);
        return form_details;
    }
}
