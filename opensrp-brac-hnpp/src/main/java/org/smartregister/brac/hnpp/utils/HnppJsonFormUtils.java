package org.smartregister.brac.hnpp.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.repository.HnppChwRepository;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.repository.StockRepository;
import org.smartregister.brac.hnpp.service.VisitLogIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.LocationPickerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.smartregister.chw.anc.util.JsonFormUtils.HOME_VISIT_GROUP;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;
import static org.smartregister.chw.anc.util.NCUtils.getClientProcessorForJava;
import static org.smartregister.chw.anc.util.NCUtils.getSyncHelper;

/**
 * Created by keyman on 13/11/2018.
 */
public class HnppJsonFormUtils extends CoreJsonFormUtils {
    public static final String METADATA = "metadata";
    public static final String SS_NAME = "ss_name";
    public static final String SIMPRINTS_ENABLE = "simprints_enable";
    public static final String VILLAGE_NAME = "village_name";
    public static final String ENCOUNTER_TYPE = "encounter_type";

    public static String[] monthStr = {"January","February","March","April","May","June","July","August","September","October","November","December"};
    public static final String REFEREL_EVENT_TYPE = "Referral Clinic";

    public static String[] monthBanglaStr = {"জানুয়ারী","ফেব্রুয়ারী","মার্চ","এপ্রিল","মে","জুন","জুলাই","আগস্ট","সেপ্টেম্বর","অক্টোবর","নভেম্বর","ডিসেম্বর"};
    public static JSONObject getVisitFormWithData(String eventJson, Context context){
        JSONObject form_object = null;
        try{
            Event baseEvent = gson.fromJson(eventJson, Event.class);
            String base_entity_id = baseEvent.getBaseEntityId();
            HashMap<String,Object>form_details = FormParser.getFormNamesFromEventObject(baseEvent);
            ArrayList<String> encounter_types = (ArrayList<String>) form_details.get("form_name");
            HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
            final CommonPersonObjectClient client = new CommonPersonObjectClient(base_entity_id, details, "");
            client.setColumnmaps(details);
            form_object = FormParser.loadFormFromAsset(encounter_types.get(0));
            JSONObject stepOne = form_object.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            for (int k = 0; k < jsonArray.length(); k++) {
                FormParser.populateValuesForFormObject(client, jsonArray.getJSONObject(k));
            }
        }catch (Exception e){
            e.printStackTrace();

        }
        return form_object;

    }

    public static void addRelationalIdAsGuest(JSONObject jsonForm){
        JSONObject stepOne = null;
        try {

            stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);

            updateFormField(jsonArray, "relational_id", HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION);



        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static int getMonthFromMonthString(String month){
        for(int i=0;i<monthStr.length;i++){
            if(monthStr[i].equalsIgnoreCase(month)){
                return i+1;
            }
        }
        return 0;
    }
    public static boolean isCurrentMonth(String month, String year){
        if(TextUtils.isEmpty(month) || TextUtils.isEmpty(year)){
            return false;
        }
        LocalDate localDate = new LocalDate(System.currentTimeMillis());
        int cMonth = localDate.getMonthOfYear();
        int cYear = localDate.getYear();
        int iMonth = 0;
        for(int i= 0;i<monthStr.length;i++){
            if(monthStr[i].equalsIgnoreCase(month)){
                iMonth = i;
            }
        }
        int iYear = Integer.parseInt(year);
        if(cMonth+1 == iMonth && cYear == iYear){
            return true;
        }
        return false;
    }
    public static boolean isCurrentYear(String year){
        if(TextUtils.isEmpty(year)){
            return false;
        }
        LocalDate localDate = new LocalDate(System.currentTimeMillis());
        int cYear = localDate.getYear();

        int iYear = Integer.parseInt(year);
        if( cYear == iYear){
            return true;
        }
        return false;
    }
    public static int getCurrentMonth(){
        LocalDate localDate = new LocalDate(System.currentTimeMillis());
        int cMonth = localDate.getMonthOfYear();
        return cMonth;
    }
    public static Visit processAndSaveSSForm(String jsonString) throws Exception{

        FormTag formTag = formTag(Utils.getAllSharedPreferences());
        formTag.appVersionName = BuildConfig.VERSION_NAME;
        String baseEntityId = generateRandomUUIDString();
        JSONObject form = new JSONObject(jsonString);
        HnppJsonFormUtils.setEncounterDateTime(form);

        String ssName = getSSNameFromForm(form);
        Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(new JSONArray(), formTag, baseEntityId);
        baseClient.setFirstName(HnppConstants.EVENT_TYPE.SS_INFO);
        baseClient.setLastName("ss_form");
        baseClient.setBirthdate(new Date(0L));
        baseClient.setGender("M");
        baseClient.addAttribute("ss_name",ssName);
        baseClient.addAttribute("ss_form_date",new Date());
        baseClient.addIdentifier("opensrp_id",generateRandomUUIDString());

        SSLocations ss = SSLocationHelper.getInstance().getSSLocationBySSName(ssName);
        List<Address> listAddress = new ArrayList<>();
        listAddress.add(SSLocationHelper.getInstance().getSSAddress(ss));
        baseClient.setAddresses(listAddress);
        JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
        getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
        Map<String, String> jsonStrings = new HashMap<>();
        jsonStrings.put("First",jsonString);
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);
        JSONObject jsonForm = (JSONObject)registrationFormParams.getMiddle();
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        updateFormWithSSNameOnly(jsonForm,ssName);
        Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, "metadata"), formTag, baseEntityId,HnppConstants.EVENT_TYPE.SS_INFO,"visits");

       // Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processVisitJsonForm(Utils.getAllSharedPreferences(), baseEntityId, HnppConstants.EVENT_TYPE.SS_INFO, jsonStrings, "visits");

        if (baseEvent != null) {
            baseEvent.setFormSubmissionId(JsonFormUtils.generateRandomUUIDString());
            org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(Utils.getAllSharedPreferences(), baseEvent);
            String visitID ="";
            if(!TextUtils.isEmpty(baseEvent.getEventId())){
                visitID = baseEvent.getEventId();
            }else{
                visitID = JsonFormUtils.generateRandomUUIDString();
            }

            Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
            visitRepository().addVisit(visit);
            visitRepository().completeProcessing(visit.getVisitId());
            JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
            getSyncHelper().addEvent(baseClient.getBaseEntityId(), eventJson);
//            List<EventClient> eventClientList = new ArrayList();
//            org.smartregister.domain.db.Event domainEvent = org.smartregister.family.util.JsonFormUtils.gson.fromJson(eventJson.toString(), org.smartregister.domain.db.Event.class);
//            org.smartregister.domain.db.Client domainClient = org.smartregister.family.util.JsonFormUtils.gson.fromJson(clientJson.toString(), org.smartregister.domain.db.Client.class);
//            eventClientList.add(new EventClient(domainEvent, domainClient));

            long lastSyncTimeStamp = Utils.getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
//            getClientProcessorForJava().processClient(eventClientList);
            Utils.getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
            return visit;
        }
        return null;


    }
    private static VisitRepository visitRepository() {
        return AncLibrary.getInstance().visitRepository();
    }
    public static void makeReadOnlyFields(JSONObject jsonObject){
        try {
            for(int i=1;i<9;i++){
                JSONObject steps = jsonObject.getJSONObject("step"+i);
                JSONArray jsonArray = steps.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                for(int j=0;j<jsonArray.length();j++){
                    JSONObject fieldObject = jsonArray.getJSONObject(j);
                    fieldObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Visit processAndSaveForum(String eventType, ForumDetails forumDetails,String baseEntityId) throws Exception{
        if(!FormApplicability.isDueAnyForm(baseEntityId,eventType)){
            return null;
        }
        FormTag formTag = formTag(Utils.getAllSharedPreferences());
        formTag.appVersionName = BuildConfig.VERSION_NAME;
        Log.v("FORUM_TEST","processAndSaveForum>>eventType:"+eventType+":baseEntityId:"+baseEntityId);
        Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(new JSONArray(), formTag, baseEntityId);
        baseClient.setFirstName(eventType);
        baseClient.setLastName("Forum");
        baseClient.setBirthdate(new Date(0L));
        baseClient.setGender("M");
        baseClient.addAttribute("forumDate",forumDetails.forumDate);
        baseClient.addAttribute("forumType",forumDetails.forumType);
        baseClient.addIdentifier("opensrp_id",generateRandomUUIDString());

        SSLocations ss = SSLocationHelper.getInstance().getSsModels().get(forumDetails.sIndex).locations.get(forumDetails.vIndex);
        List<Address> listAddress = new ArrayList<>();
        listAddress.add(SSLocationHelper.getInstance().getSSAddress(ss));
        baseClient.setAddresses(listAddress);
        JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
        getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);



        Event baseEvent = processCustomEvent(baseEntityId,eventType,forumDetails);
        if (baseEvent != null) {
            baseEvent.setFormSubmissionId(JsonFormUtils.generateRandomUUIDString());
            org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(Utils.getAllSharedPreferences(), baseEvent);
            String visitID ="";
            if(!TextUtils.isEmpty(baseEvent.getEventId())){
                visitID = baseEvent.getEventId();
            }else{
                visitID = JsonFormUtils.generateRandomUUIDString();
            }

            Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
//           try{
//               visit.setParentVisitID(visitRepository().getParentVisitEventID(visit.getBaseEntityId(), eventType, visit.getDate()));
//           }catch (Exception e){
//
//           }

            visitRepository().addVisit(visit);
            visitRepository().completeProcessing(visit.getVisitId());
            JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
            Log.v("FORUM_TEST","addEvent>>eventType:"+baseClient.getBaseEntityId()+":eventJson:"+eventJson);

            getSyncHelper().addEvent(baseClient.getBaseEntityId(), eventJson);
//            List<EventClient> eventClientList = new ArrayList();
//            org.smartregister.domain.db.Event domainEvent = org.smartregister.family.util.JsonFormUtils.gson.fromJson(eventJson.toString(), org.smartregister.domain.db.Event.class);
//            org.smartregister.domain.db.Client domainClient = org.smartregister.family.util.JsonFormUtils.gson.fromJson(clientJson.toString(), org.smartregister.domain.db.Client.class);
//            eventClientList.add(new EventClient(domainEvent, domainClient));

            long lastSyncTimeStamp = Utils.getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
//            getClientProcessorForJava().processClient(eventClientList);
            Utils.getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
            return visit;
        }
        return null;


    }
    private static Event processCustomEvent(String baseEntityId, String eventType, ForumDetails forumDetails){
        Event event = getEvent(baseEntityId, eventType, new Date(), "visits");
        final String FORM_SUBMISSION_FIELD = "formsubmissionField";
        final String DATA_TYPE = "text";

        String formSubmissionField = "forumType";
        List<Object> vall = new ArrayList<>();
        vall.add(forumDetails.forumType);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));

        formSubmissionField = "forumName";
        vall = new ArrayList<>();
        vall.add(forumDetails.forumName);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));

        formSubmissionField = "place";
        vall = new ArrayList<>();
        vall.add(forumDetails.place);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));

        formSubmissionField = "noOfParticipant";
        vall = new ArrayList<>();
        vall.add(forumDetails.noOfParticipant);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));

        formSubmissionField = "participants";
        vall = new ArrayList<>();
        vall.add(forumDetails.participants);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));

        formSubmissionField = "forumDate";
        vall = new ArrayList<>();
        vall.add(forumDetails.forumDate);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));

        formSubmissionField = "ssName";
        vall = new ArrayList<>();
        vall.add(forumDetails.ssName);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));

        formSubmissionField = "villageName";
        vall = new ArrayList<>();
        vall.add(forumDetails.villageName);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));
        formSubmissionField = "clusterName";
        vall = new ArrayList<>();
        vall.add(forumDetails.clusterName);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));
        if(!TextUtils.isEmpty(forumDetails.noOfAdoTakeFiveFood)){
            formSubmissionField = "noOfAdoTakeFiveFood";
            vall = new ArrayList<>();
            vall.add(forumDetails.noOfAdoTakeFiveFood);
            event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                    formSubmissionField));
        }
        formSubmissionField = "noOfServiceTaken";
        vall = new ArrayList<>();
        vall.add(forumDetails.noOfServiceTaken);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));
        formSubmissionField = "sIndex";
        vall = new ArrayList<>();
        vall.add(forumDetails.sIndex);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));
        formSubmissionField = "vIndex";
        vall = new ArrayList<>();
        vall.add(forumDetails.vIndex);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));
        formSubmissionField = "cIndex";
        vall = new ArrayList<>();
        vall.add(forumDetails.cIndex);
        event.addObs(new Obs(FORM_SUBMISSION_FIELD, DATA_TYPE, formSubmissionField, "", vall, new ArrayList<>(), null,
                formSubmissionField));
        return event;

    }
    private static Event getEvent(String entityId, String
            encounterType, Date encounterDate, String childType) {
        Event event = (Event) new Event().withBaseEntityId(entityId) //should be different for main and subform
                .withEventDate(encounterDate).withEventType(encounterType)
                .withEntityType(childType)
                .withFormSubmissionId(generateRandomUUIDString()).withDateCreated(new Date());

        tagSyncMetadata(event);

        return event;
    }
    protected static Event tagSyncMetadata(@NonNull Event event) {
        AllSharedPreferences allSharedPreferences = Utils.getAllSharedPreferences();
        String providerId = allSharedPreferences.fetchRegisteredANM();
        event.setProviderId(providerId);
        event.setLocationId(locationId(allSharedPreferences));


        event.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        event.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));


        event.setClientDatabaseVersion(FamilyLibrary.getInstance().getDatabaseVersion());
        event.setClientApplicationVersion(FamilyLibrary.getInstance().getApplicationVersion());
        return event;
    }

    public static synchronized Visit saveVisit(boolean isComesFromIdentity,boolean needVerified,boolean isVerified, String notVerifyCause,String memberID, String encounterType,
                            final Map<String, String> jsonString,
                            String parentEventType,String formSubmissionId,String visitId) throws Exception {
        Log.v("SAVE_VISIT","saveVisit>>");
        if(!FormApplicability.isDueAnyForm(memberID,encounterType)){
            //passing emptyVisit object with zero id
            //this will trigger when visit already exist
            Visit emptyVisit = new Visit();
            emptyVisit.setVisitId("0");
            return null;
        }

        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();

        //String derivedEncounterType = StringUtils.isBlank(parentEventType) ? encounterType : "";
        //Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processVisitJsonForm(allSharedPreferences, memberID, derivedEncounterType, jsonString, getTableName());
        //
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString.get("First"));
        JSONObject jsonForm = (JSONObject)registrationFormParams.getMiddle();
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, "metadata"), formTag(allSharedPreferences), memberID,encounterType,getTableName());

        //
        if(isComesFromIdentity){
            prepareIsIdentified(baseEvent);
        }else if(needVerified){
                prepareIsVerified(baseEvent,isVerified,notVerifyCause);

        }

        if (baseEvent != null) {
            baseEvent.setFormSubmissionId(formSubmissionId);
            try{
                Map<String,String> villageIds = new HashMap<>();
                villageIds.put("village_id",HnppDBUtils.getVillageIdByBaseEntityId(baseEvent.getBaseEntityId()));
                baseEvent.setIdentifiers(villageIds);
            }catch (Exception e){

            }

            org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
            String visitID ="";
            if(!TextUtils.isEmpty(baseEvent.getEventId())){
                visitID = baseEvent.getEventId();
            }else{
                visitID = visitId;
            }
            HnppConstants.appendLog("SAVE_VISIT","saveVisit>>>baseEntityId:"+baseEvent.getBaseEntityId()+":formSubmissionId:"+baseEvent.getFormSubmissionId()+":baseEvent:"+baseEvent.getEntityType());

            Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
            if( visitRepository().getVisitByFormSubmissionID(formSubmissionId)==null){
                visitRepository().addVisit(visit);
                HnppConstants.appendLog("SAVE_VISIT","added to visit>>>baseEntityId:"+baseEvent.getBaseEntityId()+":formSubmissionId:"+baseEvent.getFormSubmissionId()+":baseEvent:"+baseEvent.getEntityType());

                return visit;
            }else{
                Visit emptyVisit = new Visit();
                emptyVisit.setVisitId("0");
                return null;
            }
        }
        return null;
    }
    public static synchronized Visit saveVisit(String memberID, String encounterType,final Map<String, String> jsonString,String formSubmissionId,String visitId) throws Exception {
        if(!FormApplicability.isDueAnyForm(memberID,encounterType)){
            return null;
        }
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
//        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString.get("First"));
//        JSONObject jsonForm = (JSONObject)registrationFormParams.getMiddle();
//        JSONArray fields = (JSONArray)registrationFormParams.getRight();
//        Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, "metadata"), formTag(allSharedPreferences), memberID,encounterType,getTableName());
        Event baseEvent = processVisitJsonForm(allSharedPreferences, memberID, encounterType, jsonString, getTableName());

        if (baseEvent != null) {
            baseEvent.setFormSubmissionId(formSubmissionId);
            try{
                Map<String,String> villageIds = new HashMap<>();
                villageIds.put("village_id",HnppDBUtils.getVillageIdByBaseEntityId(baseEvent.getBaseEntityId()));
                baseEvent.setIdentifiers(villageIds);
            }catch (Exception e){

            }

            org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
            String visitID ="";
            if(!TextUtils.isEmpty(baseEvent.getEventId())){
                visitID = baseEvent.getEventId();
            }else{
                visitID = visitId;
            }
            HnppConstants.appendLog("SAVE_VISIT","submitVisit>>>saveVisit>>memberID:"+baseEvent.getBaseEntityId()+":formSubmissionId:"+baseEvent.getFormSubmissionId()+":type:"+baseEvent.getEventType());

            Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
            visitRepository().addVisit(visit);
            return visit;
        }
        return null;
    }
    public static Event processVisitJsonForm(AllSharedPreferences allSharedPreferences, String entityId, String encounterType, Map<String, String> jsonStrings, String tableName) {

        // aggregate all the fields into 1 payload
        JSONObject jsonForm = null;
        JSONObject metadata = null;

        List<JSONObject> fields_obj = new ArrayList<>();

        for (Map.Entry<String, String> map : jsonStrings.entrySet()) {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(map.getValue());

            if (!registrationFormParams.getLeft()) {
                return null;
            }

            if (jsonForm == null) {
                jsonForm = registrationFormParams.getMiddle();
            }

            if (metadata == null) {
                metadata = getJSONObject(jsonForm, METADATA);
            }

            // add all the fields to the event while injecting a new variable for grouping
            JSONArray local_fields = registrationFormParams.getRight();
            int x = 0;
            while (local_fields.length() > x) {
                try {
                    JSONObject obj = local_fields.getJSONObject(x);
                    obj.put(HOME_VISIT_GROUP, map.getKey());
                    fields_obj.add(obj);
                } catch (JSONException e) {
                    Timber.e(e);
                }
                x++;
            }
        }

        JSONArray fields = new JSONArray(fields_obj);
        String derivedEncounterType = StringUtils.isBlank(encounterType) ? getString(jsonForm, ENCOUNTER_TYPE) : encounterType;

        return org.smartregister.util.JsonFormUtils.createEvent(fields, metadata, formTag(allSharedPreferences), entityId, derivedEncounterType, tableName);
    }
    public static String getEncounterType(String formEncounterType) {
        switch (formEncounterType){
            case HnppConstants.EVENT_TYPE.ELCO:
                return HnppConstants.EVENT_TYPE.ELCO;
            case HnppConstants.EVENT_TYPE.MEMBER_REFERRAL:
                return HnppConstants.EVENT_TYPE.MEMBER_REFERRAL;
            case HnppConstants.EVENT_TYPE.WOMEN_REFERRAL:
                return HnppConstants.EVENT_TYPE.WOMEN_REFERRAL;
            case HnppConstants.EVENT_TYPE.CHILD_REFERRAL:
                return HnppConstants.EVENT_TYPE.CHILD_REFERRAL;
            case HnppConstants.EVENT_TYPE.GIRL_PACKAGE:
                return HnppConstants.EVENT_TYPE.GIRL_PACKAGE;
            case HnppConstants.EVENT_TYPE.WOMEN_PACKAGE:
                return HnppConstants.EVENT_TYPE.WOMEN_PACKAGE;
            case HnppConstants.EVENT_TYPE.NCD_PACKAGE:
                return HnppConstants.EVENT_TYPE.NCD_PACKAGE;
            case HnppConstants.EVENT_TYPE.IYCF_PACKAGE:
                return HnppConstants.EVENT_TYPE.IYCF_PACKAGE;

            case  HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour:
                return HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour;
            case  HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour:
                return HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour;
            case  HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY:
                return HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY;
            /*case  HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP;*/

            case  HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_0_3_MONTHS:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_0_3_MONTHS;

            case  HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_3_6_MONTHS:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_3_6_MONTHS;

            case  HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_7_11_MONTHS:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_7_11_MONTHS;

            case  HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_12_18_MONTHS:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_12_18_MONTHS;

            case  HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_19_24_MONTHS:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_19_24_MONTHS;

            case  HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_2_3_YEARS:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_2_3_YEARS;

            case  HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_3_4_YEARS:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_3_4_YEARS;

            case  HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_4_5_YEARS:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOW_UP_4_5_YEARS;

            case  HnppConstants.EVENT_TYPE.PREGNANT_WOMAN_DIETARY_DIVERSITY:
                return HnppConstants.EVENT_TYPE.PREGNANT_WOMAN_DIETARY_DIVERSITY;


            case  HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP:
                return HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP;
            case  HnppConstants.EVENT_TYPE.ENC_REGISTRATION:
                return HnppConstants.EVENT_TYPE.ENC_REGISTRATION;
            case  HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL:
                return HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL;
            case  HnppConstants.EVENT_TYPE.EYE_TEST:
                return HnppConstants.EVENT_TYPE.EYE_TEST;
            case  HnppConstants.EVENT_TYPE.BLOOD_GROUP:
                return HnppConstants.EVENT_TYPE.BLOOD_GROUP;
            case  HnppConstants.EventType.REMOVE_MEMBER:
                return HnppConstants.EventType.REMOVE_MEMBER;
            case  HnppConstants.EventType.REMOVE_CHILD:
                return HnppConstants.EventType.REMOVE_CHILD;
          /*  case  HnppConstants.EVENT_TYPE.CHILD_INFO_7_24_MONTHS:
                return HnppConstants.EVENT_TYPE.CHILD_INFO_7_24_MONTHS;
            case  HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS:
                return HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS;
            case  HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12:
                return HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12;*/
                default:
                    return org.smartregister.chw.anc.util.Constants.EVENT_TYPE.ANC_HOME_VISIT;
        }

    }

    private static String getTableName() {
        return org.smartregister.chw.anc.util.Constants.TABLES.ANC_MEMBERS;
    }

    private static void prepareIsVerified(Event baseEvent, boolean isVerified , String notVerifyCause) {
        if (baseEvent != null) {
            // add anc date obs and last
            List<Object> list = new ArrayList<>();
            list.add(isVerified);


            baseEvent.addObs(new Obs("concept", "text", "is_verified", "",
                    list, new ArrayList<>(), null, "is_verified"));

           if(!isVerified){
               List<Object> list2 = new ArrayList<>();
               list2.add(notVerifyCause);
               baseEvent.addObs(new Obs("concept", "text", "not_verify_cause", "",
                       list2, new ArrayList<>(), null, "not_verify_cause"));
           }

        }
    }
    private static void prepareIsIdentified(Event baseEvent) {
        if (baseEvent != null) {
            // add anc date obs and last
            List<Object> list = new ArrayList<>();
            list.add(true);


            baseEvent.addObs(new Obs("concept", "text", "is_identified", "",
                    list, new ArrayList<>(), null, "is_identified"));


        }
    }
    public static void addEDDField(String formName,JSONObject jsonForm,String baseEntityId){
        if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC1_FORM)
                ||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC1_FORM_OOC)
                ||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC2_FORM)
                ||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC2_FORM_OOC)
                ||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC3_FORM_OOC)
                ||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC3_FORM)){
            JSONObject stepOne = null;
            try {
                HnppVisitLogRepository visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
                ANCRegister ancRegister = null;

                ancRegister = visitLogRepository.getLastANCRegister(baseEntityId);
                if(ancRegister!=null){
                    stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                    JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                    updateFormField(jsonArray, HnppConstants.ANC_REGISTER_COLUMNS.EDD, ancRegister.getEDD());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void addReferrelReasonPlaceField(JSONObject jsonForm, String reason, String place){
            JSONObject stepOne = null;
            try {

                stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);

                updateFormField(jsonArray, "caused_referred", reason);
                updateFormField(jsonArray, "place_referred", place);
                JSONObject caused_referred = getFieldJSONObject(jsonArray, "caused_referred");
                try{
                    JSONObject place_of_referral = getFieldJSONObject(jsonArray, "place_of_referral");
                    addWhereWentGo(place_of_referral, place);
                }catch (Exception e){

                }


                caused_referred.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);



            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void addWhereWentGo(JSONObject place_of_referral, String place){

        JSONArray placeJsonArray = null;
        try {
            String[] placeArray;
            int index;
            placeJsonArray = place_of_referral.getJSONArray("options");
            if(place.contains(",")){
                placeArray = place.split(",");
                for (int i = 0; i < placeArray.length; i++){
                    for (int j = 0; j < placeJsonArray.length(); j++) {
                        String abc = placeJsonArray.getString(j);
                        if(abc.contains(placeArray[i])){
                            placeJsonArray.remove(j);
                        }
                    }
                }
            }
            else {
                for (int j = 0; j < placeJsonArray.length(); j++) {
                    String abc = placeJsonArray.getString(j);
                    if(abc.contains(place)){
                        placeJsonArray.remove(j);
                    }
                }
            }

            place_of_referral.put("options",placeJsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static String getSSNameFromForm(JSONObject jsonForm){
        JSONArray field = fields(jsonForm, STEP1);
        JSONObject ss_name = getFieldJSONObject(field, "ss_name");
       return ss_name.optString(VALUE);
    }
    public static String[] getValuesFromRegistrationForm(JSONObject jsonForm){
        JSONArray field = fields(jsonForm, STEP1);
        String[] fff = new String[3];
        JSONObject first_name = getFieldJSONObject(field, "first_name");
        JSONObject n_id = getFieldJSONObject(field, "national_id");
        JSONObject age = getFieldJSONObject(field, "age_calculated");

        fff[0] = first_name.optString(VALUE);
        fff[1] = n_id.optString(VALUE);
        fff[2] = age.optString(VALUE);
        return fff;
    }
    public static String[] getValuesFromChildRegistrationForm(JSONObject jsonForm){
        JSONArray field = fields(jsonForm, STEP1);
        String[] fff = new String[3];
        JSONObject first_name = getFieldJSONObject(field, "first_name");
        JSONObject n_id = getFieldJSONObject(field, "birth_id");
        JSONObject age = getFieldJSONObject(field, "child_age_calculated");

        fff[0] = first_name.optString(VALUE);
        fff[1] = n_id.optString(VALUE);
        fff[2] = age.optString(VALUE);
        return fff;
    }
    public static String[] getValuesFromGuestRegistrationForm(JSONObject jsonForm){
        JSONArray field = fields(jsonForm, STEP1);
        String[] fff = new String[2];
        JSONObject first_name = getFieldJSONObject(field, "first_name");
        JSONObject age = getFieldJSONObject(field, "age_calculated");

        fff[0] = first_name.optString(VALUE);
        fff[1] = age.optString(VALUE);
        return fff;
    }
    public static String getSSIdFromForm(JSONObject jsonForm){
        JSONArray field = fields(jsonForm, STEP1);
        try{
            JSONObject ss_name = getFieldJSONObject(field, "ss_id");
            return ss_name.optString(VALUE);
        }catch (Exception e){

        }
        return "";

    }
    public static void getYearMonthFromForm(JSONObject jsonForm){
        try{
            JSONArray field = fields(jsonForm, STEP1);
            JSONObject ss_name = getFieldJSONObject(field, "year");
            String year =  ss_name.optString(VALUE);
            JSONArray jsonArray2 = new JSONArray();
            jsonArray2.put(year);

            ss_name.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);

            JSONObject monthObj = getFieldJSONObject(field, "month");
            String monStr =  monthObj.optString(VALUE);
            JSONArray jsonArray3 = new JSONArray();
            jsonArray3.put(monStr);
            monthObj.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray3);
        }catch (Exception e){

        }


    }
    public static void addYear(JSONObject jsonForm){
        try{
            JSONArray field = fields(jsonForm, STEP1);
            JSONObject spinner1 = getFieldJSONObject(field, "year");
            LocalDate localDate = new LocalDate(System.currentTimeMillis());
            int cyear = localDate.getYear();
            JSONArray jsonArray2 = new JSONArray();
            for(int i= 0; i<=5;i++){
                jsonArray2.put(cyear - i);
            }
            spinner1.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);
        }catch (Exception e){

        }
    }
    public static void addVerifyIdentify(JSONObject jsonForm,boolean isIdentify,boolean needVerified, boolean isVerify, String notVerifyText){
        try{
            JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            if(isIdentify){
                JSONObject item = new JSONObject();
                item.put("key","is_identified");
                item.put("value","true");
                item.put("openmrs_data_type","text");
                item.put("type","hidden");
                item.put("openmrs_entity_parent","");
                item.put("openmrs_entity","concept");
                item.put("openmrs_entity_id","is_identified");
                jsonArray.put(item);



            }else if(needVerified){
                JSONObject item1 = new JSONObject();
                item1.put("key","is_verified");
                item1.put("value",isVerify?"true":"false");
                item1.put("openmrs_entity_parent","");
                item1.put("openmrs_entity","concept");
                item1.put("openmrs_data_type","text");
                item1.put("type","hidden");
                item1.put("openmrs_entity_id","is_verified");
                jsonArray.put(item1);
                if(!isVerify){
                    JSONObject item2 = new JSONObject();
                    item2.put("key","not_verify_cause");
                    item2.put("value",notVerifyText);
                    item2.put("openmrs_data_type","text");
                    item2.put("type","hidden");
                    item2.put("openmrs_entity_parent","");
                    item2.put("openmrs_entity","concept");
                    item2.put("openmrs_entity_id","not_verify_cause");
                    jsonArray.put(item2);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void addHeight(JSONObject jsonForm, String height){
        try {
            JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            updateFormField(jsonArray,"height",height);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static void addAddToStockValue(JSONObject jsonForm){
        try {
            JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            if(StockRepository.isEligable()){
                updateFormField(jsonArray,"add_to_stock","5");
            }else{
                updateFormField(jsonArray,"add_to_stock","0");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void addNcdSugerPressure(String baseEntityId, JSONObject jsonForm){
        try {
            String sugervalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(baseEntityId+"_SUGER");
            String pressurevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(baseEntityId+"_PRESSURE");
            if(sugervalue.isEmpty() && pressurevalue.isEmpty()) return;
            Log.v("SUGER_TEST","addNcdSugerPressure>>>sugervalue:"+sugervalue+":pressurevalue:"+pressurevalue);
            JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            if(!TextUtils.isEmpty(sugervalue) && sugervalue.equalsIgnoreCase("yes")){
                updateFormField(jsonArray,"suger_confirm_hospital","হ্যাঁ");
                JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(jsonArray, "suger_confirm_hospital");
                formObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
            }
            if(!TextUtils.isEmpty(pressurevalue) && pressurevalue.equalsIgnoreCase("yes")){
                updateFormField(jsonArray,"pressure_confirm_hospital","হ্যাঁ");
                JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(jsonArray, "pressure_confirm_hospital");
                formObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static void addValueAtJsonForm(JSONObject jsonForm, String key, String value){
        try {
            JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            updateFormField(jsonArray,key,value);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static void addNoOfAnc(JSONObject jsonForm){
        try {


            JSONArray jsonArray2 = new JSONArray();
            for(int i = 1; i<=8;i++ ){
                jsonArray2.put(i);
            }
            JSONArray field = fields(jsonForm, STEP1);
            JSONObject spinner1 = getFieldJSONObject(field, "number_of_anc");

            spinner1.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static void addNoOfPnc(JSONObject jsonForm){
        try {


            JSONArray jsonArray2 = new JSONArray();
            for(int i = 1; i<=4;i++ ){
                jsonArray2.put(i);
            }
            JSONArray field = fields(jsonForm, STEP1);
            JSONObject spinner1 = getFieldJSONObject(field, "number_of_pnc");

            spinner1.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static void addLastAnc(JSONObject jsonForm,String baseEntityId,boolean isReadOnlyView){
        JSONObject stepOne = null;
        try {

            stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            String prevalue = FormApplicability.getVisitCount(baseEntityId, CoreConstants.EventType.ANC_HOME_VISIT);

            /*if(!isReadOnlyView)*/updateFormField(jsonArray, "brac_anc", TextUtils.isEmpty(prevalue)?"0":prevalue);

            int initVal = TextUtils.isEmpty(prevalue)?0:Integer.parseInt(prevalue);
            if(isReadOnlyView){
                initVal = 0;
            }
            JSONArray jsonArray2 = new JSONArray();
            for(int i = initVal+1; i<=8;i++ ){
                jsonArray2.put(i);
            }
            JSONArray field = fields(jsonForm, STEP1);
            JSONObject spinner1 = getFieldJSONObject(field, "number_of_anc");
            JSONObject spinner2 = getFieldJSONObject(field, "other_anc_no_1");
            JSONObject spinner3 = getFieldJSONObject(field, "other_anc_no_2");
            JSONObject spinner4 = getFieldJSONObject(field, "other_anc_no_3");

            spinner1.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);
            spinner2.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);
            spinner3.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);
            spinner4.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static void addLastPnc(JSONObject jsonForm,String baseEntityId,boolean isReadOnlyView){
        JSONObject stepOne = null;
        try {

            stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            String prevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(baseEntityId+"_BRAC_PNC");
//            String preCountvalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(baseEntityId+"_TOTAL_ANC");

            if(!isReadOnlyView) updateFormField(jsonArray, "brac_pnc", TextUtils.isEmpty(prevalue)?"0":prevalue);
//            updateFormField(jsonArray, "anc_count", TextUtils.isEmpty(preCountvalue)?"0":prevalue);

            int initVal = TextUtils.isEmpty(prevalue)?0:Integer.parseInt(prevalue);
            if(isReadOnlyView){
                initVal = 0;
            }
            JSONArray jsonArray2 = new JSONArray();
            for(int i = initVal+1; i<=4;i++ ){
                jsonArray2.put(i);
            }
            JSONArray field = fields(jsonForm, STEP1);
            JSONObject spinner1 = getFieldJSONObject(field, "number_of_pnc");
            JSONObject spinner2 = getFieldJSONObject(field, "other_pnc_no_1");
            JSONObject spinner3 = getFieldJSONObject(field, "other_pnc_no_2");
            JSONObject spinner4 = getFieldJSONObject(field, "other_pnc_no_3");

            spinner1.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);
            spinner2.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);
            spinner3.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);
            spinner4.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray2);

            int pncDay = FormApplicability.getDayPassPregnancyOutcome(baseEntityId);
            String isDelay = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(baseEntityId+"_IS_DELAY");

            if(pncDay>=2 && TextUtils.isEmpty(isDelay)){
                updateFormField(jsonArray, "is_delay","true");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static void addMaritalStatus(JSONObject jsonForm,String maritalStatus){
            JSONObject stepOne = null;
            try {

                stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);

                updateFormField(jsonArray, "marital_status", maritalStatus);



            } catch (JSONException e) {
                e.printStackTrace();
            }

    }
    public static JSONObject updateFormWithModuleId(JSONObject form,String moduleId, String familyBaseEntityId) throws JSONException {
        JSONArray field = fields(form, STEP1);
        JSONObject fingerPrint = getFieldJSONObject(field, "finger_print");
        fingerPrint.put("project_id", HnppConstants.getSimPrintsProjectId());
        fingerPrint.put("user_id",CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());
        fingerPrint.put("module_id",moduleId);
        try{
            String[] familyData = HnppDBUtils.getNameMobileFromFamily(familyBaseEntityId);
            if(familyData.length >0){
                form.put("family_name",familyData[0]);
                form.put("phone_no",familyData[1]);
            }
        }catch (Exception e){

        }

        String entity_id = form.getString("entity_id");
        try {

            if(StringUtils.isEmpty(entity_id)){
                ArrayList<String> womenList = HnppDBUtils.getAllWomenInHouseHold(familyBaseEntityId);
                HnppJsonFormUtils.updateFormWithMotherName(form,womenList,familyBaseEntityId);
            }else{
                ArrayList<String> womenList = HnppDBUtils.getAllWomenInHouseHold(entity_id,familyBaseEntityId);
                HnppJsonFormUtils.updateFormWithMotherName(form,womenList,familyBaseEntityId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        form.put("relational_id", familyBaseEntityId);

        return form;
    }
    public static String[] getHouseholdIdModuleIdFromForm(JSONObject form) throws JSONException {
        String[] dfd = new String[2];
        JSONArray field = fields(form, STEP1);
        JSONObject houseHoldIdObj = getFieldJSONObject(field, "unique_id");
        JSONObject mObj = getFieldJSONObject(field, "module_id");
        dfd[0] = houseHoldIdObj.getString("value");
        dfd[1] = mObj.getString("value");
        return dfd;
    }



    public static JSONObject updateFormWithMemberId(JSONObject form,String houseHoldId, String familyBaseEntityId) throws JSONException {
        JSONArray field = fields(form, STEP1);
        JSONObject memberId = getFieldJSONObject(field, "unique_id");
        if(!TextUtils.isEmpty(houseHoldId)){
            houseHoldId = houseHoldId.replace(Constants.IDENTIFIER.FAMILY_SUFFIX,"")
                    .replace(HnppConstants.IDENTIFIER.FAMILY_TEXT,"");
        }

        int memberCount = HnppApplication.ancRegisterRepository().getMemberCountWithoutRemove(familyBaseEntityId);
        String uniqueId = houseHoldId+memberCountWithZero(memberCount+1);
        Log.v("INVALID_REQ","updateFormWithMemberId>>houseHoldId:"+houseHoldId+":memberCount:"+memberCount+":uniqueId:"+uniqueId);
        HnppConstants.appendLog("INVALID_REQ","updateFormWithMemberId>>houseHoldId:"+houseHoldId+":memberCount:"+memberCount+":uniqueId:"+uniqueId);
        memberId.put(org.smartregister.family.util.JsonFormUtils.VALUE, uniqueId);
        return form;
    }
    public static String getUniqueMemberId(String familyBaseEntityId) {
        String houseHoldId = HnppApplication.ancRegisterRepository().getHouseholdId(familyBaseEntityId);
        int memberCount = HnppApplication.ancRegisterRepository().getMemberCountWithoutRemove(familyBaseEntityId);
        return houseHoldId+memberCountWithZero(memberCount+1);
    }
//    public static String getUniqueGuestMemberId(String villageId) {
//        HouseholdId houseHoldId = HnppApplication.getHNPPInstance().getGuestMemberIdRepository().getNextHouseholdId(villageId);
//        return houseHoldId.getOpenmrsId();
//    }
    public static JSONObject updateFormWithSimPrintsEnable(JSONObject form,String baseEntityId) throws Exception{

        boolean simPrintsEnable = false;
        ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
        if(ssLocationForms.size() > 0){
            simPrintsEnable = ssLocationForms.get(0).simprints_enable;
        }
        JSONArray field = fields(form, STEP1);
        JSONObject simprintObj = getFieldJSONObject(field, SIMPRINTS_ENABLE);
        simprintObj.put(org.smartregister.family.util.JsonFormUtils.VALUE,simPrintsEnable);

        String ssName =HnppDBUtils.getSSNameByHHID(baseEntityId);
        Log.v("SS_NAME","ssName:"+ssName);
        JSONObject ssNameObject = getFieldJSONObject(field, "ss_name");
        ssNameObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, ssName);

        return form;


    }
    public static JSONObject updateChildFormWithMetaData(JSONObject form,String houseHoldId, String familyBaseEntityId) throws JSONException {

        JSONObject lookUpJSONObject = getJSONObject(getJSONObject(form, METADATA), "look_up");
        lookUpJSONObject.put("entity_id","family");
        lookUpJSONObject.put("value",familyBaseEntityId);
        form.put("relational_id", familyBaseEntityId);
        JSONArray field = fields(form, STEP1);
        JSONObject houseHoldIdObject = getFieldJSONObject(field, "house_hold_id");
        houseHoldIdObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, houseHoldId);

        return form;
    }
    public static JSONObject updateFormWithSSName(JSONObject form, ArrayList<SSModel> ssLocationForms) throws Exception{

        JSONArray jsonArray = new JSONArray();
        for(SSModel ssLocationForm : ssLocationForms){
            jsonArray.put(ssLocationForm.username);
        }
        JSONArray field = fields(form, STEP1);
        JSONObject spinner = getFieldJSONObject(field, SS_NAME);

        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
        return form;


    }
    public static JSONObject updateFormWithSSName(JSONObject form, ArrayList<SSModel> ssLocationForms,String defaultValue) throws Exception{

        JSONArray jsonArray = new JSONArray();
        for(SSModel ssLocationForm : ssLocationForms){
            jsonArray.put(ssLocationForm.username);
        }
        JSONArray field = fields(form, STEP1);
        JSONObject spinner = getFieldJSONObject(field, SS_NAME);

        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
        spinner.put(VALUE,defaultValue);
        return form;


    }
    public static JSONObject updateFormWithSSNameOnly(JSONObject form, String singleSSName) throws Exception{

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(singleSSName);
        JSONArray field = fields(form, STEP1);
        JSONObject spinner = getFieldJSONObject(field, SS_NAME);

        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
        JSONObject ssIdsObj = getFieldJSONObject(field, "ss_id");
        ssIdsObj.put(VALUE,singleSSName);
        return form;


    }
    public static JSONObject updateFormWithSSNameAndSelf(JSONObject form, ArrayList<SSModel> ssLocationForms) throws Exception{

        JSONArray jsonArray = new JSONArray();
        for(SSModel ssLocationForm : ssLocationForms){
            jsonArray.put(ssLocationForm.username);

        }
        jsonArray.put("স্বাস্থ্যকর্মী নিজে");
        JSONArray field = fields(form, STEP1);
        JSONObject spinner = getFieldJSONObject(field, SS_NAME);

        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
        return form;


    }
    public static JSONObject updateFormWithVillageName(JSONObject form, String ssName , String villageName) throws Exception{

        JSONArray jsonArray = new JSONArray();
        ArrayList<SSLocations> ssLocations = new ArrayList<>();

        ArrayList<SSModel> modelList = SSLocationHelper.getInstance().getSsModels();
        int ssIndex = -1,villageIndex = -1;
        for(int i = 0; i< modelList.size(); i++){
            SSModel ssModel = modelList.get(i);
            if(ssModel.username.equalsIgnoreCase(ssName)){
                ssLocations = ssModel.locations;
                ssIndex = i;
                break;
            }
        }

        for(int i = 0; i< ssLocations.size(); i++){
            SSLocations ssLocations1 = ssLocations.get(i);
            if(ssLocations1.village.name.equalsIgnoreCase(villageName)){
                villageIndex = i;
            }
            jsonArray.put(ssLocations1.village.name);
        }
        JSONObject step1 = form.getJSONObject(STEP1);
        try{
            step1.put("ss_index", ssIndex);
            step1.put("village_index", villageIndex);
        }
        catch (Exception exception){

        }

        JSONArray field = fields(form, STEP1);
        JSONObject spinner = getFieldJSONObject(field, VILLAGE_NAME);
        getFieldJSONObject(field, VILLAGE_NAME);
        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUE, villageName);
        return form;


    }
    public static JSONObject updateFormWithMotherName(JSONObject form , ArrayList<String> motherNameList,String familyBaseEntityId) throws Exception{

        JSONArray jsonArray = new JSONArray();
        for(String name : motherNameList){
            jsonArray.put(name);
        }
        jsonArray.put("মাতা রেজিস্টার্ড নয়");
        JSONArray field = fields(form, STEP1);
        JSONObject spinner = getFieldJSONObject(field, "mother_name");

        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
        String ssName =HnppDBUtils.getSSNameByHHID(familyBaseEntityId);
        Log.v("SS_NAME","ssName:"+ssName+":familyId:"+familyBaseEntityId);
        JSONObject ssNameObj = getFieldJSONObject(field, "ss_name");
        ssNameObj.put("value",ssName);
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        JSONObject providerIdObj = getFieldJSONObject(field, "provider_id");
        providerIdObj.put("value",userName);
        return form;
    }
    public static void updateProviderIdAtClient(JSONArray field,String familyBaseEntityId) throws Exception{
        String ssName =HnppDBUtils.getSSNameByHHID(familyBaseEntityId);
        Log.v("SS_NAME","ssName:"+ssName+":familyId:"+familyBaseEntityId);
        JSONObject ssNameObj = getFieldJSONObject(field, "ss_name");
        ssNameObj.put("value",ssName);
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        JSONObject providerIdObj = getFieldJSONObject(field, "provider_id");
        providerIdObj.put("value",userName);
    }
    public static JSONObject updateFormWithAllMemberName(JSONObject form , ArrayList<String[]> motherNameList) throws Exception{
        JSONArray field = fields(form, STEP1);
        JSONObject corona_members = getFieldJSONObject(field, "corona_affected_members");
       // JSONObject corona_members_id = getFieldJSONObject(field, "corona_affected_id");

        JSONArray jsonArrayCoronaMember = corona_members.getJSONArray("options");
       // JSONArray jsonArrayCoronaMemberIds = corona_members_id.getJSONArray("options");
        for(String[] optionList : motherNameList){
            String name = optionList[0];
            String ids = optionList[1];
            if(StringUtils.isEmpty(name))continue;

            JSONObject itemWithIds = new JSONObject();
            itemWithIds.put("key",name.replace(" ","_")+"#"+ids);
            itemWithIds.put("text",name);
            itemWithIds.put("value",false);
            itemWithIds.put("openmrs_entity","concept");
            itemWithIds.put("openmrs_entity_id",name.replace(" ","_")+"#"+ids);

            jsonArrayCoronaMember.put(itemWithIds);
//            jsonArrayCoronaMember.put(item);
//
//            JSONObject itemId = new JSONObject();
//            itemId.put("key",ids);
//            itemId.put("text",ids);
//            itemId.put("value",false);
//            itemId.put("openmrs_entity","concept");
//            itemId.put("openmrs_entity_id",ids);
           // jsonArrayCoronaMemberIds.put(itemId);
        }

        return form;


    }
    public static JSONObject getJson(String formName, String baseEntityID) throws Exception {
        String locationId = HnppApplication.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
        JSONObject jsonObject = org.smartregister.chw.anc.util.JsonFormUtils.getFormAsJson(formName);
        org.smartregister.chw.anc.util.JsonFormUtils.getRegistrationForm(jsonObject, baseEntityID, locationId);
        return jsonObject;
    }
    public static JSONObject updateLatitudeLongitude(JSONObject form,double latitude, double longitude) throws JSONException {
        JSONArray field = fields(form, STEP1);
        JSONObject latitude_field = getFieldJSONObject(field, "latitude");
        JSONObject longitude_field = getFieldJSONObject(field, "longitude");
        latitude_field.put(org.smartregister.family.util.JsonFormUtils.VALUE,latitude );
        longitude_field.put(org.smartregister.family.util.JsonFormUtils.VALUE,longitude );
        return form;
    }
    public static JSONObject updateLatitudeLongitudeFamily(JSONObject form,double latitude, double longitude) throws JSONException {
        JSONArray field = fields(form, STEP2);
        JSONObject latitude_field = getFieldJSONObject(field, "latitude");
        JSONObject longitude_field = getFieldJSONObject(field, "longitude");
        latitude_field.put(org.smartregister.family.util.JsonFormUtils.VALUE,latitude );
        longitude_field.put(org.smartregister.family.util.JsonFormUtils.VALUE,longitude );
        return form;
    }
    public static JSONObject updateHhVisitForm(JSONObject form,Map<String,String> details) throws JSONException{
        JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String key = jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY);
            if(key.equalsIgnoreCase("form_name")) continue;
            String value = details.get(key);

            Log.v("HH_VISIT","key:"+key+":value:"+value);

            if(jsonObject.has("openmrs_choice_ids")&&jsonObject.getJSONObject("openmrs_choice_ids").length()>0){
                value = processValueWithChoiceIdsForEdit(jsonObject,value);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,value);
            }else{
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,value);
            }


        }
        return form;
    }

    public static JSONObject getAutoPopulatedJsonEditFormString(String formName, Context context, CommonPersonObjectClient client, String eventType) {
        try {
            JSONObject form = FormUtils.getInstance(context).getFormJson(formName);
            LocationPickerView lpv = new LocationPickerView(context);
            lpv.init();
            Timber.d("Form is %s", form.toString());
            if (form != null) {
                form.put(org.smartregister.family.util.JsonFormUtils.ENTITY_ID, client.getCaseId());
                form.put(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE, eventType);

                JSONObject metadata = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.METADATA);
                String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

                metadata.put(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_LOCATION, lastLocationId);

                form.put(org.smartregister.family.util.JsonFormUtils.CURRENT_OPENSRP_ID, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false));

                //inject opensrp id into the form
                JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    processPopulatableFields(client, jsonObject);

                }

                if(form.has(org.smartregister.family.util.JsonFormUtils.STEP2)) {
                    JSONObject stepTwo = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP2);

                    JSONArray jsonArray2 = stepTwo.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                    for (int i = 0; i < jsonArray2.length(); i++) {
                        JSONObject jsonObject = jsonArray2.getJSONObject(i);

                        processPopulatableFields(client, jsonObject);

                    }
                }


                return form;
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }
    protected static void processPopulatableFields(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {

        switch (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).toLowerCase()) {

            case "firstname":
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),DBConstants.KEY.FIRST_NAME, false));

                break;
            case "village_name":
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),DBConstants.KEY.VILLAGE_TOWN, false));

                break;
            case Constants.JSON_FORM_KEY.DOB:
                getDob(client,jsonObject);
                break;
            case Constants.JSON_FORM_KEY.DOB_UNKNOWN:
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, false);
                JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
                optionsObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), Constants.JSON_FORM_KEY.DOB_UNKNOWN, false));
                break;
            case "ss_name":
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),HnppConstants.KEY.SS_NAME, false));

                break;
            case "serial_no":
                String serialNo = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),HnppConstants.KEY.SERIAL_NO, false);
                if(TextUtils.isEmpty(serialNo)){
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,"H");
                }else{
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,serialNo);
                }
                break;


            case "first_name":
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),DBConstants.KEY.FIRST_NAME, false));
            break;
            case "contact_phone_number":
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),DBConstants.KEY.PHONE_NUMBER, false));

             break;
            case "mother_guardian_first_name_english":
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),HnppConstants.KEY.CHILD_MOTHER_NAME, false));

                break;
            case "mother_name":
                String motherNameAlter = Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED, false);
                if(!TextUtils.isEmpty(motherNameAlter) && motherNameAlter.equalsIgnoreCase("মাতা রেজিস্টার্ড নয়")){
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,motherNameAlter);
                }else{
                    String motherEntityId = Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.MOTHER_ENTITY_ID, false);
                    String relationId = Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
                    String motherName = Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.CHILD_MOTHER_NAME, false);

                    motherName = HnppDBUtils.getMotherName(motherEntityId,relationId,motherName);
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,motherName);
                }

                break;

            case "sex":
                if(jsonObject.has("openmrs_choice_ids")&&jsonObject.getJSONObject("openmrs_choice_ids").length()>0){
                    String value = processValueWithChoiceIdsForEdit(jsonObject,org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),
                            DBConstants.KEY.GENDER, false));
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,value);
                }else{
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                            org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),DBConstants.KEY.GENDER, false));
                }


                break;
            case "id_avail":
                if(jsonObject.has("options")){
                    String value = processValueWithChoiceIdsForEdit(jsonObject,org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),
                            "id_avail", false));
                    if(StringUtils.isEmpty(value)){
                        jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,new JSONArray());
                    }else{
                        jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,new JSONArray(value));
                    }


                }
                break;
            default:
                if(jsonObject.has("openmrs_choice_ids")&&jsonObject.getJSONObject("openmrs_choice_ids").length()>0){
                    String value = processValueWithChoiceIdsForEdit(jsonObject,org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),
                            jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY), false));
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,value);
                }else{
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                            org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),
                                    jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY), false));
                }



                break;
        }
    }
    private static void getDob(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {
        String dobString = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
        if (StringUtils.isNotBlank(dobString)) {
            Date dob = org.smartregister.chw.core.utils.Utils.dobStringToDate(dobString);
            if (dob != null) {
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, dd_MM_yyyy.format(dob));
            }
        }
    }
    public static FamilyEventClient processFamilyMemberForm(AllSharedPreferences allSharedPreferences, String jsonString, String familyBaseEntityId, String encounterType) {
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);
            if (!(Boolean)registrationFormParams.getLeft()) {
                return null;
            } else {
                JSONObject jsonForm = (JSONObject)registrationFormParams.getMiddle();
                JSONArray fields = (JSONArray)registrationFormParams.getRight();
                String familyId = getString(jsonForm, "relational_id");
                String entityId = getString(jsonForm, "entity_id");
                if (StringUtils.isBlank(entityId)) {
                    entityId = generateRandomUUIDString();
                }
                HnppConstants.appendLog("INVALID_REQ","processFamilyMemberForm entity_id:"+entityId+":familyId:"+familyId);

                lastInteractedWith(fields);
                dobEstimatedUpdateFromAge(fields);
                String motherEntityId = updateMotherName(fields,familyId);

                FormTag formTag = formTag(allSharedPreferences);
                formTag.appVersionName = BuildConfig.VERSION_NAME;
                Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields, formTag, entityId);
                if (baseClient != null && !baseClient.getBaseEntityId().equals(familyBaseEntityId)) {
                    baseClient.addRelationship(Utils.metadata().familyMemberRegister.familyRelationKey, familyBaseEntityId);
                }

                Context context = HnppApplication.getInstance().getContext().applicationContext();
                addRelationship(context, motherEntityId,familyId, baseClient);
                Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, "metadata"), formTag, entityId, encounterType, Utils.metadata().familyMemberRegister.tableName);
                tagSyncMetadata(allSharedPreferences, baseEvent);

                String entity_id = baseClient.getBaseEntityId();
                updateFormSubmissionID(encounterType,entity_id,baseEvent);

                return new FamilyEventClient(baseClient, baseEvent);
            }
        } catch (Exception var10) {
            Timber.e(var10);
            return null;
        }
    }




    public static void dobEstimatedUpdateFromAge(JSONArray fields) {
        try {
            JSONObject dobUnknownObject = getFieldJSONObject(fields, "is_birthday_known");
            String dobUnKnownString = dobUnknownObject != null ? dobUnknownObject.getString("value") : null;
            if (StringUtils.isNotBlank(dobUnKnownString) && dobUnKnownString.equalsIgnoreCase("না")) {
                String ageString = getFieldValue(fields, "estimated_age");
                if (StringUtils.isNotBlank(ageString) && NumberUtils.isNumber(ageString)) {
                    int age = Integer.valueOf(ageString);
                    JSONObject dobJSONObject = getFieldJSONObject(fields, "dob");
                    dobJSONObject.put("value", getDobWithToday(age));
                }
            }
        } catch (JSONException var9) {
            Timber.e(var9);
        }
        processAttributesWithChoiceIDsForSave(fields);
    }
    public static void addConsent(JSONArray fields,boolean isConsent){
        try{
            JSONObject isConsentObj = getFieldJSONObject(fields, "is_consent");
            isConsentObj.put("value",isConsent?"1":"0");
            String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
            Log.v("USER_NAME","addConsent>>>userName:"+userName);
            JSONObject providerIdObj = getFieldJSONObject(fields, "provider_id");
            providerIdObj.put("value",userName);
        }catch (Exception e){

        }
    }
    public static String getDobWithToday(int age) {
        Calendar cal = Calendar.getInstance();
        if (age > 0)
            cal.add(Calendar.YEAR, -age);
        return DatePickerFactory.DATE_FORMAT_LOCALE_INDEPENDENT.format(cal.getTime());

    }
    public static JSONArray processAttributesWithChoiceIDsForSave(JSONArray fields) {
        for (int i = 0; i < fields.length(); i++) {
            try {
                JSONObject fieldObject = fields.getJSONObject(i);
//                if(fieldObject.has("openmrs_entity")){
//                    if(fieldObject.getString("openmrs_entity").equalsIgnoreCase("person_attribute")){
                if (fieldObject.has("openmrs_choice_ids")&&fieldObject.getJSONObject("openmrs_choice_ids").length()>0) {
                    if (fieldObject.has("value")) {
                        String valueEntered = fieldObject.getString("value");
                        fieldObject.put("value", fieldObject.getJSONObject("openmrs_choice_ids").get(valueEntered));
                    }
                }
//                    }
//                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
        return fields;
    }

    private static String processValueWithChoiceIdsForEdit(JSONObject jsonObject, String value) {
        try {
            //spinner
            if (jsonObject.has("openmrs_choice_ids")) {
                JSONObject choiceObject = jsonObject.getJSONObject("openmrs_choice_ids");

                for (int i = 0; i < choiceObject.names().length(); i++) {
                    if (value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))) {
                        value = choiceObject.names().getString(i);
                        return value;
                    }
                }
            }else if (jsonObject.has("options")) {
                JSONArray option_array = jsonObject.getJSONArray("options");
                for (int i = 0; i < option_array.length(); i++) {
                    JSONObject option = option_array.getJSONObject(i);
                    if (value.contains(option.optString("key"))) {
                        option.put("value", "true");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    public static String memberCountWithZero(int count){
        return count<10 ? "0"+count : String.valueOf(count);
    }


    public static JSONObject getFormAsJson(JSONObject form,
                                                      String formName, String id,
                                                      String currentLocationId) throws Exception {
        if (form == null) {
            return null;
        }

        String entityId = id;
        form.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);

        if (Utils.metadata().familyRegister.formName.equals(formName) || Utils.metadata().familyMemberRegister.formName.equals(formName)) {
            if (StringUtils.isNotBlank(entityId)) {
                entityId = entityId.replace("-", "");
            }

            JSONArray field = fields(form, STEP1);
            JSONObject uniqueId = getFieldJSONObject(field, Constants.JSON_FORM_KEY.UNIQUE_ID);

            if (formName.equals(Utils.metadata().familyRegister.formName)) {
                if (uniqueId != null) {
                    uniqueId.remove(org.smartregister.family.util.JsonFormUtils.VALUE);
                   uniqueId.put(org.smartregister.family.util.JsonFormUtils.VALUE, entityId);
                }

                // Inject opensrp id into the form
                field = fields(form, STEP2);
                uniqueId = getFieldJSONObject(field, Constants.JSON_FORM_KEY.UNIQUE_ID);
                if (uniqueId != null) {
                    uniqueId.remove(org.smartregister.family.util.JsonFormUtils.VALUE);
                    uniqueId.put(org.smartregister.family.util.JsonFormUtils.VALUE, entityId);
                }
            } else {
                if (uniqueId != null) {
                    uniqueId.remove(org.smartregister.family.util.JsonFormUtils.VALUE);
                    uniqueId.put(org.smartregister.family.util.JsonFormUtils.VALUE, entityId);
                }
            }

            org.smartregister.family.util.JsonFormUtils.addLocHierarchyQuestions(form);

        } else {
            Timber.w("Unsupported form requested for launch " + formName);
        }
        Timber.d("form is " + form.toString());
        return form;
    }

    public static Pair<Client, Event> processChildRegistrationForm(AllSharedPreferences allSharedPreferences, String jsonString) {
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

            if (!registrationFormParams.getLeft()) {
                return null;
            }
            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();
            String entityId = getString(jsonForm, ENTITY_ID);
            String familyId = getString(jsonForm, "relational_id");
            if (StringUtils.isBlank(entityId)) {
                entityId = generateRandomUUIDString();
            }
            String motherEntityId = updateMotherName(fields,familyId);
            lastInteractedWith(fields);
            dobUnknownUpdateFromAge(fields);
            processAttributesWithChoiceIDsForSave(fields);
            FormTag formTag = formTag(allSharedPreferences);
            formTag.appVersionName = BuildConfig.VERSION_NAME;
            Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields,formTag , entityId);
            Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, METADATA), formTag, entityId, getString(jsonForm, ENCOUNTER_TYPE), CoreConstants.TABLE_NAME.CHILD);
            tagSyncMetadata(allSharedPreferences, baseEvent);
            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
            String entity_id = baseClient.getBaseEntityId();
            updateFormSubmissionID(encounterType,entity_id,baseEvent);
            JSONObject lookUpJSONObject = getJSONObject(getJSONObject(jsonForm, METADATA), "look_up");
            String lookUpEntityId = "";
            String lookUpBaseEntityId = "";
            if (lookUpJSONObject != null) {
                lookUpEntityId = getString(lookUpJSONObject, "entity_id");
                lookUpBaseEntityId = getString(lookUpJSONObject, "value");
            }
            if ("family".equals(lookUpEntityId) && StringUtils.isNotBlank(lookUpBaseEntityId)) {
                Context context = HnppApplication.getInstance().getContext().applicationContext();
                addRelationship(context, motherEntityId,lookUpBaseEntityId, baseClient);
                SQLiteDatabase db = HnppApplication.getInstance().getRepository().getReadableDatabase();
                HnppChwRepository pathRepository = new HnppChwRepository(context, HnppApplication.getInstance().getContext());
                EventClientRepository eventClientRepository = new EventClientRepository(pathRepository);
                JSONObject clientjson = eventClientRepository.getClient(db, lookUpBaseEntityId);
                baseClient.setAddresses(updateWithSSLocation(clientjson));
            }
            if(baseClient.getAddresses().size() == 0 || TextUtils.isEmpty(lookUpBaseEntityId))
            {
                return null;
            }

            return Pair.create(baseClient, baseEvent);
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }
    public static void addRelationship(Context context, String motherEntityId, String familyId, Client child) {
        try {
            String relationships = AssetHandler.readFileFromAssetsFolder(FormUtils.ecClientRelationships, context);
            JSONArray jsonArray = null;

            jsonArray = new JSONArray(relationships);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject rObject = jsonArray.getJSONObject(i);
                if (rObject.has("field") && getString(rObject, "field").equals(ENTITY_ID)) {
                    if(rObject.getString("client_relationship").equalsIgnoreCase("family")){
                        child.addRelationship(rObject.getString("client_relationship"), familyId);

                    }
                    else if(rObject.getString("client_relationship").equalsIgnoreCase("mother")){
                        child.addRelationship(rObject.getString("client_relationship"), motherEntityId);

                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }
    private static String updateMotherName(JSONArray fields , String familyId) throws Exception{
        JSONObject motherObj = getFieldJSONObject(fields, HnppConstants.KEY.CHILD_MOTHER_NAME);
        JSONObject motherAlterObj = getFieldJSONObject(fields, HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED);
        boolean isVisible = motherObj.optBoolean("is_visible",false);
        if(!isVisible){
            String motherNameSelected = motherAlterObj.optString(VALUE);
            if(!TextUtils.isEmpty(motherNameSelected) && !motherNameSelected.equalsIgnoreCase("মাতা রেজিস্টার্ড নয়")){
                motherObj.put(VALUE,motherNameSelected);
            }

        }
        String motherName = motherObj.optString(VALUE);
        if(!TextUtils.isEmpty(motherName))return HnppDBUtils.getMotherBaseEntityId(familyId,motherName);
        return "";


    }

    private static List<Address> updateWithSSLocation(JSONObject clientjson){
        try{
            String addessJson = clientjson.getString("addresses");
            JSONArray jsonArray = new JSONArray(addessJson);
            List<Address> listAddress = new ArrayList<>();
            for(int i = 0; i <jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Address address = new Gson().fromJson(jsonObject.toString(), Address.class);
                listAddress.add(address);
            }
            return listAddress;
        }catch (Exception e){

        }
        return new ArrayList<>();

    }
    public static FamilyEventClient processFamilyUpdateForm(AllSharedPreferences allSharedPreferences, String jsonString) {
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);
            if (!(Boolean)registrationFormParams.getLeft()) {
                return null;
            } else {
                JSONObject jsonForm = (JSONObject)registrationFormParams.getMiddle();
                JSONArray fields = (JSONArray)registrationFormParams.getRight();
                String entityId = getString(jsonForm, "entity_id");
                if (StringUtils.isBlank(entityId)) {
                    entityId = generateRandomUUIDString();
                }

                lastInteractedWith(fields);
                dobUnknownUpdateFromAge(fields);
                FormTag formTag = formTag(allSharedPreferences);
                formTag.appVersionName = BuildConfig.VERSION_NAME;
                Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields, formTag, entityId);
                baseClient.setLastName("Family");
                baseClient.setBirthdate(new Date(0L));
                baseClient.setGender("Male");
                Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, "metadata"), formTag, entityId, Utils.metadata().familyRegister.registerEventType, Utils.metadata().familyRegister.tableName);
                tagSyncMetadata(allSharedPreferences, baseEvent);

                String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
                String entity_id = baseClient.getBaseEntityId();
                updateFormSubmissionID(encounterType,entity_id,baseEvent);
                return new FamilyEventClient(baseClient, baseEvent);
            }
        } catch (Exception var8) {
            Timber.e(var8);
            return null;
        }
    }

    public static void updateFormSubmissionID(String encounterType, String entity_id, Event baseEvent) throws JSONException{
        String formSubmissionID = "";

        EventClientRepository eventClientRepository = HnppApplication.getHNPPInstance().getEventClientRepository();
        JSONObject evenjsonobject = eventClientRepository.getEventsByBaseEntityIdAndEventType(entity_id, encounterType);
        if (evenjsonobject == null) {
            if (encounterType.contains("Update")) {
                evenjsonobject = eventClientRepository.getEventsByBaseEntityIdAndEventType(entity_id, encounterType.replace("Update", "").trim());
            }
        }
        if (evenjsonobject != null) {
            formSubmissionID = evenjsonobject.getString("formSubmissionId");
        }
        if (!isBlank(formSubmissionID)) {
            baseEvent.setFormSubmissionId(formSubmissionID);
        }
    }


    public static boolean updateClientStatusAsEvent(Context context,String baseEntityId, String attributeName, Object attributeValue, String entityType, String eventType) {
        try {

            ECSyncHelper syncHelper = HnppApplication.getHNPPInstance().getEcSyncHelper();


            Date date = new Date();
            EventClientRepository db = FamilyLibrary.getInstance().context().getEventClientRepository();


            JSONObject client = db.getClientByBaseEntityId(baseEntityId);
            AllSharedPreferences allSharedPreferences = GrowthMonitoringLibrary.getInstance().context()
                    .allSharedPreferences();

            Event event = (Event) new Event()
                    .withBaseEntityId(baseEntityId)
                    .withEventDate(new Date())
                    .withEventType(eventType)
                    .withLocationId(allSharedPreferences.fetchCurrentLocality())
                    .withProviderId(allSharedPreferences.fetchRegisteredANM())
                    .withEntityType(entityType)
                    .withFormSubmissionId(org.smartregister.family.util.JsonFormUtils.generateRandomUUIDString())
                    .withDateCreated(new Date());
            event.addObs((new Obs()).withFormSubmissionField(attributeName).withValue(attributeValue).withFieldCode(attributeName).withFieldType("formsubmissionField").withFieldDataType("text").withParentCode("").withHumanReadableValues(new ArrayList<Object>()));


            addMetaData(context, event, date);
            JSONObject eventJson = new JSONObject(org.smartregister.family.util.JsonFormUtils.gson.toJson(event));
            db.addEvent(baseEntityId, eventJson);
            long lastSyncTimeStamp = allSharedPreferences.fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            FamilyLibrary.getInstance().getClientProcessorForJava().getInstance(context).processClient(syncHelper.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
            allSharedPreferences.saveLastUpdatedAtDate(lastSyncDate.getTime());

            return true;


        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    public static Event addMetaData(Context context, Event event, Date start) throws JSONException {
        SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        Map<String, String> metaFields = new HashMap<>();
        metaFields.put("deviceid", "163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        metaFields.put("end", "163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        metaFields.put("start", "163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        Calendar calendar = Calendar.getInstance();

        String end = DATE_TIME_FORMAT.format(calendar.getTime());

        Obs obs = new Obs();
        obs.setFieldCode("163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        obs.setValue(DATE_TIME_FORMAT.format(start));
        obs.setFieldType("concept");
        obs.setFieldDataType("start");
        event.addObs(obs);


        obs.setFieldCode("163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        obs.setValue(end);
        obs.setFieldDataType("end");
        event.addObs(obs);

        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        obs.setFieldCode("163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        obs.setValue("");
        obs.setFieldDataType("deviceid");
        event.addObs(obs);
        return event;
    }



    /**
     * setting encounter type from end date
     * @param form is a json object
     */
    public static void setEncounterDateTime(JSONObject form){
        try {
            if(!form.getJSONObject("metadata").getJSONObject("end").getString("value").isEmpty()){
                form.getJSONObject("metadata").getJSONObject("today").put("value",form.getJSONObject("metadata").getJSONObject("end").getString("value"));
            }else {
                form.getJSONObject("metadata").getJSONObject("today").put("value","");
            }

            Log.v("DATEEEE",""+form.getJSONObject("metadata").getJSONObject("today").getString("value"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
