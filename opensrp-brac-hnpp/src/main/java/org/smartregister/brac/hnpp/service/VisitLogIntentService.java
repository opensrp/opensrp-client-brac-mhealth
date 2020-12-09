package org.smartregister.brac.hnpp.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import net.sqlcipher.Cursor;


import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.RiskyModel;
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
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.util.AssetHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC1_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC1_REGISTRATION_OOC;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC2_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC2_REGISTRATION_OOC;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC3_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC3_REGISTRATION_OOC;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CHILD_REFERRAL;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ELCO;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.ENC_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.GIRL_PACKAGE;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.IYCF_PACKAGE;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.MEMBER_REFERRAL;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.NCD_PACKAGE;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PNC_REGISTRATION;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PNC_REGISTRATION_OOC;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME_OOC;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.SS_INFO;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.WOMEN_PACKAGE;
import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.WOMEN_REFERRAL;
import static org.smartregister.chw.anc.util.NCUtils.eventToVisit;
import static org.smartregister.util.JsonFormUtils.gson;

public class VisitLogIntentService extends IntentService {

    public VisitLogIntentService() {
        super("VisitLogService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<String> visit_ids = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitIds();
        for (int i = 0; i < visit_ids.size(); i++) {
            List<Visit> v = AncLibrary.getInstance().visitRepository().getVisitsByVisitId(visit_ids.get(i));
            //getANCRegistrationVisitsFromEvent(v);
            for (Visit visit : v) {
                if(isForumEvent(visit.getVisitType())){
                   saveForumData(visit);

                }else if(visit.getVisitType().equalsIgnoreCase(SS_INFO)){
                    saveSSFormData(visit);
                }
                else{

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
                                if(encounter_type.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME_OOC)){
                                    encounter_type = HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME;
                                }
                                else if(encounter_type.equalsIgnoreCase(ANC1_REGISTRATION_OOC)){
                                    encounter_type = HnppConstants.EVENT_TYPE.ANC1_REGISTRATION;
                                }
                                else if(encounter_type.equalsIgnoreCase(ANC2_REGISTRATION_OOC)){
                                    encounter_type = HnppConstants.EVENT_TYPE.ANC2_REGISTRATION;
                                }
                                else if(encounter_type.equalsIgnoreCase(ANC3_REGISTRATION_OOC)){
                                    encounter_type = HnppConstants.EVENT_TYPE.ANC3_REGISTRATION;
                                }
                                else if(encounter_type.equalsIgnoreCase(PNC_REGISTRATION_OOC)){
                                    encounter_type = HnppConstants.EVENT_TYPE.PNC_REGISTRATION;
                                }
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
                                    updateElcoRisk(base_entity_id,details);
                                }
                                if(ANC1_REGISTRATION.equalsIgnoreCase(encounter_type) || ANC2_REGISTRATION.equalsIgnoreCase(encounter_type)
                                        || ANC3_REGISTRATION.equalsIgnoreCase(encounter_type) || CoreConstants.EventType.ANC_HOME_VISIT.equalsIgnoreCase(encounter_type)){
                                    if(details.containsKey("brac_anc") && !StringUtils.isEmpty(details.get("brac_anc"))){
                                        String ancValue = details.get("brac_anc");
                                        String prevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(base_entity_id+"_BRAC_ANC");
                                        if(!TextUtils.isEmpty(prevalue)){
                                            int lastValue = Integer.parseInt(prevalue);
                                            int ancValueInt = Integer.parseInt(ancValue);
                                            if(ancValueInt >= lastValue){

                                                FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_ANC",(ancValueInt+1)+"");
                                            }
                                        }else{
                                            FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_ANC",1+"");
                                        }
                                    }
                                    updateAncHomeVisitRisk(encounter_type,base_entity_id,details);
                                }

                                if(PNC_REGISTRATION.equalsIgnoreCase(encounter_type)|| encounter_type.equalsIgnoreCase(CoreConstants.EventType.PNC_HOME_VISIT)){
                                    if(details.containsKey("brac_pnc") && !StringUtils.isEmpty(details.get("brac_pnc"))){
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
                                    if(details.containsKey("total_anc") && !StringUtils.isEmpty(details.get("brac_pnc"))){
                                        String ancValue = details.get("total_anc");
                                        if(!TextUtils.isEmpty(ancValue)){
                                            int count = Integer.parseInt(ancValue);
                                            FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_TOTAL_ANC",count+"");

                                        }
                                    }
                                    if(details.containsKey("is_delay") && !StringUtils.isEmpty(details.get("is_delay"))){
                                        String is_delay = details.get("is_delay");
                                        if(!TextUtils.isEmpty(is_delay)){
                                            String isDelay = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(base_entity_id+"_IS_DELAY");
                                            if(TextUtils.isEmpty(isDelay)){
                                                FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_IS_DELAY",is_delay);
                                                FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(visit.getVisitId()+"_IS_DELAY",is_delay);

                                            }

                                        }
                                    }
                                    updatePncRisk(base_entity_id,details);
                                }
                                if(ANC_REGISTRATION.equalsIgnoreCase(encounter_type)){
                                    FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_ANC",0+"");
                                    FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(base_entity_id+"_BRAC_PNC",0+"");
                                    updateAncRegistrationRisk(base_entity_id,details);
                                }
                                if(IYCF_PACKAGE.equalsIgnoreCase(encounter_type)){
                                   updateIYCFRisk(base_entity_id,details);
                                }
                                if(HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE.equalsIgnoreCase(encounter_type)){
                                    updatePhysicalProblemRisk(base_entity_id,details);
                                }
                                if(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY.equalsIgnoreCase(encounter_type)){
                                    updatePreviousHistoryRisk(base_entity_id,details);
                                }
                                if(HOME_VISIT_FAMILY.equalsIgnoreCase(encounter_type)){
                                    log.setFamilyId(base_entity_id);
                                }else{
                                    log.setFamilyId(HnppDBUtils.getFamilyIdFromBaseEntityId(base_entity_id));
                                }
                                log.setVisitDate(visit.getDate().getTime());
                                log.setEventType(encounter_type);
                                log.setVisitJson(form_object.toString());
                                String ssName = HnppDBUtils.getSSName(base_entity_id);
                                log.setSsName(ssName);

                                long isInserted = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
                                if(isInserted!=-1){
                                    LocalDate localDate = new LocalDate(visit.getDate().getTime());
                                    HnppApplication.getTargetRepository().updateValue(encounter_type,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,base_entity_id);
                                    if(ELCO.equalsIgnoreCase(encounter_type)){
                                        updateFamilyPlanning(log,details);
                                    }
                                    HnppApplication.getStockRepository().updateValue(encounter_type,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,base_entity_id);

                                }

                                if (HOME_VISIT_FAMILY.equalsIgnoreCase(encounter_type)){
                                    HnppApplication.getHNPPInstance().getHnppVisitLogRepository().updateFamilyLastHomeVisit(base_entity_id,String.valueOf(visit.getDate().getTime()));
                                }
                                if(HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL.equalsIgnoreCase(encounter_type)){
                                    HnppDBUtils.updateCoronaFamilyMember(base_entity_id,"false");
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
        }
        processImmunization();
    }
    private void updateFamilyPlanning(VisitLog visit,HashMap<String,String>details){
        if(details!=null){
            if(details.containsKey("familyplanning_method_known") && !StringUtils.isEmpty(details.get("familyplanning_method_known"))) {
                String known = details.get("familyplanning_method_known");
                Log.v("IMMUNIZATION_ADD","update ado:"+known);
                if(!TextUtils.isEmpty(known) && known.equalsIgnoreCase("yes")){
                    LocalDate localDate = new LocalDate(visit.getVisitDate());
                    HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.METHOD_USER,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId());
                    if(HnppDBUtils.isAdolescent(visit.getBaseEntityId())){
                        HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.ADO_METHOD_USER,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",visit.getSsName(),visit.getBaseEntityId());

                    }
                }
            }
        }

    }
    private void updateAncRegistrationRisk(String baseEntityId,HashMap<String,String>details){
        if(details.containsKey("no_prev_preg") && !StringUtils.isEmpty(details.get("no_prev_preg"))){
            String ancValue = details.get("no_prev_preg");
            if(!TextUtils.isEmpty(ancValue)){
                int nP = Integer.parseInt(ancValue);
                if (nP>4){
                    RiskyModel riskyModel = new RiskyModel();
                    riskyModel.riskyValue = ancValue;
                    riskyModel.riskyKey = "no_prev_preg";
                    riskyModel.eventType = ANC_REGISTRATION;
                    riskyModel.baseEntityId = baseEntityId;
                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",ANC_REGISTRATION);
                    return;
                }

            }
        }
        if(details.containsKey("height") && !StringUtils.isEmpty(details.get("height"))){
            String hight = details.get("height");
            if(!TextUtils.isEmpty(hight)){
                int h = Integer.parseInt(hight);
                if (h<145){
                    RiskyModel riskyModel = new RiskyModel();
                    riskyModel.riskyValue = hight;
                    riskyModel.riskyKey = "height";
                    riskyModel.eventType = ANC_REGISTRATION;
                    riskyModel.baseEntityId = baseEntityId;
                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);

                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",ANC_REGISTRATION);
                    return;
                }

            }
        }
            HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",ANC_REGISTRATION);

    }
    private void updateAncHomeVisitRisk(String eventType , String baseEntityId,HashMap<String,String>details){
        boolean isAncHomeVisitRisk = false;
        if(details.containsKey("blood_pressure_systolic") && !StringUtils.isEmpty(details.get("blood_pressure_systolic"))){
            String bps = details.get("blood_pressure_systolic");
            if(!TextUtils.isEmpty(bps)){
                int nBPS = Integer.parseInt(bps);
                if(details.containsKey("blood_pressure_diastolic") && !StringUtils.isEmpty(details.get("blood_pressure_diastolic"))){
                    String bpd = details.get("blood_pressure_diastolic");
                    if(!TextUtils.isEmpty(bpd)) {
                        int nBPD = Integer.parseInt(bpd);
                        if(details.containsKey("has_edema") && !StringUtils.isEmpty(details.get("has_edema"))){
                            String edema = details.get("has_edema");
                            if(!TextUtils.isEmpty(edema)) {
                                if(details.containsKey("albumin_test") && !StringUtils.isEmpty(details.get("albumin_test"))){
                                    String albumin = details.get("albumin_test");
                                    if(!TextUtils.isEmpty(albumin))
                                    {
                                        if(edema.equalsIgnoreCase("yes") && (nBPS >=120 || nBPD >= 80) && albumin.equalsIgnoreCase("yes")){

                                       isAncHomeVisitRisk = true;

                                        RiskyModel riskynBPSModel = new RiskyModel();
                                        riskynBPSModel.riskyValue = bps;
                                        riskynBPSModel.riskyKey = "blood_pressure_systolic";
                                        riskynBPSModel.eventType = eventType;
                                        riskynBPSModel.baseEntityId = baseEntityId;
                                        HnppApplication.getRiskDetailsRepository().addOrUpdate(riskynBPSModel);
                                        RiskyModel riskynBPDModel = new RiskyModel();
                                        riskynBPDModel.riskyValue = bpd;
                                        riskynBPDModel.riskyKey = "blood_pressure_diastolic";
                                        riskynBPDModel.eventType = eventType;
                                        riskynBPDModel.baseEntityId = baseEntityId;
                                        HnppApplication.getRiskDetailsRepository().addOrUpdate(riskynBPDModel);
                                        RiskyModel riskyedemaModel = new RiskyModel();
                                        riskyedemaModel.riskyValue = edema;
                                        riskyedemaModel.riskyKey = "has_edema";
                                        riskyedemaModel.eventType = eventType;
                                        riskyedemaModel.baseEntityId = baseEntityId;
                                        HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyedemaModel);

                                        RiskyModel riskyalbuminModel = new RiskyModel();
                                        riskyalbuminModel.riskyValue = albumin;
                                        riskyalbuminModel.riskyKey = "albumin";
                                        riskyalbuminModel.eventType = eventType;
                                        riskyalbuminModel.baseEntityId = baseEntityId;
                                        HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyalbuminModel);

                                        }

                                    }

                                    }


                            }
                        }
                    }
                }
            }
        }
        if(isAncHomeVisitRisk) {
            HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EventType.ANC_HOME_VISIT);
        }else {
            HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",HnppConstants.EventType.ANC_HOME_VISIT);
        }
    }
    private void updatePncRisk(String baseEntityId,HashMap<String,String>details){
        if(details.containsKey("excess_bleeding") && !StringUtils.isEmpty(details.get("excess_bleeding"))){
            String eb = details.get("excess_bleeding");
            if(!TextUtils.isEmpty(eb) && eb.equalsIgnoreCase("yes")){
                    RiskyModel riskyModel = new RiskyModel();
                    riskyModel.riskyValue = eb;
                    riskyModel.riskyKey = "excess_bleeding";
                    riskyModel.eventType = PNC_REGISTRATION;
                    riskyModel.baseEntityId = baseEntityId;
                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",PNC_REGISTRATION);
                    return;

            }
        }
        if(details.containsKey("obsessive_compulsive_disorder") && !StringUtils.isEmpty(details.get("obsessive_compulsive_disorder"))){
            String obs = details.get("obsessive_compulsive_disorder");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                    RiskyModel riskyModel = new RiskyModel();
                    riskyModel.riskyValue = obs;
                    riskyModel.riskyKey = "obsessive_compulsive_disorder";
                    riskyModel.eventType = PNC_REGISTRATION;
                    riskyModel.baseEntityId = baseEntityId;
                    HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                    HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",PNC_REGISTRATION);
                    return;


            }
        }
        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",PNC_REGISTRATION);

    }
    private void updatePhysicalProblemRisk(String baseEntityId,HashMap<String,String>details){
        if(details.containsKey("high_blood_pressure") && !StringUtils.isEmpty(details.get("high_blood_pressure"))){
            String eb = details.get("high_blood_pressure");
            if(!TextUtils.isEmpty(eb) && eb.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = eb;
                riskyModel.riskyKey = "high_blood_pressure";
                riskyModel.eventType = ANC_GENERAL_DISEASE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
                return;

            }
        }
        if(details.containsKey("diabetes") && !StringUtils.isEmpty(details.get("diabetes"))){
            String obs = details.get("diabetes");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = obs;
                riskyModel.riskyKey = "diabetes";
                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
                return;


            }
        }
        if(details.containsKey("heart_disease") && !StringUtils.isEmpty(details.get("heart_disease"))){
            String obs = details.get("heart_disease");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = obs;
                riskyModel.riskyKey = "heart_disease";
                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
                return;


            }
        }
        if(details.containsKey("asthma") && !StringUtils.isEmpty(details.get("asthma"))){
            String obs = details.get("asthma");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = obs;
                riskyModel.riskyKey = "asthma";
                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
                return;


            }
        }
        if(details.containsKey("kidney_disease") && !StringUtils.isEmpty(details.get("kidney_disease"))){
            String obs = details.get("kidney_disease");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = obs;
                riskyModel.riskyKey = "kidney_disease";
                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
                return;


            }
        }
        if(details.containsKey("tuberculosis") && !StringUtils.isEmpty(details.get("tuberculosis"))){
            String obs = details.get("tuberculosis");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = obs;
                riskyModel.riskyKey = "tuberculosis";
                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);
                return;


            }
        }
        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE);

    }
    private void updatePreviousHistoryRisk(String baseEntityId,HashMap<String,String>details){
        if(details.containsKey("abortion_mr") && !StringUtils.isEmpty(details.get("abortion_mr"))){
            String eb = details.get("abortion_mr");
            if(!TextUtils.isEmpty(eb) && eb.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = eb;
                riskyModel.riskyKey = "abortion_mr";
                riskyModel.eventType = ANC_PREGNANCY_HISTORY;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
                return;

            }
        }
        if(details.containsKey("still_birth") && !StringUtils.isEmpty(details.get("still_birth"))){
            String obs = details.get("still_birth");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = obs;
                riskyModel.riskyKey = "still_birth";
                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
                return;


            }
        }
        if(details.containsKey("c_section") && !StringUtils.isEmpty(details.get("c_section"))){
            String obs = details.get("c_section");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = obs;
                riskyModel.riskyKey = "c_section";
                riskyModel.eventType =HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
                return;


            }
        }
        if(details.containsKey("obsessive_compulsive_disorder") && !StringUtils.isEmpty(details.get("obsessive_compulsive_disorder"))){
            String obs = details.get("obsessive_compulsive_disorder");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = obs;
                riskyModel.riskyKey = "obsessive_compulsive_disorder";
                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
                return;


            }
        }
        if(details.containsKey("postnatal_bleeding") && !StringUtils.isEmpty(details.get("postnatal_bleeding"))){
            String obs = details.get("postnatal_bleeding");
            if(!TextUtils.isEmpty(obs) && obs.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = obs;
                riskyModel.riskyKey = "postnatal_bleeding";
                riskyModel.eventType = HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);
                return;


            }
        }
        HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY);

    }

    private void updateElcoRisk(String baseEntityId,HashMap<String,String>details){
        if(details.containsKey("complications_known") && !StringUtils.isEmpty(details.get("complications_known"))){
            String pck = details.get("complications_known");
            if(!TextUtils.isEmpty(pck) && pck.equalsIgnoreCase("yes")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = pck;
                riskyModel.riskyKey = "complications_known";
                riskyModel.eventType = ELCO;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"true",ELCO);
                return;


            }else{
                HnppDBUtils.updateIsRiskFamilyMember(baseEntityId,"false",ELCO);
            }
        }

    }
    private void updateIYCFRisk(String baseEntityId,HashMap<String,String>details){
        boolean isIycfRisk = false;
        if(details.containsKey("head_balance") && !StringUtils.isEmpty(details.get("head_balance"))){
            String head_balance = details.get("head_balance");
            if(!TextUtils.isEmpty(head_balance) && head_balance.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = head_balance;
                riskyModel.riskyKey = "head_balance";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_sit") && !StringUtils.isEmpty(details.get("can_sit"))){
            String can_sit = details.get("can_sit");
            if(!TextUtils.isEmpty(can_sit) && can_sit.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_sit;
                riskyModel.riskyKey = "can_sit";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_sound") && !StringUtils.isEmpty(details.get("can_sound"))){
            String can_sound = details.get("can_sound");
            if(!TextUtils.isEmpty(can_sound) && can_sound.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_sound;
                riskyModel.riskyKey = "can_sound";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_crap") && !StringUtils.isEmpty(details.get("can_crap"))){
            String can_crap = details.get("can_crap");
            if(!TextUtils.isEmpty(can_crap) && can_crap.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_crap;
                riskyModel.riskyKey = "can_crap";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_sound_baba_maa") && !StringUtils.isEmpty(details.get("can_sound_baba_maa"))){
            String can_sound_baba_maa = details.get("can_sound_baba_maa");
            if(!TextUtils.isEmpty(can_sound_baba_maa) && can_sound_baba_maa.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_sound_baba_maa;
                riskyModel.riskyKey = "can_sound_baba_maa";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_catch") && !StringUtils.isEmpty(details.get("can_catch"))){
            String can_catch = details.get("can_catch");
            if(!TextUtils.isEmpty(can_catch) && can_catch.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_catch;
                riskyModel.riskyKey = "can_catch";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_make_word") && !StringUtils.isEmpty(details.get("can_make_word"))){
            String can_make_word = details.get("can_make_word");
            if(!TextUtils.isEmpty(can_make_word) && can_make_word.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_make_word;
                riskyModel.riskyKey = "can_make_word";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_walk") && !StringUtils.isEmpty(details.get("can_walk"))){
            String can_walk = details.get("can_walk");
            if(!TextUtils.isEmpty(can_walk) && can_walk.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_walk;
                riskyModel.riskyKey = "can_walk";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_two_sound") && !StringUtils.isEmpty(details.get("can_two_sound"))){
            String can_two_sound = details.get("can_two_sound");
            if(!TextUtils.isEmpty(can_two_sound) && can_two_sound.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_two_sound;
                riskyModel.riskyKey = "can_two_sound";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_one_walk") && !StringUtils.isEmpty(details.get("can_one_walk"))){
            String can_one_walk = details.get("can_one_walk");
            if(!TextUtils.isEmpty(can_one_walk) && can_one_walk.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_one_walk;
                riskyModel.riskyKey = "can_one_walk";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_run") && !StringUtils.isEmpty(details.get("can_run"))){
            String can_run = details.get("can_run");
            if(!TextUtils.isEmpty(can_run) && can_run.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_run;
                riskyModel.riskyKey = "can_run";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        if(details.containsKey("can_make_sentance") && !StringUtils.isEmpty(details.get("can_make_sentance"))){
            String can_make_sentance = details.get("can_make_sentance");
            if(!TextUtils.isEmpty(can_make_sentance) && can_make_sentance.equalsIgnoreCase("no")){
                RiskyModel riskyModel = new RiskyModel();
                riskyModel.riskyValue = can_make_sentance;
                riskyModel.riskyKey = "can_make_sentance";
                riskyModel.eventType = IYCF_PACKAGE;
                riskyModel.baseEntityId = baseEntityId;
                HnppApplication.getRiskDetailsRepository().addOrUpdate(riskyModel);
                HnppDBUtils.updateIsRiskChild(baseEntityId,"true");
                return;

            }
        }
        HnppDBUtils.updateIsRiskChild(baseEntityId,"false");


    }
    private boolean isForumEvent(String eventType){
        switch (eventType) {
            case HnppConstants.EVENT_TYPE.FORUM_CHILD:
            case HnppConstants.EVENT_TYPE.FORUM_WOMEN:
            case HnppConstants.EVENT_TYPE.FORUM_ADO:
            case HnppConstants.EVENT_TYPE.FORUM_NCD:
            case HnppConstants.EVENT_TYPE.FORUM_ADULT:
                return true;
            default:
                return false;
        }

    }
    private  void saveSSFormData(Visit visit)
    {
        try{
            JSONObject form_object = new JSONObject(AssetHandler.readFileFromAssetsFolder("json.form/"+HnppConstants.JSON_FORMS.SS_FORM+".json",VisitLogIntentService.this));
            Event baseEvent = gson.fromJson(visit.getJson(), Event.class);
            String base_entity_id = baseEvent.getBaseEntityId();
            HashMap<String,Object>form_details = getFormNamesFromEventObject(baseEvent);
            HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");

            final CommonPersonObjectClient client = new CommonPersonObjectClient(base_entity_id, details, "");
            client.setColumnmaps(details);

           try{
               for(int i= 1;i<8;i++){
                   JSONObject steps = form_object.getJSONObject("step"+i);
                   JSONArray jsonArray = steps.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);

                   for (int k = 0; k < jsonArray.length(); k++) {
                       populateValuesForFormObject(client, jsonArray.getJSONObject(k));
                   }
               }
               String monthValue = "", yearValue = "";
               if(details.containsKey("month") && !StringUtils.isEmpty(details.get("month"))){
                   monthValue = details.get("month");

               }
               if(details.containsKey("year") && !StringUtils.isEmpty(details.get("year"))){
                   yearValue = details.get("year");

               }
               if(HnppJsonFormUtils.isCurrentMonth(monthValue,yearValue)){
                   FamilyLibrary.getInstance().context().allSharedPreferences().savePreference(HnppConstants.KEY_IS_SAME_MONTH,"true");
               }
           }catch (Exception e){
               e.printStackTrace();
           }
            VisitLog log = new VisitLog();
            log.setVisitId(visit.getVisitId());
            log.setVisitType(visit.getVisitType());
            log.setBaseEntityId(base_entity_id);
            log.setFamilyId(HnppDBUtils.getFamilyIdFromBaseEntityId(base_entity_id));
            log.setVisitDate(visit.getDate().getTime());
            log.setEventType(visit.getVisitType());
            log.setVisitJson(form_object.toString());
            HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    private static synchronized void saveForumData(Visit visit) {
        switch (visit.getVisitType()){
            case HnppConstants.EVENT_TYPE.FORUM_CHILD:
            case HnppConstants.EVENT_TYPE.FORUM_WOMEN:
            case HnppConstants.EVENT_TYPE.FORUM_ADO:
            case HnppConstants.EVENT_TYPE.FORUM_NCD:
            case HnppConstants.EVENT_TYPE.FORUM_ADULT:
                if (visit.getJson() != null) {
                    Event baseEvent = gson.fromJson(visit.getJson(), Event.class);
                    List<Obs> obsList = baseEvent.getObs();
                    ForumDetails forumDetails = new ForumDetails();
                    for(Obs obs:obsList){
                        try{
                            String key = obs.getFormSubmissionField();

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
                            e.printStackTrace();
                        }

                    }
                    if(!TextUtils.isEmpty(forumDetails.forumName)){
                        VisitLog log = new VisitLog();
                        log.setVisitId(visit.getVisitId());
                        log.setVisitType(visit.getVisitType());
                        log.setBaseEntityId(visit.getBaseEntityId());
                        log.setVisitDate(visit.getDate().getTime());
                        log.setEventType(visit.getVisitType());
                        log.setVisitJson(gson.toJson(forumDetails));
                        log.setFamilyId(forumDetails.place.getBaseEntityId());

                        String ssName = HnppDBUtils.getSSName(visit.getBaseEntityId());
                        log.setSsName(ssName);
                        long inserted = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
                        if(inserted != -1){
                            LocalDate localDate = new LocalDate(visit.getDate().getTime());
                            HnppApplication.getTargetRepository().updateValue(visit.getVisitType(),localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId());
                            if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_CHILD)){
                                HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_IYCF_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant));

                            }else if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_WOMEN)){
                                HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_WOMEN_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant));

                            }else if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_ADO)){
                                HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADO_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant));

                            }else if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_NCD)){
                                HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_NCD_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant));

                            }else if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.FORUM_ADULT)){
                                HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId(),Integer.parseInt(forumDetails.noOfParticipant));

                            }
                        }

                    }

                }
                break;
            default:
                break;


        }

    }
    private void processImmunization(){
        List<Visit> v = getImmunizationVisitsFromEvent();
        for(Visit visit : v){
            String eventJson = visit.getJson();
            if(!StringUtils.isEmpty(eventJson)){
                try{
                    Event baseEvent = gson.fromJson(eventJson, Event.class);
                    String base_entity_id = baseEvent.getBaseEntityId();
                    VisitLog log = new VisitLog();
                    log.setVisitId(visit.getVisitId());
                    log.setVisitType(visit.getVisitType());
                    log.setBaseEntityId(base_entity_id);

                    log.setVisitDate(visit.getDate().getTime());
                    log.setEventType(visit.getVisitType());
                    log.setVisitJson(eventJson);
                    String ssName = HnppDBUtils.getSSName(base_entity_id);
                    Log.v("IMMUNIZATION_ADD","ssname:"+ssName);
                    log.setSsName(ssName);
                    log.setFamilyId(HnppDBUtils.getFamilyIdFromBaseEntityId(base_entity_id));
                    long rowId = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().add(log);
                    if(rowId != -1){
                        LocalDate localDate = new LocalDate(log.getVisitDate());
                        Log.v("IMMUNIZATION_ADD","update:"+ssName);
                        HnppApplication.getTargetRepository().updateValue(HnppConstants.EVENT_TYPE.CHILD_IMMUNIZATION_0_59,localDate.getDayOfMonth()+"",localDate.getMonthOfYear()+"",localDate.getYear()+"",ssName,visit.getBaseEntityId());

                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Visit> getImmunizationVisitsFromEvent() {
        List<Visit> v = new ArrayList<>();
        String query = "SELECT event.baseEntityId,event.eventId, event.json,event.eventType FROM event WHERE (event.eventType = 'Vaccination' OR event.eventType = 'Recurring Service') AND event.eventId NOT IN (Select ec_visit_log.visit_id from ec_visit_log)";
        Cursor cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        try{
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
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor !=null) cursor.close();
        }

        return v;
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
    private static void populateValuesForFormObject(CommonPersonObjectClient client, JSONObject jsonObject) {
        try {
            String value = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(),jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY),false);
            //spinner
//            if(jsonObject.getString("key").equalsIgnoreCase("number_of_pnc")){
//
//                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUES,value);
//            }

            if (jsonObject.has("openmrs_choice_ids")) {
                JSONObject choiceObject = jsonObject.getJSONObject("openmrs_choice_ids");
                try{
                    for (int i = 0; i < choiceObject.names().length(); i++) {
                        if (value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))) {
                            value = choiceObject.names().getString(i);
                        }
                    }
                }catch ( Exception e){

                }

                if(!TextUtils.isEmpty(value)){
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,value);
                }

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
                if(jsonObject.getString("key").equalsIgnoreCase("corona_affected_members")){
                    JSONArray option_array = jsonObject.getJSONArray("options");
                    String[] strs = value.split(",");
                    if(strs.length == 0){

                    }else{
                        for(String name : strs){
                            String[] nameIds = name.split("#");
                            JSONObject item = new JSONObject();
                                item.put("key",nameIds[0].replace(" ","_")+"#"+nameIds[1]);
                                item.put("text",nameIds[0]);
                                item.put("value",true);
                                item.put("openmrs_entity","concept");
                                item.put("openmrs_entity_id",nameIds[0].replace(" ","_")+"#"+nameIds[1]);
                                option_array.put(item);
                                HnppDBUtils.updateCoronaFamilyMember(nameIds[1],"true");
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
        switch (encounter_type) {
            case ANC_PREGNANCY_HISTORY:
                form_name = HnppConstants.JSON_FORMS.PREGNANCY_HISTORY + ".json";
                break;
            case ANC_GENERAL_DISEASE:
                form_name = HnppConstants.JSON_FORMS.GENERAL_DISEASE + ".json";
                break;
            case ANC1_REGISTRATION:
                form_name = HnppConstants.JSON_FORMS.ANC1_FORM + ".json";
                break;
            case ANC1_REGISTRATION_OOC:
                form_name = HnppConstants.JSON_FORMS.ANC1_FORM_OOC + ".json";
                break;
            case ANC2_REGISTRATION:
                form_name = HnppConstants.JSON_FORMS.ANC2_FORM + ".json";
                break;
            case ANC2_REGISTRATION_OOC:
                form_name = HnppConstants.JSON_FORMS.ANC2_FORM_OOC + ".json";
                break;
            case ANC3_REGISTRATION:
                form_name = HnppConstants.JSON_FORMS.ANC3_FORM + ".json";
                break;
            case ANC3_REGISTRATION_OOC:
                form_name = HnppConstants.JSON_FORMS.ANC3_FORM_OOC + ".json";
                break;
            case MEMBER_REFERRAL:
                form_name = HnppConstants.JSON_FORMS.MEMBER_REFERRAL + ".json";
                break;
            case HnppConstants.EVENT_TYPE.WOMEN_REFERRAL:
                form_name = HnppConstants.JSON_FORMS.WOMEN_REFERRAL + ".json";
                break;
            case HnppConstants.EVENT_TYPE.CHILD_REFERRAL:
                form_name = HnppConstants.JSON_FORMS.CHILD_REFERRAL + ".json";
                break;
            case PNC_REGISTRATION:
                form_name = HnppConstants.JSON_FORMS.PNC_FORM + ".json";
                break;
            case PNC_REGISTRATION_OOC:
                form_name = HnppConstants.JSON_FORMS.PNC_FORM_OOC + ".json";
                break;
            case ELCO:
                form_name = HnppConstants.JSON_FORMS.ELCO + ".json";
                break;
            case NCD_PACKAGE:
                form_name = HnppConstants.JSON_FORMS.NCD_PACKAGE + ".json";
                break;
            case WOMEN_PACKAGE:
                form_name = HnppConstants.JSON_FORMS.WOMEN_PACKAGE + ".json";
                break;
            case GIRL_PACKAGE:
                form_name = HnppConstants.JSON_FORMS.GIRL_PACKAGE + ".json";
                break;
            case IYCF_PACKAGE:
                form_name = HnppConstants.JSON_FORMS.IYCF_PACKAGE + ".json";
                break;
            case ENC_REGISTRATION:
                form_name = HnppConstants.JSON_FORMS.ENC_REGISTRATION + ".json";
                break;
            case HOME_VISIT_FAMILY:
                form_name = HnppConstants.JSON_FORMS.HOME_VISIT_FAMILY + ".json";
                break;
            case REFERREL_FOLLOWUP:
                form_name = HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP + ".json";
                break;
            case CHILD_FOLLOWUP:
                form_name = HnppConstants.JSON_FORMS.CHILD_FOLLOWUP + ".json";
                break;
            case ANC_REGISTRATION:
                form_name = HnppConstants.JSON_FORMS.ANC_FORM + ".json";
                break;
            case PREGNANCY_OUTCOME:
                form_name = HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME + ".json";
                break;
            case PREGNANCY_OUTCOME_OOC:
                form_name = HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME_OOC + ".json";

                break;
            case SS_INFO:
                form_name = HnppConstants.JSON_FORMS.SS_FORM + ".json";
                break;
            case CORONA_INDIVIDUAL:
                form_name = HnppConstants.JSON_FORMS.CORONA_INDIVIDUAL + ".json";
                break;
                default:
                    break;
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

                    if(o.getValue() == null){
                       String value =(String)o.getHumanReadableValues().get(0);
                        details.put(o.getFormSubmissionField(),value);
                    }else{
                        details.put(o.getFormSubmissionField(),o.getValue());
                    }



            }
        }
        HashMap<String,Object>form_details = new HashMap<>();
        form_details.put("form_name",forms);
        form_details.put("details",details);
        return form_details;
    }
}
