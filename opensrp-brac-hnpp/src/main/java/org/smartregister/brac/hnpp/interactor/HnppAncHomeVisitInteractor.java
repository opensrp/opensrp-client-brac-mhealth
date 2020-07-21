package org.smartregister.brac.hnpp.interactor;

import android.content.Context;
import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.utils.ANCRegister;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppHomeVisitActionHelper;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.interactor.BaseAncHomeVisitInteractor;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import java.util.LinkedHashMap;

import static org.smartregister.brac.hnpp.utils.FormApplicability.getLmp;

public class HnppAncHomeVisitInteractor extends BaseAncHomeVisitInteractor {

    HnppHomeVisitActionHelper ANC1_FORMHelper;
    HnppHomeVisitActionHelper GENERAL_DISEASEHelper;
    HnppHomeVisitActionHelper PREGNANCY_HISTORYHelper;


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
                    HnppJsonFormUtils.addLastAnc(jsonPayload,memberObject.getBaseEntityId());
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
