package org.smartregister.brac.hnpp.interactor;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.utils.ANCRegister;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppHomeVisitActionHelper;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.interactor.BaseAncHomeVisitInteractor;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import java.util.LinkedHashMap;

public class HnppAncHomeVisitInteractor extends BaseAncHomeVisitInteractor {

    HnppHomeVisitActionHelper ANC1_FORMHelper;
    HnppHomeVisitActionHelper GENERAL_DISEASEHelper;
    HnppHomeVisitActionHelper PREGNANCY_HISTORYHelper;


    @Override
    public void calculateActions(final BaseAncHomeVisitContract.View view, final MemberObject memberObject, final BaseAncHomeVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {
            final LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

            try {
                HnppVisitLogRepository visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();

                Context context = view.getContext();

                String title1 = HnppConstants.visitEventTypeMapping.get(HnppConstants.JSON_FORMS.ANC1_FORM);
                String title2 = HnppConstants.visitEventTypeMapping.get(HnppConstants.JSON_FORMS.GENERAL_DISEASE);
                String title3 = HnppConstants.visitEventTypeMapping.get(HnppConstants.JSON_FORMS.PREGNANCY_HISTORY);
                ANC1_FORMHelper = new HnppHomeVisitActionHelper();
                BaseAncHomeVisitAction ANC1_FORM = new BaseAncHomeVisitAction.Builder(context,title1 )
                        .withOptional(false)
                        .withFormName(HnppConstants.JSON_FORMS.ANC1_FORM)
                        .withHelper(ANC1_FORMHelper)
                        .build();
                try {
                    JSONObject jsonPayload = new JSONObject(ANC1_FORM.getJsonPayload());
                    addEDDField(memberObject.getBaseEntityId(),HnppConstants.JSON_FORMS.ANC1_FORM,jsonPayload);
                    addHeightField(memberObject.getBaseEntityId(),HnppConstants.JSON_FORMS.ANC1_FORM,jsonPayload);
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
            JSONObject stepOne = null;
            try {
                stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                updateFormField(jsonArray,"height",visitLogRepository.getHeight(baseEntityId));
            } catch (JSONException e) {
                e.printStackTrace();
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
