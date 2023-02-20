package org.smartregister.unicef.dghs.fragment;

import android.annotation.SuppressLint;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.database.Cursor;
import android.os.Bundle;
import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.model.FamilyRegisterFramentModel;
import org.smartregister.family.fragment.BaseFamilyRegisterFragment;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
import org.smartregister.unicef.dghs.presenter.HnppFamilyRegisterFragmentPresenter;
import org.smartregister.unicef.dghs.utils.FilterDialog;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static android.view.View.inflate;

public class HnppBaseFamilyRegisterFragment extends BaseFamilyRegisterFragment implements View.OnClickListener{
    protected final String DEFAULT_MAIN_CONDITION = "date_removed is null";
    String searchFilterString = "";
    protected String mSelectedVillageName, mSelectedClasterName;
    protected TextView textViewVillageNameFilter, textViewClasterNameFilter,textViewMonthNameFilter;
    private ImageView imageViewVillageNameFilter, imageViewClasterNameFilter;
    private RelativeLayout clusterView,monthFilterView;
    private ViewGroup clients_header_layout;
    protected int month =-1,year =-1;
    private View view;
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new HnppFamilyRegisterFragmentPresenter(this, new FamilyRegisterFramentModel(), viewConfigurationIdentifier);
    }
    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //TODO
        Timber.d("setAdvancedSearchFormData unimplemented");
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
    public void setupViews(View view) {
        super.setupViews(view);
        this.view = view;
        HnppConstants.updateAppBackground((view.findViewById(R.id.register_nav_bar_container)));
        HnppConstants.updateAppBackground(view.findViewById(org.smartregister.R.id.register_toolbar));
        RelativeLayout sortAndFilterView = view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout);
        sortAndFilterView.setVisibility(android.view.View.GONE);
        TextView sortView = sortAndFilterView.findViewById(R.id.sort_text_view);
        TextView filterTextView = sortAndFilterView.findViewById(R.id.filter_text_view);
        sortView.setText(getString(R.string.sort));
        filterTextView.setText(getString(R.string.filter));
        View searchBarLayout = view.findViewById(org.smartregister.family.R.id.search_bar_layout);
        searchBarLayout.setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);
//        if (getSearchView() != null) {
//            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
//            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
//        }

        searchBarLayout.setBackgroundResource(R.color.primary);
        searchBarLayout.setPadding(searchBarLayout.getPaddingLeft(), searchBarLayout.getPaddingTop(), searchBarLayout.getPaddingRight(), (int) org.smartregister.chw.core.utils.Utils.convertDpToPixel(10, getActivity()));

        view.findViewById(org.smartregister.chw.core.R.id.due_only_layout).setVisibility(View.GONE);
        filterTextView.setOnClickListener(registerActionHandler);
        clients_header_layout = view.findViewById(org.smartregister.chw.core.R.id.clients_header_layout);
        View filterView = inflate(getContext(), R.layout.filter_top_view, clients_header_layout);
        textViewVillageNameFilter = filterView.findViewById(R.id.village_name_filter);
        textViewClasterNameFilter = filterView.findViewById(R.id.claster_name_filter);
        textViewMonthNameFilter = filterView.findViewById(R.id.month_name_filter);
        monthFilterView =  filterView.findViewById(R.id.month_filter_view);
        clusterView = filterView.findViewById(R.id.cluster_filter_view);
        imageViewVillageNameFilter = filterView.findViewById(R.id.village_filter_img);
        imageViewClasterNameFilter = filterView.findViewById(R.id.claster_filter_img);
        filterView.findViewById(R.id.month_filter_img).setOnClickListener(this);
        imageViewVillageNameFilter.setOnClickListener(this);
        imageViewClasterNameFilter.setOnClickListener(this);
        clients_header_layout.getLayoutParams().height = 100;
        clients_header_layout.setVisibility(View.GONE);
        if (getSearchCancelView() != null) {
            getSearchCancelView().setOnClickListener(this);
        }
        //setTotalPatients();
    }
    @Override
    public void onResume() {
        super.onResume();

        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);
        NavigationMenu.getInstance(getActivity(), null, toolbar);
    }
    @SuppressLint("NonConstantResourceId")
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
            case R.id.month_filter_img:
                month = -1;
                year = -1;
                updateFilterView();
                break;
            case R.id.btn_search_cancel:
                mSelectedVillageName = "";
                mSelectedClasterName = "";
                searchFilterString = "";
                month = -1;
                year = -1;
                if (getSearchView() != null) {
                    getSearchView().setText("");
                }
                clients_header_layout.setVisibility(View.GONE);
                updateFilterView();
                break;
        }
    }
    protected void openFilterDialog(boolean isNeedToShowDate){
        new FilterDialog().showDialog(isNeedToShowDate,getActivity(), new FilterDialog.OnFilterDialogFilter() {
            @Override
            public void onDialogPress(String ssName, String villageName, String cluster,int m, int y) {
                mSelectedClasterName = cluster;
                mSelectedVillageName = villageName;
                month = m;
                year = y;
                if(!isNeedToShowDate) monthFilterView.setVisibility(View.INVISIBLE);
                updateFilterView();
            }
        });
    }
    public void updateFilterView(){
        if(StringUtils.isEmpty(mSelectedVillageName) && StringUtils.isEmpty(mSelectedClasterName) && month==-1 && year == -1){
            clients_header_layout.setVisibility(View.GONE);
        } else {
            clients_header_layout.setVisibility(View.VISIBLE);
        }
        if(month == -1 && year == -1){
            textViewMonthNameFilter.setText(getString(R.string.filter_month_name, "সকল"));
        }else{
            String monthYearStr = HnppJsonFormUtils.monthBanglaStr[month-1]+","+year;
            textViewMonthNameFilter.setText(getString(R.string.filter_month_name, monthYearStr));
        }
        if(StringUtils.isEmpty(mSelectedVillageName)){
            textViewVillageNameFilter.setText(getString(R.string.filter_village_name, "সকল"));

        }else{
            textViewVillageNameFilter.setText(getString(R.string.filter_village_name, mSelectedVillageName));
        }

        if(HnppConstants.isPALogin()){
            clusterView.setVisibility(View.GONE);
        }else{
            if(StringUtils.isEmpty(mSelectedClasterName)){
                textViewClasterNameFilter.setText(getString(R.string.claster_village_name, "সকল"));

            }else{
                textViewClasterNameFilter.setText(getString(R.string.claster_village_name, HnppConstants.getClusterNameFromValue(mSelectedClasterName)));
            }
        }
        filter(searchFilterString, "", DEFAULT_MAIN_CONDITION,false);

    }

    @Override
    public void countExecute() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(searchFilterString)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.SERIAL_NO, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, DBConstants.KEY.PHONE_NUMBER, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));

        }
        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
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
        @SuppressLint("StaticFieldLeak")
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
                    String query =  defaultFilterAndSortQuery();

                    Cursor cursor = commonRepository().rawCustomQueryForAdapter(query);
                    if(cursor!=null){

                        if(clientAdapter!=null) setTotalCount(query);
                    }
                    return cursor;
                }
            };
        }
        return super.onCreateLoader(id, args);
    }
        private void setTotalCount(String query){
        query = query.substring(0,query.indexOf("LIMIT"));
        Cursor cursor = commonRepository().rawCustomQueryForAdapter(query+";");
        if(cursor!=null){
            clientAdapter.setTotalcount(cursor.getCount());
            cursor.close();
        }
    }
    protected String defaultFilterAndSortQuery() {
        Sortqueries = getDefaultSortQuery();
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(searchFilterString)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.SERIAL_NO, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, DBConstants.KEY.PHONE_NUMBER, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));

        }
        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
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
}
