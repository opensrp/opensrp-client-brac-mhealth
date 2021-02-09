package org.smartregister.brac.hnpp.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppChildProfileActivity;
import org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.brac.hnpp.model.HnppAllMemberRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.HnppAllMemberRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.provider.HnppAllMemberRegisterProvider;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.activity.CoreChildHomeVisitActivity;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.util.Set;

public class HnppAllMemberRegisterFragment extends HnppBaseChildRegisterFragment implements android.view.View.OnClickListener {

    protected boolean isNeedToShowDateFilter = false;
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new HnppAllMemberRegisterFragmentPresenter(this, new HnppAllMemberRegisterFragmentModel(), viewConfigurationIdentifier);

    }
    @Override
    protected void onResumption() {

        if(HnppConstants.isViewRefresh){
            super.onResumption();
        }


    }
    @Override
    public void goToChildDetailActivity(CommonPersonObjectClient patient, boolean launchDialog) {
        String familyId = Utils.getValue(patient.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
        patient.getColumnmaps().put(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        String dobString = org.smartregister.family.util.Utils.getDuration(org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.DOB, false));
        Integer yearOfBirth = CoreChildUtils.dobStringToYear(dobString);
        if (yearOfBirth == null || yearOfBirth < 5) {
            HnppChildProfileActivity.startMe(getActivity(), familyId, false, new MemberObject(patient), HnppChildProfileActivity.class);

        } else {
            // HnppChildProfileActivity.startMe(getActivity(), houseHoldId,false, new MemberObject(patient), HnppChildProfileActivity.class);
            String houseHoldHead = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_NAME, true);
            String address = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.VILLAGE_NAME, true);
            String houseHoldId = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_ID, true);
            String moduleId = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.MODULE_ID, true);
            Intent intent = new Intent(getActivity(), HnppFamilyOtherMemberProfileActivity.class);
            intent.putExtras(getArguments());
            intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
            intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyId);
            intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, patient);
            intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, familyId);
            intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, familyId);
            intent.putExtra(Constants.INTENT_KEY.VILLAGE_TOWN, address);
            intent.putExtra(DBConstants.KEY.UNIQUE_ID,houseHoldId);
            intent.putExtra(HnppConstants.KEY.HOUSE_HOLD_ID,moduleId);
            intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, houseHoldHead);
            startActivity(intent);
        }


    }


    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HnppAllMemberRegisterProvider childRegisterProvider = new HnppAllMemberRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }



    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
    }

    @Override
    public void setTotalPatients() {
        if (headerTextDisplay != null) {
            headerTextDisplay.setText(
                    String.format(getString(R.string.clients_member), HnppConstants.getTotalCountBn(clientAdapter.getTotalcount())));
            headerTextDisplay.setTextColor(getResources().getColor(android.R.color.black));
            headerTextDisplay.setTypeface(Typeface.DEFAULT_BOLD);
            ((android.view.View)headerTextDisplay.getParent()).findViewById(R.id.filter_display_view).setVisibility(android.view.View.GONE);
            ((android.view.View)headerTextDisplay.getParent()).setVisibility(android.view.View.VISIBLE);
        }
    }
    @Override
    public void filter(String filterString, String joinTableString, String mainConditionString, boolean qrCode) {
        this.joinTables = new String[]{CoreConstants.TABLE_NAME.FAMILY};
        searchFilterString = filterString;

        clientAdapter.setCurrentoffset(0);
        super.filter(searchFilterString, joinTableString, mainConditionString, qrCode);

    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        switch (id) {
            case LOADER_ID:
                // Returns a new CursorLoader
                return new CursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        // Count query
                        if (args != null && args.getBoolean("count_execute")) {
                            countExecute();
                        }
                        return commonRepository().rawCustomQueryForAdapter(filterandSortQuery());
                    }
                };
            default:
                // An invalid id was passed in
                return null;
        }

    }


    @Override
    public void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
        if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_DOSAGE_STATUS && view.getTag() instanceof CommonPersonObjectClient) {
            CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
            String baseEntityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, true);
            if (StringUtils.isNotBlank(baseEntityId)) {
                CoreChildHomeVisitActivity.startMe(getActivity(), new MemberObject(client), false);
            }
        } else if (view.getId() == R.id.filter_text_view) {
            openFilterDialog(isNeedToShowDateFilter);
        }
    }


    @Override
    protected boolean isValidFilterForFts(CommonRepository commonRepository) {
        return false;
    }



    @Override
    protected int getToolBarTitle() {
        return R.string.menu_all_member;
    }
}
