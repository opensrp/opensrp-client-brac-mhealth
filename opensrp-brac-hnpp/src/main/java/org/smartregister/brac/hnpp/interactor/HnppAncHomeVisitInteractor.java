package org.smartregister.brac.hnpp.interactor;

import android.content.Context;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.utils.ANCRegister;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppHomeVisitActionHelper;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.interactor.BaseAncHomeVisitInteractor;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.smartregister.brac.hnpp.utils.FormApplicability.getLmp;

public class HnppAncHomeVisitInteractor extends BaseAncHomeVisitInteractor {

    HnppHomeVisitActionHelper ANC1_FORMHelper;
    HnppHomeVisitActionHelper GENERAL_DISEASEHelper;
    HnppHomeVisitActionHelper PREGNANCY_HISTORYHelper;
    private boolean sIsIdentify,sNeedVerified,sIsVerify;
    private  String sNotVerifyText;
    private double latitude,longitude;
    private boolean isProcessing;

    public HnppAncHomeVisitInteractor(boolean isIdentify,boolean needVerified,boolean isVerify, String notVerifyText,double lat,double lng){
        sIsIdentify = isIdentify;
        sNeedVerified = needVerified;
        sIsVerify = isVerify;
        sNotVerifyText = notVerifyText;
        latitude = lat;
        longitude = lng;
    }

    @Override
    public void calculateActions(final BaseAncHomeVisitContract.View view, final MemberObject memberObject, final BaseAncHomeVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {
            final LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

            try {

                Context context = view.getContext();
                String lmp = getLmp(memberObject.getBaseEntityId());
                int dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp), new DateTime()).getDays();
                String eventType = FormApplicability.getANCEvent(dayPass);
                String formName = HnppConstants.eventTypeFormNameMapping.get(eventType);
                String title1 = HnppConstants.visitEventTypeMapping.get(formName);
                String title2 = HnppConstants.visitEventTypeMapping.get(HnppConstants.JSON_FORMS.GENERAL_DISEASE);
                String title3 = HnppConstants.visitEventTypeMapping.get(HnppConstants.JSON_FORMS.PREGNANCY_HISTORY);
                ANC1_FORMHelper = new HnppHomeVisitActionHelper();
                BaseAncHomeVisitAction ANC1_FORM = new BaseAncHomeVisitAction.Builder(context,title1 )
                        .withOptional(false)
                        .withFormName(formName)
                        .withHelper(ANC1_FORMHelper)
                        .build();
                try {
                    JSONObject jsonPayload = new JSONObject(ANC1_FORM.getJsonPayload());
                    addEDDField(memberObject.getBaseEntityId(),formName,jsonPayload);
                    addHeightField(memberObject.getBaseEntityId(),formName,jsonPayload);
                    HnppJsonFormUtils.addVerifyIdentify(jsonPayload,sIsIdentify,sNeedVerified,sIsVerify,sNotVerifyText);
                    HnppJsonFormUtils.addLastAnc(jsonPayload,memberObject.getBaseEntityId(),false);
                    try{
                        HnppJsonFormUtils.addAddToStockValue(jsonPayload);
                    }catch (Exception e){

                    }
                    try{
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonPayload,latitude,longitude);
                    }catch (Exception e){

                    }
                    try{
                        String[] weights = HnppDBUtils.getWeightFromBaseEntityId(memberObject.getBaseEntityId());
                        if(weights.length>0){
                            HnppJsonFormUtils.addJsonKeyValue(jsonPayload,"weight",weights[0]);
                            int monthDiff = FormApplicability.getMonthsDifference(new LocalDate(weights[1]),new LocalDate(System.currentTimeMillis()));
                            HnppJsonFormUtils.addJsonKeyValue(jsonPayload,"month_diff",monthDiff+"");
                            //national_id,birth_id,phone_number,blood_group
                            HnppJsonFormUtils.addJsonKeyValue(jsonPayload,"national_id",weights[2]+"");
                            HnppJsonFormUtils.addJsonKeyValue(jsonPayload,"birth_id",weights[3]+"");
                            HnppJsonFormUtils.addJsonKeyValue(jsonPayload,"phone_number",weights[4]+"");
                            HnppJsonFormUtils.addJsonKeyValue(jsonPayload,"blood_group",weights[5]+"");
                        }
                    }catch (Exception e){

                    }
                    ANC1_FORM.setJsonPayload(jsonPayload.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                GENERAL_DISEASEHelper = new HnppHomeVisitActionHelper();
                BaseAncHomeVisitAction GENERAL_DISEASE = new BaseAncHomeVisitAction.Builder(context,title2 )
                        .withOptional(false)
                        .withFormName(HnppConstants.JSON_FORMS.GENERAL_DISEASE)
                        .withHelper(GENERAL_DISEASEHelper)
                        .build();
                try {
                    JSONObject jsonPayload = new JSONObject(GENERAL_DISEASE.getJsonPayload());
                    GENERAL_DISEASE.setJsonPayload(jsonPayload.toString());

                }catch (Exception e){

                }

                PREGNANCY_HISTORYHelper = new HnppHomeVisitActionHelper();

                BaseAncHomeVisitAction PREGNANCY_HISTORY = new BaseAncHomeVisitAction.Builder(context, title3)
                        .withOptional(false)
                        .withFormName(HnppConstants.JSON_FORMS.PREGNANCY_HISTORY)
                        .withHelper(PREGNANCY_HISTORYHelper)
                        .build();
                try {
                    JSONObject jsonPayload = new JSONObject(PREGNANCY_HISTORY.getJsonPayload());
                    PREGNANCY_HISTORY.setJsonPayload(jsonPayload.toString());
                }catch (Exception e){

                }

                actionList.put(title3, PREGNANCY_HISTORY);
                actionList.put(title2, GENERAL_DISEASE);
                actionList.put(title1, ANC1_FORM);




            } catch (BaseAncHomeVisitAction.ValidationException e) {
                e.printStackTrace();
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void submitVisit(boolean editMode, String memberID, Map<String, BaseAncHomeVisitAction> map, BaseAncHomeVisitContract.InteractorCallBack callBack) {
        HnppConstants.appendLog("SAVE_VISIT","submitVisit>>>memberID:"+memberID+":isProcessing:"+isProcessing);

        if(TextUtils.isEmpty(memberID)){
           callBack.onSubmitted(false);
           return;
       }
        if(isProcessing) return;
        AtomicBoolean isSave = new AtomicBoolean(false);
        final Runnable runnable = () -> {
            try {
                if(!isProcessing) {
                    isProcessing = true;
                    String formSubmissionId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                    String visitId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                    HnppConstants.appendLog("SAVE_VISIT","submitVisit>>>memberID:"+memberID+":formSubmissionId:"+formSubmissionId);

                    isSave.set(submitVisit(memberID, map,formSubmissionId,visitId)!=null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            appExecutors.mainThread().execute(() -> {
                callBack.onSubmitted(isSave.get());
                isProcessing = false;
            });
        };

        appExecutors.diskIO().execute(runnable);
    }
    private Visit submitVisit(final String memberID, final Map<String, BaseAncHomeVisitAction> map,String formSubmissionId, String visitId){
        // create a map of the different types

        Map<String, String> combinedJsons = new HashMap<>();
        // aggregate forms to be processed
        for (Map.Entry<String, BaseAncHomeVisitAction> entry : map.entrySet()) {
            String json = entry.getValue().getJsonPayload();
            try {
                JSONObject form = new JSONObject(json);
                HnppJsonFormUtils.setEncounterDateTime(form);
                json = form.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (StringUtils.isNotBlank(json)) {
                combinedJsons.put(entry.getKey(), json);
            }
        }

        String type = StringUtils.isBlank("") ? getEncounterType() : getEncounterType();
        HnppConstants.appendLog("SAVE_VISIT","submitVisit>>>memberID:"+memberID+":formSubmissionId:"+formSubmissionId+":type:"+type);

        // persist to database
        Visit visit = null;
        try {
            visit = HnppJsonFormUtils.saveVisit(memberID, type, combinedJsons,formSubmissionId,visitId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return visit;
    }

    public void addHeightField(String baseEntityId, String formName, JSONObject jsonForm) {
        if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC1_FORM)||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC2_FORM)||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC3_FORM)) {
            HnppVisitLogRepository visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
            String heightValue = visitLogRepository.getHeight(baseEntityId);
            if(!TextUtils.isEmpty(heightValue)){
                JSONObject stepOne = null;
                try {
                    stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                    JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                    updateFormField(jsonArray,"height",heightValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }
    }
    public void addEDDField(String baseEntityId, String formName, JSONObject jsonForm){
        if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC1_FORM)||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC2_FORM)||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC3_FORM)){
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
    private void updateFormField(JSONArray formFieldArrays, String formFeildKey, String updateValue) {
        if (updateValue != null) {
            JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(formFieldArrays, formFeildKey);
            if (formObject != null) {
                try {
                    formObject.remove(org.smartregister.util.JsonFormUtils.VALUE);
                    formObject.put(org.smartregister.util.JsonFormUtils.VALUE, updateValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
