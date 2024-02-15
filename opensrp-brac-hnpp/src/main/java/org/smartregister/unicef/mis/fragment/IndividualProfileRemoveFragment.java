package org.smartregister.unicef.mis.fragment;

import android.content.Intent;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.core.contract.FamilyRemoveMemberContract;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.util.DBConstants;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.FamilyRegisterActivity;
import org.smartregister.unicef.mis.activity.IndividualProfileRemoveActivity;
import org.smartregister.unicef.mis.activity.IndividualProfileRemoveJsonFormActivity;
import org.smartregister.unicef.mis.job.VisitLogServiceJob;
import org.smartregister.unicef.mis.model.FamilyRemoveMemberModel;
import org.smartregister.unicef.mis.model.GlobalLocationModel;
import org.smartregister.unicef.mis.presenter.FamilyRemoveMemberPresenter;
import org.smartregister.unicef.mis.provider.FamilyRemoveMemberProvider;
import org.smartregister.unicef.mis.repository.GlobalLocationRepository;
import org.smartregister.unicef.mis.service.HnppHomeVisitIntentService;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.fragment.CoreFamilyProfileChangeDialog;
import org.smartregister.chw.core.fragment.FamilyRemoveMemberConfirmDialog;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class IndividualProfileRemoveFragment extends BaseFamilyProfileMemberFragment implements FamilyRemoveMemberContract.View  {
    protected FamilyRemoveMemberProvider removeMemberProvider;
    protected String familyBaseEntityId;
    protected CommonPersonObjectClient pc;
    protected String memberName;
    protected String baseEntityId;
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
    public void initializeAdapter(Set<View> visibleColumns, String familyHead, String primaryCaregiver) {
        setRemoveMemberProvider(visibleColumns, familyHead, primaryCaregiver);
        this.clientAdapter = new RecyclerViewPaginatedAdapter(null, removeMemberProvider, this.context().commonrepository(this.tablename));
        this.clientAdapter.setCurrentlimit(0);
        this.clientsView.setAdapter(this.clientAdapter);
        this.clientsView.setVisibility(android.view.View.GONE);
    }
    @Override
    protected void initializePresenter() {
        if (getArguments() != null) {
            familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
            String familyHead = getArguments().getString(Constants.INTENT_KEY.FAMILY_HEAD);
            String primaryCareGiver = getArguments().getString(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
            pc = (CommonPersonObjectClient) getArguments().getSerializable(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON);
            setPresenter(familyHead, primaryCareGiver);
            openDeleteDialog();
        }
    }
    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        Timber.d("setAdvancedSearchFormData");
    }
    private void openDeleteDialog() {
        memberName = String.format("%s", pc.getColumnmaps().get(DBConstants.KEY.FIRST_NAME));

        String dod = pc.getColumnmaps().get(DBConstants.KEY.DOD);
        baseEntityId = pc.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID);
        if (StringUtils.isBlank(dod)) {
            getPresenter().removeMember(pc);
        }
    }
    public FamilyRemoveMemberContract.Presenter getPresenter() {
        return (FamilyRemoveMemberContract.Presenter) presenter;
    }
    @Override
    public void removeMember(CommonPersonObjectClient client) {
        getPresenter().removeMember(client);
    }
    @Override
    public void displayChangeFamilyHeadDialog(final CommonPersonObjectClient client, final String familyHeadID) {
        if (getActivity() != null && getActivity().getFragmentManager() != null) {
            CoreFamilyProfileChangeDialog dialog = getChangeFamilyHeadDialog();
            dialog.setOnSaveAndClose(new Runnable() {
                @Override
                public void run() {
                    setFamilyHead(familyHeadID);
                    getPresenter().removeMember(client);
                }
            });
            dialog.setOnRemoveActivity(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            });
            dialog.show(getActivity().getFragmentManager(), "FamilyProfileChangeDialogHF");
        }
    }
    protected CoreFamilyProfileChangeDialog getChangeFamilyHeadDialog() {
        return FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                CoreConstants.PROFILE_CHANGE_ACTION.HEAD_OF_FAMILY);
    }
    @Override
    public void displayChangeCareGiverDialog(final CommonPersonObjectClient client, final String careGiverID) {
        if (getActivity() != null && getActivity().getFragmentManager() != null) {
            CoreFamilyProfileChangeDialog dialog = getChangeFamilyCareGiverDialog();
            dialog.setOnSaveAndClose(new Runnable() {
                @Override
                public void run() {
                    setPrimaryCaregiver(careGiverID);
                    getPresenter().removeMember(client);
                }
            });
            dialog.setOnRemoveActivity(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            });
            dialog.show(getActivity().getFragmentManager(), "FamilyProfileChangeDialogPC");
        }
    }
    protected CoreFamilyProfileChangeDialog getChangeFamilyCareGiverDialog() {
        return FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                CoreConstants.PROFILE_CHANGE_ACTION.PRIMARY_CARE_GIVER);
    }
    @Override
    public void closeFamily(String familyName, String details) {

        getPresenter().removeEveryone(familyName, details);

    }

    @Override
    public void goToPrevious() {
        // open family register
        startActivity(new Intent(getContext(), FamilyRegisterActivity.class));
    }

    @Override
    protected String getMainCondition() {
        return "";
    }

    @Override
    protected String getDefaultSortQuery() {
        return "";
    }
    protected void setRemoveMemberProvider(Set visibleColumns, String familyHead, String primaryCaregiver) {

        this.removeMemberProvider = new FamilyRemoveMemberProvider(familyBaseEntityId, this.getActivity(),
                this.commonRepository(), visibleColumns, null, null, familyHead, primaryCaregiver);
    }

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
        ///setting gender data to  hidden gender field
        HnppJsonFormUtils.addGender(jsonObject,pc.getDetails().get("gender"));
        Intent intent = new Intent(getActivity(), IndividualProfileRemoveJsonFormActivity.class);
        try{
            JSONArray divJsonArray = new JSONArray();
            ArrayList<GlobalLocationModel> divModels = HnppApplication.getGlobalLocationRepository().getLocationByTagId(GlobalLocationRepository.LOCATION_TAG.DIVISION.getValue());
            for (GlobalLocationModel globalLocationModel:divModels){
                divJsonArray.put(globalLocationModel.name);
            }
            HnppJsonFormUtils.updateFormWithDivision(jsonObject,divJsonArray);
        }catch (Exception e){

        }
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonObject.toString());

        Form form = new Form();
        form.setActionBarBackground( HnppConstants.isReleaseBuild()?R.color.customAppThemeBlue:R.color.alert_urgent_red);
        form.setWizard(true);
        form.setSaveLabel(getActivity().getString(R.string.submit));
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
    public void confirmRemove(final JSONObject form) {
        if (StringUtils.isNotBlank(memberName) && getFragmentManager() != null) {
            String title ="";
            JSONArray field = org.smartregister.util.JsonFormUtils.fields(form);
            JSONObject removeReasonObj = org.smartregister.util.JsonFormUtils.getFieldJSONObject(field, "remove_reason");
            try{
                String value = removeReasonObj.getString(CoreJsonFormUtils.VALUE);
                if(value.equalsIgnoreCase(getActivity().getString(R.string.dead_reg))){
                    title = String.format(getString(R.string.confirm_remove_text), memberName);
                }else if(value.equalsIgnoreCase(getActivity().getString(R.string.replace_only))){
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
                   Visit visit =  HnppJsonFormUtils.saveVisit(baseEntityId, type, jsonStrings,formSubmissionId,visitId,form.toString());
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

}
