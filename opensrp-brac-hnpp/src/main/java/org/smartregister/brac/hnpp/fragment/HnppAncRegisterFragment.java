package org.smartregister.brac.hnpp.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.AncMemberProfileActivity;
import org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.model.HnppPncRegisterFragmentModel;
import org.smartregister.brac.hnpp.provider.HnppAncRegisterProvider;
import org.smartregister.brac.hnpp.provider.HnppPncRegisterProvider;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.model.BaseAncRegisterFragmentModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterFragmentPresenter;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.presenter.AncRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.QueryBuilder;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.pnc.presenter.BasePncRegisterFragmentPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.Constants;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static android.view.View.inflate;

public class HnppAncRegisterFragment extends AncRegisterFragment implements View.OnClickListener{

    @Override
    protected void openProfile(CommonPersonObjectClient client) {

        HashMap<String, String> detailsMap = CoreChwApplication.ancRegisterRepository().getFamilyNameAndPhone(Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.FAMILY_HEAD, false));

        String familyName = "";
        String familyHeadPhone = "";
        if (detailsMap != null) {
            familyName = detailsMap.get(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.FAMILY_HEAD_NAME);
            familyHeadPhone = detailsMap.get(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.FAMILY_HEAD_PHONE);
        }
        String familyId = org.smartregister.util.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
        String houseHoldHead = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_NAME, true);
        String address = org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.VILLAGE_NAME, true);

        Intent intent = new Intent(getActivity(), HnppFamilyOtherMemberProfileActivity.class);
        intent.putExtras(getArguments());
        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, client.getCaseId());
        intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyId);
        intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, client);
        intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, familyId);
        intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, familyId);
        intent.putExtra(Constants.INTENT_KEY.VILLAGE_TOWN, address);
        intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, houseHoldHead);
        startActivity(intent);
    }
    @Override
    public void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
        if (view.getId() == R.id.filter_sort_layout) {


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
//                        villageSpinnerArrayAdapter.notifyDataSetChanged();
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
    private final String DEFAULT_MAIN_CONDITION = "date_removed is null";
    ArrayAdapter<String> villageSpinnerArrayAdapter;
    String searchFilterString = "";
    private String mSelectedVillageName, mSelectedClasterName;
    private TextView textViewVillageNameFilter, textViewClasterNameFilter;
    private ImageView imageViewVillageNameFilter, imageViewClasterNameFilter;
    private ViewGroup clients_header_layout;
    @Override
    public void setupViews(android.view.View view) {
        try{
            super.setupViews(view);
        }catch (Exception e){
            e.printStackTrace();
            HnppApplication.getHNPPInstance().forceLogout();
            return;
        }
        HnppConstants.updateAppBackground((view.findViewById(R.id.register_nav_bar_container)));
        HnppConstants.updateAppBackground(view.findViewById(org.smartregister.R.id.register_toolbar));
        CustomFontTextView titleView = view.findViewById(org.smartregister.family.R.id.txt_title_label);
        if (titleView != null) {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(getString(getToolBarTitle()));
            titleView.setFontVariant(FontVariant.REGULAR);
            titleView.setPadding(0, titleView.getTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
        }
        ((TextView) view.findViewById(org.smartregister.chw.core.R.id.filter_text_view)).setText("");
        view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout).setVisibility(android.view.View.VISIBLE);
        android.view.View searchBarLayout = view.findViewById(org.smartregister.family.R.id.search_bar_layout);
        searchBarLayout.setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);
        if (getSearchView() != null) {
            getSearchView().setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
        }
        view.findViewById(org.smartregister.chw.core.R.id.due_only_layout).setVisibility(android.view.View.GONE);
        view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout).setOnClickListener(registerActionHandler);
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

        NavigationMenu.getInstance(getActivity(), null, view.findViewById(org.smartregister.R.id.register_toolbar));
    }
    public String getFilterString() {
        String selected_claster = "";
        if(!StringUtils.isEmpty(mSelectedClasterName)){
            selected_claster = mSelectedClasterName.replace("_"," AND ");
        }
        String str = StringUtils.isEmpty(mSelectedVillageName) ?
                (StringUtils.isEmpty(mSelectedClasterName) ?
                        "" : selected_claster) : (StringUtils.isEmpty(mSelectedClasterName) ?
                mSelectedVillageName : "" + mSelectedVillageName + " AND " + selected_claster + "");

        return str;
    }
    public void updateFilterView(){
        if(StringUtils.isEmpty(mSelectedVillageName) && StringUtils.isEmpty(mSelectedClasterName)){
            clients_header_layout.setVisibility(android.view.View.GONE);
        } else {
            clients_header_layout.setVisibility(android.view.View.VISIBLE);
        }
        textViewVillageNameFilter.setText(getString(R.string.filter_village_name, mSelectedVillageName));
        textViewClasterNameFilter.setText(getString(R.string.claster_village_name, HnppConstants.getClusterNameFromValue(mSelectedClasterName)));
        String filterString = getFilterString();
        filter(filterString, "", DEFAULT_MAIN_CONDITION);


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
    protected void refreshSyncProgressSpinner() {
        if (syncProgressBar != null) {
            syncProgressBar.setVisibility(View.GONE);
        }
        if (syncButton != null) {
            syncButton.setVisibility(View.GONE);
        }
    }


    private String defaultFilterAndSortQuery() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
        joinTables = new String[]{"ec_family"};
        String query = "";
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(filters)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.FIRST_NAME, filters));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.LAST_NAME, filters));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.MIDDLE_NAME, filters));
        }
        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" or ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
            customFilter.append(MessageFormat.format(" or {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" or {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
        }
        if (StringUtils.isNotBlank(filters))
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.UNIQUE_ID, filters));

        if (dueFilterActive) {
            customFilter.append(MessageFormat.format(" and ( {0} ) ", getDueCondition()));
        }
        customFilter.append(MessageFormat.format(" and ( {0} ) ", getCondition()));
        try {
//            if (isValidFilterForFts(commonRepository())) {
//
//                String myquery = QueryBuilder.getQuery(joinTables, mainCondition, tablename, customFilter.toString(), clientAdapter, Sortqueries);
//                List<String> ids = commonRepository().findSearchIds(myquery);
//                query = sqb.toStringFts(ids, tablename, CommonRepository.ID_COLUMN,
//                        Sortqueries);
//                query = sqb.Endquery(query);
//            } else {
                sqb.addCondition(customFilter.toString());
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));

           // }
        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }
    private boolean dueFilterActive = false;

    @Override
    public void countExecute() {

        Cursor c = null;
        try {

            String query = "select count(*) from " + presenter().getMainTable() + " inner join " + HnppConstants.TABLE_NAME.FAMILY_MEMBER +
                    " on " + presenter().getMainTable() + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " +
                    HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID +
                    " INNER JOIN ec_anc_log ON  ec_anc_register.base_entity_id = ec_anc_log.base_entity_id COLLATE NOCASE " +
                    " inner join ec_family  on ec_family.base_entity_id = ec_family_member.relational_id "+
                    " where " + getCondition();
            StringBuilder customFilter = new StringBuilder();
            if (StringUtils.isNotBlank(filters)) {
                customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.FIRST_NAME, filters));
                customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.LAST_NAME, filters));
                customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.MIDDLE_NAME, filters));
            }
            if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
                customFilter.append(MessageFormat.format(" or ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
                customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

            }else if(!StringUtils.isEmpty(mSelectedClasterName)){
                customFilter.append(MessageFormat.format(" or {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

            }else if(!StringUtils.isEmpty(mSelectedVillageName)){
                customFilter.append(MessageFormat.format(" or {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, DBConstants.KEY.VILLAGE_TOWN, mSelectedVillageName));
            }
            if (StringUtils.isNotBlank(filters))
                customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.UNIQUE_ID, filters));


            if (StringUtils.isNotBlank(customFilter)) {
                query = query + customFilter;
            }



            c = commonRepository().rawCustomQueryForAdapter(query);
            c.moveToFirst();
            clientAdapter.setTotalcount(c.getInt(0));
            Timber.v("total count here %s", clientAdapter.getTotalcount());

            clientAdapter.setCurrentlimit(20);
            clientAdapter.setCurrentoffset(0);

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        if (id == LOADER_ID) {
            return new CursorLoader(getActivity()) {
                @Override
                public Cursor loadInBackground() {
                    // Count query
                    final String COUNT = "count_execute";
                    if (args != null && args.getBoolean(COUNT)) {
                        countExecute();
                    }
                    String query = defaultFilterAndSortQuery();
                    return commonRepository().rawCustomQueryForAdapter(query);
                }
            };
        }
        return super.onCreateLoader(id, args);


    }
    private static final String DUE_FILTER_TAG = "PRESSED";

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        HnppAncRegisterProvider provider = new HnppAncRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
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

    private String getDueCondition() {
        return " ifnull(substr(delivery_date, 7, 4)||'-'||substr(delivery_date, 4, 2)||'-'||substr(delivery_date, 1, 2), '+2 day') < STRFTIME('%Y%m%d', datetime('now')) " +
                " and julianday() - julianday(substr(delivery_date, 7, 4)||'-'||substr(delivery_date, 4, 2)||'-'||substr(delivery_date, 1, 2)) >= 2   ";
    }

    @Override
    public void filter(String filterString, String joinTableString, String mainConditionString, boolean qrCode) {
        this.joinTables = new String[]{CoreConstants.TABLE_NAME.FAMILY};
        searchFilterString = filterString;
        mSelectedVillageName = "";
        mSelectedClasterName = "";
        if(clients_header_layout.getVisibility() == android.view.View.VISIBLE){
            clients_header_layout.setVisibility(android.view.View.GONE);
        }
        clientAdapter.setCurrentoffset(0);
        super.filter(filterString, joinTableString, mainConditionString, qrCode);

    }
    private void switchViews(View dueOnlyLayout, boolean isPress) {
        TextView dueOnlyTextView = dueOnlyLayout.findViewById(R.id.due_only_text_view);
        if (isPress) {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_due_filter_on, 0);
        } else {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_due_filter_off, 0);
        }
    }
    protected int getToolBarTitle() {
        return R.string.menu_anc_clients;
    }
    private String getCondition() {
        return " " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DATE_REMOVED + " is null " +
                "AND " + HnppConstants.TABLE_NAME.ANC_MEMBER + "." + DBConstants.KEY.IS_CLOSED + " = '0' " +
                "AND " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.IS_CLOSED + " = '0' " ;
//                +
//                "AND ec_anc_register.base_entity_id NOT IN (SELECT ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0')";
    }
}
