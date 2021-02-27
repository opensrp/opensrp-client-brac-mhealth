package org.smartregister.brac.hnpp.fragment;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppChildProfileActivity;
import org.smartregister.brac.hnpp.model.HnppChildRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.HnppChildRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.provider.HnppChildRegisterProvider;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.activity.CoreChildHomeVisitActivity;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import timber.log.Timber;


public class HnppChildRegisterFragment extends HnppBaseChildRegisterFragment implements android.view.View.OnClickListener {

    protected boolean isNeedToShowDateFilter = false;
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

        if(month!=-1){

            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
            if(!visitType.isEmpty()){
                customFilter.append(visitType);
            }
            customFilter.append(" group by ec_child.base_entity_id");
        }
        String sql = "";
        try {
            if(month != -1){
                int beforeIndex = mainSelect.indexOf("WHERE");
                int length = mainSelect.length();
                String lastPart = mainSelect.substring(beforeIndex,length);
                String tempmainSelect = mainSelect.substring(0,beforeIndex);
                sql = tempmainSelect+" inner join ec_visit_log on ec_child.base_entity_id = ec_visit_log.base_entity_id "+lastPart;
            }else{
                sql = mainSelect;
            }
            if (StringUtils.isNotBlank(customFilter)) {
                sql = sql + customFilter;
            }
            List<String> ids = commonRepository().findSearchIds(sql);
            clientAdapter.setTotalcount(ids.size());
            Log.v("VIST_QUERY","count sql:"+sql);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
    @Override
    protected String filterandSortQuery() {
        String sql = "";
        if(month != -1){
            int beforeIndex = mainSelect.indexOf("WHERE");
            int length = mainSelect.length();
            String lastPart = mainSelect.substring(beforeIndex,length);
            String tempmainSelect = mainSelect.substring(0,beforeIndex);
            sql = tempmainSelect+" inner join ec_visit_log on ec_child.base_entity_id = ec_visit_log.base_entity_id "+lastPart;
        }else{
            sql = mainSelect;
        }
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(sql);
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
        if(month!=-1){
            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
            if(!visitType.isEmpty()){
                customFilter.append(visitType);
            }
            customFilter.append(" group by ec_child.base_entity_id");
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
                CoreChildHomeVisitActivity.startMe(getActivity(),baseEntityId, false);
            }
        } else if (view.getId() == R.id.filter_text_view) {

            openFilterDialog(isNeedToShowDateFilter);
        }
    }

}
