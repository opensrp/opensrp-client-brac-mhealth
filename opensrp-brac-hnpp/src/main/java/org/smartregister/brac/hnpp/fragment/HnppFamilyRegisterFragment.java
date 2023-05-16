package org.smartregister.brac.hnpp.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import com.evernote.android.job.JobManager;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.MigrationSearchDetailsActivity;
import org.smartregister.brac.hnpp.activity.SkSelectionActivity;
import org.smartregister.brac.hnpp.job.HnppPncCloseJob;
import org.smartregister.brac.hnpp.job.NotificationGeneratorJob;
import org.smartregister.brac.hnpp.job.PullHouseholdIdsServiceJob;
import org.smartregister.brac.hnpp.job.StockFetchJob;
import org.smartregister.brac.hnpp.job.TargetFetchJob;
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
import org.smartregister.domain.ResponseErrorStatus;
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
import static org.smartregister.brac.hnpp.utils.HnppConstants.HH_SORTED_BY;

public class HnppFamilyRegisterFragment extends HnppBaseFamilyRegisterFragment implements View.OnClickListener {

    private MigrationSearchContentData migrationSearchContentData;
    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity =(Activity) context;
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        CoreRegisterProvider chwRegisterProvider = new HnppFamilyRegisterProvider(mActivity, commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, chwRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    public void setMigrationSearchContentData(MigrationSearchContentData migrationSearchContentData) {
        this.migrationSearchContentData = migrationSearchContentData;
    }

    @Override
    protected void initializePresenter() {
        if (mActivity == null || mActivity.isFinishing()) {
            HnppApplication.getHNPPInstance().forceLogout();
            return;
        }
        presenter = new HnppFamilyRegisterFragmentPresenter(this, new HnppFamilyRegisterFragmentModel(), null);
    }

    @Override
    protected void goToPatientDetailActivity(CommonPersonObjectClient patient, boolean goToDuePage) {
        Intent intent = new Intent(mActivity, Utils.metadata().profileActivity);
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
        updateSortView(HnppConstants.sSortedBy);
        filterTextView.setText(getString(R.string.filter));
        View searchBarLayout = view.findViewById(org.smartregister.family.R.id.search_bar_layout);
        searchBarLayout.setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);
        if (getSearchView() != null) {
            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
        }

        dueOnlyLayout.setVisibility(View.GONE);
        filterTextView.setOnClickListener(registerActionHandler);

        //setTotalPatients();
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


    private void updateSortView(int sortBy){
        switch (sortBy){
            case HnppConstants.SORT_BY.SORT_BY_LAST_VISIT:
                sortByView.setImageResource(R.drawable.childrow_history);
                break;
            case HnppConstants.SORT_BY.SORT_BY_REGIGTRATION:
                sortByView.setImageResource(R.drawable.ic_home);
                break;
            case HnppConstants.SORT_BY.SORT_BY_SERIAL:
                sortByView.setImageResource(R.drawable.ic_serial);
                break;

        }
        filter(searchFilterString, "", DEFAULT_MAIN_CONDITION,false);

    }

    @Override
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        if(view.getId() == R.id.sort_text_view || view.getId() == R.id.sort_by_image){
            Dialog dialog = new Dialog(mActivity, android.R.style.Theme_NoTitleBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
            dialog.setContentView(R.layout.sort_options_dialog);
            dialog.findViewById(R.id.household_no_sort_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    HnppConstants.sSortedBy = HnppConstants.SORT_BY.SORT_BY_REGIGTRATION;
                    CoreLibrary.getInstance().context().allSharedPreferences().savePreference(HH_SORTED_BY,HnppConstants.SORT_BY.SORT_BY_REGIGTRATION+"");
                    updateSortView(HnppConstants.SORT_BY.SORT_BY_REGIGTRATION);

                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.last_visit_sort_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HnppConstants.sSortedBy = HnppConstants.SORT_BY.SORT_BY_LAST_VISIT;
                    CoreLibrary.getInstance().context().allSharedPreferences().savePreference(HH_SORTED_BY,HnppConstants.SORT_BY.SORT_BY_LAST_VISIT+"");
                    updateSortView(HnppConstants.SORT_BY.SORT_BY_LAST_VISIT);
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.serial_no_sort_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HnppConstants.sSortedBy = HnppConstants.SORT_BY.SORT_BY_SERIAL;
                    CoreLibrary.getInstance().context().allSharedPreferences().savePreference(HH_SORTED_BY,HnppConstants.SORT_BY.SORT_BY_SERIAL+"");
                    updateSortView(HnppConstants.SORT_BY.SORT_BY_SERIAL);
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
    protected void registerSyncStatusBroadcastReceiver() {
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);
    }

    @Override
    protected void unregisterSyncStatusBroadcastReceiver() {
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
    }

    @Override
    public void onSyncStart() {
        refreshSyncStatusViews(null);
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        refreshSyncStatusViews(fetchStatus);
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        refreshSyncStatusViews(fetchStatus);
    }

    protected void refreshSyncStatusViews(FetchStatus fetchStatus) {
        if(mActivity==null || mActivity.isFinishing()) return;
            if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
              showToast(getString(org.smartregister.R.string.syncing));

            } else {
                if (fetchStatus != null) {

                    if (fetchStatus.equals(FetchStatus.fetchedFailed)) {
                        if(fetchStatus.displayValue().equals(ResponseErrorStatus.malformed_url.name())) {
                            showToast(getString(org.smartregister.R.string.sync_failed_malformed_url));
                        }
                        else if (fetchStatus.displayValue().equals(ResponseErrorStatus.timeout.name())) {
                            showToast(getString(org.smartregister.R.string.sync_failed_timeout_error));
                        }
                        else {
                            showToast(getString(org.smartregister.R.string.sync_failed));
                        }

                    } else if (fetchStatus.equals(FetchStatus.fetched) || fetchStatus.equals(FetchStatus.nothingFetched)) {

                       // renderView();
                        showToast(getString(org.smartregister.R.string.sync_complete));
                        if(JobManager.instance().getAllJobRequestsForTag(PullHouseholdIdsServiceJob.TAG).isEmpty()){
                            PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
                        }
                        if(JobManager.instance().getAllJobRequestsForTag(NotificationGeneratorJob.TAG).isEmpty()){
                            NotificationGeneratorJob.scheduleJobImmediately(NotificationGeneratorJob.TAG);

                        }
                        //if we open this it'll cause the issue to remove from anc list
                        if(JobManager.instance().getAllJobRequestsForTag(HnppPncCloseJob.TAG).isEmpty()){
                            HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
                        }
                        //if(JobManager.instance().getAllJobRequestsForTag(TargetFetchJob.TAG).isEmpty()){
                            TargetFetchJob.scheduleJobImmediately(TargetFetchJob.TAG);
                        //}
                        //if(JobManager.instance().getAllJobRequestsForTag(StockFetchJob.TAG).isEmpty()){
                            StockFetchJob.scheduleJobImmediately(StockFetchJob.TAG);
                        //}
                        HnppConstants.isViewRefresh = true;
                        setRefreshList(true);
                        renderView();

                    } else if (fetchStatus.equals(FetchStatus.noConnection)) {
                        showToast(getString(org.smartregister.R.string.sync_failed_no_internet));

                    }
                }
                else{
                    Timber.i("Fetch Status NULL");
                }

            }

            refreshSyncProgressSpinner();
    }
    private void showToast(String message){
        if(mActivity==null || mActivity.isFinishing()) return;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    Utils.showShortToast(mActivity, message);
                }catch (Exception e){

                }
            }
        });
    }

    //    @Override
//    public void onSyncInProgress(FetchStatus fetchStatus) {
//        try{
//            if (!SyncStatusBroadcastReceiver.getInstance().isSyncing() && (FetchStatus.fetched.equals(fetchStatus) || FetchStatus.nothingFetched.equals(fetchStatus))) {
//                org.smartregister.util.Utils.showShortToast(mActivity, getString(org.smartregister.chw.core.R.string.sync_complete));
//                refreshSyncProgressSpinner();
//            }
//        }catch (WindowManager.BadTokenException e){
//
//        }
//
//    }
//
//
//    @Override
//    public void onSyncComplete(FetchStatus fetchStatus) {
//        //super.onSyncComplete(fetchStatus);
//        try{
//            org.smartregister.util.Utils.showShortToast(mActivity, getString(org.smartregister.chw.core.R.string.sync_complete));
//        }catch (WindowManager.BadTokenException e){
//            e.printStackTrace();
//        }
//        refreshSyncProgressSpinner();
//
//        if(JobManager.instance().getAllJobRequestsForTag(PullHouseholdIdsServiceJob.TAG).isEmpty()){
//            PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
//        }
//        if(JobManager.instance().getAllJobRequestsForTag(VisitLogServiceJob.TAG).isEmpty()){
//            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
//        }
//        if(JobManager.instance().getAllJobRequestsForTag(NotificationGeneratorJob.TAG).isEmpty()){
//            NotificationGeneratorJob.scheduleJobImmediately(NotificationGeneratorJob.TAG);
//
//        }
//        //if we open this it'll cause the issue to remove from anc list
//       if(JobManager.instance().getAllJobRequestsForTag(HnppPncCloseJob.TAG).isEmpty()){
//            HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
//        }
//
//        HnppConstants.isViewRefresh = true;
//    }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity = null;
    }
}
