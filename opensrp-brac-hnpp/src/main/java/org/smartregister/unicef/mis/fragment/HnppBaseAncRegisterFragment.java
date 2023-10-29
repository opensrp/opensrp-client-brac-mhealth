package org.smartregister.unicef.mis.fragment;

import android.annotation.SuppressLint;
import android.view.View;

public class HnppBaseAncRegisterFragment extends AncRegisterFragment implements View.OnClickListener{
    private final String DEFAULT_MAIN_CONDITION = "date_removed is null";
    String searchFilterString = "";
//    protected String mSelectedVillageName, mSelectedClasterName;
//    private TextView textViewVillageNameFilter, textViewClasterNameFilter,textViewMonthNameFilter;
//    private ImageView imageViewVillageNameFilter, imageViewClasterNameFilter;
//    private RelativeLayout clusterView,monthFilterView;
//    private ViewGroup clients_header_layout;
    protected int month =-1,year =-1;

//    @Override
//    public void setupViews(View view) {
//        try{
//            super.setupViews(view);
//        }catch (Exception e){
//            e.printStackTrace();
//            HnppApplication.getHNPPInstance().forceLogout();
//            return;
//        }
//        HnppConstants.updateAppBackground((view.findViewById(R.id.register_nav_bar_container)));
//        HnppConstants.updateAppBackground(view.findViewById(org.smartregister.R.id.register_toolbar));
//        RelativeLayout sortAndFilterView = view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout);
//        sortAndFilterView.setVisibility(android.view.View.GONE);
//        TextView sortView = sortAndFilterView.findViewById(R.id.sort_text_view);
//        sortView.setVisibility(View.GONE);
//        TextView filterTextView = sortAndFilterView.findViewById(R.id.filter_text_view);
//        filterTextView.setVisibility(View.GONE);
//        sortView.setText(getString(R.string.sort));
//        filterTextView.setText(getString(R.string.filter));
//        View searchBarLayout = view.findViewById(org.smartregister.family.R.id.search_bar_layout);
//        searchBarLayout.setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);
//        if (getSearchView() != null) {
//            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
//            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
//        }
//        view.findViewById(org.smartregister.chw.core.R.id.due_only_layout).setVisibility(android.view.View.GONE);
//
//        filterTextView.setOnClickListener(registerActionHandler);
////        clients_header_layout = view.findViewById(org.smartregister.chw.core.R.id.clients_header_layout);
////        View filterView = inflate(getContext(), R.layout.filter_top_view, clients_header_layout);
////        filterView.setVisibility(View.GONE);
////        textViewVillageNameFilter = filterView.findViewById(R.id.village_name_filter);
////        textViewClasterNameFilter = filterView.findViewById(R.id.claster_name_filter);
////        textViewMonthNameFilter = filterView.findViewById(R.id.month_name_filter);
////        clusterView = filterView.findViewById(R.id.cluster_filter_view);
////        monthFilterView =  filterView.findViewById(R.id.month_filter_view);
////        imageViewVillageNameFilter = filterView.findViewById(R.id.village_filter_img);
////        imageViewClasterNameFilter = filterView.findViewById(R.id.claster_filter_img);
////        filterView.findViewById(R.id.month_filter_img).setOnClickListener(this);
////        imageViewVillageNameFilter.setOnClickListener(this);
////        imageViewClasterNameFilter.setOnClickListener(this);
////        clients_header_layout.getLayoutParams().height = 100;
////        clients_header_layout.setVisibility(View.GONE);
//        if (getSearchCancelView() != null) {
//            getSearchCancelView().setOnClickListener(this);
//        }
//        //setTotalPatients();
//        NavigationMenu.getInstance(getActivity(), null, view.findViewById(org.smartregister.R.id.register_toolbar));
//
//    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        super.onViewClicked(v);
//        switch (v.getId()) {
//            case R.id.village_filter_img:
//                mSelectedVillageName = "";
//                updateFilterView();
//                break;
//            case R.id.claster_filter_img:
//                mSelectedClasterName = "";
//                updateFilterView();
//                break;
//            case R.id.month_filter_img:
//                month = -1;
//                year = -1;
//                updateFilterView();
//                break;
//            case R.id.btn_search_cancel:
//                mSelectedVillageName = "";
//                mSelectedClasterName = "";
//                searchFilterString = "";
//                month = -1;
//                year = -1;
//                if (getSearchView() != null) {
//                    getSearchView().setText("");
//                }
////                clients_header_layout.setVisibility(View.GONE);
//                updateFilterView();
//                break;
//        }
    }
    protected void openFilterDialog(boolean isNeedToShowDate){
//        new FilterDialog().showDialog(isNeedToShowDate,getActivity(), new FilterDialog.OnFilterDialogFilter() {
//            @Override
//            public void onDialogPress(String ssName, String villageName, String cluster,int m, int y) {
//                mSelectedClasterName = cluster;
//                mSelectedVillageName = villageName;
//                month = m;
//                year = y;
////                if(!isNeedToShowDate) monthFilterView.setVisibility(View.INVISIBLE);
//                updateFilterView();
//            }
//        });
    }
//    public void updateFilterView(){
//        if(StringUtils.isEmpty(mSelectedVillageName) && StringUtils.isEmpty(mSelectedClasterName) && month==-1 && year == -1){
//            clients_header_layout.setVisibility(View.GONE);
//        } else {
//            clients_header_layout.setVisibility(View.VISIBLE);
//        }
//        if(month == -1 && year == -1){
//            textViewMonthNameFilter.setText(getString(R.string.filter_month_name, "সকল"));
//        }else{
//            String monthYearStr = HnppJsonFormUtils.monthBanglaStr[month-1]+","+year;
//            textViewMonthNameFilter.setText(getString(R.string.filter_month_name, monthYearStr));
//        }
//        if(StringUtils.isEmpty(mSelectedVillageName)){
//            textViewVillageNameFilter.setText(getString(R.string.filter_village_name, "সকল"));
//
//        }else{
//            textViewVillageNameFilter.setText(getString(R.string.filter_village_name, mSelectedVillageName));
//        }
//
//        if(HnppConstants.isPALogin()){
////            clusterView.setVisibility(View.GONE);
//        }else{
//            if(StringUtils.isEmpty(mSelectedClasterName)){
//                textViewClasterNameFilter.setText(getString(R.string.claster_village_name, "সকল"));
//
//            }else{
//                textViewClasterNameFilter.setText(getString(R.string.claster_village_name, HnppConstants.getClusterNameFromValue(mSelectedClasterName)));
//            }
//        }
//        filter(searchFilterString, "", DEFAULT_MAIN_CONDITION,false);
//
//    }

    @Override
    public void countExecute() {
//        StringBuilder customFilter = new StringBuilder();
//        if (StringUtils.isNotBlank(searchFilterString)) {
//            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.LAST_NAME, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.MIDDLE_NAME, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.PHONE_NUMBER, searchFilterString));
//
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));
//
//        }
//        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
//            customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
//            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));
//
//        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
//            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));
//
//        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
//            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
//        }
//        if(month!=-1){
//
//            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
//            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
//            customFilter.append(" group by ec_family_member.base_entity_id");
//        }
//        String sql = "";
//        try {
//            if(month != -1){
//                    int beforeIndex = mainSelect.indexOf("WHERE");
//                    int length = mainSelect.length();
//                    String lastPart = mainSelect.substring(beforeIndex,length);
//                    String tempmainSelect = mainSelect.substring(0,beforeIndex);
//                    sql = tempmainSelect+" inner join ec_visit_log on ec_family_member.base_entity_id = ec_visit_log.base_entity_id "+lastPart;
//                }else{
//                    sql = mainSelect;
//                }
//            if (StringUtils.isNotBlank(customFilter)) {
//                sql = sql + customFilter;
//            }
//            List<String> ids = commonRepository().findSearchIds(sql);
//            clientAdapter.setTotalcount(ids.size());
//            Log.v("VIST_QUERY","count sql:"+sql);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
//
//
//    @Override
//    protected String filterandSortQuery() {
//        String sql = "";
//        if(month != -1){
//            int beforeIndex = mainSelect.indexOf("WHERE");
//            int length = mainSelect.length();
//            String lastPart = mainSelect.substring(beforeIndex,length);
//            String tempmainSelect = mainSelect.substring(0,beforeIndex);
//            sql = tempmainSelect+" inner join ec_visit_log on ec_family_member.base_entity_id = ec_visit_log.base_entity_id "+lastPart;
//        }else{
//            sql = mainSelect;
//        }
//        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(sql);
//        StringBuilder customFilter = new StringBuilder();
//        if (StringUtils.isNotBlank(searchFilterString)) {
//            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.LAST_NAME, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.MIDDLE_NAME, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));
//
//        }
//        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
//            customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
//            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));
//
//        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
//            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));
//
//        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
//            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
//        }
//        if(month!=-1){
//            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
//            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
//            customFilter.append(" group by ec_family_member.base_entity_id");
//        }
//        String query = "";
//        try {
//            sqb.addCondition(customFilter.toString());
//            query = sqb.orderbyCondition(Sortqueries);
//            query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));
//
//        } catch (Exception e) {
//            Timber.e(e);
//        }
//        Log.v("VIST_QUERY","filter:"+query);
//
//        return query;
//    }
}
