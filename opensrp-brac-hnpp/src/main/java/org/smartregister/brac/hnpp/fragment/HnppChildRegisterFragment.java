package org.smartregister.brac.hnpp.fragment;

import android.app.Dialog;
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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppChildProfileActivity;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.model.HnppChildRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.HnppChildRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.provider.HnppChildRegisterProvider;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.activity.CoreChildHomeVisitActivity;
import org.smartregister.chw.core.fragment.CoreChildRegisterFragment;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static android.view.View.inflate;
import static org.smartregister.chw.core.utils.ChildDBConstants.limitClause;
import static org.smartregister.chw.core.utils.ChildDBConstants.orderByClause;
import static org.smartregister.chw.core.utils.ChildDBConstants.tableColConcat;

public class HnppChildRegisterFragment extends CoreChildRegisterFragment implements android.view.View.OnClickListener {
    private final String DEFAULT_MAIN_CONDITION = "date_removed is null";
    ArrayAdapter<String> villageSpinnerArrayAdapter;
    String searchFilterString = "";
    private String mSelectedVillageName, mSelectedClasterName;
    private TextView textViewVillageNameFilter, textViewClasterNameFilter;
    private ImageView imageViewVillageNameFilter, imageViewClasterNameFilter;
    private ViewGroup clients_header_layout;
    @Override
    protected void onResumption() {

        if(HnppConstants.isViewRefresh){
            super.onResumption();
        }


    }

    @Override
    protected int getToolBarTitle() {
        return R.string.menu_child_clients;
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new HnppChildRegisterFragmentPresenter(this, new HnppChildRegisterFragmentModel(), viewConfigurationIdentifier);

    }
    @Override
    protected boolean isValidFilterForFts(CommonRepository commonRepository) {
        return false;
    }
    @Override
    public void goToChildDetailActivity(CommonPersonObjectClient patient, boolean launchDialog) {
        if (launchDialog) {
            Timber.i(patient.name);
        }
        String houseHoldId = Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_ID, false);
        HnppChildProfileActivity.startMe(getActivity(), houseHoldId, false, new MemberObject(patient), HnppChildProfileActivity.class);
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HnppChildRegisterProvider childRegisterProvider = new HnppChildRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public void filter(String filterString, String joinTableString, String mainConditionString, boolean qrCode) {
        searchFilterString = filterString;
//        mSelectedVillageName = "";
//        mSelectedClasterName = "";
//        if(clients_header_layout.getVisibility() == android.view.View.VISIBLE){
//            clients_header_layout.setVisibility(android.view.View.GONE);
//        }
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
    protected String getMainCondition() {
        return super.getMainCondition();
    }

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
        HnppConstants.updateAppBackground((view.findViewById(R.id.register_nav_bar_container)));
        HnppConstants.updateAppBackground(view.findViewById(org.smartregister.R.id.register_toolbar));

        RelativeLayout sortAndFilterView = view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout);
        sortAndFilterView.setVisibility(android.view.View.VISIBLE);
        TextView sortView = sortAndFilterView.findViewById(R.id.sort_text_view);
        TextView filterTextView = sortAndFilterView.findViewById(R.id.filter_text_view);
        sortView.setText(getString(R.string.sort));
        filterTextView.setText(getString(R.string.filter));
        android.view.View searchBarLayout = view.findViewById(org.smartregister.family.R.id.search_bar_layout);
        searchBarLayout.setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);
        if (getSearchView() != null) {
            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
        }

        dueOnlyLayout.setVisibility(android.view.View.GONE);
        filterTextView.setOnClickListener(registerActionHandler);
        clients_header_layout = view.findViewById(org.smartregister.chw.core.R.id.clients_header_layout);
        android.view.View filterView = inflate(getContext(), R.layout.filter_top_view, clients_header_layout);
        textViewVillageNameFilter = filterView.findViewById(R.id.village_name_filter);
        textViewClasterNameFilter = filterView.findViewById(R.id.claster_name_filter);
        imageViewVillageNameFilter = filterView.findViewById(R.id.village_filter_img);
        imageViewClasterNameFilter = filterView.findViewById(R.id.claster_filter_img);
        imageViewVillageNameFilter.setOnClickListener(this);
        imageViewClasterNameFilter.setOnClickListener(this);
        clients_header_layout.getLayoutParams().height = 100;
        clients_header_layout.setVisibility(android.view.View.GONE);
        if (getSearchCancelView() != null) {
            getSearchCancelView().setOnClickListener(this);
        }
        setTotalPatients();
//        TextView dueOnly = ((TextView)view.findViewById(org.smartregister.chw.core.R.id.due_only_text_view));
//        dueOnly.setVisibility(View.VISIBLE);
    }
    @Override
    public void setTotalPatients() {
        if (headerTextDisplay != null) {
            headerTextDisplay.setText(
                    String.format(getString(R.string.clients_child), HnppConstants.getTotalCountBn(clientAdapter.getTotalcount())));
            headerTextDisplay.setTextColor(getResources().getColor(android.R.color.black));
            headerTextDisplay.setTypeface(Typeface.DEFAULT_BOLD);
            ((android.view.View)headerTextDisplay.getParent()).findViewById(R.id.filter_display_view).setVisibility(android.view.View.GONE);
            ((android.view.View)headerTextDisplay.getParent()).setVisibility(android.view.View.VISIBLE);
        }
    }
    @Override
    public void onClick(android.view.View v) {
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
    public void countExecute() {
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(searchFilterString)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.CHILD, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.CHILD, HnppConstants.KEY.CHILD_MOTHER_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.CHILD, HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.CHILD, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));

        }
        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
        }

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
    protected String filterandSortQuery() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(searchFilterString)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.CHILD, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.CHILD, HnppConstants.KEY.CHILD_MOTHER_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.CHILD, HnppConstants.KEY.CHILD_MOTHER_NAME_REGISTERED, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.CHILD, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));

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

    @Override
    public void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
        if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_DOSAGE_STATUS && view.getTag() instanceof CommonPersonObjectClient) {
            CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
            String baseEntityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
            if (StringUtils.isNotBlank(baseEntityId)) {
                CoreChildHomeVisitActivity.startMe(getActivity(), new MemberObject(client), false);
            }
        } else if (view.getId() == R.id.filter_text_view) {

            ArrayList<String> ssSpinnerArray = new ArrayList<>();


            ArrayList<String> villageSpinnerArray = new ArrayList<>();

            ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
            for (SSModel ssModel : ssLocationForms) {
                ssSpinnerArray.add(ssModel.username);
            }


            ArrayAdapter<String> ssSpinnerArrayAdapter = new ArrayAdapter<String>
                    (getActivity(), android.R.layout.simple_spinner_item,
                            ssSpinnerArray){
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

            villageSpinnerArrayAdapter = new ArrayAdapter<String>
                    (getActivity(), android.R.layout.simple_spinner_item,
                            villageSpinnerArray){
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

            ArrayAdapter<String> clusterSpinnerArrayAdapter = new ArrayAdapter<String>
                    (getActivity(), android.R.layout.simple_spinner_item,
                            HnppConstants.getClasterSpinnerArray()){
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

            Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_NoTitleBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
            dialog.setContentView(R.layout.filter_options_dialog);
            Spinner ss_spinner = dialog.findViewById(R.id.ss_filter_spinner);
            Spinner village_spinner = dialog.findViewById(R.id.village_filter_spinner);
            Spinner cluster_spinner = dialog.findViewById(R.id.klaster_filter_spinner);
            village_spinner.setAdapter(villageSpinnerArrayAdapter);
            cluster_spinner.setAdapter(clusterSpinnerArrayAdapter);
            ss_spinner.setAdapter(ssSpinnerArrayAdapter);
            ss_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                    if (position != -1) {
                        villageSpinnerArray.clear();
                        ArrayList<SSLocations> ssLocations = SSLocationHelper.getInstance().getSsModels().get(position).locations;
                        for (SSLocations ssLocations1 : ssLocations) {
                            villageSpinnerArray.add(ssLocations1.village.name);
                        }
                        villageSpinnerArrayAdapter = new ArrayAdapter<String>
                                (getActivity(), android.R.layout.simple_spinner_item,
                                        villageSpinnerArray);
                        village_spinner.setAdapter(villageSpinnerArrayAdapter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            village_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
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
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                    if (position != -1) {
                        mSelectedClasterName = HnppConstants.getClasterNames().get(HnppConstants.getClasterSpinnerArray().get(position));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            Button proceed = dialog.findViewById(R.id.filter_apply_button);
            proceed.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {

                    updateFilterView();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    public void updateFilterView() {
        if (StringUtils.isEmpty(mSelectedVillageName) && StringUtils.isEmpty(mSelectedClasterName)) {
            clients_header_layout.setVisibility(android.view.View.GONE);
        } else {
            clients_header_layout.setVisibility(android.view.View.VISIBLE);
        }

        textViewVillageNameFilter.setText(getString(R.string.filter_village_name, mSelectedVillageName));
        textViewClasterNameFilter.setText(getString(R.string.claster_village_name, HnppConstants.getClusterNameFromValue(mSelectedClasterName)));
        filter(searchFilterString, "", DEFAULT_MAIN_CONDITION,false);


    }

}
