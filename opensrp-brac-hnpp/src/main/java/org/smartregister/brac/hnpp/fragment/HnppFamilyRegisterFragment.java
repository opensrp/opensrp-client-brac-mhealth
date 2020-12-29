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
import org.smartregister.chw.core.fragment.CoreFamilyRegisterFragment;
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static android.view.View.inflate;

public class HnppFamilyRegisterFragment extends CoreFamilyRegisterFragment implements View.OnClickListener {

    private final String DEFAULT_MAIN_CONDITION = "date_removed is null";
    ArrayAdapter<String> villageSpinnerArrayAdapter;
    String searchFilterString = "";
    private String mSelectedVillageName, mSelectedClasterName;
    private TextView textViewVillageNameFilter, textViewClasterNameFilter;
    private ImageView imageViewVillageNameFilter, imageViewClasterNameFilter;
    private ViewGroup clients_header_layout;
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
        clients_header_layout = view.findViewById(org.smartregister.chw.core.R.id.clients_header_layout);
        View filterView = inflate(getContext(), R.layout.filter_top_view, clients_header_layout);
        textViewVillageNameFilter = filterView.findViewById(R.id.village_name_filter);
        textViewClasterNameFilter = filterView.findViewById(R.id.claster_name_filter);
        imageViewVillageNameFilter = filterView.findViewById(R.id.village_filter_img);
        imageViewClasterNameFilter = filterView.findViewById(R.id.claster_filter_img);
        imageViewVillageNameFilter.setOnClickListener(this);
        imageViewClasterNameFilter.setOnClickListener(this);
        clients_header_layout.getLayoutParams().height = 100;
        clients_header_layout.setVisibility(View.GONE);
        if (getSearchCancelView() != null) {
            getSearchCancelView().setOnClickListener(this);
        }
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
    public void onClick(View v) {
        super.onViewClicked(v);
        switch (v.getId()) {
            case R.id.village_filter_img:
                mSelectedVillageName = "";
                updateFilterView();
                break;
            case R.id.claster_filter_img:
                mSelectedClasterName = "";
                updateFilterView();
                break;
            case R.id.btn_search_cancel:
                mSelectedVillageName = "";
                mSelectedClasterName = "";
                searchFilterString = "";
                if (getSearchView() != null) {
                    getSearchView().setText("");
                }
                clients_header_layout.setVisibility(android.view.View.GONE);

                break;
        }
    }
    @Override
    protected boolean isValidFilterForFts(CommonRepository commonRepository) {
        return false;
    }
    @Override
    public void countExecute() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(searchFilterString)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.SERIAL_NO, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));

        }
        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
        }
        String query = "";
        try {
                String sql = "";
                sql = mainSelect;
                if (StringUtils.isNotBlank(customFilter)) {
                    sql = sql + customFilter;
                }
                List<String> ids = commonRepository().findSearchIds(sql);
                clientAdapter.setTotalcount(ids.size());

        } catch (Exception e) {
            Timber.e(e);
        }
    }
    @Override
    protected String defaultFilterAndSortQuery() {
        Sortqueries = getDefaultSortQuery();
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(searchFilterString)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.SERIAL_NO, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));

        }
        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
        }
        String query = "";
        try {

                sqb.addCondition(customFilter.toString());
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));


        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }
    private void updateSortView(boolean isVisitWise){
        if(isVisitWise){
            sortByView.setImageResource(R.drawable.childrow_history);
        }else{
            sortByView.setImageResource(R.drawable.ic_home);
        }
        filter(searchFilterString, "", DEFAULT_MAIN_CONDITION,false);

    }
    ArrayAdapter<String> ssSpinnerArrayAdapter;
    ArrayList<SSModel> ssListModel  = new ArrayList<>();
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
            ArrayList<String> ssSpinnerArray = new ArrayList<>();
            ArrayList<String> skSpinnerArray = new ArrayList<>();
            ArrayList<String> villageSpinnerArray = new ArrayList<>();


            ArrayList<SSModel> skLocationForms = SSLocationHelper.getInstance().getAllSks();
            for (SSModel ssModel : skLocationForms) {
                skSpinnerArray.add(ssModel.skName+"("+ssModel.skUserName+")");
            }
            ArrayAdapter<String> sKSpinnerArrayAdapter = new ArrayAdapter<String>
                    (getActivity(), android.R.layout.simple_spinner_item,
                            skSpinnerArray){
                @Override
                public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
                    convertView = super.getDropDownView(position, convertView,
                            parent);

                    AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                    appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                    appCompatTextView.setHeight(100);

                    return convertView;
                }
            };


           /* ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
            for (SSModel ssModel : ssLocationForms) {
                ssSpinnerArray.add(ssModel.username);
            }
*/
            ssSpinnerArrayAdapter = new ArrayAdapter<String>
                    (getActivity(), android.R.layout.simple_spinner_item,
                            ssSpinnerArray){
                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    convertView = super.getDropDownView(position, convertView,
                            parent);

                    AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                    appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                    appCompatTextView.setHeight(100);

                    return convertView;
                }
            };

            villageSpinnerArrayAdapter = new ArrayAdapter<String>
                    (getActivity(), android.R.layout.simple_spinner_item,
                            villageSpinnerArray){
                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    convertView = super.getDropDownView(position, convertView,
                            parent);
                    AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                    appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                    appCompatTextView.setHeight(100);
                    return convertView;
                }
            };

            ArrayAdapter<String> clusterSpinnerArrayAdapter = new ArrayAdapter<String>
                    (getActivity(), android.R.layout.simple_spinner_item,
                            HnppConstants.getClasterSpinnerArray()){
                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    convertView = super.getDropDownView(position, convertView,
                            parent);
                    AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                    appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                    appCompatTextView.setHeight(100);

                    return convertView;
                }
            };

            Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_NoTitleBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
            dialog.setContentView(R.layout.filter_options_dialog);
            Spinner sk_spinner = dialog.findViewById(R.id.sk_filter_spinner);
            Spinner ss_spinner = dialog.findViewById(R.id.ss_filter_spinner);
            Spinner village_spinner = dialog.findViewById(R.id.village_filter_spinner);
            Spinner cluster_spinner = dialog.findViewById(R.id.klaster_filter_spinner);
            village_spinner.setAdapter(villageSpinnerArrayAdapter);
            cluster_spinner.setAdapter(clusterSpinnerArrayAdapter);
            if(HnppConstants.isPALogin()){
                dialog.findViewById(R.id.sk_filter_view).setVisibility(view.VISIBLE);
                sk_spinner.setAdapter(sKSpinnerArrayAdapter);
                sk_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                        if (position != -1) {
                            SSModel ssModel = skLocationForms.get(position);
                            ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getAllSS(ssModel.skUserName);
                            ssSpinnerArray.clear();
                            ssListModel.clear();
                            for (SSModel ssModel1 : ssLocationForms) {
                                ssSpinnerArray.add(ssModel1.username);
                                ssListModel.add(ssModel1);
                            }
                            ssSpinnerArrayAdapter = new ArrayAdapter<String>
                                    (getActivity(), android.R.layout.simple_spinner_item,
                                            ssSpinnerArray);
                            ss_spinner.setAdapter(ssSpinnerArrayAdapter);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
            ss_spinner.setAdapter(ssSpinnerArrayAdapter);
            ss_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != -1) {
                        ArrayList<SSLocations> ssLocations = SSLocationHelper.getInstance().getSsModels().get(position).locations;
                        villageSpinnerArray.clear();
                        for (SSLocations ssLocations1 : ssLocations) {
                            villageSpinnerArray.add(ssLocations1.village.name.trim());
                        }
                        villageSpinnerArrayAdapter = new ArrayAdapter<String>
                                (getActivity(), android.R.layout.simple_spinner_item,
                                        villageSpinnerArray);
                        village_spinner.setAdapter(villageSpinnerArrayAdapter);
                        //villageSpinnerArrayAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            village_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != -1) {
                        mSelectedVillageName = villageSpinnerArray.get(position);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            cluster_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != -1) {
                        mSelectedClasterName = HnppConstants.getClasterNames().get(HnppConstants.getClasterSpinnerArray().get(position));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            Button proceed = dialog.findViewById(R.id.filter_apply_button);
            proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateFilterView();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    public void updateFilterView() {
        if (StringUtils.isEmpty(mSelectedVillageName) && StringUtils.isEmpty(mSelectedClasterName)) {
            clients_header_layout.setVisibility(View.GONE);
        } else {
            clients_header_layout.setVisibility(View.VISIBLE);
        }

        textViewVillageNameFilter.setText(getString(R.string.filter_village_name, mSelectedVillageName));
        textViewClasterNameFilter.setText(getString(R.string.claster_village_name, HnppConstants.getClusterNameFromValue(mSelectedClasterName)));
        filter(searchFilterString, "", DEFAULT_MAIN_CONDITION,false);
    }


    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        super.onSyncComplete(fetchStatus);
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
//        if(JobManager.instance().getAllJobRequestsForTag(HnppPncCloseJob.TAG).isEmpty()){
//            HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
//        }

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
