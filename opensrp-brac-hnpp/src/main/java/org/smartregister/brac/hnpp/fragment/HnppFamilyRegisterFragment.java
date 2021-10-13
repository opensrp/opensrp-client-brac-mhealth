package org.smartregister.brac.hnpp.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.evernote.android.job.JobManager;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.MigrationSearchDetailsActivity;
import org.smartregister.brac.hnpp.activity.SkSelectionActivity;
import org.smartregister.brac.hnpp.job.HnppPncCloseJob;
import org.smartregister.brac.hnpp.job.NotificationGeneratorJob;
import org.smartregister.brac.hnpp.job.PullHouseholdIdsServiceJob;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.model.HnppFamilyRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.HnppFamilyRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.provider.HnppFamilyRegisterProvider;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppQueryBuilder;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.chw.core.provider.CoreRegisterProvider;
import org.smartregister.chw.core.utils.QueryBuilder;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.domain.FetchStatus;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static android.view.View.inflate;

public class HnppFamilyRegisterFragment extends HnppBaseFamilyRegisterFragment implements View.OnClickListener {

    private MigrationSearchContentData migrationSearchContentData;

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        CoreRegisterProvider chwRegisterProvider = new HnppFamilyRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, chwRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    public void setMigrationSearchContentData(MigrationSearchContentData migrationSearchContentData) {
        this.migrationSearchContentData = migrationSearchContentData;
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new HnppFamilyRegisterFragmentPresenter(this, new HnppFamilyRegisterFragmentModel(), null);
    }

    @Override
    protected void goToPatientDetailActivity(CommonPersonObjectClient patient, boolean goToDuePage) {
        Intent intent = new Intent(getActivity(), Utils.metadata().profileActivity);
        intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, patient.getCaseId());
        intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.FAMILY_HEAD, false));
        intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.PRIMARY_CAREGIVER, false));
        intent.putExtra(Constants.INTENT_KEY.VILLAGE_TOWN, Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, false));
        intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.FIRST_NAME, false));
        intent.putExtra(Constants.INTENT_KEY.GO_TO_DUE_PAGE, goToDuePage);
        intent.putExtra(DBConstants.KEY.UNIQUE_ID, Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false));
        intent.putExtra(HnppConstants.KEY.MODULE_ID, Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.MODULE_ID, false));
        if(migrationSearchContentData!=null){
            intent.putExtra(MigrationSearchDetailsActivity.EXTRA_SEARCH_CONTENT,migrationSearchContentData);
        }
        startActivity(intent);
    }
    ImageView sortByView;
    @Override
    public void setupViews(View view) {
       try{
           super.setupViews(view);
       }catch (Exception e){
           HnppApplication.getHNPPInstance().forceLogout();
           return;
       }
        HnppConstants.updateAppBackground((view.findViewById(R.id.register_nav_bar_container)));
        HnppConstants.updateAppBackground(view.findViewById(org.smartregister.R.id.register_toolbar));
        RelativeLayout sortAndFilterView = view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout);
        sortAndFilterView.setVisibility(android.view.View.VISIBLE);
        TextView sortView = sortAndFilterView.findViewById(R.id.sort_text_view);
        sortView.setVisibility(View.VISIBLE);
        sortByView = sortAndFilterView.findViewById(R.id.sort_by_image);
        sortByView.setVisibility(View.VISIBLE);
        sortView.setOnClickListener(registerActionHandler);
        sortByView.setOnClickListener(registerActionHandler);
        TextView filterTextView = sortAndFilterView.findViewById(R.id.filter_text_view);
        sortView.setText(getString(R.string.sort));
        if(HnppConstants.isSortByLastVisit){
            sortByView.setImageResource(R.drawable.childrow_history);
        }else{
            sortByView.setImageResource(R.drawable.ic_home);
        }
        filterTextView.setText(getString(R.string.filter));
        View searchBarLayout = view.findViewById(org.smartregister.family.R.id.search_bar_layout);
        searchBarLayout.setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);
        if (getSearchView() != null) {
            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
        }

        dueOnlyLayout.setVisibility(View.GONE);
        filterTextView.setOnClickListener(registerActionHandler);

        setTotalPatients();
//        TextView dueOnly = ((TextView)view.findViewById(org.smartregister.chw.core.R.id.due_only_text_view));
//        dueOnly.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResumption() {

        if(HnppConstants.isViewRefresh){
            super.onResumption();
        }


    }

    @Override
    public void setTotalPatients() {
        if (headerTextDisplay != null) {
            headerTextDisplay.setText(
                    String.format(getString(R.string.clients_household), HnppConstants.getTotalCountBn(clientAdapter.getTotalcount())));
            headerTextDisplay.setTextColor(getResources().getColor(android.R.color.black));
            headerTextDisplay.setTypeface(Typeface.DEFAULT_BOLD);
            ((View) headerTextDisplay.getParent()).findViewById(R.id.filter_display_view).setVisibility(View.GONE);
            ((View) headerTextDisplay.getParent()).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void filter(String filterString, String joinTableString, String mainConditionString, boolean qrCode) {
//        if(HnppConstants.isExistSpecialCharacter(filterString)){
//            filterString = "\""+filterString;
//        }
        searchFilterString = filterString;
        clientAdapter.setCurrentoffset(0);
        super.filter(filterString, joinTableString, mainConditionString, qrCode);
    }

    @Override
    protected boolean isValidFilterForFts(CommonRepository commonRepository) {
        return false;
    }


    private void updateSortView(boolean isVisitWise){
        if(isVisitWise){
            sortByView.setImageResource(R.drawable.childrow_history);
        }else{
            sortByView.setImageResource(R.drawable.ic_home);
        }
        filter(searchFilterString, "", DEFAULT_MAIN_CONDITION,false);

    }

    @Override
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        if(view.getId() == R.id.sort_text_view || view.getId() == R.id.sort_by_image){
            Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_NoTitleBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
            dialog.setContentView(R.layout.sort_options_dialog);
            dialog.findViewById(R.id.household_no_sort_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    HnppConstants.isSortByLastVisit = false;
                    updateSortView(false);
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.last_visit_sort_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HnppConstants.isSortByLastVisit = true;
                    updateSortView(true);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        else if (view.getId() == R.id.filter_text_view) {
            openFilterDialog(false);
        }
    }
    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        try{
            if (!SyncStatusBroadcastReceiver.getInstance().isSyncing() && (FetchStatus.fetched.equals(fetchStatus) || FetchStatus.nothingFetched.equals(fetchStatus))) {
                org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.chw.core.R.string.sync_complete));
                refreshSyncProgressSpinner();
            }
        }catch (WindowManager.BadTokenException e){

        }

    }


    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        //super.onSyncComplete(fetchStatus);
        try{
            org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.chw.core.R.string.sync_complete));
        }catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        refreshSyncProgressSpinner();

        if(JobManager.instance().getAllJobRequestsForTag(PullHouseholdIdsServiceJob.TAG).isEmpty()){
            PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
        }
        if(JobManager.instance().getAllJobRequestsForTag(VisitLogServiceJob.TAG).isEmpty()){
            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
        }
        if(JobManager.instance().getAllJobRequestsForTag(NotificationGeneratorJob.TAG).isEmpty()){
            NotificationGeneratorJob.scheduleJobImmediately(NotificationGeneratorJob.TAG);

        }
        //if we open this it'll cause the issue to remove from anc list
       if(JobManager.instance().getAllJobRequestsForTag(HnppPncCloseJob.TAG).isEmpty()){
            HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
        }

        HnppConstants.isViewRefresh = true;
    }

    @Override
    public void onResume() {
        try{
            super.onResume();
        }catch (Exception e){
            e.printStackTrace();
            HnppApplication.getHNPPInstance().forceLogout();
            return;
        }
    }
}
