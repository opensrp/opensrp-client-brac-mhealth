package org.smartregister.unicef.dghs.sync;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.growthmonitoring.domain.MUAC;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.ZScore;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.MUACRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.service.intent.HeightIntentService;
import org.smartregister.growthmonitoring.service.intent.MuacIntentService;
import org.smartregister.growthmonitoring.service.intent.WeightIntentService;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.job.MuactIntentServiceJob;
import org.smartregister.unicef.dghs.utils.GrowthUtil;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.Column;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceType;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.service.intent.RecurringIntentService;
import org.smartregister.immunization.service.intent.VaccineIntentService;
import org.smartregister.sync.ClientProcessorForJava;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import timber.log.Timber;

import static org.smartregister.chw.anc.util.NCUtils.eventToVisit;
import static java.lang.Float.parseFloat;


public class HnppClientProcessor extends ClientProcessorForJava {

    private ClientClassification classification;
    private Table vaccineTable;
    private Table serviceTable;
    private Table heightTable;
    private Table weightTable;
    private Table muacTable;
    public HnppClientProcessor(Context context) {
        super(context);
    }

    public static ClientProcessorForJava getInstance(Context context) {
        if (instance == null) {
            instance = new HnppClientProcessor(context);
        }
        return instance;
    }

    @Override
    public synchronized void processClient(List<EventClient> eventClients) throws Exception {
        long startTime = System.currentTimeMillis();
        Log.v("SYNC_URL", "processClient started");
        ClientClassification clientClassification = getClassification();


        if (!eventClients.isEmpty()) {
            for (EventClient eventClient : eventClients) {
                Event event = eventClient.getEvent();
                if (event == null) {
                    return;
                }

                String eventType = event.getEventType();
                if (eventType == null) {
                    continue;
                }
                Log.v("FORUM_TEST","processEvents>>eventType:"+eventType);

                processEvents(clientClassification,eventClient, event, eventType);
            }
            Log.v("SYNC_URL", "processClient end >>"+(System.currentTimeMillis() - startTime));
            //VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
            long lastSyncTime = HnppApplication.getHNPPInstance().getEcSyncHelper().getLastCheckTimeStamp();
            if(lastSyncTime == 0){
                //only firsttime it'll parse data
                FormParser.makeVisitLog();
                Log.v("SYNC_URL", "after parse >>"+(System.currentTimeMillis() - startTime));
            }


        }
    }

    private ClientClassification getClassification() {
        if (classification == null) {
            classification = assetJsonToJava("ec_client_classification.json", ClientClassification.class);
        }
        return classification;
    }

    private Table getVaccineTable() {
        if (vaccineTable == null) {
            vaccineTable = assetJsonToJava("ec_client_vaccine.json", Table.class);
        }
        return vaccineTable;
    }

    private Table getServiceTable() {
        if (serviceTable == null) {
            serviceTable = assetJsonToJava("ec_client_service.json", Table.class);
        }
        return serviceTable;
    }
    private Table getHeightTable() {
        if (heightTable == null) {
            heightTable = assetJsonToJava("ec_client_height.json", Table.class);
        }
        return heightTable;
    }
    private Table getWeightTable() {
        if (weightTable == null) {
            weightTable = assetJsonToJava("ec_client_weight.json", Table.class);
        }
        return weightTable;
    }

    public Table getMuacTable() {
        if (muacTable == null) {
            muacTable = assetJsonToJava("ec_client_muac.json", Table.class);
        }
        return muacTable;
    }

    protected void processEvents(ClientClassification clientClassification, EventClient eventClient, Event event, String eventType) throws Exception {
        Log.v("PROCESS_EVENT","processEvents2>>"+eventType);
        switch (eventType) {
            case VaccineIntentService.EVENT_TYPE:
            case VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT:
                processVaccine(eventClient, getVaccineTable(), eventType.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                break;
            case RecurringIntentService.EVENT_TYPE:
                processService(eventClient, getServiceTable());
                break;
            case HeightIntentService.EVENT_TYPE:
            case HeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT:
                processHeight(eventClient, getHeightTable(),eventType.equals(HeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                break;
            case WeightIntentService.EVENT_TYPE:
            case WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT:
                processWeight(eventClient, getWeightTable(),eventType.equals(WeightIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                break;
            case MuacIntentService.EVENT_TYPE:
            case MuacIntentService.EVENT_TYPE_OUT_OF_CATCHMENT:
                processMUAC(eventClient, getMuacTable(),eventType.equals(MuacIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                break;
            case CoreConstants.EventType.ANC_HOME_VISIT:
            case HnppConstants.EVENT_TYPE.ELCO:
            case HnppConstants.EVENT_TYPE.MEMBER_REFERRAL:
            case HnppConstants.EVENT_TYPE.WOMEN_REFERRAL:
            case HnppConstants.EVENT_TYPE.CHILD_REFERRAL:
            case HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP:
            case HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP:
            case HnppConstants.EVENT_TYPE.GIRL_PACKAGE:
            case HnppConstants.EVENT_TYPE.WOMEN_PACKAGE:
            case HnppConstants.EVENT_TYPE.NCD_PACKAGE:
            case HnppConstants.EVENT_TYPE.IYCF_PACKAGE:

            case HnppConstants.EVENT_TYPE.PNC_REGISTRATION:
            case HnppConstants.EVENT_TYPE.ENC_REGISTRATION:
            case HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY:
            case HnppConstants.EVENT_TYPE.CHILD_PROFILE_VISIT:
            case HnppConstants.EVENT_TYPE.MEMBER_PROFILE_VISIT:
            case HnppConstants.EVENT_TYPE.FORUM_CHILD:
            case HnppConstants.EVENT_TYPE.FORUM_WOMEN:
            case HnppConstants.EVENT_TYPE.FORUM_ADO:
            case HnppConstants.EVENT_TYPE.FORUM_NCD:
            case HnppConstants.EVENT_TYPE.FORUM_ADULT:
            case HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL:
            case HnppConstants.EVENT_TYPE.SS_INFO:
            case HnppConstants.EVENT_TYPE.ANC_REGISTRATION:
                case HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME:
            case HnppConstants.EVENT_TYPE.EYE_TEST:
            case HnppConstants.EVENT_TYPE.BLOOD_GROUP:
            case CoreConstants.EventType.REMOVE_MEMBER:
            case CoreConstants.EventType.REMOVE_CHILD:
                if (eventClient.getEvent() == null) {
                    return;
                }
                processVisitEvent(eventClient);
                processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
                break;
            case CoreConstants.EventType.REMOVE_FAMILY:
                if (eventClient.getClient() == null) {
                    return;
                }
                processRemoveFamily(eventClient.getClient().getBaseEntityId(), event.getEventDate().toDate());
                break;
//            case CoreConstants.EventType.REMOVE_MEMBER:
//                if (eventClient.getClient() == null) {
//                    return;
//                }
//                processVisitEvent(eventClient);
//                processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
//                processRemoveMember(eventClient.getClient().getBaseEntityId(), event.getEventDate().toDate());
//                break;
//            case CoreConstants.EventType.REMOVE_CHILD:
//                if (eventClient.getClient() == null) {
//                    return;
//                }
//                processVisitEvent(eventClient);
//                processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
//                processRemoveChild(eventClient.getClient().getBaseEntityId(), event.getEventDate().toDate());
//                break;

            default:
                if (eventClient.getClient() != null) {
                    if (eventType.equals(CoreConstants.EventType.UPDATE_FAMILY_RELATIONS) && event.getEntityType().equalsIgnoreCase(CoreConstants.TABLE_NAME.FAMILY_MEMBER)) {
                        event.setEventType(CoreConstants.EventType.UPDATE_FAMILY_MEMBER_RELATIONS);
                    }
                    processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
                }
                break;
        }
    }

    @Override
    public Boolean processCaseModel(Event event, Client client, List<String> createsCase) {
        try {

            if (createsCase == null || createsCase.isEmpty()) {
                return false;
            }
            for (String clientType : createsCase) {
                Table table = getColumnMappings(clientType);
                List<Column> columns = table.columns;
                String baseEntityId = client != null ? client.getBaseEntityId() : event != null ? event.getBaseEntityId() : null;

                ContentValues contentValues = new ContentValues();
                //Add the base_entity_id
                contentValues.put("base_entity_id", baseEntityId);
                contentValues.put("is_closed", 0);

                for (Column colObject : columns) {
                    processCaseModel(event, client, colObject, contentValues);
                }

                // Modify openmrs generated identifier, Remove hyphen if it exists
                updateIdenitifier(contentValues);

                // save the values to db
                executeInsertStatement(contentValues, clientType);
                updateRegisterCount(baseEntityId);
                updateClientDetailsTable(event, client);
            }

            return true;
        } catch (Exception e) {
            Timber.e(e);

            return null;
        }
    }

    @Override
    public void updateFTSsearch(String tableName, String entityId, ContentValues contentValues) {
        //no need to implement
    }

    private void updateIdenitifier(ContentValues values) {
        try {
            for (String identifier : getOpenmrsGenIds()) {
                Object value = values.get(identifier); //TODO
                if (value != null) {
                    String sValue = value.toString();
                    if (value instanceof String && StringUtils.isNotBlank(sValue)) {
                        values.remove(identifier);
                        values.put(identifier, sValue.replace("-", ""));
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    // possible to delegate
    protected Boolean processVaccine(EventClient vaccine, Table vaccineTable, boolean outOfCatchment) {

        try {
            if (vaccine == null || vaccine.getEvent() == null) {
                return false;
            }

            if (vaccineTable == null) {
                return false;
            }

            Timber.d("Starting processVaccine table: %s", vaccineTable.name);

            ContentValues contentValues = processCaseModel(vaccine, vaccineTable);

            // updateFamilyRelations the values to db
            if (contentValues != null && contentValues.size() > 0) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = simpleDateFormat.parse(contentValues.getAsString(VaccineRepository.DATE));

                VaccineRepository vaccineRepository = HnppApplication.getInstance().vaccineRepository();
                Vaccine vaccineObj = new Vaccine();
                vaccineObj.setBaseEntityId(contentValues.getAsString(VaccineRepository.BASE_ENTITY_ID));
                vaccineObj.setName(contentValues.getAsString(VaccineRepository.NAME));
                if (contentValues.containsKey(VaccineRepository.CALCULATION)) {
                    vaccineObj.setCalculation(parseInt(contentValues.getAsString(VaccineRepository.CALCULATION)));
                }
                vaccineObj.setDate(date);
                vaccineObj.setAnmId(contentValues.getAsString(VaccineRepository.ANMID));
                vaccineObj.setLocationId(contentValues.getAsString(VaccineRepository.LOCATION_ID));
                vaccineObj.setSyncStatus(VaccineRepository.TYPE_Synced);
                vaccineObj.setFormSubmissionId(vaccine.getEvent().getFormSubmissionId());
                vaccineObj.setEventId(vaccine.getEvent().getEventId());
                vaccineObj.setOutOfCatchment(outOfCatchment ? 1 : 0);

                String createdAtString = contentValues.getAsString(VaccineRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                vaccineObj.setCreatedAt(createdAt);

                addVaccine(vaccineRepository, vaccineObj);

                Timber.d("Ending processVaccine table: %s", vaccineTable.name);
            }
            return true;

        } catch (Exception e) {

            Timber.e(e, "Process Vaccine Error");
            return null;
        }
    }

    // possible to delegate
    protected Boolean processService(EventClient service, Table serviceTable) {

        try {

            if (service == null || service.getEvent() == null) {
                return false;
            }

            if (serviceTable == null) {
                return false;
            }

            Timber.d("Starting processService table: %s", serviceTable.name);

            ContentValues contentValues = processCaseModel(service, serviceTable);

            // updateFamilyRelations the values to db
            if (contentValues != null && contentValues.size() > 0) {
                String name = contentValues.getAsString(RecurringServiceTypeRepository.NAME);

                if (StringUtils.isNotBlank(name)) {
                    name = name.replaceAll("_", " ").replace("dose", "").trim();
                }


                String eventDateStr = contentValues.getAsString(RecurringServiceRecordRepository.DATE);
                Date date = getDate(eventDateStr);
                String value = null;

                if (StringUtils.containsIgnoreCase(name, "Exclusive breastfeeding")) {
                    value = contentValues.getAsString(RecurringServiceRecordRepository.VALUE);
                }

                RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
                List<ServiceType> serviceTypeList = recurringServiceTypeRepository.searchByName(name);
                if (serviceTypeList == null || serviceTypeList.isEmpty()) {
                    return false;
                }

                if (date == null) {
                    return false;
                }

                RecurringServiceRecordRepository recurringServiceRecordRepository = ImmunizationLibrary.getInstance().recurringServiceRecordRepository();
                ServiceRecord serviceObj = new ServiceRecord();
                serviceObj.setBaseEntityId(contentValues.getAsString(RecurringServiceRecordRepository.BASE_ENTITY_ID));
                serviceObj.setName(name);
                serviceObj.setDate(date);
                serviceObj.setAnmId(contentValues.getAsString(RecurringServiceRecordRepository.ANMID));
                serviceObj.setLocationId(contentValues.getAsString(RecurringServiceRecordRepository.LOCATION_ID));
                serviceObj.setSyncStatus(RecurringServiceRecordRepository.TYPE_Synced);
                serviceObj.setFormSubmissionId(service.getEvent().getFormSubmissionId());
                serviceObj.setEventId(service.getEvent().getEventId()); //FIXME hard coded id
                serviceObj.setValue(value);
                serviceObj.setRecurringServiceId(serviceTypeList.get(0).getId());

                String createdAtString = contentValues.getAsString(RecurringServiceRecordRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                serviceObj.setCreatedAt(createdAt);

                recurringServiceRecordRepository.add(serviceObj);

                Timber.d("Ending processService table: %s", serviceTable.name);
            }
            return true;

        } catch (Exception e) {
            Timber.e(e, "Process Service Error");
            return null;
        }
    }
    private Boolean processHeight(EventClient height,Table heightTable, boolean outOfCatchment) throws Exception {

        try {

            if (height == null || height.getEvent() == null) {
                return false;
            }

            if (heightTable == null) {
                return false;
            }

            ContentValues contentValues = processCaseModel(height, heightTable);
            Log.v("CLIENT_PROCESSOR","processHeight>>"+height);
            Log.v("CLIENT_PROCESSOR","processHeight>>"+contentValues);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {

                HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().getHeightRepository();
                Height heightObj = new Height();
                heightObj.setBaseEntityId(contentValues.getAsString(HeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(HeightRepository.CM)) {
                    heightObj.setCm(parseFloat(contentValues.getAsString(HeightRepository.CM)));
                }
                try{
                    String eventDateStr = contentValues.getAsString(HeightRepository.DATE);
                    Date date = getDate(eventDateStr);
                    heightObj.setDate(date);
                }catch (Exception e){

                }
                heightObj.setZScore(Double.parseDouble(contentValues.getAsString(HeightRepository.Z_SCORE)));
                heightObj.setAnmId(contentValues.getAsString(HeightRepository.ANMID));
                heightObj.setLocationId(contentValues.getAsString(HeightRepository.LOCATIONID));
                heightObj.setSyncStatus(HeightRepository.TYPE_Synced);
                heightObj.setFormSubmissionId(height.getEvent().getFormSubmissionId());
                try{
                    heightObj.setEventId(height.getEvent().getEventId());
                    String createdAtString = contentValues.getAsString(HeightRepository.CREATED_AT);
                    Date createdAt = getDate(createdAtString);
                    heightObj.setCreatedAt(createdAt);
                }catch (Exception e){

                }
                heightObj.setOutOfCatchment(outOfCatchment ? 1 : 0);
                heightRepository.add(heightObj);
                String heightText = ZScore.getZScoreText(heightObj.getZScore());
                GrowthUtil.updateLastHeight(heightObj.getCm(),heightObj.getZScore(),heightObj.getBaseEntityId(),heightText);
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Boolean processWeight(EventClient weight,Table weightTable, boolean outOfCatchment) throws Exception {

        try {

            if (weight == null || weight.getEvent() == null) {
                return false;
            }

            if (weightTable == null) {
                return false;
            }

            ContentValues contentValues = processCaseModel(weight, weightTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {

                WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
                Weight weightObj = new Weight();
                weightObj.setBaseEntityId(contentValues.getAsString(WeightRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(WeightRepository.KG)) {
                    weightObj.setKg(parseFloat(contentValues.getAsString(WeightRepository.KG)));
                }
                try{
                    String eventDateStr = contentValues.getAsString(WeightRepository.DATE);
                    Date date = getDate(eventDateStr);
                    weightObj.setDate(date);
                }catch (Exception e){

                }
                weightObj.setZScore(contentValues.getAsDouble(WeightRepository.Z_SCORE));
                weightObj.setAnmId(contentValues.getAsString(WeightRepository.ANMID));
                weightObj.setLocationId(contentValues.getAsString(WeightRepository.LOCATIONID));
                weightObj.setSyncStatus(WeightRepository.TYPE_Synced);
                weightObj.setFormSubmissionId(weight.getEvent().getFormSubmissionId());
                try{
                    weightObj.setEventId(weight.getEvent().getEventId());
                    String createdAtString = contentValues.getAsString(WeightRepository.CREATED_AT);
                    Date createdAt = getDate(createdAtString);
                    weightObj.setCreatedAt(createdAt);
                }catch (Exception e){

                }
                weightObj.setOutOfCatchment(outOfCatchment ? 1 : 0);
                Log.v("WEIGHT","taken>>>>"+weightObj.getKg());

                double zScore = ZScore.roundOff(weightObj.getZScore());
                String weightText = ZScore.getZScoreText(zScore);
                weightRepository.add(weightObj);
                //need to update child table
                GrowthUtil.updateLastWeight(weightObj.getKg(),zScore,weightObj.getBaseEntityId(),weightText);
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Boolean processMUAC(EventClient muac,Table muacTable, boolean outOfCatchment) throws Exception {

        try {

            if (muac == null || muac.getEvent() == null) {
                return false;
            }

            if (muacTable == null) {
                return false;
            }

            ContentValues contentValues = processCaseModel(muac, muacTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {


                MUACRepository muacRepository = GrowthMonitoringLibrary.getInstance().getMuacRepository();
                MUAC muacObj = new MUAC();
                muacObj.setBaseEntityId(contentValues.getAsString(MUACRepository.BASE_ENTITY_ID));
                if (contentValues.containsKey(MUACRepository.CM)) {
                    muacObj.setCm(parseFloat(contentValues.getAsString(MUACRepository.CM)));
                }
                try{
                    String eventDateStr = contentValues.getAsString(MUACRepository.DATE);
                    Date date = getDate(eventDateStr);
                    muacObj.setDate(date);
                }catch (Exception e){

                }
                muacObj.setAnmId(contentValues.getAsString(MUACRepository.ANMID));
                muacObj.setLocationId(contentValues.getAsString(MUACRepository.LOCATIONID));
                muacObj.setSyncStatus(MUACRepository.TYPE_Synced);
                muacObj.setFormSubmissionId(muac.getEvent().getFormSubmissionId());
                try{
                    muacObj.setEventId(muac.getEvent().getEventId());
                    String createdAtString = contentValues.getAsString(MUACRepository.CREATED_AT);
                    Date createdAt = getDate(createdAtString);
                    muacObj.setCreatedAt(createdAt);
                }catch (Exception e){

                }
                muacObj.setOutOfCatchment(outOfCatchment ? 1 : 0);
                muacObj.setEdemaValue(contentValues.getAsString(MUACRepository.EDEMA_VALUE));
                String status = ZScore.getMuacText(muacObj.getCm());

                muacRepository.add(muacObj);
                GrowthUtil.updateLastMuac(muacObj.getCm(),muacObj.getBaseEntityId(),status,muacObj.getEdemaValue());
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // possible to delegate
    protected void processVisitEvent(EventClient eventClient) {
        try {
            processAncHomeVisit(eventClient, null, null); // save locally
        } catch (Exception e) {
            String formID = (eventClient != null && eventClient.getEvent() != null) ? eventClient.getEvent().getFormSubmissionId() : "no form id";
            Timber.e("Form id " + formID + ". " + e.toString());
        }
    }
    protected void processAncHomeVisit(EventClient baseEvent, SQLiteDatabase database, String parentEventType) {
        try {
            Visit visit = AncLibrary.getInstance().visitRepository().getVisitByFormSubmissionID(baseEvent.getEvent().getFormSubmissionId());
            if (visit == null) {
                visit = eventToVisit(baseEvent.getEvent());

//                if (StringUtils.isNotBlank(parentEventType) && !parentEventType.equalsIgnoreCase(visit.getVisitType())) {
//                    String parentVisitID = AncLibrary.getInstance().visitRepository().getParentVisitEventID(visit.getBaseEntityId(), parentEventType, visit.getDate());
//                    visit.setParentVisitID(parentVisitID);
//                }

                if (database != null) {
                    AncLibrary.getInstance().visitRepository().addVisit(visit, database);
                } else {
                    AncLibrary.getInstance().visitRepository().addVisit(visit);
                }


            }
//            else{
//                //need to handle to update visit table to process again
//                //TODO need to remove this logic from 2.0.6 version production
//                Log.v("STOCK_ADD","pre process"+baseEvent.getEvent().getEventType()+":is process:"+visit.getProcessed());
//
//                switch (baseEvent.getEvent().getEventType()){
//                    case CoreConstants.EventType.ANC_HOME_VISIT:
//                    case HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour:
//                    case HnppConstants.EVENT_TYPE.GIRL_PACKAGE:
//                    case HnppConstants.EVENT_TYPE.WOMEN_PACKAGE:
//                    case HnppConstants.EVENT_TYPE.NCD_PACKAGE:
//                    case HnppConstants.EVENT_TYPE.IYCF_PACKAGE:
//                        SQLiteDatabase db = HnppApplication.getInstance().getRepository().getReadableDatabase();
//                        String event = (new JSONObject(JsonFormUtils.gson.toJson(baseEvent.getEvent())).toString());
//                        db.execSQL("UPDATE visits set processed='2',visit_json ='"+event+"' where visit_id='"+visit.getVisitId()+"' and processed ='1'");
//                        Log.v("STOCK_ADD","updated:"+visit.getVisitId());
//                        break;
//                    default:
//                        break;
//                }
//              }
        } catch (JSONException e) {
            Timber.e(e);
        }
    }



    public static void processSubHomeVisit(EventClient baseEvent, String parentEventType) {
        processHomeVisit(baseEvent, null, parentEventType);
    }
    private void processVisitEvent(EventClient eventClient, String parentEventName) {
        try {
            processSubHomeVisit(eventClient, parentEventName); // save locally
        } catch (Exception e) {
            String formID = (eventClient != null && eventClient.getEvent() != null) ? eventClient.getEvent().getFormSubmissionId() : "no form id";
            Timber.e("Form id " + formID + ". " + e.toString());
        }
    }
    public static void processHomeVisit(EventClient baseEvent, SQLiteDatabase database, String parentEventType) {
        try {
            Visit visit = AncLibrary.getInstance().visitRepository().getVisitByFormSubmissionID(baseEvent.getEvent().getFormSubmissionId());
            if (visit == null) {
                visit = eventToVisit(baseEvent.getEvent());

//                if (StringUtils.isNotBlank(parentEventType) && !parentEventType.equalsIgnoreCase(visit.getVisitType())) {
//                    String parentVisitID = AncLibrary.getInstance().visitRepository().getParentVisitEventID(visit.getBaseEntityId(), parentEventType, visit.getDate());
//                    visit.setParentVisitID(parentVisitID);
//                }

                if (database != null) {
                    AncLibrary.getInstance().visitRepository().addVisit(visit, database);
                } else {
                    AncLibrary.getInstance().visitRepository().addVisit(visit);
                }
                //saveForumData(visit);

            }
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    /**
     * Update the family members
     *
     * @param familyID
     */
    @SuppressLint("SimpleDateFormat")
    protected void processRemoveFamily(String familyID, Date eventDate) {

        Date myEventDate = eventDate;
        if (myEventDate == null) {
            myEventDate = new Date();
        }

        if (familyID == null) {
            return;
        }

        AllCommonsRepository commonsRepository = HnppApplication.getInstance().getAllCommonsRepository(CoreConstants.TABLE_NAME.FAMILY);
        if (commonsRepository != null) {

            ContentValues values = new ContentValues();
            values.put(DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(myEventDate));
            values.put("is_closed", 1);

            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.FAMILY, values,
                    DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{familyID});

            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.CHILD, values,
                    DBConstants.KEY.RELATIONAL_ID + " = ?  ", new String[]{familyID});

            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.FAMILY_MEMBER, values,
                    DBConstants.KEY.RELATIONAL_ID + " = ?  ", new String[]{familyID});

            // clean fts table
//            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY), values,
//                    CommonFtsObject.idColumn + " = ?  ", new String[]{familyID});
//
//            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.CHILD), values,
//                    String.format(" %s in (select base_entity_id from %s where relational_id = ? )  ", CommonFtsObject.idColumn, CoreConstants.TABLE_NAME.CHILD), new String[]{familyID});
//
//            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY_MEMBER), values,
//                    String.format(" %s in (select base_entity_id from %s where relational_id = ? )  ", CommonFtsObject.idColumn, CoreConstants.TABLE_NAME.FAMILY_MEMBER), new String[]{familyID});

        }
    }

    @SuppressLint("SimpleDateFormat")
    protected void processRemoveMember(String baseEntityId, Date eventDate) {

        Date myEventDate = eventDate;
        if (myEventDate == null) {
            myEventDate = new Date();
        }

        if (baseEntityId == null) {
            return;
        }

        AllCommonsRepository commonsRepository = HnppApplication.getInstance().getAllCommonsRepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER);
        if (commonsRepository != null) {

            ContentValues values = new ContentValues();
            values.put(DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(myEventDate));
            values.put("is_closed", 1);

            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.FAMILY_MEMBER, values,
                    DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseEntityId});

            // clean fts table
//            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY_MEMBER), values,
//                    " object_id  = ?  ", new String[]{baseEntityId});

            // Utils.context().commonrepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER).populateSearchValues(baseEntityId, DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(eventDate), null);

        }
    }

    @SuppressLint("SimpleDateFormat")
    protected void processRemoveChild(String baseEntityId, Date eventDate) {

        Date myEventDate = eventDate;
        if (myEventDate == null) {
            myEventDate = new Date();
        }

        if (baseEntityId == null) {
            return;
        }

        AllCommonsRepository commonsRepository = HnppApplication.getInstance().getAllCommonsRepository(CoreConstants.TABLE_NAME.CHILD);
        if (commonsRepository != null) {

            ContentValues values = new ContentValues();
            values.put(DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(myEventDate));
            values.put("is_closed", 1);

            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.CHILD, values,
                    DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseEntityId});

            // clean fts table
//            HnppApplication.getInstance().getRepository().getWritableDatabase().update(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.CHILD), values,
//                    CommonFtsObject.idColumn + "  = ?  ", new String[]{baseEntityId});

            // Utils.context().commonrepository(CoreConstants.TABLE_NAME.CHILD).populateSearchValues(baseEntityId, DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(eventDate), null);

        }
    }


    private ContentValues processCaseModel(EventClient eventClient, Table table) {
        try {
            List<Column> columns = table.columns;
            ContentValues contentValues = new ContentValues();

            for (Column column : columns) {
                processCaseModel(eventClient.getEvent(), eventClient.getClient(), column, contentValues);
            }

            return contentValues;
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    private Integer parseInt(String string) {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
        return null;
    }
    @SuppressLint("SimpleDateFormat")
    private Date getDate(String eventDateStr) {
        Date date = null;
        if (StringUtils.isNotBlank(eventDateStr)) {
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
                date = dateFormat.parse(eventDateStr);
            } catch (ParseException e) {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    date = dateFormat.parse(eventDateStr);
                } catch (ParseException pe) {
                    try {
                        date = DateUtil.parseDate(eventDateStr);
                    } catch (ParseException pee) {
                        Timber.e(pee, pee.toString());
                    }
                }
            }
        }
        return date;
    }

    public static void addVaccine(VaccineRepository vaccineRepository, Vaccine vaccine) {
        try {
            if (vaccineRepository == null || vaccine == null) {
                return;
            }

            // Add the vaccine
            vaccineRepository.add(vaccine);

            String name = vaccine.getName();
            if (StringUtils.isBlank(name)) {
                return;
            }

            // Update vaccines in the same group where either can be given
            // For example measles 1 / mr 1
            name = VaccineRepository.removeHyphen(name);
            String ftsVaccineName = null;

            if (VaccineRepo.Vaccine.measles1.display().equalsIgnoreCase(name)) {
                ftsVaccineName = VaccineRepo.Vaccine.mr1.display();
            } else if (VaccineRepo.Vaccine.mr1.display().equalsIgnoreCase(name)) {
                ftsVaccineName = VaccineRepo.Vaccine.measles1.display();
            } else if (VaccineRepo.Vaccine.measles2.display().equalsIgnoreCase(name)) {
                ftsVaccineName = VaccineRepo.Vaccine.mr2.display();
            } else if (VaccineRepo.Vaccine.mr2.display().equalsIgnoreCase(name)) {
                ftsVaccineName = VaccineRepo.Vaccine.measles2.display();
            }

            if (ftsVaccineName != null) {
                ftsVaccineName = VaccineRepository.addHyphen(ftsVaccineName.toLowerCase());
                Vaccine ftsVaccine = new Vaccine();
                ftsVaccine.setBaseEntityId(vaccine.getBaseEntityId());
                ftsVaccine.setName(ftsVaccineName);
                vaccineRepository.updateFtsSearch(ftsVaccine);
            }

        } catch (Exception e) {
            Timber.e(e);
        }

    }

    @Override
    public void updateClientDetailsTable(Event event, Client client) {
//        Timber.d("Started updateClientDetailsTable");
        event.addDetails("detailsUpdated", Boolean.TRUE.toString());
//        Timber.d("Finished updateClientDetailsTable");
    }

    private void processVisitEvent(List<EventClient> eventClients) {
        for (EventClient eventClient : eventClients) {
            processVisitEvent(eventClient); // save locally
        }
    }

    private Float parseFloat(String string) {
        try {
            return Float.valueOf(string);
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
        return null;
    }
}