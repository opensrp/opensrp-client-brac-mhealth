package org.smartregister.unicef.dghs.fragment;

import android.content.Intent;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.AncRegisterActivity;
import org.smartregister.unicef.dghs.activity.FamilyRegisterActivity;
import org.smartregister.unicef.dghs.activity.IndividualProfileRemoveActivity;
import org.smartregister.unicef.dghs.activity.IndividualProfileRemoveJsonFormActivity;
import org.smartregister.unicef.dghs.job.VisitLogServiceJob;
import org.smartregister.unicef.dghs.model.FamilyRemoveMemberModel;
import org.smartregister.unicef.dghs.presenter.FamilyRemoveMemberPresenter;
import org.smartregister.unicef.dghs.provider.FamilyRemoveMemberProvider;
import org.smartregister.unicef.dghs.service.HnppHomeVisitIntentService;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.activity.CoreAncRegisterActivity;
import org.smartregister.chw.core.activity.CoreFamilyRegisterActivity;
import org.smartregister.chw.core.fragment.CoreFamilyProfileChangeDialog;
import org.smartregister.chw.core.fragment.CoreIndividualProfileRemoveFragment;
import org.smartregister.chw.core.fragment.FamilyRemoveMemberConfirmDialog;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IndividualProfileRemoveFragment extends CoreIndividualProfileRemoveFragment {

    public static IndividualProfileRemoveFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        IndividualProfileRemoveFragment fragment = new IndividualProfileRemoveFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setRemoveMemberProvider(Set visibleColumns, String familyHead, String primaryCaregiver) {
        this.removeMemberProvider = new FamilyRemoveMemberProvider(familyBaseEntityId, this.getActivity(),
                this.commonRepository(), visibleColumns, null, null, familyHead, primaryCaregiver);
    }

    @Override
    protected void setPresenter(String familyHead, String primaryCareGiver) {
        this.presenter = new FamilyRemoveMemberPresenter(this, new FamilyRemoveMemberModel(), null, familyBaseEntityId, familyHead, primaryCareGiver);
    }

    @Override
    public void onMemberRemoved(String removalType) {
        if (getActivity() != null) {
            if (CoreConstants.EventType.REMOVE_FAMILY.equalsIgnoreCase(removalType)) {
                Intent intent = new Intent(getActivity(), FamilyRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            } else {
                if (getActivity() != null) {
                    if (getActivity() instanceof IndividualProfileRemoveActivity) {
                        IndividualProfileRemoveActivity p = (IndividualProfileRemoveActivity) getActivity();
                        p.onRemoveMember();
                    }
                }
            }
        }
    }
    @Override
    public void startJsonActivity(JSONObject jsonObject) {
        // Intent intent = new Intent(getContext(), Utils.metadata().familyMemberFormActivity);
        Intent intent = new Intent(getActivity(), IndividualProfileRemoveJsonFormActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonObject.toString());

        Form form = new Form();
        form.setActionBarBackground( HnppConstants.isReleaseBuild()?R.color.customAppThemeBlue:R.color.alert_urgent_red);
        form.setWizard(false);
        form.setSaveLabel("জমা দিন");
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }
    @Override
    public void onEveryoneRemoved() {
        if (getActivity() != null && getActivity() instanceof IndividualProfileRemoveActivity) {
            IndividualProfileRemoveActivity p = (IndividualProfileRemoveActivity) getActivity();
            p.onRemoveMember();
        }
    }

    @Override
    protected Class<? extends CoreFamilyRegisterActivity> getFamilyRegisterActivityClass() {
        return FamilyRegisterActivity.class;
    }

    @Override
    protected CoreFamilyProfileChangeDialog getChangeFamilyCareGiverDialog() {
        return FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                CoreConstants.PROFILE_CHANGE_ACTION.PRIMARY_CARE_GIVER);
    }

    @Override
    protected CoreFamilyProfileChangeDialog getChangeFamilyHeadDialog() {
        return FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                CoreConstants.PROFILE_CHANGE_ACTION.HEAD_OF_FAMILY);
    }

    @Override
    public void confirmRemove(final JSONObject form) {
        if (StringUtils.isNotBlank(memberName) && getFragmentManager() != null) {
            String title ="";
            JSONArray field = org.smartregister.util.JsonFormUtils.fields(form);
            JSONObject removeReasonObj = org.smartregister.util.JsonFormUtils.getFieldJSONObject(field, "remove_reason");
            try{
                String value = removeReasonObj.getString(CoreJsonFormUtils.VALUE);
                if(value.equalsIgnoreCase("মৃত্যু নিবন্ধন")){
                    title = String.format(getString(R.string.confirm_remove_text), memberName);
                }else if(value.equalsIgnoreCase("স্থানান্তর")){
                    title = String.format(getString(R.string.confirm_migrate_text), memberName);
                }else {
                    title = String.format(getString(R.string.confirm_other_text), memberName);
                }
            }catch (Exception e){

            }
            FamilyRemoveMemberConfirmDialog dialog = FamilyRemoveMemberConfirmDialog.newInstance(title);
            dialog.show(getFragmentManager(), FamilyRemoveMemberFragment.DIALOG_TAG);
            dialog.setOnRemove(() -> {
                //getPresenter().processRemoveForm(form);
                try{
                    String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                    type = HnppJsonFormUtils.getEncounterType(type);
                    Map<String, String> jsonStrings = new HashMap<>();
                    jsonStrings.put("First",form.toString());
                    String formSubmissionId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                    String visitId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                   Visit visit =  HnppJsonFormUtils.saveVisit(false,false,false,"", baseEntityId, type, jsonStrings, "",formSubmissionId,visitId);
                   if(visit !=null && !visit.getVisitId().equals("0")){
                       HnppHomeVisitIntentService.processVisits();
                       VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                   }
                }catch (Exception e){
                    e.printStackTrace();

                }


                Intent intent = new Intent(getActivity(), FamilyRegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
            dialog.setOnRemoveActivity(() -> {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }
    }

    @Override
    protected String getRemoveFamilyMemberDialogTag() {
        return FamilyRemoveMemberFragment.DIALOG_TAG;
    }

    @Override
    protected Class<? extends CoreAncRegisterActivity> getAncRegisterActivityClass() {
        return AncRegisterActivity.class;
    }

}
