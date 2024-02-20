package org.smartregister.unicef.mis.fragment;

import static android.view.View.inflate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.mis.provider.HnppAncRegisterProvider;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.Constants;
import org.smartregister.unicef.mis.utils.HnppDBConstants;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;

import java.text.MessageFormat;
import java.util.Set;
import timber.log.Timber;

public class HnppAncRegisterFragment extends HnppBaseAncRegisterFragment implements View.OnClickListener{
    protected String isRiskyWoman,isAncOverdue,isEddOverdue;
    private boolean isClickedRisky, isClickedAncOverdue, isClickedEddOverdue;
    @Override
    protected void onResumption() {

        if(HnppConstants.isViewRefresh){
            super.onResumption();
        }


    }

    @Override
    public void setupViews(View view) {
        if(getContext() == null) return;
        super.setupViews(view);
        CustomFontTextView titleView = view.findViewById(org.smartregister.family.R.id.txt_title_label);
        if (titleView != null) {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(getString(getToolBarTitle()));
            titleView.setFontVariant(FontVariant.REGULAR);
            titleView.setPadding(0, titleView.getTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
        }
        if (getSearchView() != null) {
            getSearchView().setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
        }
        view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout).setVisibility(View.GONE);
        ViewGroup clients_header_layout = view.findViewById(org.smartregister.chw.core.R.id.clients_header_layout);
        android.view.View filterView;
        if(checkDevice() == DeviceType.TABLET){
            filterView= inflate(getContext(), R.layout.anc_list_filter_buttons_tablet, clients_header_layout);
            clients_header_layout.getLayoutParams().height = 100;
        }else{
            filterView= inflate(getContext(), R.layout.anc_list_filter_buttons_phone, clients_header_layout);
            clients_header_layout.getLayoutParams().height = 130;
        }



        clients_header_layout.setVisibility(View.VISIBLE);
        filterView.findViewById(R.id.risky_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("RISKY","risky clicked");
                updateRiskyButton(false,view.findViewById(R.id.risky_button));
                isClickedRisky = !isClickedRisky;
            }
        });
        filterView.findViewById(R.id.edd_due_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("RISKY","edd clicked");
                updateEddButton(false,view.findViewById(R.id.edd_due_button));
                isClickedEddOverdue = !isClickedEddOverdue;
            }
        });
        filterView.findViewById(R.id.anc_due_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("RISKY","edd clicked");
                updateAncButton(false,view.findViewById(R.id.anc_due_button));
                isClickedAncOverdue = !isClickedAncOverdue;
            }
        });
        filterView.findViewById(R.id.reset_filter_button).setOnClickListener(this);
    }

    @Override
    protected void openProfile(CommonPersonObjectClient client) {


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
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
        switch (view.getId()){
            case R.id.edd_due_button:
                Log.v("RISKY","edd due clicked");
                break;
            case R.id.anc_due_button:
                Log.v("RISKY","anc due clicked");
                break;
            case R.id.reset_filter_button:
                Log.v("RISKY","reset filter clicked");
                break;
            case R.id.filter_text_view:
                openFilterDialog(true);
                break;
            case R.id.due_button:
                String mobileNo = (String) view.getTag();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mobileNo));
                startActivity(intent);
                break;
        }


    }
    void updateRiskyButton(boolean isReset, Button button){
        Drawable buttonDrawable = button.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);

        if(isReset){
            DrawableCompat.setTint(buttonDrawable, Color.GRAY);
            isRiskyWoman = "";
        }else {
            if(!isClickedRisky){
                DrawableCompat.setTint(buttonDrawable, getResources().getColor(R.color.btn_blue));
                isRiskyWoman = "yes";
            }else {
                DrawableCompat.setTint(buttonDrawable, Color.GRAY);
                isRiskyWoman = "";
            }
        }
        updateFilterView();
        button.setBackground(buttonDrawable);
    }
    void updateEddButton(boolean isReset, Button button){
        Drawable buttonDrawable = button.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);

        if(isReset){
            DrawableCompat.setTint(buttonDrawable, Color.GRAY);
            isEddOverdue = "";
        }else {
            if(!isClickedEddOverdue){
                DrawableCompat.setTint(buttonDrawable, getResources().getColor(R.color.btn_blue));
                isEddOverdue = "yes";
            }else {
                DrawableCompat.setTint(buttonDrawable, Color.GRAY);
                isEddOverdue = "";
            }
        }
        updateFilterView();
        button.setBackground(buttonDrawable);
    }
    void updateAncButton(boolean isReset, Button button){
        Drawable buttonDrawable = button.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);

        if(isReset){
            DrawableCompat.setTint(buttonDrawable, Color.GRAY);
            isAncOverdue = "";
        }else {
            if(!isClickedAncOverdue){
                DrawableCompat.setTint(buttonDrawable, getResources().getColor(R.color.btn_blue));
                isAncOverdue = "yes";
            }else {
                DrawableCompat.setTint(buttonDrawable, Color.GRAY);
                isAncOverdue = "";
            }
        }
        updateFilterView();
        button.setBackground(buttonDrawable);
    }
    private final String DEFAULT_MAIN_CONDITION = "date_removed is null";
    public void updateFilterView(){
        filter(searchFilterString, "", DEFAULT_MAIN_CONDITION,false);
    }

    @Override
    public void setTotalPatients() {
        if (headerTextDisplay != null) {
            headerTextDisplay.setText(
                    String.format(getString(R.string.clients_member), HnppConstants.getTotalCount(clientAdapter.getTotalcount())));
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


        String sql = "";
        if(month != -1){
            int beforeIndex = mainSelect.indexOf("WHERE");
            int length = mainSelect.length();
            String lastPart = mainSelect.substring(beforeIndex,length);
            String tempmainSelect = mainSelect.substring(0,beforeIndex);
            sql = tempmainSelect+" inner join ec_visit_log on ec_visit_log.base_entity_id = ec_family_member.base_entity_id "+lastPart;
        }else{
            sql = mainSelect;
        }
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(sql);
        joinTables = new String[]{"ec_family"};
        StringBuilder customFilter = new StringBuilder();
        String query = sql+" where " + getCondition();
        if (StringUtils.isNotBlank(searchFilterString)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.LAST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.MIDDLE_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.PHONE_NUMBER, searchFilterString));

            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));

        }
        else if(!StringUtils.isEmpty(isRiskyWoman)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, HnppConstants.KEY.IS_RISK, "true"));
        }
        else if(!StringUtils.isEmpty(isEddOverdue)){
            String q = "and cast(julianday(datetime('now')) - julianday(datetime(substr(edd, 7,4)  || '-' || substr(edd, 4,2) || '-' || substr(edd, 1,2))) as integer) >= 1";
            customFilter.append(q);
        }
        else if(!StringUtils.isEmpty(isAncOverdue)){
            String q = "and cast(julianday(datetime('now')) - julianday(datetime(substr("+ HnppDBConstants.NEXT_VISIT_DATE +", 7,4)  || '-' || substr("+ HnppDBConstants.NEXT_VISIT_DATE +", 4,2) || '-' || substr("+ HnppDBConstants.NEXT_VISIT_DATE +", 1,2))) as integer) >= 1";
            customFilter.append(q);
        }
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
        if(month!=-1){

            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
            customFilter.append(" and ec_visit_log.visit_type ='ANC Home Visit'");
            customFilter.append(" group by ec_family_member.base_entity_id");
        }
        try {

                sqb.addCondition(customFilter.toString());
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));
            Log.v("FILTER_QUERY","filter:"+query);
        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

//    @Override
//    public void countExecute() {
//
//        Cursor c = null;
//        try {
//
//            String query = "select count(*) from " + presenter().getMainTable() + " inner join " + HnppConstants.TABLE_NAME.FAMILY_MEMBER +
//                    " on " + presenter().getMainTable() + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " +
//                    HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID +
//                    " INNER JOIN ec_anc_log ON  ec_anc_register.base_entity_id = ec_anc_log.base_entity_id COLLATE NOCASE " +
//                    " inner join ec_family  on ec_family.base_entity_id = ec_family_member.relational_id "+
//                    ""+
//                    " where " + getCondition();
//            StringBuilder customFilter = new StringBuilder();
//            if (StringUtils.isNotBlank(searchFilterString)) {
//                customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
//                customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.LAST_NAME, searchFilterString));
//                customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.MIDDLE_NAME, searchFilterString));
//                customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));
//
//            }
//            if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
//                customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
//                customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));
//
//            }else if(!StringUtils.isEmpty(mSelectedClasterName)){
//                customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));
//
//            }else if(!StringUtils.isEmpty(mSelectedVillageName)){
//                customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
//            }
//            if(month!=-1){
//
//                customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
//                customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
//                customFilter.append(" group by ec_family_member.base_entity_id");
//            }
//
//            if (StringUtils.isNotBlank(customFilter)) {
//                query = query + customFilter;
//            }
//
//
//
//            c = commonRepository().rawCustomQueryForAdapter(query);
//            c.moveToFirst();
//            clientAdapter.setTotalcount(c.getInt(0));
//            Timber.v("total count here %s", clientAdapter.getTotalcount());
//
//            clientAdapter.setCurrentlimit(20);
//            clientAdapter.setCurrentoffset(0);
//
//        } catch (Exception e) {
//            Timber.e(e);
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//        }
//    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        setTotalPatients();
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
                    String query = defaultFilterAndSortQuery();
                    Cursor cursor = commonRepository().rawCustomQueryForAdapter(query);
                    if(cursor!=null && clientAdapter!=null){
                        setTotalCount(query);
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
    private static final String DUE_FILTER_TAG = "PRESSED";

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        HnppAncRegisterProvider provider = new HnppAncRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
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
    protected String getCondition() {
        return " " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DATE_REMOVED + " is null " +
                "AND " + HnppConstants.TABLE_NAME.ANC_MEMBER + "." + DBConstants.KEY.IS_CLOSED + " = '0' " +
                "AND " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.IS_CLOSED + " = '0' " ;
//                +
//                "AND ec_anc_register.base_entity_id NOT IN (SELECT ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0')";
    }
    private DeviceType checkDevice() {
        try{
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            float yInches= metrics.heightPixels/metrics.ydpi;
            float xInches= metrics.widthPixels/metrics.xdpi;
            double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
            if (diagonalInches >= 6.5){
                return DeviceType.TABLET;
            }else{
                return DeviceType.MOBILE;
            }
        }catch (Exception e){
            return DeviceType.MOBILE;
        }
    }
}
