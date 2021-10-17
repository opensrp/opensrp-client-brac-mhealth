package org.smartregister.brac.hnpp.fragment;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.FilterDialog;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.fragment.CoreFamilyRegisterFragment;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;

import java.text.MessageFormat;
import java.util.List;

import timber.log.Timber;

import static android.view.View.inflate;

public class HnppBaseFamilyRegisterFragment extends CoreFamilyRegisterFragment implements View.OnClickListener{
    protected final String DEFAULT_MAIN_CONDITION = "date_removed is null";
    String searchFilterString = "";
    protected String mSelectedVillageName, mSelectedClasterName;
    protected TextView textViewVillageNameFilter, textViewClasterNameFilter,textViewMonthNameFilter;
    private ImageView imageViewVillageNameFilter, imageViewClasterNameFilter;
    private RelativeLayout clusterView,monthFilterView;
    private ViewGroup clients_header_layout;
    protected int month =-1,year =-1;

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        HnppConstants.updateAppBackground((view.findViewById(R.id.register_nav_bar_container)));
        HnppConstants.updateAppBackground(view.findViewById(org.smartregister.R.id.register_toolbar));
        RelativeLayout sortAndFilterView = view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout);
        sortAndFilterView.setVisibility(View.VISIBLE);
        TextView sortView = sortAndFilterView.findViewById(R.id.sort_text_view);
        TextView filterTextView = sortAndFilterView.findViewById(R.id.filter_text_view);
        sortView.setText(getString(R.string.sort));
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
        setTotalPatients();
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
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY, DBConstants.KEY.PHONE_NUMBER, searchFilterString));
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
}
