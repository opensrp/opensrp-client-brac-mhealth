package org.smartregister.unicef.dghs.utils;

import android.content.Context;
import android.content.res.Resources;
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

import org.smartregister.chw.core.domain.FamilyMember;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.location.CampModel;
import org.smartregister.unicef.dghs.location.HALocation;
import org.smartregister.unicef.dghs.location.HALocationHelper;
import org.smartregister.unicef.dghs.location.WardLocation;
import org.smartregister.unicef.dghs.model.ForumDetails;
import org.smartregister.unicef.dghs.model.GlobalLocationModel;
import org.smartregister.unicef.dghs.repository.GlobalLocationRepository;
import org.smartregister.unicef.dghs.repository.HnppChwRepository;
import org.smartregister.unicef.dghs.repository.HnppVisitLogRepository;
import org.smartregister.unicef.dghs.repository.StockRepository;
import org.smartregister.unicef.dghs.sync.FormParser;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;
import org.smartregister.util.LangUtils;
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

/**
 * Created by keyman on 13/11/2018.
 */
public class HnppJsonFormUtils extends CoreJsonFormUtils {
    public static final String METADATA = "metadata";
    public static final String WARD_NAME = "ward_name";
    public static final String UNION_ZONE = "union_zone";
    public static final String BLOCK_NAME = "block_name";
    public static final String BLOCK_ID = "block_id";
    public static final String CHAMP_TYPE = "camp_type";
    public static final String SIMPRINTS_ENABLE = "simprints_enable";
    public static final String VILLAGE_NAME = "village_name";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final String TITLE = "title";
    public static String[] monthStr = {"January","February","March","April","May","June","July","August","September","October","November","December"};

    public static String[] monthBanglaStr = {"জানুয়ারী","ফেব্রুয়ারী","মার্চ","এপ্রিল","মে","জুন","জুলাই","আগস্ট","সেপ্টেম্বর","অক্টোবর","নভেম্বর","ডিসেম্বর"};
    public static final String REFEREL_EVENT_TYPE = "Referral Clinic";
    public static Pair<List<Client>, List<Event>> processFamilyUpdateRelations(HnppApplication HnppApplication, Context context, FamilyMember familyMember, String lastLocationId) throws Exception {
        List<Client> clients = new ArrayList<>();
        List<Event> events = new ArrayList<>();


        ECSyncHelper syncHelper = HnppApplication.getEcSyncHelper();
        JSONObject clientObject = syncHelper.getClient(familyMember.getFamilyID());
        Client familyClient = syncHelper.convert(clientObject, Client.class);
        if (familyClient == null) {
            String birthDate = clientObject.getString("birthdate");
            if (StringUtils.isNotBlank(birthDate)) {
                birthDate = birthDate.replace("-00:44:30", getTimeZone());
                clientObject.put("birthdate", birthDate);
            }

            familyClient = syncHelper.convert(clientObject, Client.class);
        }

        Map<String, List<String>> relationships = familyClient.getRelationships();

        if (familyMember.getPrimaryCareGiver()) {
            relationships.put(CoreConstants.RELATIONSHIP.PRIMARY_CAREGIVER, toStringList(familyMember.getMemberID()));
            familyClient.setRelationships(relationships);
        }

        if (familyMember.getFamilyHead()) {
            relationships.put(CoreConstants.RELATIONSHIP.FAMILY_HEAD, toStringList(familyMember.getMemberID()));
            familyClient.setRelationships(relationships);
        }

        clients.add(familyClient);


        JSONObject metadata = HnppJsonFormUtils.getJsonObject(org.smartregister.chw.core.utils.Utils.metadata().familyRegister.formName)
                .getJSONObject(org.smartregister.family.util.JsonFormUtils.METADATA);

        metadata.put(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_LOCATION, lastLocationId);

        FormTag formTag = new FormTag();
        formTag.providerId = org.smartregister.chw.core.utils.Utils.context().allSharedPreferences().fetchRegisteredANM();
        formTag.appVersion = FamilyLibrary.getInstance().getApplicationVersion();
        formTag.databaseVersion = FamilyLibrary.getInstance().getDatabaseVersion();

        Event eventFamily = createEvent(new JSONArray(), metadata, formTag, familyMember.getFamilyID(),
                CoreConstants.EventType.UPDATE_FAMILY_RELATIONS, org.smartregister.chw.core.utils.Utils.metadata().familyRegister.tableName);
        tagSyncMetadata(org.smartregister.chw.core.utils.Utils.context().allSharedPreferences(), eventFamily);


        Event eventMember = createEvent(new JSONArray(), metadata, formTag, familyMember.getMemberID(),
                CoreConstants.EventType.UPDATE_FAMILY_MEMBER_RELATIONS,
                org.smartregister.chw.core.utils.Utils.metadata().familyMemberRegister.tableName);
        tagSyncMetadata(org.smartregister.chw.core.utils.Utils.context().allSharedPreferences(), eventMember);

        eventMember.addObs(new Obs("concept", "text", CoreConstants.FORM_CONSTANTS.CHANGE_CARE_GIVER.PHONE_NUMBER.CODE, "",
                toList(familyMember.getPhone()), new ArrayList<>(), null, DBConstants.KEY.PHONE_NUMBER));

        eventMember.addObs(new Obs("concept", "text", CoreConstants.FORM_CONSTANTS.CHANGE_CARE_GIVER.OTHER_PHONE_NUMBER.CODE, CoreConstants.FORM_CONSTANTS.CHANGE_CARE_GIVER.OTHER_PHONE_NUMBER.PARENT_CODE,
                toList(familyMember.getOtherPhone()), new ArrayList<>(), null, DBConstants.KEY.OTHER_PHONE_NUMBER));

        eventMember.addObs(new Obs("concept", "text", CoreConstants.FORM_CONSTANTS.CHANGE_CARE_GIVER.HIGHEST_EDU_LEVEL.CODE, "",
                toList(getEducationLevels(context).get(familyMember.getEduLevel())), toList(familyMember.getEduLevel()), null, DBConstants.KEY.HIGHEST_EDU_LEVEL));


        events.add(eventFamily);
        events.add(eventMember);

        return Pair.create(clients, events);
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
                    .withFormSubmissionId(JsonFormUtils.generateRandomUUIDString())
                    .withDateCreated(new Date());
            event.addObs((new Obs()).withFormSubmissionField(attributeName).withValue(attributeValue).withFieldCode(attributeName).withFieldType("formsubmissionField").withFieldDataType("text").withParentCode("").withHumanReadableValues(new ArrayList<Object>()));


            addMetaData(context, event, date);
            JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(event));
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

    public static synchronized Visit saveVisit(String memberID, String encounterType,
                            final Map<String, String> jsonString,String formSubmissionId,String visitId, String rawJson) throws Exception {
        Log.v("SAVE_VISIT","saveVisit>>");
        if(!FormApplicability.isDueAnyForm(memberID,encounterType)){
            //passing emptyVisit object with zero id
            //this will trigger when visit already exist
            Visit emptyVisit = new Visit();
            emptyVisit.setVisitId("0");
            return emptyVisit;
        }

        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();

        //String derivedEncounterType = StringUtils.isBlank(parentEventType) ? encounterType : "";
        //Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processVisitJsonForm(allSharedPreferences, memberID, derivedEncounterType, jsonString, getTableName());
        //
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString.get("First"));
        JSONObject jsonForm = (JSONObject)registrationFormParams.getMiddle();
        JSONArray fields = (JSONArray)registrationFormParams.getRight();

        Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, "metadata"), formTag(allSharedPreferences), memberID,encounterType,getTableName());
        //save identifier
        String blockId = org.smartregister.util.JsonFormUtils.getFieldValue(fields,BLOCK_ID);
        if(TextUtils.isEmpty(blockId)){
            BaseLocation blocks =HnppDBUtils.getBlocksHHID(memberID);
            blockId = blocks.id+"";
            if(blockId.isEmpty() || blockId.equals("0")){
                blockId =  HnppDBUtils.getBlocksIdFromMember(memberID);
            }
        }


        Log.v("SAVE_VISIT","blockId>>>"+blockId);
        HALocation selectedLocation = HnppApplication.getHALocationRepository().getLocationByBlock(blockId);
        baseEvent.setIdentifiers(HALocationHelper.getInstance().getGeoIdentifier(selectedLocation));
        baseEvent.setFormSubmissionId(formSubmissionId);
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
            case  HnppConstants.EVENT_TYPE.PNC_REGISTRATION:
                return HnppConstants.EVENT_TYPE.PNC_REGISTRATION;
            case  HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY:
                return HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY;
            case  HnppConstants.EVENT_TYPE.MEMBER_PROFILE_VISIT:
                return HnppConstants.EVENT_TYPE.MEMBER_PROFILE_VISIT;
            case  HnppConstants.EVENT_TYPE.CHILD_PROFILE_VISIT:
                return HnppConstants.EVENT_TYPE.CHILD_PROFILE_VISIT;
            case  HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP:
                return HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP;
            case  HnppConstants.EVENT_TYPE.NEW_BORN_PNC_1_4:
                return HnppConstants.EVENT_TYPE.NEW_BORN_PNC_1_4;
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
            case  HnppConstants.EVENT_TYPE.CHILD_INFO_7_24_MONTHS:
                return HnppConstants.EVENT_TYPE.CHILD_INFO_7_24_MONTHS;
            case  HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS:
                return HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS;
            case  HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12:
                return HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12;
            case  HnppConstants.EVENT_TYPE.CHILD_DISEASE:
                return HnppConstants.EVENT_TYPE.CHILD_DISEASE;
            case  HnppConstants.EVENT_TYPE.MEMBER_DISEASE:
                return HnppConstants.EVENT_TYPE.MEMBER_DISEASE;
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
        if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC_VISIT_FORM)
                ||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC_VISIT_FORM_OOC)){
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

    public static void addGender(JSONObject jsonForm,String gender){
        try{
            JSONArray field = fields(jsonForm, STEP1);
            JSONObject genderField = getFieldJSONObject(field, "gender");
            genderField.put(org.smartregister.family.util.JsonFormUtils.VALUE,gender);
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
    public static JSONObject updateFormWithChampType(JSONObject form, JSONArray campTypeArr) {
        try {
            JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            JSONObject camp_types = getFieldJSONObject(jsonArray, "camp_type");
            camp_types.put(org.smartregister.family.util.JsonFormUtils.VALUES,campTypeArr);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return form;
    }
    public static JSONObject updateFormWithDivision(JSONObject form, JSONArray divisionList) {
        try {
            JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            JSONObject camp_types = getFieldJSONObject(jsonArray, "division_per");
            camp_types.put(org.smartregister.family.util.JsonFormUtils.VALUES,divisionList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return form;
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
                updateFormField(jsonArray,"suger_confirm_hospital",HnppApplication.appContext.getString(R.string.yes));
                JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(jsonArray, "suger_confirm_hospital");
                formObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
            }
            if(!TextUtils.isEmpty(pressurevalue) && pressurevalue.equalsIgnoreCase("yes")){
                updateFormField(jsonArray,"pressure_confirm_hospital",HnppApplication.appContext.getString(R.string.year));
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
    public static void changeFormTitle(JSONObject jsonForm, String value){
        try {
            JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            stepOne.put("title",value);

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
    public static JSONObject updateFormWithChampType(JSONObject form, String moduleId, String familyBaseEntityId) throws JSONException {
        try{
            String[] familyData = HnppDBUtils.getNameMobileFromFamily(familyBaseEntityId);
            if(familyData.length >0){
                form.put("first_name",familyData[0]);
                form.put("last_name",familyData[1]);
                form.put("phone_no",familyData[2]);
            }
        }catch (Exception e){

        }
//
//        String entity_id = form.getString("entity_id");
//        try {
//
//            if(StringUtils.isEmpty(entity_id)){
//                ArrayList<String> womenList = HnppDBUtils.getAllWomenInHouseHold(familyBaseEntityId);
//                HnppJsonFormUtils.updateFormWithMotherName(form,womenList,familyBaseEntityId);
//            }else{
//                ArrayList<String> womenList = HnppDBUtils.getAllWomenInHouseHold(entity_id,familyBaseEntityId);
//                HnppJsonFormUtils.updateFormWithMotherName(form,womenList,familyBaseEntityId);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        JSONArray field = fields(form, STEP1);

        String champtype =HnppDBUtils.getChampType(familyBaseEntityId);
        Log.v("champtype","champtype:"+champtype);
        JSONObject champtypeObject = getFieldJSONObject(field, CHAMP_TYPE);
        champtypeObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, champtype);
        form.put("relational_id", familyBaseEntityId);

        return form;
    }
    public static String getHouseholdIdFromForm(JSONObject form) throws JSONException {
        JSONArray field = fields(form, STEP1);
        JSONObject houseHoldIdObj = getFieldJSONObject(field, "unique_id");
        return houseHoldIdObj.getString("value");
    }



    public static JSONObject updateFormWithMemberId(JSONObject form,String houseHoldId, String familyBaseEntityId) throws JSONException {
        JSONArray field = fields(form, STEP1);
        JSONObject memberId = getFieldJSONObject(field, "unique_id");
//        if(!TextUtils.isEmpty(houseHoldId)){
//            houseHoldId = houseHoldId.replace(Constants.IDENTIFIER.FAMILY_SUFFIX,"")
//                    .replace(HnppConstants.IDENTIFIER.FAMILY_TEXT,"");
//        }

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
    public static JSONObject updateFormWithBlockInformation(JSONObject form,String baseEntityId) throws Exception{


        JSONArray field = fields(form, STEP1);

        BaseLocation blocks =HnppDBUtils.getBlocksHHID(baseEntityId);
        Log.v("BLOCK_NAME","BLOCK_NAME:"+blocks.name+":"+blocks.id);
        JSONObject blockNameObject = getFieldJSONObject(field, BLOCK_NAME);
        blockNameObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, blocks.name);
        JSONObject ssIdObject = getFieldJSONObject(field, BLOCK_ID);
        ssIdObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, blocks.id);
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

        String champtype =HnppDBUtils.getChampType(familyBaseEntityId);
        Log.v("champtype","champtype:"+champtype);
        JSONObject champtypeObject = getFieldJSONObject(field, CHAMP_TYPE);
        champtypeObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, champtype);

        return form;
    }
    public static JSONObject updateFormWithUnionName(JSONObject form, ArrayList<WardLocation> geoLocations) throws Exception{

        JSONArray jsonArray = new JSONArray();
        for(WardLocation geoLocation : geoLocations){
            jsonArray.put(geoLocation.ward.name);
        }
        JSONArray field = fields(form, STEP1);
        JSONObject spinner = getFieldJSONObject(field, UNION_ZONE);
        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);

        JSONArray campJsonArray = new JSONArray();
        ArrayList<CampModel> campModels = HnppApplication.getCampRepository().getAllCamp();
        for (CampModel campModel:campModels){
            campJsonArray.put(campModel.type+","+campModel.centerName);
        }
        JSONArray divJsonArray = new JSONArray();
        ArrayList<GlobalLocationModel> divModels = HnppApplication.getGlobalLocationRepository().getLocationByTagId(GlobalLocationRepository.LOCATION_TAG.DIVISION.getValue());
        for (GlobalLocationModel globalLocationModel:divModels){
            divJsonArray.put(globalLocationModel.name);
        }
        updateFormWithChampType(form,campJsonArray);
        updateFormWithDivision(form,divJsonArray);
        return form;
    }

    public static JSONObject updateFormWithWardBlockName(JSONObject form,String blockName, String blockId) throws Exception{

        Log.v("GEO_LOCATION","updateFormWithWardBlockName>blockName:"+blockName+":blockId:"+blockId);
        JSONArray jsonArray = new JSONArray();

        HALocation blockListByWard = HnppApplication.getHALocationRepository().getLocationByBlock(blockId);
        jsonArray.put(blockListByWard.block.name);
        JSONObject step1 = form.getJSONObject(STEP1);
        try{
            step1.put("block_id", blockId);
        }
        catch (Exception exception){

        }

        JSONArray field = fields(form, STEP1);
        JSONObject spinner = getFieldJSONObject(field, BLOCK_NAME);
        getFieldJSONObject(field, BLOCK_NAME);
        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUES,jsonArray);
        spinner.put(org.smartregister.family.util.JsonFormUtils.VALUE, blockName);
        return form;


    }
    public static JSONObject updateFormWithMotherName(JSONObject form , String motherNameEnglish,String motherNameBangla,String motherBaseEntityId,String familyBaseEntityId, String mobileNo) throws Exception{

        JSONArray field = fields(form, STEP1);

        JSONObject blockNameObj = getFieldJSONObject(field, "mother_name_english");
        blockNameObj.put("value",motherNameEnglish);
        try{
            JSONObject blockIdObj = getFieldJSONObject(field, "mother_name_bangla");
            blockIdObj.put("value",motherNameBangla);
        }catch (Exception e){

        }
        JSONObject phoneNoObj = getFieldJSONObject(field, "phone_number");
        phoneNoObj.put("value",mobileNo);

        JSONObject motherIdObj = getFieldJSONObject(field, "mother_id");
        motherIdObj.put("value",motherBaseEntityId);

        JSONObject metaDataJson = form.getJSONObject("metadata");
        JSONObject lookup = metaDataJson.getJSONObject("look_up");
        lookup.put("entity_id", "mother");
        lookup.put("value", motherBaseEntityId);

        lookup.put("entity_id", "family");
        lookup.put("value", familyBaseEntityId);
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        JSONObject providerIdObj = getFieldJSONObject(field, "provider_id");
        providerIdObj.put("value",userName);
        return form;
    }
    public static JSONObject updateFormWithBlockInfo(JSONObject form ,String familyBaseEntityId) throws Exception{

        JSONArray field = fields(form, STEP1);
        BaseLocation blocks =HnppDBUtils.getBlocksHHID(familyBaseEntityId);
        Log.v("SS_NAME","updateFormWithMotherName:"+blocks.name+":blockId:"+blocks.id);
        JSONObject blockNameObj = getFieldJSONObject(field, BLOCK_NAME);
        blockNameObj.put("value",blocks.name);
        try{
            JSONObject blockIdObj = getFieldJSONObject(field, BLOCK_ID);
            blockIdObj.put("value",blocks.id);
        }catch (Exception e){

        }
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        JSONObject providerIdObj = getFieldJSONObject(field, "provider_id");
        providerIdObj.put("value",userName);
        return form;
    }
    public static JSONObject readOnlyChildDOb(JSONObject form){
        JSONArray field = fields(form, STEP1);
        JSONObject dobObj = getFieldJSONObject(field, "dob");
        try {
            dobObj.put(JsonFormUtils.READ_ONLY, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return form;
    }
    public static void updateProviderIdAtClient(JSONArray field,String familyBaseEntityId) throws Exception{
        BaseLocation blocks =HnppDBUtils.getBlocksHHID(familyBaseEntityId);
        Log.v("SS_NAME","ssName:"+blocks+":familyId:"+familyBaseEntityId);
        JSONObject blockNameObj = getFieldJSONObject(field, BLOCK_NAME);
        blockNameObj.put("value",blocks.name);
        try{
            JSONObject blockIdObj = getFieldJSONObject(field, BLOCK_ID);
            blockIdObj.put("value",blocks.id);
        }catch (Exception e){

        }
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        JSONObject providerIdObj = getFieldJSONObject(field, "provider_id");
        providerIdObj.put("value",userName);
    }
    public static JSONObject getJson(String formName, String baseEntityID) throws Exception {
        String locationId = HnppApplication.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
        JSONObject jsonObject = org.smartregister.chw.anc.util.JsonFormUtils.getFormAsJson(formName);
        org.smartregister.chw.anc.util.JsonFormUtils.getRegistrationForm(jsonObject, baseEntityID, locationId);
        return jsonObject;
    }
    public static JSONObject updateLatitudeLongitude(JSONObject form,double latitude, double longitude, String familyBaseEntityId) throws Exception {
        JSONArray field = fields(form, STEP1);
        JSONObject latitude_field = getFieldJSONObject(field, "latitude");
        JSONObject longitude_field = getFieldJSONObject(field, "longitude");
        latitude_field.put(org.smartregister.family.util.JsonFormUtils.VALUE,latitude );
        longitude_field.put(org.smartregister.family.util.JsonFormUtils.VALUE,longitude );
        if(!familyBaseEntityId.isEmpty())updateFormWithBlockInformation(form,familyBaseEntityId);
        return form;
    }
    public static JSONObject updateLatitudeLongitudeFamily(JSONObject form,double latitude, double longitude) throws JSONException {
        JSONArray field = fields(form, STEP1);
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
        Log.v("HH_VISIT","form>>>>>"+form);
        return form;
    }

    public static JSONObject getAutoPopulatedJsonEditFormString(String formName, Context context, CommonPersonObjectClient client, String eventType) {
        try {
            JSONObject form =  HnppJsonFormUtils.getJsonObject(formName);
            ;
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
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),HnppConstants.KEY.VILLAGE_NAME, false));

                break;
            case Constants.JSON_FORM_KEY.DOB:
                getDob(client,jsonObject);
                break;
            case Constants.JSON_FORM_KEY.DOB_UNKNOWN:
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, false);
                JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
                optionsObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), Constants.JSON_FORM_KEY.DOB_UNKNOWN, false));
                break;
            case "block_name":
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),HnppConstants.KEY.BLOCK_NAME, false));

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
            case "mother_name_english":
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),HnppConstants.KEY.CHILD_MOTHER_NAME, false));

                break;
            case "mother_name_bangla":
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,
                        org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED, false));

                break;
//            case "mother_name":
//                String motherNameAlter = Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED, false);
//                if(!TextUtils.isEmpty(motherNameAlter) && motherNameAlter.equalsIgnoreCase("মাতা রেজিস্টার্ড নয়")){
//                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,motherNameAlter);
//                }else{
//                    String motherEntityId = Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.MOTHER_ENTITY_ID, false);
//                    String relationId = Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
//                    String motherName = Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.CHILD_MOTHER_NAME, false);
//
//                    motherName = HnppDBUtils.getMotherName(motherEntityId,relationId,motherName);
//                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,motherName);
//                }
//
//                break;

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
            case "new_born_info":
                if(jsonObject.has("options")){
                    String value = processValueWithChoiceIdsForEdit(jsonObject,org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),
                            "new_born_info", false));
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
                FormTag formTag = formTag(allSharedPreferences);
                formTag.appVersionName = BuildConfig.VERSION_NAME;
                Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields, formTag, entityId);
                if (baseClient != null && !baseClient.getBaseEntityId().equals(familyBaseEntityId)) {
                    baseClient.addRelationship(Utils.metadata().familyMemberRegister.familyRelationKey, familyBaseEntityId);
                }
                JSONObject blockIdIdObj = getFieldJSONObject(fields, "block_id");
                String blockId = blockIdIdObj.getString("value");
                Log.v("MEMBER_REGISTER","processFamilyMemberForm:blockId:"+blockId);
                HALocation selectedLocation = HnppApplication.getHALocationRepository().getLocationByBlock(blockId);
                HALocationHelper.getInstance().addGeolocationIds(selectedLocation,baseClient);
                try{
                    String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
                    JSONObject providerIdObj = getFieldJSONObject(fields, "provider_id");
                    providerIdObj.put("value",userName);

                    String motherEntityId = updateMotherName(fields,familyId);
                    Context context = HnppApplication.getInstance().getContext().applicationContext();
                    addRelationship(context, motherEntityId,familyId, baseClient);
                }catch (Exception e){

                }
                Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, "metadata"), formTag, entityId, encounterType, Utils.metadata().familyMemberRegister.tableName);
                tagSyncMetadata(allSharedPreferences, baseEvent);
                String entity_id = baseClient.getBaseEntityId();
                updateFormSubmissionID(encounterType,entity_id,baseEvent);
                baseEvent.setIdentifiers(HALocationHelper.getInstance().getGeoIdentifier(selectedLocation));
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

            if (StringUtils.isNotBlank(dobUnKnownString) && dobUnKnownString.equalsIgnoreCase(HnppApplication.appContext.getString(R.string.no))) {
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

    public static String processValueWithChoiceIdsForEdit(JSONObject jsonObject, String value) {
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
            JSONObject blockIdIdObj = getFieldJSONObject(fields, "block_id");
            String blockId = blockIdIdObj.getString("value");
            Log.v("HH_REGISTER","processChildRegistrationForm:blockId:"+blockId);
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
            HALocation selectedLocation = HnppApplication.getHALocationRepository().getLocationByBlock(blockId);
            HALocationHelper.getInstance().addGeolocationIds(selectedLocation,baseClient);
            baseEvent.setIdentifiers(HALocationHelper.getInstance().getGeoIdentifier(selectedLocation));
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

       try{
           JSONObject motherAlterObj = getFieldJSONObject(fields, HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED);
           boolean isVisible = motherObj.optBoolean("is_visible",false);
           if(!isVisible){
               String motherNameSelected = motherAlterObj.optString(VALUE);
               if(!TextUtils.isEmpty(motherNameSelected) && !motherNameSelected.equalsIgnoreCase(HnppApplication.appContext.getString(R.string.mother_not_reg))){
                   motherObj.put(VALUE,motherNameSelected);
               }

           }
           String motherName = motherObj.optString(VALUE);
           if(!TextUtils.isEmpty(motherName))return HnppDBUtils.getMotherBaseEntityId(familyId,motherName);
       }catch (Exception e){

       }

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
               // baseClient.setLastName("Family");
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

    /**
     * getting language here
     */
    public static JSONObject getJsonObject(String formName) throws JSONException {
        String local = LangUtils.getLanguage(HnppApplication.getInstance().getApplicationContext());
        String lang = "";
        String jsonEx = ".json";
        if(local.equals("bn")){
            lang = "-bn";
        }
        String[] localSP = formName.split("\\.");

        if(localSP.length>1){
            if(localSP[1].equals("json")){
                jsonEx = "";
            }
        }


        return  new JSONObject(AssetHandler.readFileFromAssetsFolder("json.form" +lang+"/" +formName+jsonEx, HnppApplication.getHNPPInstance().getApplicationContext()));
    }
}
