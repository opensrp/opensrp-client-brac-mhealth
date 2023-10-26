package org.smartregister.brac.hnpp.fragment;

import android.content.Intent;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.FamilyRegisterActivity;
import org.smartregister.brac.hnpp.activity.IndividualProfileRemoveJsonFormActivity;
import org.smartregister.brac.hnpp.model.FamilyRemoveMemberModel;
import org.smartregister.chw.core.activity.CoreFamilyRegisterActivity;
import org.smartregister.chw.core.fragment.CoreFamilyProfileChangeDialog;
import org.smartregister.chw.core.fragment.CoreFamilyRemoveMemberFragment;
import org.smartregister.chw.core.fragment.FamilyRemoveMemberConfirmDialog;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.brac.hnpp.presenter.FamilyRemoveMemberPresenter;
import org.smartregister.brac.hnpp.provider.HfFamilyRemoveMemberProvider;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.util.HashMap;
import java.util.Set;

import timber.log.Timber;

public class FamilyRemoveMemberFragment extends CoreFamilyRemoveMemberFragment {

    public static final String DIALOG_TAG = FamilyRemoveMemberFragment.class.getSimpleName();

    public static CoreFamilyRemoveMemberFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        FamilyRemoveMemberFragment fragment = new FamilyRemoveMemberFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void startJsonActivity(JSONObject jsonObject) {
        // Intent intent = new Intent(getContext(), Utils.metadata().familyMemberFormActivity);
        Intent intent = new Intent(getActivity(), IndividualProfileRemoveJsonFormActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonObject.toString());

        Form form = new Form();
        form.setActionBarBackground(org.smartregister.family.R.color.family_actionbar);
        form.setWizard(false);
        form.setSaveLabel("জমা দিন");
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }
    @Override
    protected void setRemoveMemberProvider(Set visibleColumns, String familyHead, String primaryCaregiver, String familyBaseEntityId) {
        this.removeMemberProvider = new HfFamilyRemoveMemberProvider(familyBaseEntityId, this.getActivity(),
                this.commonRepository(), visibleColumns, new RemoveMemberListener(), new FooterListener(), familyHead, primaryCaregiver);
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        Timber.v(DIALOG_TAG, "setAdvancedSearchFormData");
    }

    @Override
    protected void setPresenter(String familyHead, String primaryCareGiver) {
        this.presenter = new FamilyRemoveMemberPresenter(this, new FamilyRemoveMemberModel(), null, familyBaseEntityId, familyHead, primaryCareGiver);
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
    protected String getRemoveFamilyMemberDialogTag() {
        return FamilyRemoveMemberFragment.DIALOG_TAG;
    }

    @Override
    public void confirmRemove(JSONObject form) {
        if (StringUtils.isNotBlank(memberName)) {
            FamilyRemoveMemberConfirmDialog dialog;
            if (processingFamily) {
                dialog = FamilyRemoveMemberConfirmDialog.newInstance(
                        String.format(getContext().getString(org.smartregister.chw.core.R.string.remove_warning_family), memberName, memberName)
                );

            } else {
                String title ="";
                JSONArray field = org.smartregister.util.JsonFormUtils.fields(form);
                JSONObject removeReasonObj = org.smartregister.util.JsonFormUtils.getFieldJSONObject(field, "remove_reason");
                try{
                    String value = removeReasonObj.getString(CoreJsonFormUtils.VALUE);
                    if(value.equalsIgnoreCase("মৃত্যু নিবন্ধন")){
                        title = String.format(getString(org.smartregister.chw.core.R.string.confirm_remove_text), memberName);
                    }else if(value.equalsIgnoreCase("স্থানান্তর")){
                        title = String.format(getString(R.string.confirm_migrate_text), memberName);
                    }else {
                        title = String.format(getString(R.string.confirm_other_text), memberName);
                    }
                }catch (Exception e){

                }
                dialog = FamilyRemoveMemberConfirmDialog.newInstance(
                        String.format(title, memberName)
                );
            }
            if (getFragmentManager() != null) {
                dialog.show(getFragmentManager(), getRemoveFamilyMemberDialogTag());
                dialog.setOnRemove(() -> getPresenter().processRemoveForm(form));
            }
        }
    }
}
