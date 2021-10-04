package org.smartregister.chw.core.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.activity.CoreChildProfileActivity;
import org.smartregister.chw.core.activity.CoreChildRegisterActivity;
import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.model.CoreChildRegisterFragmentModel;
import org.smartregister.chw.core.presenter.CoreChildRegisterFragmentPresenter;
import org.smartregister.chw.core.provider.CoreChildRegisterProvider;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.fragment.NoMatchDialogFragment;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class CoreChildRegisterFragment extends BaseChwRegisterFragment implements CoreChildRegisterFragmentContract.View {

    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String CLICK_VIEW_DOSAGE_STATUS = "click_view_dosage_status";
    private static final String DUE_FILTER_TAG = "PRESSED";
    private View view;
    protected View dueOnlyLayout;
    private boolean dueFilterActive = false;

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new CoreChildRegisterFragmentPresenter(this, new CoreChildRegisterFragmentModel(), viewConfigurationIdentifier);

    }

    @Override
    public void setUniqueID(String s) {
        if (getSearchView() != null) {
            getSearchView().setText(s);
        }
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //// TODO: 15/08/19  
    }

    @Override
    protected void onResumption() {
        if (dueFilterActive && dueOnlyLayout != null) {
            dueFilter(dueOnlyLayout);
        } else {
            super.onResumption();
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
    protected void startRegistration() {
        ((CoreChildRegisterActivity) getActivity()).startFormActivity(CoreConstants.JSON_FORM.getChildRegister(), null, null);
        //getActivity().startFormActivity(Utils.metadata().familyRegister.formName, null, null);
    }

    @Override
    protected void onViewClicked(View view) {
        if (getActivity() == null) {
            return;
        }

        if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
            if (view.getTag() instanceof CommonPersonObjectClient) {
                goToChildDetailActivity((CommonPersonObjectClient) view.getTag(), false);
            }
        } else if (view.getId() == R.id.due_only_layout) {
            toggleFilterSelection(view);
        }
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        try{
            if (!SyncStatusBroadcastReceiver.getInstance().isSyncing() && (FetchStatus.fetched.equals(fetchStatus) || FetchStatus.nothingFetched.equals(fetchStatus)) && dueFilterActive && dueOnlyLayout != null) {
                dueFilter(dueOnlyLayout);
                if(getActivity()!=null && !getActivity().isFinishing()){
                    org.smartregister.util.Utils.showShortToast(getActivity(), getString(R.string.sync_complete));
                    refreshSyncProgressSpinner();
                }

            } else {
               // super.onSyncInProgress(fetchStatus);
            }
        }catch (WindowManager.BadTokenException e){

        }

    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        try{
            if (!SyncStatusBroadcastReceiver.getInstance().isSyncing() && (FetchStatus.fetched.equals(fetchStatus) || FetchStatus.nothingFetched.equals(fetchStatus)) && (dueFilterActive && dueOnlyLayout != null)) {
                dueFilter(dueOnlyLayout);
                if(getActivity()!=null && !getActivity().isFinishing()){
                    org.smartregister.util.Utils.showShortToast(getActivity(), getString(R.string.sync_complete));
                    refreshSyncProgressSpinner();
                }

            } else {
               // super.onSyncComplete(fetchStatus);
            }

            if (syncProgressBar != null) {
                syncProgressBar.setVisibility(View.GONE);
            }
            if (syncButton != null) {
                syncButton.setVisibility(View.GONE);
            }
        }catch (WindowManager.BadTokenException e){

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = view.findViewById(R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);
        NavigationMenu.getInstance(getActivity(), null, toolbar);
    }

    @Override
    protected void refreshSyncProgressSpinner() {
        super.refreshSyncProgressSpinner();
        if (syncButton != null) {
            syncButton.setVisibility(View.GONE);
        }
    }

    public void goToChildDetailActivity(CommonPersonObjectClient patient,
                                        boolean launchDialog) {
        if (launchDialog) {
            Timber.i(patient.name);
        }

        Intent intent = new Intent(getActivity(), CoreChildProfileActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        startActivity(intent);
    }

    public void toggleFilterSelection(View dueOnlyLayout) {
        if (dueOnlyLayout != null) {
            if (dueOnlyLayout.getTag() == null) {
                dueFilterActive = true;
                dueFilter(dueOnlyLayout);
            } else if (dueOnlyLayout.getTag().toString().equals(DUE_FILTER_TAG)) {
                dueFilterActive = false;
                normalFilter(dueOnlyLayout);
            }
        }
    }

    private void normalFilter(View dueOnlyLayout) {
        filter(searchText(), "", presenter().getMainCondition());
        dueOnlyLayout.setTag(null);
        switchViews(dueOnlyLayout, false);
    }

    private void dueFilter(View dueOnlyLayout) {
        filter(searchText(), "", presenter().getDueFilterCondition());
        dueOnlyLayout.setTag(DUE_FILTER_TAG);
        switchViews(dueOnlyLayout, true);
    }

    protected void filter(String filterString, String joinTableString, String mainConditionString) {
        filters = filterString;
        joinTable = joinTableString;
        mainCondition = mainConditionString;
        filterandSortExecute(countBundle());
    }

    private String searchText() {
        return (getSearchView() == null) ? "" : getSearchView().getText().toString();
    }

    private void switchViews(View dueOnlyLayout, boolean isPress) {
        TextView dueOnlyTextView = dueOnlyLayout.findViewById(R.id.due_only_text_view);
        if (isPress) {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_due_filter_on, 0);
        } else {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_due_filter_off, 0);

        }
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        CoreChildRegisterProvider childRegisterProvider = new CoreChildRegisterProvider(getActivity(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public CoreChildRegisterFragmentContract.Presenter presenter() {
        return (CoreChildRegisterFragmentContract.Presenter) presenter;
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        this.view = view;

        dueOnlyLayout = view.findViewById(R.id.due_only_layout);
        dueOnlyLayout.setVisibility(View.VISIBLE);
        dueOnlyLayout.setOnClickListener(registerActionHandler);
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.child_register_title;
    }

    @Override
    public void showNotFoundPopup(String uniqueId) {
        if (getActivity() == null) {
            return;
        }
        NoMatchDialogFragment.launchDialog((BaseRegisterActivity) getActivity(), DIALOG_TAG, uniqueId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (StringUtils.isBlank(filters)) {
            return super.onCreateLoader(id, args);
        } else {
            if (id == LOADER_ID) {// Returns a new CursorLoader
                return new CursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        // Count query
                        String query = "";
                        // Select register query
                        query = filterandSortQuery();
                        return commonRepository().rawCustomQueryForAdapter(query);
                    }
                };
            }// An invalid id was passed in
            return null;
        }
    }

    protected String filterandSortQuery() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);

        String query = "";
        try {
            if (isValidFilterForFts(commonRepository())) {
                String sql = ChildDBConstants.childMainFilter(mainCondition, presenter().getMainCondition(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY_MEMBER)), filters, Sortqueries, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset());
                List<String> ids = commonRepository().findSearchIds(sql);
                query = sqb.toStringFts(ids, tablename, CommonRepository.ID_COLUMN,
                        Sortqueries);
                query = sqb.Endquery(query);
            } else {
                sqb.addCondition(filters);
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));

            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

}
