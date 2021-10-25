

package org.smartregister.brac.hnpp.service;
import android.app.IntentService;
import android.content.Intent;

import org.smartregister.brac.hnpp.sync.FormParser;

//
//import android.app.IntentService;
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.Intent;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.google.gson.reflect.TypeToken;
//
//import net.sqlcipher.Cursor;
//import net.sqlcipher.database.SQLiteDatabase;
//
//
//import org.apache.commons.lang3.StringUtils;
//import org.joda.time.LocalDate;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.smartregister.brac.hnpp.HnppApplication;
//import org.smartregister.brac.hnpp.model.ForumDetails;
//import org.smartregister.brac.hnpp.model.HHMemberProperty;
//import org.smartregister.brac.hnpp.repository.StockRepository;
//import org.smartregister.brac.hnpp.utils.HnppConstants;
//import org.smartregister.brac.hnpp.utils.HnppDBConstants;
//import org.smartregister.brac.hnpp.utils.HnppDBUtils;
//import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
//import org.smartregister.brac.hnpp.utils.RiskyModel;
//import org.smartregister.brac.hnpp.utils.VisitLog;
//import org.smartregister.chw.anc.AncLibrary;
//import org.smartregister.chw.anc.domain.Visit;
//import org.smartregister.chw.anc.domain.VisitDetail;
//import org.smartregister.chw.anc.util.DBConstants;
//import org.smartregister.chw.anc.util.NCUtils;
//import org.smartregister.chw.core.application.CoreChwApplication;
//import org.smartregister.chw.core.utils.CoreConstants;
//import org.smartregister.clientandeventmodel.Event;
//import org.smartregister.clientandeventmodel.Obs;
//import org.smartregister.commonregistry.AllCommonsRepository;
//import org.smartregister.commonregistry.CommonFtsObject;
//import org.smartregister.commonregistry.CommonPersonObjectClient;
//import org.smartregister.family.FamilyLibrary;
//import org.smartregister.family.util.JsonFormUtils;
//import org.smartregister.util.AssetHandler;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC1_REGISTRATION;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC1_REGISTRATION_OOC;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC2_REGISTRATION;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC2_REGISTRATION_OOC;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC3_REGISTRATION;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC3_REGISTRATION_OOC;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.BLOOD_GROUP;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CHILD_INFO_7_24_MONTHS;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CHILD_REFERRAL;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ELCO;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ENC_REGISTRATION;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.EYE_TEST;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.GIRL_PACKAGE;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.IYCF_PACKAGE;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.MEMBER_REFERRAL;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.NCD_PACKAGE;
//
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour_OOC;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour_OOC;
//
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME_OOC;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.SS_INFO;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.WOMEN_PACKAGE;
//import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.WOMEN_REFERRAL;
//import static org.smartregister.chw.anc.util.NCUtils.eventToVisit;
//import static org.smartregister.util.JsonFormUtils.gson;
//
public class VisitLogIntentService extends IntentService {
//
    public VisitLogIntentService() {
        super("VisitLogService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        FormParser.makeVisitLog();

//        Log.v("SYNC_URL","visit log stated");
//        ArrayList<String> visit_ids = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitIds();
//        for (int i = 0; i < visit_ids.size(); i++) {
//            List<Visit> v = AncLibrary.getInstance().visitRepository().getVisitsByVisitId(visit_ids.get(i));
//            //getANCRegistrationVisitsFromEvent(v);
//            for (Visit visit : v) {
//                String formSubmissionId = visit.getFormSubmissionId();
//                if(isForumEvent(visit.getVisitType())){
//                   saveForumData(visit,formSubmissionId);
//
//                }else if(visit.getVisitType().equalsIgnoreCase(SS_INFO)){
//                    saveSSFormData(visit);
//                }
//                else{
//
//                    String eventJson = visit.getJson();
//                    if (!StringUtils.isEmpty(eventJson)) {
//                        try {
//
//                            Event baseEvent = gson.fromJson(eventJson, Event.class);
//                            String base_entity_id = baseEvent.getBaseEntityId();
//                            HashMap<String,Object>form_details = getFormNamesFromEventObject(baseEvent);
//                            ArrayList<String> encounter_types = (ArrayList<String>) form_details.get("form_name");
//                            HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
//                            final CommonPersonObjectClient client = new CommonPersonObjectClient(base_entity_id, details, "");
//                            client.setColumnmaps(details);
//                            for (String encounter_type : encounter_types) {
//                                Log.v("ANC_HOME_VISIT","encounter_type:"+encounter_type);
//
//                                if(encounter_type.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME_OOC)){
//                                    encounter_type = HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME;
//                                }
//                                else if(encounter_type.equalsIgnoreCase(ANC1_REGISTRATION_OOC)){
//                                    encounter_type = HnppConstants.EVENT_TYPE.ANC1_REGISTRATION;
//                                }
//                                else if(encounter_type.equalsIgnoreCase(ANC2_REGISTRATION_OOC)){
//                                    encounter_type = HnppConstants.EVENT_TYPE.ANC2_REGISTRATION;
//                                }
//                                else if(encounter_type.equalsIgnoreCase(ANC3_REGISTRATION_OOC)){
//                                    encounter_type = HnppConstants.EVENT_TYPE.ANC3_REGISTRATION;
//                                }
//                                else if(encounter_type.equalsIgnoreCase(PNC_REGISTRATION_BEFORE_48_hour_OOC)){
//                                    encounter_type = HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour;
//                                }
//                                else if(encounter_type.equalsIgnoreCase(PNC_REGISTRATION_AFTER_48_hour_OOC)){
//                                    encounter_type = HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour;
//                                }
//                                if(encounter_type.equalsIgnoreCase(HOME_VISIT_FAMILY)){
//                                     JSONObject form_object = loadFormFromAsset(encounter_type,this);
//                                    JSONObject stepOne = form_object.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
//                                    JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
//                                    for (int k = 0; k < jsonArray.length(); k++) {
//                                        populateValuesForFormObject(client, jsonArray.getJSONObject(k));
//                                    }
//                                }
//
//                                VisitLog log = new VisitLog();
//                                log.setVisitId(visit.getVisitId());
//                                log.setVisitType(visit.getVisitType());
//                                log.setBaseEntityId(base_entity_id);
//                                String ssName = HnppDBUtils.getSSName(base_entity_id);
//                                log.setSsName(ssName);
//                                log.setVisitDate(visit.getDate().getTime());
//                                log.setEventType(encounter_type);
//                                //log.setVisitJson(form_object.toString());
//                                processReferral(encounter_type,log,details,formSubmissionId);
//                                try{
//                                    processIndicator(base_entity_id,encounter_type,log,details,formSubmissionId);
//                                    processSimprintsVerification(log,details);
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//
//
//                                if(ELCO.equalsIgnoreCase(encounter_type)){
//                                    if(details.containsKey("pregnancy_test_result")&&!StringUtils.isEmpty(details.get("pregnancy_test_result"))){
//                                        log.setPregnantStatus(details.get("pregnancy_test_result"));
//                                    }
//                                    updateElcoRisk(base_entity_id,details);
//                                }
//                                if(BLOOD_GROUP.equalsIgnoreCase(encounter_type)){
//                                    if(details.containsKey("blood_group_name")&&!StringUtils.isEmpty(details.get("blood_group_name"))){
//                                       String bloodGroup = details.get("blood_group_name");
//                                       if(!TextUtils.isEmpty(bloodGroup)){
//                                           HnppDBUtils.updateBloodGroup(base_entity_id,bloodGroup);
//                                       }
//                                    }
//
//                                }
//                                if(ANC1_REGISTRATION.equalsIgnoreCase(encounter_type) || ANC2_REGISTRATION.equalsIgnoreCase(encounter_type)
//                                        || ANC3_REGISTRATION.equalsIgnoreCase(encounter_type) || CoreConstants.EventType.ANC_HOME_VISIT.equalsIgnoreCase(encounter_type)){
//                                    if(details.containsKey("brac_anc") && !StringUtils.isEmpty(details.get("brac_anc"))){
//                                        String ancValue = details.get("brac_anc");
//                                        String prevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(base_entity_id+"_BRAC_ANC");
//                                        if(!TextUtils.isEmpty(prevalue)){
//                                            try{
//                                                int lastValue = Integer.parseInt(prevalue);
//                                                int ancValueInt = Integer.parseInt(ancValue);
//                                                if(ancValueInt >= lastValue){
//
//                                                    FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_ANC",(ancValueInt+1)+"");
//                                                }
//                                            }catch (NumberFormatException ne){
//
//                                            }
//
//                                        }else{
//                                            FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_ANC",1+"");
//                                        }
//                                    }
//                                    updateAncHomeVisitRisk(encounter_type,base_entity_id,details);
//                                }
//
//                                if(PNC_REGISTRATION_BEFORE_48_hour.equalsIgnoreCase(encounter_type)||
//                                        PNC_REGISTRATION_AFTER_48_hour.equalsIgnoreCase(encounter_type)){
//                                    if(details.containsKey("brac_pnc") && !StringUtils.isEmpty(details.get("brac_pnc"))){
//                                        String ancValue = details.get("brac_pnc");
//                                        String prevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(base_entity_id+"_BRAC_PNC");
//                                        if(!TextUtils.isEmpty(prevalue)){
//                                            try{
//                                                int lastValue = Integer.parseInt(prevalue);
//                                                int ancValueInt = Integer.parseInt(ancValue);
//                                                if(ancValueInt >= lastValue){
//                                                    FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_PNC",(ancValueInt+1)+"");
//                                                }
//                                            }catch (NumberFormatException e){
//
//                                            }
//
//                                        }else{
//                                            FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_PNC",1+"");
//                                        }
//                                    }
//                                    if(details.containsKey("total_anc") && !StringUtils.isEmpty(details.get("brac_pnc"))){
//                                        String ancValue = details.get("total_anc");
//                                        try{
//                                            if(!TextUtils.isEmpty(ancValue)){
//                                                int count = Integer.parseInt(ancValue);
//                                                FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_TOTAL_ANC",count+"");
//
//                                            }
//                                        }catch (NumberFormatException ne){
//
//                                        }
//
//                                    }
//
//                                    updatePncRisk(base_entity_id,details, encounter_type );
//                                }
//                                if(ANC_REGISTRATION.equalsIgnoreCase(encounter_type)){
//                                    FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_ANC",0+"");
//                                    FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_PNC",0+"");
//                                    updateAncRegistrationRisk(base_entity_id,details);
//                                }
//                                if(IYCF_PACKAGE.equalsIgnoreCase(encounter_type)){
//                                   updateIYCFRisk(base_entity_id,details);
//                                }
//                                if(HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE.equalsIgnoreCase(encounter_type)){
//                                    updatePhysicalProblemRisk(base_entity_id,details);
//                                }
//                                if(NCD_PACKAGE.equalsIgnoreCase(encounter_type)){
//                                    updateNcdPackageRisk(base_entity_id,details);
//                                }
//                                if(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY.equalsIgnoreCase(encounter_type)){
//                                    updatePreviousHistoryRisk(base_entity_id,details);
//                                }
//                                if(HOME_VISIT_FAMILY.equalsIgnoreCase(encounter_type)){
//                                    log.setFamilyId(base_entity_id);
//                                }else{
//                                    log.setFamilyId(HnppDBUtils.getFamilyIdFromBaseEntityId(base_entity_id));
//                                }
//                                long isInserted = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
//                                if(isInserted!=-1){
//                                    Log.d(VisitLogIntentService.class.getSimpleName(), "Encounter type: "+ encounter_type);
//                                    LocalDate localDate = new LocalDate(visit.getDate().getTime());
//                                    if(!encounter_type.equalsIgnoreCase(PNC_REGISTRATION_AFTER_48_hour)){
//                                        HnppApplication.getTargetRepository().updateValue(encounter_type,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,base_entity_id,formSubmissionId);
//
//                                    }
//                                    if(ELCO.equalsIgnoreCase(encounter_type)){
//                                        updateFamilyPlanning(log,details,formSubmissionId);
//                                    }
//                                    if(NCD_PACKAGE.equalsIgnoreCase(encounter_type)){
//                                        updateNcdDiabeticsTarget(log,details,formSubmissionId);
//                                        updateNcdBpTarget(log,details,formSubmissionId);
//                                    }
//                                    if(isNeedToAddStockTable(encounter_type,details)){
//                                        HnppApplication.getStockRepository().updateValue(encounter_type,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,base_entity_id,log.getVisitDate(),formSubmissionId);
//
//                                    }
//                                    if(EYE_TEST.equalsIgnoreCase(encounter_type)){
//                                        processEyeTest(details,log,formSubmissionId);
//                                    }
//                                    if (HOME_VISIT_FAMILY.equalsIgnoreCase(encounter_type)){
//                                        processHHVisitForm(details,log);
//                                    }
//                                    if(HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL.equalsIgnoreCase(encounter_type)){
//                                        HnppDBUtils.updateCoronaFamilyMember(base_entity_id,"false");
//                                    }
//                                }
//
//
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//
//            }
//        }
//        processImmunization();
//        //processAlreadySubmittedDataForStock();
//        processInstitutionalDeliveryForTarget();
    }
//
//    /**
//     * this will update the "Institutionalized Delivery" count at target table for already subitted forms only execute if on upgrader
//     */
//    private void processInstitutionalDeliveryForTarget(){
//       String value = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference("IS_UPGRADED");
//       if(TextUtils.isEmpty(value) || value.equalsIgnoreCase("0")){
//           return;
//       }
//        ArrayList<String> visit_ids = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getPregnancyOutcomeEvents();
//        Log.v("TARGET_FETCH","processInstitutionalDeliveryForTarget"+visit_ids.size());
//        for (int i = 0; i < visit_ids.size(); i++) {
//            List<Visit> v = AncLibrary.getInstance().visitRepository().getVisitsByVisitId(visit_ids.get(i));
//            for (Visit visit : v) {
//                Event baseEvent = gson.fromJson(visit.getJson(), Event.class);
//                String base_entity_id = baseEvent.getBaseEntityId();
//                HashMap<String,Object>form_details = getFormNamesFromEventObject(baseEvent);
//                ArrayList<String> encounter_types = (ArrayList<String>) form_details.get("form_name");
//                HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
//                for (String encounter_type : encounter_types) {
//                    if(encounter_type.equalsIgnoreCase(PREGNANCY_OUTCOME) || encounter_type.equalsIgnoreCase(PREGNANCY_OUTCOME_OOC)){
//                        if(details.containsKey("delivery_place")&&!StringUtils.isEmpty(details.get("delivery_place"))) {
//                            String delivery_place = details.get("delivery_place");
//                            if(!delivery_place.equalsIgnoreCase("home") || !delivery_place.equalsIgnoreCase("বাড়ি")){
//                                String ssName = HnppDBUtils.getSSName(base_entity_id);
//                                LocalDate localDate = new LocalDate(visit.getDate().getTime());
//                                HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.INSTITUTIONALIZES_DELIVERY,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,base_entity_id,visit.getFormSubmissionId());
//
//                            }
//
//                        }
//                    }
//
//                }
//                Log.v("TARGET_FETCH","processInstitutionalDeliveryForTarget>>done");
//            }
//        }
//        FamilyLibrary.getInstance().context().allSharedPreferences().savePreference("IS_UPGRADED","0");
//    }
//
////    /**
////     * this method searched already stored at local device but need to update the stock amount
////     */
////    private void processAlreadySubmittedDataForStock(){
////        //TODO need to remove this logic from 2.0.6 version production
////        ArrayList<String> visit_ids = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getNeedToUpdateAlreadyProccedVisit();
////        Log.v("STOCK_ADD","processAlreadySubmittedDataForStock"+visit_ids.size());
////        for (int i = 0; i < visit_ids.size(); i++) {
////            List<Visit> v = AncLibrary.getInstance().visitRepository().getVisitsByVisitId(visit_ids.get(i));
////            for (Visit visit : v) {
////                Event baseEvent = gson.fromJson(visit.getJson(), Event.class);
////                String base_entity_id = baseEvent.getBaseEntityId();
////                HashMap<String,Object>form_details = getFormNamesFromEventObject(baseEvent);
////                ArrayList<String> encounter_types = (ArrayList<String>) form_details.get("form_name");
////                HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
////                for (String encounter_type : encounter_types) {
////                    if(encounter_type.equalsIgnoreCase(ANC1_REGISTRATION_OOC)){
////                        encounter_type = HnppConstants.EVENT_TYPE.ANC1_REGISTRATION;
////                    }
////                    else if(encounter_type.equalsIgnoreCase(ANC2_REGISTRATION_OOC)){
////                        encounter_type = HnppConstants.EVENT_TYPE.ANC2_REGISTRATION;
////                    }
////                    else if(encounter_type.equalsIgnoreCase(ANC3_REGISTRATION_OOC)){
////                        encounter_type = HnppConstants.EVENT_TYPE.ANC3_REGISTRATION;
////                    }
////                    else if(encounter_type.equalsIgnoreCase(PNC_REGISTRATION_BEFORE_48_hour_OOC)){
////                        encounter_type = HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour;
////                    }
////                    else if(encounter_type.equalsIgnoreCase(PNC_REGISTRATION_AFTER_48_hour_OOC)){
////                        encounter_type = HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour;
////                    }
////                    if(isNeedToAddStockTableForExisting(encounter_type,details)){
////                        String ssName = HnppDBUtils.getSSName(base_entity_id);
////                        LocalDate localDate = new LocalDate(visit.getDate().getTime());
////
////                        HnppApplication.getStockRepository().updateValue(encounter_type,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,base_entity_id,visit.getDate().getTime(),visit.getFormSubmissionId());
////
////
////                    }
////                }
////                SQLiteDatabase db = CoreChwApplication.getInstance().getRepository().getReadableDatabase();
////
////                db.execSQL("UPDATE visits set processed='1' where visit_id='"+visit.getVisitId()+"'");
////                Log.v("STOCK_ADD","processAlreadySubmittedDataForStock>>done");
////            }
////        }
////    }
////    private boolean isNeedToAddStockTableForExisting(String eventType,HashMap<String, String> details){
////        String targetName = StockRepository.getTargetName(eventType);
////        if(TextUtils.isEmpty(targetName)) return false;
////        if(details.containsKey("add_to_stock")&&!StringUtils.isEmpty(details.get("add_to_stock"))) {
////            String value = details.get("add_to_stock");
////            Log.v("STOCK_ADD","isNeedToAddStockTable>>"+value);
////            if(!TextUtils.isEmpty(value) && value.equalsIgnoreCase("2")){
////                return true;
////
////            }
////
////        }
////        return false;
////    }
//
//    private boolean isNeedToAddStockTable(String eventType,HashMap<String, String> details){
//        String targetName = StockRepository.getTargetName(eventType);
//        if(TextUtils.isEmpty(targetName)) return false;
//        if(details.containsKey("add_to_stock")&&!StringUtils.isEmpty(details.get("add_to_stock"))) {
//            String value = details.get("add_to_stock");
//            Log.v("STOCK_ADD","isNeedToAddStockTable>>"+value);
//            if(!TextUtils.isEmpty(value) && value.equalsIgnoreCase("3")){
//                return true;
//
//            }
//
//        }
//        return false;
//    }
//    private void processHHVisitForm(HashMap<String, String> details, VisitLog log) {
//        try{
//            ContentValues values = new ContentValues();
//            HashMap<String, String> mapWithTable = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().tableHasColumn(details);
//            for(String key: mapWithTable.keySet()){
//                values.put(key,mapWithTable.get(key));
//            }
//
//            HnppApplication.getHNPPInstance().getHnppVisitLogRepository().updateFamilyFromHomeVisit(values,log.getBaseEntityId(),String.valueOf(log.getVisitDate()));
//
//        }catch (Exception e){
//
//        }
//
//    }
//
//    private void processSimprintsVerification(VisitLog log, HashMap<String, String> details) {
//        if(details.containsKey("is_verified")&&!StringUtils.isEmpty(details.get("is_verified"))) {
//            LocalDate localDate = new LocalDate(log.getVisitDate());
//            String value = details.get("is_verified");
//            if(!TextUtils.isEmpty(value) && value.equalsIgnoreCase("true")){
//                HnppApplication.getIndicatorRepository().updateValue("is_verified",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//            }
//
//        }
//        if(details.containsKey("is_identified")&&!StringUtils.isEmpty(details.get("is_identified"))) {
//            LocalDate localDate = new LocalDate(log.getVisitDate());
//            String value = details.get("is_identified");
//            if(!TextUtils.isEmpty(value) && value.equalsIgnoreCase("true")){
//                HnppApplication.getIndicatorRepository().updateValue("is_identified",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//            }
//
//        }
//    }
//
//    private void processIndicator(String baseEntityId,String encounter_type, VisitLog log, HashMap<String,String>details,String formSubmissionId){
//        LocalDate localDate = new LocalDate(log.getVisitDate());
//        switch (encounter_type){
//
//
//            case ELCO:
//                if(details.containsKey("familyplanning_method_known")&&!StringUtils.isEmpty(details.get("familyplanning_method_known"))) {
//                    String value = details.get("familyplanning_method_known");
//                    HnppApplication.getIndicatorRepository().updateValue("familyplanning_method_known",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("familyplanning_method")&&!StringUtils.isEmpty(details.get("familyplanning_method"))) {
//                    String value = details.get("familyplanning_method");
//                    HnppApplication.getIndicatorRepository().updateValue("familyplanning_method",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("distribution_location")&&!StringUtils.isEmpty(details.get("distribution_location"))) {
//                    String value = details.get("distribution_location");
//                    HnppApplication.getIndicatorRepository().updateValue("distribution_location",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                    break;
//            case HnppConstants.EventType.ANC_HOME_VISIT:
//            case ANC1_REGISTRATION:
//            case ANC2_REGISTRATION:
//            case ANC3_REGISTRATION:
//                if(details.containsKey("other_source_anc_1")&&!StringUtils.isEmpty(details.get("other_source_anc_1"))) {
//                    String value = details.get("other_source_anc_1");
//                        HnppApplication.getIndicatorRepository().updateValue(HnppConstants.INDICATOR.ANC_OTHER_SOURCE,"true",localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//
//
//                }
//                if(details.containsKey("other_source_anc_2")&&!StringUtils.isEmpty(details.get("other_source_anc_2"))) {
//                    String value = details.get("other_source_anc_2");
//
//                        HnppApplication.getIndicatorRepository().updateValue(HnppConstants.INDICATOR.ANC_OTHER_SOURCE, "true", localDate.getDayOfMonth() + "", localDate.getMonthOfYear() + "", localDate.getYear() + "", log.getSsName(), log.getBaseEntityId());
//
//                }
//                if(details.containsKey("other_source_anc_3")&&!StringUtils.isEmpty(details.get("other_source_anc_3"))) {
//                    String value = details.get("other_source_anc_3");
//
//                        HnppApplication.getIndicatorRepository().updateValue(HnppConstants.INDICATOR.ANC_OTHER_SOURCE, "true", localDate.getDayOfMonth() + "", localDate.getMonthOfYear() + "", localDate.getYear() + "", log.getSsName(), log.getBaseEntityId());
//
//                }
//                if(details.containsKey("vaccination_tt_dose_completed")&&!StringUtils.isEmpty(details.get("vaccination_tt_dose_completed"))) {
//                    String value = details.get("vaccination_tt_dose_completed");
//                    HnppApplication.getIndicatorRepository().updateValue(HnppConstants.INDICATOR.ANC_TT,value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("last_tt_vaccination_given")&&!StringUtils.isEmpty(details.get("last_tt_vaccination_given"))) {
//                    String value = details.get("last_tt_vaccination_given");
//                    if(!TextUtils.isEmpty(value)){
//                        if(value.equalsIgnoreCase("tt1") || value.equalsIgnoreCase("tt2") || value.equalsIgnoreCase("tt3")
//                         || value.equalsIgnoreCase("tt4")){
//                            HnppApplication.getIndicatorRepository().updateValue(HnppConstants.INDICATOR.ANC_TT,"yes",localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                        }
//
//                    }
//
//                }
//
//                break;
//            case PNC_REGISTRATION_AFTER_48_hour:
//            case PNC_REGISTRATION_BEFORE_48_hour:
//
//                if(details.containsKey("number_of_pnc")&&!StringUtils.isEmpty(details.get("number_of_pnc"))) {
//                    String value = details.get("number_of_pnc");
//                    HnppApplication.getIndicatorRepository().updateValue("number_of_pnc",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                break;
//
//            case WOMEN_REFERRAL:
//                if(details.containsKey("cause_of_referral_woman")&&!StringUtils.isEmpty(details.get("cause_of_referral_woman"))) {
//                    String value = details.get("cause_of_referral_woman");
//                    HnppApplication.getIndicatorRepository().updateValue("cause_of_referral_woman",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                break;
//            case PREGNANCY_OUTCOME:
//                if(details.containsKey("is_tt_completed")&&!StringUtils.isEmpty(details.get("is_tt_completed"))) {
//                    String value = details.get("is_tt_completed");
//                    if(value.equalsIgnoreCase("yes")){
//                        HnppApplication.getIndicatorRepository().updateValue(HnppConstants.INDICATOR.OUTCOME_TT,value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                    }
//
//                }
//                if(details.containsKey("no_anc_at_pregnant")&&!StringUtils.isEmpty(details.get("no_anc_at_pregnant"))) {
//                    String value = details.get("no_anc_at_pregnant");
//                    HnppApplication.getIndicatorRepository().updateValue("no_anc_at_pregnant",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//
//                if(details.containsKey("breastfeeding_time")&&!StringUtils.isEmpty(details.get("breastfeeding_time"))) {
//                    String value = details.get("breastfeeding_time");
//                    HnppApplication.getIndicatorRepository().updateValue("breastfeeding_time",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("delivery_method_c_section")&&!StringUtils.isEmpty(details.get("delivery_method_c_section"))) {
//                    String value = details.get("delivery_method_c_section");
//                    HnppApplication.getIndicatorRepository().updateValue("delivery_method_c_section",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("delivery_method_general")&&!StringUtils.isEmpty(details.get("delivery_method_general"))) {
//                    String value = details.get("delivery_method_general");
//                    HnppApplication.getIndicatorRepository().updateValue("delivery_method_general",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("breastfeeding_time")&&!StringUtils.isEmpty(details.get("breastfeeding_time"))) {
//                    String value = details.get("breastfeeding_time");
//                    HnppApplication.getIndicatorRepository().updateValue("breastfeeding_time",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("preg_outcome")&&!StringUtils.isEmpty(details.get("preg_outcome"))) {
//                    String value = details.get("preg_outcome");
//                    HnppApplication.getIndicatorRepository().updateValue("preg_outcome",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("delivery_place")&&!StringUtils.isEmpty(details.get("delivery_place"))) {
//                    String value = details.get("delivery_place");
//                    Log.v("TARGET_FETCH","delivery_place>>"+value);
//                    if(!value.equalsIgnoreCase("home") || !value.equalsIgnoreCase("বাড়ি")){
//                        HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.INSTITUTIONALIZES_DELIVERY,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId(),formSubmissionId);
//                    }
//
//                }
//                break;
//            case CHILD_FOLLOWUP:
//                String bfValue ="",efValue="";
//                if(details.containsKey("breast_feed_in_24hr")&&!StringUtils.isEmpty(details.get("breast_feed_in_24hr"))) {
//                    bfValue = details.get("breast_feed_in_24hr");
//
//                }
//                if(details.containsKey("extra_food_in_24hr")&&!StringUtils.isEmpty(details.get("extra_food_in_24hr"))) {
//                    efValue = details.get("extra_food_in_24hr");
//
//                }
//                if(!TextUtils.isEmpty(bfValue) && bfValue.equalsIgnoreCase("yes") && !TextUtils.isEmpty(efValue) && efValue.equalsIgnoreCase("no")){
//                    HnppApplication.getIndicatorRepository().updateValue(HnppConstants.INDICATOR.FEEDING_UPTO_6_MONTH,"true",localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("solid_food_month")&&!StringUtils.isEmpty(details.get("solid_food_month"))) {
//                    String value = details.get("solid_food_month");
//                    HnppApplication.getIndicatorRepository().updateValue("solid_food_month",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                    String prevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(baseEntityId+"_SOLID_FOOD");
//                    if(TextUtils.isEmpty(prevalue)){
//                        FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(baseEntityId+"_SOLID_FOOD",value);
//                    }
//                }
//                break;
//            case HnppConstants.EventType.REMOVE_MEMBER: {
//                if (details.containsKey("cause_of_death") && !StringUtils.isEmpty(details.get("cause_of_death"))) {
//                    String value = details.get("cause_of_death");
//                    HnppApplication.getIndicatorRepository().updateValue("cause_of_death", value, localDate.getDayOfMonth() + "", localDate.getMonthOfYear() + "", localDate.getYear() + "", log.getSsName(), log.getBaseEntityId());
//
//                }
//                if (details.containsKey("remove_reason") && !StringUtils.isEmpty(details.get("remove_reason"))) {
//                    String value = details.get("remove_reason");
//                    HnppApplication.getIndicatorRepository().updateValue("remove_reason", value, localDate.getDayOfMonth() + "", localDate.getMonthOfYear() + "", localDate.getYear() + "", log.getSsName(), log.getBaseEntityId());
//
//                }
//                String dod = "";
//                if (details.containsKey("date_died") && !StringUtils.isEmpty(details.get("date_died"))) {
//                    dod = details.get("date_died");
//
//                }
//                processRemoveMember(baseEntityId, log.getVisitDate(), dod);
//            }
//                break;
//            case HnppConstants.EventType.REMOVE_CHILD: {
//                if (details.containsKey("cause_of_death") && !StringUtils.isEmpty(details.get("cause_of_death"))) {
//                    String value = details.get("cause_of_death");
//                    HnppApplication.getIndicatorRepository().updateValue("cause_of_death", value, localDate.getDayOfMonth() + "", localDate.getMonthOfYear() + "", localDate.getYear() + "", log.getSsName(), log.getBaseEntityId());
//
//                }
//                if (details.containsKey("remove_reason") && !StringUtils.isEmpty(details.get("remove_reason"))) {
//                    String value = details.get("remove_reason");
//                    HnppApplication.getIndicatorRepository().updateValue("remove_reason", value, localDate.getDayOfMonth() + "", localDate.getMonthOfYear() + "", localDate.getYear() + "", log.getSsName(), log.getBaseEntityId());
//
//                }
//                String dod = "";
//                if (details.containsKey("date_died") && !StringUtils.isEmpty(details.get("date_died"))) {
//                    dod = details.get("date_died");
//
//                }
//                processRemoveChild(baseEntityId, log.getVisitDate(),dod);
//            }
//
//                break;
//            case HOME_VISIT_FAMILY:
//                if(details.containsKey("is_affected_member")&&!StringUtils.isEmpty(details.get("is_affected_member"))) {
//                    String value = details.get("is_affected_member");
//                    HnppApplication.getIndicatorRepository().updateValue("is_affected_member",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("member_count")&&!StringUtils.isEmpty(details.get("member_count"))) {
//                    String value = details.get("member_count");
//                    HnppApplication.getIndicatorRepository().updateValue("member_count",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                break;
//            case CORONA_INDIVIDUAL:
//                if(details.containsKey("corona_test_result")&&!StringUtils.isEmpty(details.get("corona_test_result"))) {
//                    String value = details.get("corona_test_result");
//                    HnppApplication.getIndicatorRepository().updateValue("corona_test_result",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                if(details.containsKey("isolation")&&!StringUtils.isEmpty(details.get("isolation"))) {
//                    String value = details.get("isolation");
//                    HnppApplication.getIndicatorRepository().updateValue("isolation",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//
//                }
//                break;
//        }
//
//    }
//
//    private void processReferral(String encounter_type, VisitLog log, HashMap<String,String>details, String formSubmissionId) {
//        if( HnppConstants.EVENT_TYPE.CHILD_REFERRAL.equalsIgnoreCase(encounter_type)){
//            if(details.containsKey("cause_of_referral_child")&&!StringUtils.isEmpty(details.get("cause_of_referral_child"))){
//                log.setReferReason(details.get("cause_of_referral_child"));
//
//            }
//            if(details.containsKey("place_of_referral")){
//                log.setReferPlace( details.get("place_of_referral"));
//            }
//
//        }else if( HnppConstants.EVENT_TYPE.WOMEN_REFERRAL.equalsIgnoreCase(encounter_type)){
//            if(details.containsKey("cause_of_referral_woman")&&!StringUtils.isEmpty(details.get("cause_of_referral_woman"))){
//                log.setReferReason(details.get("cause_of_referral_woman"));
//
//            }
//            if(details.containsKey("place_of_referral")){
//                log.setReferPlace( details.get("place_of_referral"));
//            }
//
//        }
//        else if( HnppConstants.EVENT_TYPE.MEMBER_REFERRAL.equalsIgnoreCase(encounter_type)){
//            if(details.containsKey("cause_of_referral_all")&&!StringUtils.isEmpty(details.get("cause_of_referral_all"))){
//
//                log.setReferReason(details.get("cause_of_referral_all"));
//                String cataractRefer =  details.get("cause_of_referral_all");
//                if(!TextUtils.isEmpty(cataractRefer) && cataractRefer.equalsIgnoreCase("cataract_problem")){
//                    LocalDate localDate = new LocalDate(log.getVisitDate());
//                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.CATARACT_SURGERY_REFER,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId(),formSubmissionId);
//
//                }
//
//
//            }
//            if(details.containsKey("place_of_referral")){
//                log.setReferPlace( details.get("place_of_referral"));
//            }
//
//        }
//        if(REFERREL_FOLLOWUP.equalsIgnoreCase(encounter_type)){
//            String refer_reason = "";
//            String place_of_refer = "";
//            if(details.containsKey("caused_referred")&&!StringUtils.isEmpty(details.get("caused_referred"))){
//                refer_reason = details.get("caused_referred");
//            }
//            if(details.containsKey("is_operation_done")&&!StringUtils.isEmpty(details.get("is_operation_done"))){
//                String operationDone = details.get("is_operation_done");
//                if(!TextUtils.isEmpty(operationDone) && operationDone.equalsIgnoreCase("Yes")){
//                    LocalDate localDate = new LocalDate(log.getVisitDate());
//                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.CATARACT_SURGERY,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId(),formSubmissionId);
//
//                }
//            }
//
//
//            log.setReferReason(refer_reason);
//
//            if(details.containsKey("place_referred")){
//                place_of_refer = details.get("place_of_referral");
//            }
//            log.setReferPlace(place_of_refer);
//
//
//        }
//    }
//    //TODO need to improvement
//
//    private void processEyeTest(HashMap<String, String> details, VisitLog visit, String formSubmissionId) {
//        if(details!=null){
//            if(details.containsKey("exam_result") && !StringUtils.isEmpty(details.get("exam_result"))) {
//                String known = details.get("exam_result");
//                if(!TextUtils.isEmpty(known) && known.equalsIgnoreCase("presbiopia")){
//                    LocalDate localDate = new LocalDate(visit.getVisitDate());
//                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.MARKED_PRESBYOPIA,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),formSubmissionId);
//                }
//            }
//            if(details.containsKey("glasses_sell") && !StringUtils.isEmpty(details.get("glasses_sell"))) {
//                String value = details.get("glasses_sell");
//                if(!TextUtils.isEmpty(value)){
//                    LocalDate localDate = new LocalDate(visit.getVisitDate());
//                    HnppApplication.getIndicatorRepository().updateValue("glasses_sell", value, localDate.getDayOfMonth() + "", localDate.getMonthOfYear() + "", localDate.getYear() + "", visit.getSsName(), visit.getBaseEntityId());
//                }
//            }
//            if(details.containsKey("is_need_glasses") && !StringUtils.isEmpty(details.get("is_need_glasses"))) {
//                String known = details.get("is_need_glasses");
//                if(!TextUtils.isEmpty(known) && known.equalsIgnoreCase("yes")){
//                    LocalDate localDate = new LocalDate(visit.getVisitDate());
//                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.PRESBYOPIA_CORRECTION,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),formSubmissionId);
//                    if(details.containsKey("add_to_stock") && !StringUtils.isEmpty(details.get("add_to_stock"))) {
//                        String add_to_stock = details.get("add_to_stock");
//                        if (!TextUtils.isEmpty(add_to_stock) && add_to_stock.equalsIgnoreCase("1")) {
//                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.GLASS,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//
//                        }
//                    }
//
//                }
//            }
//            if(details.containsKey("glasses_type") && !StringUtils.isEmpty(details.get("glasses_type"))) {
//                String known = details.get("glasses_type");
//                if(!TextUtils.isEmpty(known) && known.equalsIgnoreCase("sv")){
//                    if(details.containsKey("power") && !StringUtils.isEmpty(details.get("power"))) {
//                        String power = details.get("power");
//                        if(!TextUtils.isEmpty(power)) {
//                            if(details.containsKey("add_to_stock") && !StringUtils.isEmpty(details.get("add_to_stock"))) {
//                                String add_to_stock = details.get("add_to_stock");
//                                if (!TextUtils.isEmpty(add_to_stock) && add_to_stock.equalsIgnoreCase("1")) {
//                                    LocalDate localDate = new LocalDate(visit.getVisitDate());
//
//                                    switch (power){
//                                        case "1":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.SV_1,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//                                        case "1.5":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.SV_1_5,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//                                        case "2":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.SV_2,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//                                        case "2.5":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.SV_2_5,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//                                        case "3":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.SV_3,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//
//                                    }
//                                }
//                            }
//
//
//                        }
//
//                        }
//
//                }
//                else  if(!TextUtils.isEmpty(known) && known.equalsIgnoreCase("bf")){
//                    if(details.containsKey("power") && !StringUtils.isEmpty(details.get("power"))) {
//                        String power = details.get("power");
//                        if(!TextUtils.isEmpty(power)) {
//                            if(details.containsKey("add_to_stock") && !StringUtils.isEmpty(details.get("add_to_stock"))) {
//                                String add_to_stock = details.get("add_to_stock");
//                                if (!TextUtils.isEmpty(add_to_stock) && add_to_stock.equalsIgnoreCase("1")) {
//                                    LocalDate localDate = new LocalDate(visit.getVisitDate());
//
//                                    switch (power){
//                                        case "1":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.BF_1,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//                                        case "1.5":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.BF_1_5,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//                                        case "2":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.BF_2,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//                                        case "2.5":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.BF_2_5,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//                                        case "3":
//                                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.BF_3,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//                                            break;
//
//                                    }
//                                }
//                            }
//
//
//                        }
//
//                    }
//
//                }
//                else  if(!TextUtils.isEmpty(known) && known.equalsIgnoreCase("sg")){
//                    if(details.containsKey("add_to_stock") && !StringUtils.isEmpty(details.get("add_to_stock"))) {
//                        String add_to_stock = details.get("add_to_stock");
//                        if (!TextUtils.isEmpty(add_to_stock) && add_to_stock.equalsIgnoreCase("1")) {
//                            LocalDate localDate = new LocalDate(visit.getVisitDate());
//                            HnppApplication.getStockRepository().updateValue(HnppConstants.EVENT_TYPE.SUN_GLASS,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),visit.getVisitDate(),formSubmissionId);
//
//                        }
//                    }
//
//                }
//            }
//        }
//    }
//
//    private void updateFamilyPlanning(VisitLog visit,HashMap<String,String>details,String formSubmissionId){
//        if(details!=null){
//            if(details.containsKey("familyplanning_method_known") && !StringUtils.isEmpty(details.get("familyplanning_method_known"))) {
//                String known = details.get("familyplanning_method_known");
//                Log.v("IMMUNIZATION_ADD","update ado:"+known);
//                if(!TextUtils.isEmpty(known) && known.equalsIgnoreCase("yes")){
//                    LocalDate localDate = new LocalDate(visit.getVisitDate());
//                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.METHOD_USER,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),formSubmissionId);
//                    if(HnppDBUtils.isAdolescent(visit.getBaseEntityId())){
//                        HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ADO_METHOD_USER,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),formSubmissionId);
//
//                    }
//                }
//            }
//        }
//
//    }
//    private void updateNcdDiabeticsTarget(VisitLog visit,HashMap<String,String>details,String formSubmissionId){
//        if(details.containsKey("fasting_blood_sugar") && !StringUtils.isEmpty(details.get("fasting_blood_sugar"))){
//            String fbsValue = details.get("fasting_blood_sugar");
//            if(!TextUtils.isEmpty(fbsValue)){
//                float nP = Float.parseFloat(fbsValue);
//                if (nP>=7){
//                    LocalDate localDate = new LocalDate(visit.getVisitDate());
//                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ESTIMATE_DIABETES,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),formSubmissionId);
//                    return;
//                }
//
//            }
//        }
//        if(details.containsKey("random_blood_sugar") && !StringUtils.isEmpty(details.get("random_blood_sugar"))){
//            String rbs = details.get("random_blood_sugar");
//            if(!TextUtils.isEmpty(rbs)){
//                float h = Float.parseFloat(rbs);
//                if (h>=11.1){
//                    LocalDate localDate = new LocalDate(visit.getVisitDate());
//                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ESTIMATE_DIABETES,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),formSubmissionId);
//                    return;
//                }
//
//            }
//        }
//    }
//    private void updateNcdBpTarget(VisitLog visit,HashMap<String,String>details,String formSubmissionId){
//        LocalDate localDate = new LocalDate(visit.getVisitDate());
////        if(details.containsKey("blood_pressure_systolic") && !StringUtils.isEmpty(details.get("blood_pressure_systolic"))){
////            String fbsValue = details.get("blood_pressure_systolic");
////            if(!TextUtils.isEmpty(fbsValue)){
////                try{
////                    int bps = Integer.parseInt(fbsValue);
////                    if (bps>=140){
////                        HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ESTIMATE_HBP,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId());
////                    }
////                }catch (NumberFormatException e){
////
////                }
////
////
////            }
////        }
//        if(details.containsKey("cause_of_ncd") && !StringUtils.isEmpty(details.get("cause_of_ncd"))){
//            String fbsValue = details.get("cause_of_ncd");
//            Log.v("testValue: ",fbsValue);
//            if(!TextUtils.isEmpty(fbsValue)){
//                if (fbsValue.contains("high_blood_pressure")){
//                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ESTIMATE_HBP,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),formSubmissionId);
//                }
//
//            }
//        }
//        if(details.containsKey("cause_of_ncd") && !StringUtils.isEmpty(details.get("cause_of_ncd"))){
//            String fbsValue = details.get("cause_of_ncd");
//            Log.v("testValue: ",fbsValue);
//            if(!TextUtils.isEmpty(fbsValue)){
//                if (fbsValue.contains("diabetics")){
//                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ESTIMATE_DIABETES,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId(),formSubmissionId);
//                }
//
//            }
//        }
//
////        if(details.containsKey("blood_pressure_diastolic") && !StringUtils.isEmpty(details.get("blood_pressure_diastolic"))){
////            String bpd = details.get("blood_pressure_diastolic");
////            if(!TextUtils.isEmpty(bpd)){
////                try{
////                    int h = Integer.parseInt(bpd);
////                    if (h>=90){
////                        HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ESTIMATE_HBP,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId());
////                    }
////                }catch (NumberFormatException e){
////
////                }
////
////
////            }
////        }
//        //if(HnppConstants.isPALogin())HnppApplication.getTargetRepository().updateValue(NCD_BY_PA,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId());
//
//
//    }
//    private void updateNcdPackageRisk(String baseEntityId,HashMap<String,String>details){
//        if(details.containsKey("fasting_blood_sugar") && !StringUtils.isEmpty(details.get("fasting_blood_sugar"))){
//            String fbsValue = details.get("fasting_blood_sugar");
//            if(!TextUtils.isEmpty(fbsValue)){
//                float nP = Float.parseFloat(fbsValue);
//                if (nP>=7){
//                    RiskyModel riskyModel = new RiskyModel();
//                    riskyModel.riskyValue = fbsValue;
//                    riskyModel.riskyKey = "fasting_blood_sugar";
//                    riskyModel.eventType = NCD_PACKAGE;
//                    riskyModel.baseEntityId = baseEntityId;
//                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",NCD_PACKAGE);
//                    return;
//                }
//
//            }
//        }
//        if(details.containsKey("random_blood_sugar") && !StringUtils.isEmpty(details.get("random_blood_sugar"))){
//            String rbs = details.get("random_blood_sugar");
//            if(!TextUtils.isEmpty(rbs)){
//                float h = Float.parseFloat(rbs);
//                if (h>=11.1){
//                    RiskyModel riskyModel = new RiskyModel();
//                    riskyModel.riskyValue = rbs;
//                    riskyModel.riskyKey = "random_blood_sugar";
//                    riskyModel.eventType = NCD_PACKAGE;
//                    riskyModel.baseEntityId = baseEntityId;
//                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//
//                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",NCD_PACKAGE);
//                    return;
//                }
//
//            }
//        }
//        if(details.containsKey("blood_pressure_systolic") && !StringUtils.isEmpty(details.get("blood_pressure_systolic"))){
//            String fbsValue = details.get("blood_pressure_systolic");
//            if(!TextUtils.isEmpty(fbsValue)){
//                try{
//                    int bps = Integer.parseInt(fbsValue);
//                    if (bps>=140){
//                        RiskyModel riskyModel = new RiskyModel();
//                        riskyModel.riskyValue = fbsValue;
//                        riskyModel.riskyKey = "blood_pressure_systolic";
//                        riskyModel.eventType = NCD_PACKAGE;
//                        riskyModel.baseEntityId = baseEntityId;
//                        HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",NCD_PACKAGE);
//                        return;
//                    }
//                }catch (NumberFormatException e){
//
//                }
//
//
//            }
//        }
//        if(details.containsKey("cause_of_ncd") && !StringUtils.isEmpty(details.get("cause_of_ncd"))){
//            String fbsValue = details.get("cause_of_ncd");
//            Log.v("testValue: ",fbsValue);
//            if(!TextUtils.isEmpty(fbsValue)){
//                if (fbsValue.contains("high_blood_pressure")){
//                    RiskyModel riskyModel = new RiskyModel();
//                    riskyModel.riskyValue = "high_blood_pressure";
//                    riskyModel.riskyKey = "high_blood_pressure";
//                    riskyModel.eventType = NCD_PACKAGE;
//                    riskyModel.baseEntityId = baseEntityId;
//                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//
//                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",NCD_PACKAGE);
//                    return;
//                }
//
//            }
//        }
//        if(details.containsKey("cause_of_ncd") && !StringUtils.isEmpty(details.get("cause_of_ncd"))){
//            String fbsValue = details.get("cause_of_ncd");
//            Log.v("testValue: ",fbsValue);
//            if(!TextUtils.isEmpty(fbsValue)){
//                if (fbsValue.contains("diabetics")){
//                    RiskyModel riskyModel = new RiskyModel();
//                    riskyModel.riskyValue = "diabetics";
//                    riskyModel.riskyKey = "diabetics";
//                    riskyModel.eventType = NCD_PACKAGE;
//                    riskyModel.baseEntityId = baseEntityId;
//                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//
//                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",NCD_PACKAGE);
//                    return;
//                }
//
//            }
//        }
//        if(details.containsKey("blood_pressure_diastolic") && !StringUtils.isEmpty(details.get("blood_pressure_diastolic"))){
//            String bpd = details.get("blood_pressure_diastolic");
//            if(!TextUtils.isEmpty(bpd)){
//                try{
//                    int h = Integer.parseInt(bpd);
//                    if (h>=90){
//                        RiskyModel riskyModel = new RiskyModel();
//                        riskyModel.riskyValue = bpd;
//                        riskyModel.riskyKey = "blood_pressure_diastolic";
//                        riskyModel.eventType = NCD_PACKAGE;
//                        riskyModel.baseEntityId = baseEntityId;
//                        HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//
//                        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",NCD_PACKAGE);
//                        return;
//                    }
//                }catch (NumberFormatException e){
//
//                }
//
//
//            }
//        }
//        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",NCD_PACKAGE);
//        if(details.containsKey("suger_confirm_hospital") && !StringUtils.isEmpty(details.get("suger_confirm_hospital"))){
//            String sugerHospital = details.get("suger_confirm_hospital");
//            Log.v("SUGER_TEST","visitlog>>>sugerHospital:"+sugerHospital);
//
//            FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(baseEntityId+"_SUGER",sugerHospital);
//
//        }
//        if(details.containsKey("pressure_confirm_hospital") && !StringUtils.isEmpty(details.get("pressure_confirm_hospital"))){
//            String pressureHospital = details.get("pressure_confirm_hospital");
//            Log.v("SUGER_TEST","visitlog>>>pressureHospital:"+pressureHospital);
//            FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(baseEntityId+"_PRESSURE",pressureHospital);
//
//        }
//    }
//    private void updateAncRegistrationRisk(String baseEntityId,HashMap<String,String>details){
//        if(details.containsKey("no_prev_preg") && !StringUtils.isEmpty(details.get("no_prev_preg"))){
//            String ancValue = details.get("no_prev_preg");
//            if(!TextUtils.isEmpty(ancValue)){
//                try{
//                    int nP = Integer.parseInt(ancValue);
//                    if (nP>4){
//                        RiskyModel riskyModel = new RiskyModel();
//                        riskyModel.riskyValue = ancValue;
//                        riskyModel.riskyKey = "no_prev_preg";
//                        riskyModel.eventType = ANC_REGISTRATION;
//                        riskyModel.baseEntityId = baseEntityId;
//                        HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",ANC_REGISTRATION);
//                        return;
//                    }
//                }catch (NumberFormatException e){
//
//                }
//
//
//            }
//        }
//        if(details.containsKey("height") && !StringUtils.isEmpty(details.get("height"))){
//            String hight = details.get("height");
//            if(!TextUtils.isEmpty(hight)){
//                double h = Double.parseDouble(hight);
//                if (h<145){
//                    RiskyModel riskyModel = new RiskyModel();
//                    riskyModel.riskyValue = hight;
//                    riskyModel.riskyKey = "height";
//                    riskyModel.eventType = ANC_REGISTRATION;
//                    riskyModel.baseEntityId = baseEntityId;
//                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//
//                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",ANC_REGISTRATION);
//                    return;
//                }
//
//            }
//        }
//            HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",ANC_REGISTRATION);
//
//    }
//    private void updateAncHomeVisitRisk(String eventType , String baseEntityId,HashMap<String,String>details){
//        boolean isAncHomeVisitRisk = false;
//        if(details.containsKey("blood_pressure_systolic") && !StringUtils.isEmpty(details.get("blood_pressure_systolic"))){
//            String bps = details.get("blood_pressure_systolic");
//            if(!TextUtils.isEmpty(bps)){
//                try{
//                    int nBPS = Integer.parseInt(bps);
//                    if(details.containsKey("blood_pressure_diastolic") && !StringUtils.isEmpty(details.get("blood_pressure_diastolic"))){
//                        String bpd = details.get("blood_pressure_diastolic");
//                        if(!TextUtils.isEmpty(bpd)) {
//                            int nBPD = Integer.parseInt(bpd);
//                            if(details.containsKey("has_edema") && !StringUtils.isEmpty(details.get("has_edema"))){
//                                String edema = details.get("has_edema");
//                                if(!TextUtils.isEmpty(edema)) {
//                                    if(details.containsKey("albumin_test") && !StringUtils.isEmpty(details.get("albumin_test"))){
//                                        String albumin = details.get("albumin_test");
//                                        if(!TextUtils.isEmpty(albumin))
//                                        {
//                                            if(edema.equalsIgnoreCase("yes") && (nBPS >=120 || nBPD >= 80) && albumin.equalsIgnoreCase("yes")){
//
//                                                isAncHomeVisitRisk = true;
//
//                                                RiskyModel riskynBPSModel = new RiskyModel();
//                                                riskynBPSModel.riskyValue = bps;
//                                                riskynBPSModel.riskyKey = "blood_pressure_systolic";
//                                                riskynBPSModel.eventType = eventType;
//                                                riskynBPSModel.baseEntityId = baseEntityId;
//                                                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskynBPSModel);
//                                                RiskyModel riskynBPDModel = new RiskyModel();
//                                                riskynBPDModel.riskyValue = bpd;
//                                                riskynBPDModel.riskyKey = "blood_pressure_diastolic";
//                                                riskynBPDModel.eventType = eventType;
//                                                riskynBPDModel.baseEntityId = baseEntityId;
//                                                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskynBPDModel);
//                                                RiskyModel riskyedemaModel = new RiskyModel();
//                                                riskyedemaModel.riskyValue = edema;
//                                                riskyedemaModel.riskyKey = "has_edema";
//                                                riskyedemaModel.eventType = eventType;
//                                                riskyedemaModel.baseEntityId = baseEntityId;
//                                                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyedemaModel);
//
//                                                RiskyModel riskyalbuminModel = new RiskyModel();
//                                                riskyalbuminModel.riskyValue = albumin;
//                                                riskyalbuminModel.riskyKey = "albumin";
//                                                riskyalbuminModel.eventType = eventType;
//                                                riskyalbuminModel.baseEntityId = baseEntityId;
//                                                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyalbuminModel);
//
//                                            }
//
//                                        }
//
//                                    }
//
//
//                                }
//                            }
//                        }
//                    }
//
//                }catch (NumberFormatException e){
//
//                }
//
//            }
//        }
//        if(isAncHomeVisitRisk) {
//            HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EventType.ANC_HOME_VISIT);
//        }else {
//            HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",HnppConstants.EventType.ANC_HOME_VISIT);
//        }
//    }
//    private void updatePncRisk(String baseEntityId, HashMap<String, String> details, String encounter_type){
//        if(details.containsKey("excess_bleeding") && !StringUtils.isEmpty(details.get("excess_bleeding"))){
//            String eb = details.get("excess_bleeding");
//            if(!TextUtils.isEmpty(eb) && eb.equalsIgnoreCase("yes")){
//                    RiskyModel riskyModel = new RiskyModel();
//                    riskyModel.riskyValue = eb;
//                    riskyModel.riskyKey = "excess_bleeding";
//                    riskyModel.eventType = encounter_type;
//                    riskyModel.baseEntityId = baseEntityId;
//                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",encounter_type);
//                    return;
//
//            }
//        }
//        if(details.containsKey("obsessive_compulsive_disorder") && !StringUtils.isEmpty(details.get("obsessive_compulsive_disorder"))){
//            String obs = details.get("obsessive_compulsive_disorder");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                    RiskyModel riskyModel = new RiskyModel();
//                    riskyModel.riskyValue = obs;
//                    riskyModel.riskyKey = "obsessive_compulsive_disorder";
//                    riskyModel.eventType = encounter_type;
//                    riskyModel.baseEntityId = baseEntityId;
//                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",encounter_type);
//                    return;
//
//
//            }
//        }
//        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",PNC_REGISTRATION_BEFORE_48_hour); // todo
//
//    }
//
//    private void updatePhysicalProblemRisk(String baseEntityId,HashMap<String,String>details){
//        if(details.containsKey("high_blood_pressure") && !StringUtils.isEmpty(details.get("high_blood_pressure"))){
//            String eb = details.get("high_blood_pressure");
//            if(!TextUtils.isEmpty(eb) && eb.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = eb;
//                riskyModel.riskyKey = "high_blood_pressure";
//                riskyModel.eventType = ANC_GENERAL_DISEASE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
//                return;
//
//            }
//        }
//        if(details.containsKey("diabetes") && !StringUtils.isEmpty(details.get("diabetes"))){
//            String obs = details.get("diabetes");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = obs;
//                riskyModel.riskyKey = "diabetes";
//                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
//                return;
//
//
//            }
//        }
//        if(details.containsKey("heart_disease") && !StringUtils.isEmpty(details.get("heart_disease"))){
//            String obs = details.get("heart_disease");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = obs;
//                riskyModel.riskyKey = "heart_disease";
//                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
//                return;
//
//
//            }
//        }
//        if(details.containsKey("asthma") && !StringUtils.isEmpty(details.get("asthma"))){
//            String obs = details.get("asthma");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = obs;
//                riskyModel.riskyKey = "asthma";
//                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
//                return;
//
//
//            }
//        }
//        if(details.containsKey("kidney_disease") && !StringUtils.isEmpty(details.get("kidney_disease"))){
//            String obs = details.get("kidney_disease");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = obs;
//                riskyModel.riskyKey = "kidney_disease";
//                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
//                return;
//
//
//            }
//        }
//        if(details.containsKey("tuberculosis") && !StringUtils.isEmpty(details.get("tuberculosis"))){
//            String obs = details.get("tuberculosis");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = obs;
//                riskyModel.riskyKey = "tuberculosis";
//                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
//                return;
//
//
//            }
//        }
//        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
//
//    }
//    private void updatePreviousHistoryRisk(String baseEntityId,HashMap<String,String>details){
//        if(details.containsKey("abortion_mr") && !StringUtils.isEmpty(details.get("abortion_mr"))){
//            String eb = details.get("abortion_mr");
//            if(!TextUtils.isEmpty(eb) && eb.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = eb;
//                riskyModel.riskyKey = "abortion_mr";
//                riskyModel.eventType = ANC_PREGNANCY_HISTORY;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
//                return;
//
//            }
//        }
//        if(details.containsKey("still_birth") && !StringUtils.isEmpty(details.get("still_birth"))){
//            String obs = details.get("still_birth");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = obs;
//                riskyModel.riskyKey = "still_birth";
//                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
//                return;
//
//
//            }
//        }
//        if(details.containsKey("c_section") && !StringUtils.isEmpty(details.get("c_section"))){
//            String obs = details.get("c_section");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = obs;
//                riskyModel.riskyKey = "c_section";
//                riskyModel.eventType =HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
//                return;
//
//
//            }
//        }
//        if(details.containsKey("obsessive_compulsive_disorder") && !StringUtils.isEmpty(details.get("obsessive_compulsive_disorder"))){
//            String obs = details.get("obsessive_compulsive_disorder");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = obs;
//                riskyModel.riskyKey = "obsessive_compulsive_disorder";
//                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
//                return;
//
//
//            }
//        }
//        if(details.containsKey("postnatal_bleeding") && !StringUtils.isEmpty(details.get("postnatal_bleeding"))){
//            String obs = details.get("postnatal_bleeding");
//            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = obs;
//                riskyModel.riskyKey = "postnatal_bleeding";
//                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
//                return;
//
//
//            }
//        }
//        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
//
//    }
//
//    private void updateElcoRisk(String baseEntityId,HashMap<String,String>details){
//        if(details.containsKey("complications_known") && !StringUtils.isEmpty(details.get("complications_known"))){
//            String pck = details.get("complications_known");
//            if(!TextUtils.isEmpty(pck) && pck.equalsIgnoreCase("yes")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = pck;
//                riskyModel.riskyKey = "complications_known";
//                riskyModel.eventType = ELCO;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",ELCO);
//                return;
//
//
//            }else{
//                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",ELCO);
//            }
//        }
//
//    }
//    private void updateIYCFRisk(String baseEntityId,HashMap<String,String>details){
//        if(details.containsKey("head_balance") && !StringUtils.isEmpty(details.get("head_balance"))){
//            String head_balance = details.get("head_balance");
//            if(!TextUtils.isEmpty(head_balance) && head_balance.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = head_balance;
//                riskyModel.riskyKey = "head_balance";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_sit") && !StringUtils.isEmpty(details.get("can_sit"))){
//            String can_sit = details.get("can_sit");
//            if(!TextUtils.isEmpty(can_sit) && can_sit.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_sit;
//                riskyModel.riskyKey = "can_sit";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_sound") && !StringUtils.isEmpty(details.get("can_sound"))){
//            String can_sound = details.get("can_sound");
//            if(!TextUtils.isEmpty(can_sound) && can_sound.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_sound;
//                riskyModel.riskyKey = "can_sound";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_crap") && !StringUtils.isEmpty(details.get("can_crap"))){
//            String can_crap = details.get("can_crap");
//            if(!TextUtils.isEmpty(can_crap) && can_crap.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_crap;
//                riskyModel.riskyKey = "can_crap";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_sound_baba_maa") && !StringUtils.isEmpty(details.get("can_sound_baba_maa"))){
//            String can_sound_baba_maa = details.get("can_sound_baba_maa");
//            if(!TextUtils.isEmpty(can_sound_baba_maa) && can_sound_baba_maa.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_sound_baba_maa;
//                riskyModel.riskyKey = "can_sound_baba_maa";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_catch") && !StringUtils.isEmpty(details.get("can_catch"))){
//            String can_catch = details.get("can_catch");
//            if(!TextUtils.isEmpty(can_catch) && can_catch.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_catch;
//                riskyModel.riskyKey = "can_catch";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_make_word") && !StringUtils.isEmpty(details.get("can_make_word"))){
//            String can_make_word = details.get("can_make_word");
//            if(!TextUtils.isEmpty(can_make_word) && can_make_word.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_make_word;
//                riskyModel.riskyKey = "can_make_word";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_walk") && !StringUtils.isEmpty(details.get("can_walk"))){
//            String can_walk = details.get("can_walk");
//            if(!TextUtils.isEmpty(can_walk) && can_walk.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_walk;
//                riskyModel.riskyKey = "can_walk";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_two_sound") && !StringUtils.isEmpty(details.get("can_two_sound"))){
//            String can_two_sound = details.get("can_two_sound");
//            if(!TextUtils.isEmpty(can_two_sound) && can_two_sound.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_two_sound;
//                riskyModel.riskyKey = "can_two_sound";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_one_walk") && !StringUtils.isEmpty(details.get("can_one_walk"))){
//            String can_one_walk = details.get("can_one_walk");
//            if(!TextUtils.isEmpty(can_one_walk) && can_one_walk.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_one_walk;
//                riskyModel.riskyKey = "can_one_walk";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_run") && !StringUtils.isEmpty(details.get("can_run"))){
//            String can_run = details.get("can_run");
//            if(!TextUtils.isEmpty(can_run) && can_run.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_run;
//                riskyModel.riskyKey = "can_run";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        if(details.containsKey("can_make_sentance") && !StringUtils.isEmpty(details.get("can_make_sentance"))){
//            String can_make_sentance = details.get("can_make_sentance");
//            if(!TextUtils.isEmpty(can_make_sentance) && can_make_sentance.equalsIgnoreCase("no")){
//                RiskyModel riskyModel = new RiskyModel();
//                riskyModel.riskyValue = can_make_sentance;
//                riskyModel.riskyKey = "can_make_sentance";
//                riskyModel.eventType = IYCF_PACKAGE;
//                riskyModel.baseEntityId = baseEntityId;
//                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
//                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
//                return;
//
//            }
//        }
//        HnppDBUtils.updateIsRiskChild(baseEntityId,"false");
//
//
//    }
//    private boolean isForumEvent(String eventType){
//        switch (eventType) {
//            case HnppConstants.EVENT_TYPE.FORUM_CHILD:
//            case HnppConstants.EVENT_TYPE.FORUM_WOMEN:
//            case HnppConstants.EVENT_TYPE.FORUM_ADO:
//            case HnppConstants.EVENT_TYPE.FORUM_NCD:
//            case HnppConstants.EVENT_TYPE.FORUM_ADULT:
//                return true;
//            default:
//                return false;
//        }
//
//    }
//    private  void saveSSFormData(Visit visit)
//    {
//        try{
//            JSONObject form_object = new JSONObject(AssetHandler.readFileFromAssetsFolder("json.form/"+HnppConstants.JSON_FORMS.SS_FORM+".json",VisitLogIntentService.this));
//            Event baseEvent = gson.fromJson(visit.getJson(), Event.class);
//            String base_entity_id = baseEvent.getBaseEntityId();
//            HashMap<String,Object>form_details = getFormNamesFromEventObject(baseEvent);
//            HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
//
//            final CommonPersonObjectClient client = new CommonPersonObjectClient(base_entity_id, details, "");
//            client.setColumnmaps(details);
//
//           try{
//               for(int i= 1;i<9;i++){
//                   JSONObject steps = form_object.getJSONObject("step"+i);
//                   JSONArray jsonArray = steps.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
//
//                   for (int k = 0; k < jsonArray.length(); k++) {
//                       populateValuesForFormObject(client, jsonArray.getJSONObject(k));
//                   }
//               }
//               String monthValue = "", yearValue = "";
//               if(details.containsKey("month") && !StringUtils.isEmpty(details.get("month"))){
//                   monthValue = details.get("month");
//
//               }
//               if(details.containsKey("year") && !StringUtils.isEmpty(details.get("year"))){
//                   yearValue = details.get("year");
//
//               }
//               if(HnppJsonFormUtils.isCurrentMonth(monthValue,yearValue)){
//                   FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(HnppConstants.KEY_IS_SAME_MONTH,"true");
//               }
//
//           }catch (Exception e){
//               e.printStackTrace();
//           }
//            VisitLog log = new VisitLog();
//            log.setVisitId(visit.getVisitId());
//            log.setVisitType(visit.getVisitType());
//            log.setBaseEntityId(base_entity_id);
//            log.setFamilyId(HnppDBUtils.getFamilyIdFromBaseEntityId(base_entity_id));
//            log.setVisitDate(visit.getDate().getTime());
//            log.setEventType(visit.getVisitType());
//
//            log.setVisitJson(form_object.toString());
//           long inserted =  HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
//           if(inserted!=-1){
//
//               try{
//                   addSSFormToIndicator(log,details);
//               }catch (Exception e){
//                   e.printStackTrace();
//               }
//           }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//    }
//
//    /**
//     * keep all the ss forms value in this indicator table for dashboard
//     * @param log
//     * @param details
//     */
//
//    private void addSSFormToIndicator(VisitLog log,HashMap<String, String> details) {
//        LocalDate localDate = new LocalDate(log.getVisitDate());
//        String year = localDate.getYear()+"";
//        int month = localDate.getMonthOfYear();
//        String date = "01";
//        if(details.containsKey("ss_name")&&!StringUtils.isEmpty(details.get("ss_name"))) {
//            String value = details.get("ss_name");
//            if(!TextUtils.isEmpty(value))log.setSsName(value);
//
//        }
//        if(details.containsKey("year")&&!StringUtils.isEmpty(details.get("year"))) {
//            String value = details.get("year");
//            if(!TextUtils.isEmpty(value)){
//                year = value;
//            }
//
//        }
//        if(details.containsKey("month")&&!StringUtils.isEmpty(details.get("month"))) {
//            String value = details.get("month");
//            if(!TextUtils.isEmpty(value)){
//                month = HnppJsonFormUtils.getMonthFromMonthString(value);
//            }
//
//        }
//        if(TextUtils.isEmpty(log.getSsName())){
//            String ssName = HnppDBUtils.getSSName(log.getBaseEntityId());
//            log.setSsName(ssName);
//        }
//        if(details.containsKey("one_hour_after_birth") && !StringUtils.isEmpty(details.get("one_hour_after_birth"))) {
//            String value = details.get("one_hour_after_birth");
//            if(!TextUtils.isEmpty(value)){
//             HnppApplication.getIndicatorRepository().updateValue("one_hour_after_birth",value,date,month+"",year+"",log.getSsName(),log.getBaseEntityId());
//            }
//        }
//        if(details.containsKey("income_from_medicine") && !StringUtils.isEmpty(details.get("income_from_medicine"))) {
//            String value = details.get("income_from_medicine");
//            if(!TextUtils.isEmpty(value)){
//                HnppApplication.getIndicatorRepository().updateValue("income_from_medicine",value,date,month+"",year+"",log.getSsName(),log.getBaseEntityId());
//            }
//        }
//        if(details.containsKey("no_of_epi_present") && !StringUtils.isEmpty(details.get("no_of_epi_present"))) {
//            String value = details.get("no_of_epi_present");
//            if(!TextUtils.isEmpty(value)){
//                HnppApplication.getIndicatorRepository().updateValue("no_of_epi_present",value,date,month+"",year+"",log.getSsName(),log.getBaseEntityId());
//            }
//        }
//        if(details.containsKey("male_patient") && !StringUtils.isEmpty(details.get("male_patient"))) {
//            String value = details.get("male_patient");
//            if(!TextUtils.isEmpty(value)){
//                HnppApplication.getIndicatorRepository().updateValue("male_patient",value,date,month+"",year+"",log.getSsName(),log.getBaseEntityId());
//            }
//        }
//        if(details.containsKey("female_patient") && !StringUtils.isEmpty(details.get("female_patient"))) {
//            String value = details.get("female_patient");
//            if(!TextUtils.isEmpty(value)){
//                HnppApplication.getIndicatorRepository().updateValue("female_patient",value,date,month+"",year+"",log.getSsName(),log.getBaseEntityId());
//            }
//        }
//        if(details.containsKey("glass_metal_count") && !StringUtils.isEmpty(details.get("glass_metal_count"))) {
//            String value = details.get("glass_metal_count");
//            if(!TextUtils.isEmpty(value)){
//                HnppApplication.getIndicatorRepository().updateValue("glass_metal_count",value,date,month+"",year+"",log.getSsName(),log.getBaseEntityId());
//            }
//        }
//        if(details.containsKey("glass_plastic_count") && !StringUtils.isEmpty(details.get("glass_plastic_count"))) {
//            String value = details.get("glass_plastic_count");
//            if(!TextUtils.isEmpty(value)){
//                HnppApplication.getIndicatorRepository().updateValue("glass_plastic_count",value,date,month+"",year+"",log.getSsName(),log.getBaseEntityId());
//            }
//        }
//        if(details.containsKey("glass_sunglass_count") && !StringUtils.isEmpty(details.get("glass_sunglass_count"))) {
//            String value = details.get("glass_sunglass_count");
//            if(!TextUtils.isEmpty(value)){
//                HnppApplication.getIndicatorRepository().updateValue("glass_sunglass_count",value,date,month+"",year+"",log.getSsName(),log.getBaseEntityId());
//            }
//        }
//    }
//
//    private static synchronized void saveForumData(Visit visit,String formSubmissionId) {
//        switch (visit.getVisitType()){
//            case HnppConstants.EVENT_TYPE.FORUM_CHILD:
//            case HnppConstants.EVENT_TYPE.FORUM_WOMEN:
//            case HnppConstants.EVENT_TYPE.FORUM_ADO:
//            case HnppConstants.EVENT_TYPE.FORUM_NCD:
//            case HnppConstants.EVENT_TYPE.FORUM_ADULT:
//                if (visit.getJson() != null) {
//                    Event baseEvent = gson.fromJson(visit.getJson(), Event.class);
//                    List<Obs> obsList = baseEvent.getObs();
//                    ForumDetails forumDetails = new ForumDetails();
//                    for(Obs obs:obsList){
//                        try{
//                            String key = obs.getFormSubmissionField();
//
//                            if(key.equalsIgnoreCase("forumType")){
//                                forumDetails.forumType = (String) obs.getValue();
//                            }
//                            if(key.equalsIgnoreCase("forumName")){
//                                forumDetails.forumName = (String) obs.getValue();
//                            }
//                            if(key.equalsIgnoreCase("place")){
//                                String jsonFromMap = gson.toJson(obs.getValue());
//                                forumDetails.place = gson.fromJson(jsonFromMap, HHMemberProperty.class);
//                            }
//                            if(key.equalsIgnoreCase("participants")){
//                                String jsonFromMap = gson.toJson(obs.getValue());
//                                forumDetails.participants = gson.fromJson(jsonFromMap,  new TypeToken<ArrayList<HHMemberProperty>>() {
//                                }.getType());
//                            }
//                            if(key.equalsIgnoreCase("noOfParticipant")){
//                                forumDetails.noOfParticipant = (String) obs.getValue();
//                            }
//                            if(key.equalsIgnoreCase("forumDate")){
//                                forumDetails.forumDate = (String) obs.getValue();
//                            }
//                            if(key.equalsIgnoreCase("ssName")){
//                                forumDetails.ssName = (String) obs.getValue();
//                            }
//                            if(key.equalsIgnoreCase("villageName")){
//                                forumDetails.villageName =  (String) obs.getValue();
//                            }
//                            if(key.equalsIgnoreCase("clusterName")){
//                                forumDetails.clusterName =  (String) obs.getValue();
//                            }
//                            if(key.equalsIgnoreCase("noOfAdoTakeFiveFood")){
//                                forumDetails.noOfAdoTakeFiveFood = (String) obs.getValue();
//                            }
//                            if(key.equalsIgnoreCase("noOfServiceTaken")){
//                                forumDetails.noOfServiceTaken = (String) obs.getValue();
//                            }
//                            if(key.equalsIgnoreCase("sIndex")){
//                                double d =  (Double) obs.getValue();
//                                forumDetails.sIndex = (int)d;
//                            }
//                            if(key.equalsIgnoreCase("vIndex")){
//                                double d =  (Double) obs.getValue();
//                                forumDetails.vIndex =  (int) d;
//                            }
//                            if(key.equalsIgnoreCase("cIndex")){
//                                double d =  (Double) obs.getValue();
//                                forumDetails.cIndex =  (int) d;
//                            }
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//
//                    }
//                    if(!TextUtils.isEmpty(forumDetails.forumName)){
//                        VisitLog log = new VisitLog();
//                        log.setVisitId(visit.getVisitId());
//                        log.setVisitType(visit.getVisitType());
//                        log.setBaseEntityId(visit.getBaseEntityId());
//                        log.setVisitDate(visit.getDate().getTime());
//                        log.setEventType(visit.getVisitType());
//                        log.setVisitJson(gson.toJson(forumDetails));
//                        log.setFamilyId(forumDetails.place.getBaseEntityId());
//
//                        String ssName = HnppDBUtils.getSSName(visit.getBaseEntityId());
//                        log.setSsName(ssName);
//                        long inserted = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
//                        if(inserted != -1){
//                            try{
//                                LocalDate localDate = new LocalDate(visit.getDate().getTime());
//                                HnppApplication.getTargetRepository().updateValue(visit.getVisitType(),localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),formSubmissionId);
//                                if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_CHILD)){
//                                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_IYCF_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant),formSubmissionId);
//
//                                }else if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_WOMEN)){
//                                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_WOMEN_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant),formSubmissionId);
//
//                                }else if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_ADO)){
//                                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADO_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant),formSubmissionId);
//
//                                }else if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_NCD)){
//                                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_NCD_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant),formSubmissionId);
//
//                                }else if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_ADULT)){
//                                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ADULT_FORUM_SERVICE_TAKEN,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfServiceTaken),formSubmissionId);
//
//                                    if(HnppConstants.isPALogin()){
//                                        HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ADULT_FORUM_ATTENDANCE,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant),formSubmissionId);
//
//                                    }else{
//                                        HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant),formSubmissionId);
//
//                                    }
//                                }
//                            }catch (NumberFormatException e){
//
//                            }
//
//                        }
//
//                    }
//
//                }
//                break;
//            default:
//                break;
//
//
//        }
//
//    }
//    private void processImmunization(){
//        List<Visit> v = getImmunizationVisitsFromEvent();
//        for(Visit visit : v){
//            String eventJson = visit.getJson();
//            String formSubmissionId = visit.getFormSubmissionId();
//            if(!StringUtils.isEmpty(eventJson)){
//                try{
//                    Event baseEvent = gson.fromJson(eventJson, Event.class);
//                    String base_entity_id = baseEvent.getBaseEntityId();
//                    VisitLog log = new VisitLog();
//                    log.setVisitId(visit.getVisitId());
//                    log.setVisitType(visit.getVisitType());
//                    log.setBaseEntityId(base_entity_id);
//
//                    log.setVisitDate(visit.getDate().getTime());
//                    log.setEventType(visit.getVisitType());
//                    log.setVisitJson(eventJson);
//                    String ssName = HnppDBUtils.getSSName(base_entity_id);
//                    Log.v("IMMUNIZATION_ADD","ssname:"+ssName);
//                    log.setSsName(ssName);
//                    log.setFamilyId(HnppDBUtils.getFamilyIdFromBaseEntityId(base_entity_id));
//                    long rowId = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
//                    if(rowId != -1){
//                        LocalDate localDate = new LocalDate(log.getVisitDate());
//                        Log.v("IMMUNIZATION_ADD","update:"+ssName);
//                        HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.CHILD_IMMUNIZATION_0_59,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),formSubmissionId);
//                        updateBcg(baseEvent,log);
//                    }
//
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private List<Visit> getImmunizationVisitsFromEvent() {
//        List<Visit> v = new ArrayList<>();
//        String query = "SELECT event.baseEntityId,event.eventId, event.json,event.eventType FROM event WHERE (event.eventType = 'Vaccination' OR event.eventType = 'Recurring Service') AND event.eventId NOT IN (Select ec_visit_log.visit_id from ec_visit_log)";
//        Cursor cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
//        try{
//            if(cursor !=null && cursor.getCount() > 0) {
//                cursor.moveToFirst();
//                while (!cursor.isAfterLast()) {
//                    String baseEntityId = cursor.getString(0);
//                    String eventId = cursor.getString(1);
//                    String json = cursor.getString(2);
//                    String eventType = cursor.getString(3);
//                    Event baseEvent = gson.fromJson(json, Event.class);
//
//                    try {
//                        Visit visit = NCUtils.eventToVisit(baseEvent, eventId);
//                        v.add(visit);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    cursor.moveToNext();
//                }
//                cursor.close();
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            if(cursor !=null) cursor.close();
//        }
//
//        return v;
//    }
//    public static void populateValuesForFormObject(CommonPersonObjectClient client, JSONObject jsonObject) {
//        try {
//            String value = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY),false);
//
//            if (jsonObject.has("openmrs_choice_ids")) {
//                JSONObject choiceObject = jsonObject.getJSONObject("openmrs_choice_ids");
//                try{
//                    for (int i = 0; i < choiceObject.names().length(); i++) {
//                        if (value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))) {
//                            value = choiceObject.names().getString(i);
//                        }
//                    }
//                }catch ( Exception e){
//
//                }
//
//                if(!TextUtils.isEmpty(value)){
//                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,value);
//                }
//
//            }else if (jsonObject.has("options")) {
//                if(jsonObject.getString("key").equalsIgnoreCase("corona_affected_members")){
//                    JSONArray option_array = jsonObject.getJSONArray("options");
//                    String[] strs = value.split(",");
//                    if(strs.length == 0){
//
//                    }else{
//                        for(String name : strs){
//                            try{
//                                String[] nameIds = name.split("#");
//                                JSONObject item = new JSONObject();
//                                item.put("key",nameIds[0].replace(" ","_")+"#"+nameIds[1]);
//                                item.put("text",nameIds[0]);
//                                item.put("value",true);
//                                item.put("openmrs_entity","concept");
//                                item.put("openmrs_entity_id",nameIds[0].replace(" ","_")+"#"+nameIds[1]);
//                                option_array.put(item);
//                                HnppDBUtils.updateCoronaFamilyMember(nameIds[1],"true");
//                            }catch (Exception e){
//
//                            }
//
//                        }
//                    }
//
//                }
//
//                else{
//                    JSONArray option_array = jsonObject.getJSONArray("options");
//                    for (int i = 0; i < option_array.length(); i++) {
//                        JSONObject option = option_array.getJSONObject(i);
//                        if(jsonObject.getString("key").equalsIgnoreCase("preg_outcome")){
//                            String[] strs = value.split(",");
//                            for(String name : strs){
//                                if (name.equalsIgnoreCase(option.optString("key"))) {
//                                    option.put("value", "true");
//                                }
//                            }
//                        }else if(jsonObject.getString("key").equalsIgnoreCase("list_of_assets")){
//                            String[] strs = value.split(",");
//                            for(String name : strs){
//                                if (name.equalsIgnoreCase(option.optString("key"))) {
//                                    option.put("value", "true");
//                                }
//                            }
//                        }
//                        else if (value.contains(option.optString("key"))) {
//                            option.put("value", "true");
//                        }
//                    }
//                }
//
//            }
//            else{
//                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, value);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static JSONObject loadFormFromAsset(String encounter_type, Context context) {
//        String form_name = "";
//        switch (encounter_type) {
//            case ANC_PREGNANCY_HISTORY:
//                form_name = HnppConstants.JSON_FORMS.PREGNANCY_HISTORY + ".json";
//                break;
//            case ANC_GENERAL_DISEASE:
//                form_name = HnppConstants.JSON_FORMS.GENERAL_DISEASE + ".json";
//                break;
//            case ANC1_REGISTRATION:
//                form_name = HnppConstants.JSON_FORMS.ANC1_FORM + ".json";
//                break;
//            case ANC1_REGISTRATION_OOC:
//                form_name = HnppConstants.JSON_FORMS.ANC1_FORM_OOC + ".json";
//                break;
//            case ANC2_REGISTRATION:
//                form_name = HnppConstants.JSON_FORMS.ANC2_FORM + ".json";
//                break;
//            case ANC2_REGISTRATION_OOC:
//                form_name = HnppConstants.JSON_FORMS.ANC2_FORM_OOC + ".json";
//                break;
//            case ANC3_REGISTRATION:
//                form_name = HnppConstants.JSON_FORMS.ANC3_FORM + ".json";
//                break;
//            case ANC3_REGISTRATION_OOC:
//                form_name = HnppConstants.JSON_FORMS.ANC3_FORM_OOC + ".json";
//                break;
//            case MEMBER_REFERRAL:
//                form_name = HnppConstants.isPALogin()? HnppConstants.JSON_FORMS.MEMBER_REFERRAL + "_pa.json":HnppConstants.JSON_FORMS.MEMBER_REFERRAL + ".json";
//                break;
//            case HnppConstants.EVENT_TYPE.WOMEN_REFERRAL:
//                form_name = HnppConstants.JSON_FORMS.WOMEN_REFERRAL + ".json";
//                break;
//            case HnppConstants.EVENT_TYPE.CHILD_REFERRAL:
//                form_name = HnppConstants.JSON_FORMS.CHILD_REFERRAL + ".json";
//                break;
//            case PNC_REGISTRATION_AFTER_48_hour:
//                form_name = HnppConstants.JSON_FORMS.PNC_FORM_AFTER_48_HOUR + ".json";
//                break;
//            case PNC_REGISTRATION_BEFORE_48_hour:
//                form_name = HnppConstants.JSON_FORMS.PNC_FORM_BEFORE_48_HOUR + ".json";
//                break;
//
//            case PNC_REGISTRATION_BEFORE_48_hour_OOC:
//                form_name = HnppConstants.JSON_FORMS.PNC_FORM_BEFORE_48_HOUR_OOC + ".json";
//                break;
//            case PNC_REGISTRATION_AFTER_48_hour_OOC:
//                form_name = HnppConstants.JSON_FORMS.PNC_FORM_AFTER_48_HOUR_OOC + ".json";
//                break;
//            case ELCO:
//                form_name = HnppConstants.JSON_FORMS.ELCO + ".json";
//                break;
//            case NCD_PACKAGE:
//                form_name = HnppConstants.JSON_FORMS.NCD_PACKAGE + ".json";
//                break;
//            case WOMEN_PACKAGE:
//                form_name = HnppConstants.JSON_FORMS.WOMEN_PACKAGE + ".json";
//                break;
//            case GIRL_PACKAGE:
//                form_name = HnppConstants.JSON_FORMS.GIRL_PACKAGE + ".json";
//                break;
//            case IYCF_PACKAGE:
//                form_name = HnppConstants.JSON_FORMS.IYCF_PACKAGE + ".json";
//                break;
//            case ENC_REGISTRATION:
//                form_name = HnppConstants.JSON_FORMS.ENC_REGISTRATION + ".json";
//                break;
//            case HOME_VISIT_FAMILY:
//                form_name = HnppConstants.JSON_FORMS.HOME_VISIT_FAMILY + ".json";
//                break;
//            case REFERREL_FOLLOWUP:
//                form_name = HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP + ".json";
//                break;
//            case CHILD_FOLLOWUP:
//                form_name = HnppConstants.JSON_FORMS.CHILD_FOLLOWUP + ".json";
//                break;
//            case CHILD_INFO_EBF12:
//            case "Child Info EBF 1&2":
//                form_name = HnppConstants.JSON_FORMS.CHILD_INFO_EBF12 + ".json";
//                break;
//            case CHILD_INFO_7_24_MONTHS:
//                form_name = HnppConstants.JSON_FORMS.CHILD_INFO_7_24_MONTHS + ".json";
//                break;
//            case CHILD_INFO_25_MONTHS:
//                form_name = HnppConstants.JSON_FORMS.CHILD_INFO_25_MONTHS + ".json";
//                break;
//            case ANC_REGISTRATION:
//                form_name = HnppConstants.JSON_FORMS.ANC_FORM + ".json";
//                break;
//            case PREGNANCY_OUTCOME:
//                form_name = HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME + ".json";
//                break;
//            case PREGNANCY_OUTCOME_OOC:
//                form_name = HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME_OOC + ".json";
//
//                break;
//            case SS_INFO:
//                form_name = HnppConstants.JSON_FORMS.SS_FORM + ".json";
//                break;
//            case CORONA_INDIVIDUAL:
//                form_name = HnppConstants.JSON_FORMS.CORONA_INDIVIDUAL + ".json";
//                break;
//            case EYE_TEST:
//                form_name = HnppConstants.JSON_FORMS.EYE_TEST + ".json";
//                break;
//            case BLOOD_GROUP:
//                form_name = HnppConstants.JSON_FORMS.BLOOD_TEST + ".json";
//                break;
//            case HnppConstants.EventType.REMOVE_MEMBER:
//                form_name = "family_details_remove_member.json";
//                break;
//            case HnppConstants.EventType.REMOVE_CHILD:
//                form_name = "family_details_remove_child.json";
//                break;
//                default:
//                    break;
//        }
//
//
//        try {
//
//            String jsonString = AssetHandler.readFileFromAssetsFolder("json.form/"+form_name, context);
//            return new JSONObject(jsonString);
//        } catch (Exception e) {
//            Log.v("LOAD_FILE","file name:"+form_name+":encounter_type:"+encounter_type);
//            e.printStackTrace();
//        }
//        return new JSONObject();
//    }
//    private void updateBcg(Event baseEvent, VisitLog log){
//        for (Obs o : baseEvent.getObs()) {
//            if ("bcg".equalsIgnoreCase(o.getFormSubmissionField())) {
//                String value = (String)o.getValue();
//                LocalDate localDate = new LocalDate(log.getVisitDate());
//                HnppApplication.getIndicatorRepository().updateValue("bcg",value,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",log.getSsName(),log.getBaseEntityId());
//                break;
//            }
//        }
//
//    }
//
//    public static HashMap<String,Object> getFormNamesFromEventObject(Event baseEvent) {
//        ArrayList<String> forms = new ArrayList<>();
//        HashMap<String,Object>details = new HashMap<>();
//        for (Obs o : baseEvent.getObs()) {
//            if ("form_name".equalsIgnoreCase(o.getFormSubmissionField())) {
//                forms.add(o.getFieldCode());
//            }
//
//            if(details.containsKey(o.getFormSubmissionField())) {
//                details.put(o.getFormSubmissionField(),details.get(o.getFormSubmissionField())+","+o.getValue());
//            } else {
//
//                    if(o.getValue() == null){
//                       String value =(String)o.getHumanReadableValues().get(0);
//                        details.put(o.getFormSubmissionField(),value);
//                    }else{
//                        details.put(o.getFormSubmissionField(),o.getValue());
//                    }
//
//
//
//            }
//        }
//        HashMap<String,Object>form_details = new HashMap<>();
//        form_details.put("form_name",forms);
//        form_details.put("details",details);
//        return form_details;
//    }
//    protected void processRemoveMember(String baseEntityId, long eventDate, String dod) {
//
//
//        if (baseEntityId == null) {
//            return;
//        }
//
//        AllCommonsRepository commonsRepository = CoreChwApplication.getInstance().getAllCommonsRepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER);
//        if (commonsRepository != null) {
//
//            ContentValues values = new ContentValues();
//            values.put(DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(eventDate));
//            values.put(DBConstants.KEY.DOD, dod);
//            values.put("is_closed", 1);
//
//            CoreChwApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.FAMILY_MEMBER, values,
//                    DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseEntityId});
//
//            // clean fts table
//            CoreChwApplication.getInstance().getRepository().getWritableDatabase().update(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY_MEMBER), values,
//                    " object_id  = ?  ", new String[]{baseEntityId});
//
//            // Utils.context().commonrepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER).populateSearchValues(baseEntityId, DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(eventDate), null);
//
//        }
//    }
//
//    protected void processRemoveChild(String baseEntityId, long eventDate, String dod) {
//
//
//        if (baseEntityId == null) {
//            return;
//        }
//
//        AllCommonsRepository commonsRepository = CoreChwApplication.getInstance().getAllCommonsRepository(CoreConstants.TABLE_NAME.CHILD);
//        if (commonsRepository != null) {
//
//            ContentValues values = new ContentValues();
//            values.put(DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(eventDate));
//            values.put(DBConstants.KEY.DOD, dod);
//            values.put("is_closed", 1);
//
//            CoreChwApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.CHILD, values,
//                    DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseEntityId});
//            CoreChwApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.FAMILY_MEMBER, values,
//                    DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseEntityId});
//
//            // clean fts table
//            CoreChwApplication.getInstance().getRepository().getWritableDatabase().update(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.CHILD), values,
//                    CommonFtsObject.idColumn + "  = ?  ", new String[]{baseEntityId});
//
//            // Utils.context().commonrepository(CoreConstants.TABLE_NAME.CHILD).populateSearchValues(baseEntityId, DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(eventDate), null);
//
//        }
//    }
}
