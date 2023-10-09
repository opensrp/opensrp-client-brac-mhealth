package org.smartregister.unicef.mis.fragment;

import android.content.Intent;
import android.os.Bundle;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.unicef.mis.activity.FamilyProfileActivity;
import org.smartregister.unicef.mis.activity.HnppChildProfileActivity;
import org.smartregister.unicef.mis.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.mis.model.HNPPFamilyProfileMemberModel;
import org.smartregister.unicef.mis.presenter.HnppBaseFamilyProfileMemberPresenter;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.provider.CoreMemberRegisterProvider;
import org.smartregister.unicef.mis.provider.HNPPMemberRegisterProvider;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.presenter.BaseFamilyProfileMemberPresenter;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.util.HashMap;
import java.util.Set;

import static org.smartregister.unicef.mis.activity.HnppFamilyOtherMemberProfileActivity.IS_COMES_IDENTITY;

import timber.log.Timber;

public class FamilyProfileMemberFragment extends BaseFamilyProfileMemberFragment {

    public static BaseFamilyProfileMemberFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        BaseFamilyProfileMemberFragment fragment = new FamilyProfileMemberFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns, String familyHead, String primaryCaregiver) {
        CoreMemberRegisterProvider chwMemberRegisterProvider = new HNPPMemberRegisterProvider(this.getActivity(), this.commonRepository(), visibleColumns, this.registerActionHandler, this.paginationViewHandler, familyHead, primaryCaregiver);
        this.clientAdapter = new RecyclerViewPaginatedAdapter(null, chwMemberRegisterProvider, this.context().commonrepository(this.tablename));
        this.clientAdapter.setCurrentlimit(20);
        this.clientsView.setAdapter(this.clientAdapter);
    }
    @Override
    protected void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
        int i = view.getId();
        if (i == org.smartregister.chw.core.R.id.patient_column) {
            if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
                goToProfileActivity(view);
            }
        } else if (i == org.smartregister.chw.core.R.id.next_arrow && view.getTag() != null &&
                view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NEXT_ARROW) {
            goToProfileActivity(view);
        }
        else if (i == org.smartregister.chw.core.R.id.primary_caregiver&& view.getTag() != null &&
                view.getTag(org.smartregister.family.R.id.VIEW_ID) == "click_add_child") {
            openChildForm(view);
        }
    }
    @Override
    protected void initializePresenter() {
        String familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        String familyHead = getArguments().getString(Constants.INTENT_KEY.FAMILY_HEAD);
        String primaryCareGiver = getArguments().getString(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        presenter = new HnppBaseFamilyProfileMemberPresenter(this, new HNPPFamilyProfileMemberModel(), null, familyBaseEntityId, familyHead, primaryCareGiver);
    }
    public void goToProfileActivity(android.view.View view) {
        if (view.getTag() instanceof CommonPersonObjectClient) {
            CommonPersonObjectClient commonPersonObjectClient = (CommonPersonObjectClient) view.getTag();
            String DOD = Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOD, false);
            if(StringUtils.isEmpty(DOD)){
                String entityType = Utils.getValue(commonPersonObjectClient.getColumnmaps(), ChildDBConstants.KEY.ENTITY_TYPE, false);
                if (CoreConstants.TABLE_NAME.FAMILY_MEMBER.equals(entityType)) {
                    goToOtherMemberProfileActivity(commonPersonObjectClient);
                } else {
                    goToChildProfileActivity(commonPersonObjectClient);
                }
            }

        }
    }
    public void openChildForm(android.view.View view){
        if(view.getTag() instanceof CommonPersonObjectClient){
            CommonPersonObjectClient commonPersonObjectClient = (CommonPersonObjectClient) view.getTag();
            String entityId = Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
            if(getActivity() instanceof FamilyProfileActivity){
                FamilyProfileActivity activity = (FamilyProfileActivity) getActivity();
                try{
                    activity.startChildFromWithMotherInfo(entityId);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public void goToOtherMemberProfileActivity(CommonPersonObjectClient patient) {
        String DOD = Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.DOD, false);
        if(StringUtils.isEmpty(DOD)){
            Intent intent = new Intent(getActivity(), getFamilyOtherMemberProfileActivityClass());
            intent.putExtras(getArguments());
            intent.putExtra(IS_COMES_IDENTITY,false);
            intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
            intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, patient);
            intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, ((BaseFamilyProfileMemberPresenter) presenter).getFamilyHead());
            intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, ((BaseFamilyProfileMemberPresenter) presenter).getPrimaryCaregiver());
            startActivity(intent);
        }

    }
    public void goToChildProfileActivity(CommonPersonObjectClient patient) {
        String DOD = Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.DOD, false);
        if(StringUtils.isEmpty(DOD)){
            String dobString = Utils.getDuration(Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.DOB, false));
            Integer yearOfBirth = CoreChildUtils.dobStringToYear(dobString);
            Intent intent;
            if (yearOfBirth != null && yearOfBirth >= 5) {
                goToOtherMemberProfileActivity(patient);
                return;
            } else {
                intent = new Intent(getActivity(), getChildProfileActivityClass());
            }
            if (getArguments() != null) {
                intent.putExtras(getArguments());
            }
            intent.putExtra(CoreConstants.INTENT_KEY.IS_COMES_FROM_FAMILY, true);
            intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
            intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, new MemberObject(patient));
            startActivity(intent);
        }

    }
    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        Timber.v("setAdvancedSearchFormData");
    }
    protected Class<?> getFamilyOtherMemberProfileActivityClass() {
        return HnppFamilyOtherMemberProfileActivity.class;
    }

    protected Class<? extends HnppChildProfileActivity> getChildProfileActivityClass() {
        return HnppChildProfileActivity.class;
    }

}
