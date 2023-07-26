package org.smartregister.unicef.dghs.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.core.contract.FamilyRemoveMemberContract;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.util.DBConstants;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.FamilyRegisterActivity;
import org.smartregister.unicef.dghs.activity.IndividualProfileRemoveJsonFormActivity;
import org.smartregister.unicef.dghs.model.FamilyRemoveMemberModel;
import org.smartregister.chw.core.fragment.CoreFamilyProfileChangeDialog;
import org.smartregister.chw.core.fragment.FamilyRemoveMemberConfirmDialog;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.dghs.presenter.FamilyRemoveMemberPresenter;
import org.smartregister.unicef.dghs.provider.FamilyRemoveMemberProvider;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;

import java.util.HashMap;
import java.util.Set;

import timber.log.Timber;

public class FamilyRemoveMemberFragment extends BaseFamilyProfileMemberFragment implements FamilyRemoveMemberContract.View {

    public static final String DIALOG_TAG = FamilyRemoveMemberFragment.class.getSimpleName();
    protected boolean processingFamily = false;
    protected String memberName;
    protected String familyBaseEntityId;
    protected FamilyRemoveMemberProvider removeMemberProvider;
    CommonPersonObjectClient pc;
    public static FamilyRemoveMemberFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        FamilyRemoveMemberFragment fragment = new FamilyRemoveMemberFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns, String familyHead, String primaryCaregiver) {
        setRemoveMemberProvider(visibleColumns, familyHead, primaryCaregiver, familyBaseEntityId);
        this.clientAdapter = new RecyclerViewPaginatedAdapter(null, removeMemberProvider, this.context().commonrepository(this.tablename));
        this.clientAdapter.setCurrentlimit(100);
        this.clientsView.setAdapter(this.clientAdapter);
    }
    @Override
    protected void initializePresenter() {
        if (getArguments() != null) {
            String familyHead = getArguments().getString(Constants.INTENT_KEY.FAMILY_HEAD);
            String primaryCareGiver = getArguments().getString(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
            familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
            setPresenter(familyHead, primaryCareGiver);
        }
    }
    @Override
    protected String getMainCondition() {
        return presenter().getMainCondition();
    }

    @Override
    protected String getDefaultSortQuery() {
        return presenter().getDefaultSortQuery();
    }
    @Override
    public void removeMember(CommonPersonObjectClient client) {
        getPresenter().removeMember(client);
    }

    public FamilyRemoveMemberContract.Presenter getPresenter() {
        return (FamilyRemoveMemberContract.Presenter) presenter;
    }
    @Override
    public void displayChangeFamilyHeadDialog(final CommonPersonObjectClient client, final String familyHeadID) {
        CoreFamilyProfileChangeDialog dialog = getChangeFamilyHeadDialog();
        dialog.setOnSaveAndClose(() -> {
            setFamilyHead(familyHeadID);
            refreshMemberList(FetchStatus.fetched);
            getPresenter().removeMember(client);
            refreshListView();
        });
        dialog.show(getActivity().getFragmentManager(), "FamilyProfileChangeDialogHF");
    }

    @Override
    public void displayChangeCareGiverDialog(final CommonPersonObjectClient client, final String careGiverID) {
        CoreFamilyProfileChangeDialog dialog = getChangeFamilyCareGiverDialog();
        dialog.setOnSaveAndClose(new Runnable() {
            @Override
            public void run() {
                setPrimaryCaregiver(careGiverID);
                refreshMemberList(FetchStatus.fetched);
                getPresenter().removeMember(client);
                refreshListView();
            }
        });

        dialog.show(getActivity().getFragmentManager(), "FamilyProfileChangeDialogPC");
    }
    public void refreshMemberList(final FetchStatus fetchStatus) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (fetchStatus.equals(FetchStatus.fetched)) {
                refreshListView();
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (fetchStatus.equals(FetchStatus.fetched)) {
                        refreshListView();
                    }

                }
            });
        }

    }
    @Override
    public void closeFamily(String familyName, String details) {
        getPresenter().removeEveryone(familyName, details);
    }
    @Override
    public void onMemberRemoved(String removalType) {
        // display alert
        if (getActivity() != null) {
            if (CoreConstants.EventType.REMOVE_FAMILY.equalsIgnoreCase(removalType)) {
                Intent intent = new Intent(getActivity(), getFamilyRegisterActivityClass());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                getActivity().finish();
            }
        }
    }

    @Override
    public void onEveryoneRemoved() {
        // close family and return to main register
        Intent intent = new Intent(getActivity(), getFamilyRegisterActivityClass());
        startActivity(intent);
        getActivity().finish();
    }
    @Override
    public void goToPrevious() {
        // open family register
        startActivity(new Intent(getContext(), getFamilyRegisterActivityClass()));
    }
    @Override
    public void startJsonActivity(JSONObject jsonObject) {
        // Intent intent = new Intent(getContext(), Utils.metadata().familyMemberFormActivity);

        CommonRepository commonRepository =Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);
        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(jsonObject.optString("entity_id"));

        try{
            HnppJsonFormUtils.addGender(jsonObject,commonPersonObject.getColumnmaps().get("gender"));
        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(getActivity(), IndividualProfileRemoveJsonFormActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonObject.toString());

        Form form = new Form();
        form.setActionBarBackground(org.smartregister.family.R.color.family_actionbar);
        form.setWizard(false);
        form.setSaveLabel("জমা দিন");
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }
    protected void setRemoveMemberProvider(Set visibleColumns, String familyHead, String primaryCaregiver, String familyBaseEntityId) {
        this.removeMemberProvider = new FamilyRemoveMemberProvider(familyBaseEntityId, this.getActivity(),
                this.commonRepository(), visibleColumns, new RemoveMemberListener(), new FooterListener(), familyHead, primaryCaregiver);
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        Timber.v(DIALOG_TAG, "setAdvancedSearchFormData");
    }

    protected void setPresenter(String familyHead, String primaryCareGiver) {
        this.presenter = new FamilyRemoveMemberPresenter(this, new FamilyRemoveMemberModel(), null, familyBaseEntityId, familyHead, primaryCareGiver);
    }

    protected Class<? extends FamilyRegisterActivity> getFamilyRegisterActivityClass() {
        return FamilyRegisterActivity.class;
    }

    protected CoreFamilyProfileChangeDialog getChangeFamilyCareGiverDialog() {
        return FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                CoreConstants.PROFILE_CHANGE_ACTION.PRIMARY_CARE_GIVER);
    }

    protected CoreFamilyProfileChangeDialog getChangeFamilyHeadDialog() {
        return FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                CoreConstants.PROFILE_CHANGE_ACTION.HEAD_OF_FAMILY);
    }

    protected String getRemoveFamilyMemberDialogTag() {
        return FamilyRemoveMemberFragment.DIALOG_TAG;
    }
    public class RemoveMemberListener implements android.view.View.OnClickListener {
        @Override
        public void onClick(android.view.View v) {
            if (v.getTag(org.smartregister.chw.core.R.id.VIEW_ID) == BaseFamilyProfileMemberFragment.CLICK_VIEW_NEXT_ARROW ||
                    v.getTag(org.smartregister.chw.core.R.id.VIEW_ID) == BaseFamilyProfileMemberFragment.CLICK_VIEW_NORMAL) {
                final CommonPersonObjectClient pc = (CommonPersonObjectClient) v.getTag();

                memberName = String.format("%s", pc.getColumnmaps().get(DBConstants.KEY.FIRST_NAME));

                String dod = pc.getColumnmaps().get(DBConstants.KEY.DOD);

                if (StringUtils.isBlank(dod)) {
                    processingFamily = false;
                    removeMember(pc);
                }
            }
        }
    }

    public class FooterListener implements android.view.View.OnClickListener {
        @Override
        public void onClick(final android.view.View v) {
            processingFamily = true;
            HashMap<String, String> payload = (HashMap<String, String>) v.getTag();
            String message = payload.get("message");
            memberName = payload.get("name");
            closeFamily(String.format(getString(org.smartregister.chw.core.R.string.family), memberName), message);
        }
    }
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
