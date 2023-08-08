package org.smartregister.unicef.dghs.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import org.apache.commons.lang3.StringUtils;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.HnppChildProfileActivity;
import org.smartregister.unicef.dghs.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.dghs.model.HnppElcoMemberRegisterFragmentModel;
import org.smartregister.unicef.dghs.presenter.HnppElcoMemberRegisterFragmentPresenter;
import org.smartregister.unicef.dghs.provider.ElcoMemberRegisterProvider;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.activity.CoreChildHomeVisitActivity;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import java.util.Set;


public class HnppElcoMemberRegisterFragment extends HnppBaseChildRegisterFragment implements android.view.View.OnClickListener {


    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new HnppElcoMemberRegisterFragmentPresenter(this, new HnppElcoMemberRegisterFragmentModel(), viewConfigurationIdentifier);

    }
    @Override
    protected void onResumption() {

        if(HnppConstants.isViewRefresh){
            super.onResumption();
        }


    }
    @Override
    public void goToChildDetailActivity(CommonPersonObjectClient patient, boolean launchDialog) {
        String familyId = Utils.getValue(patient.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
        patient.getColumnmaps().put(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        String dobString = org.smartregister.family.util.Utils.getDuration(org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.DOB, false));
        Integer yearOfBirth = CoreChildUtils.dobStringToYear(dobString);
        if (yearOfBirth == null || yearOfBirth < 5) {
            HnppChildProfileActivity.startMe(getActivity(), familyId, false, new MemberObject(patient), HnppChildProfileActivity.class);

        } else {
            // HnppChildProfileActivity.startMe(getActivity(), houseHoldId,false, new MemberObject(patient), HnppChildProfileActivity.class);
            String houseHoldHead = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_NAME, true);
            String address = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.VILLAGE_NAME, true);
            String houseHoldId = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_ID, true);
            String moduleId = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.MODULE_ID, true);
            Intent intent = new Intent(getActivity(), HnppFamilyOtherMemberProfileActivity.class);
            intent.putExtras(getArguments());
            intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
            intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyId);
            intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, patient);
            intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, familyId);
            intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, familyId);
            intent.putExtra(Constants.INTENT_KEY.VILLAGE_TOWN, address);
            intent.putExtra(DBConstants.KEY.UNIQUE_ID,houseHoldId);
            intent.putExtra(HnppConstants.KEY.HOUSE_HOLD_ID,moduleId);
            intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, houseHoldHead);
            startActivity(intent);
        }


    }


    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        ElcoMemberRegisterProvider childRegisterProvider = new ElcoMemberRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        CommonRepository commonRepository = context().commonrepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, commonRepository);
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
        clients_header_layout.setVisibility(android.view.View.GONE);
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
    public void filter(String filterString, String joinTableString, String mainConditionString, boolean qrCode) {
        this.joinTables = new String[]{CoreConstants.TABLE_NAME.FAMILY};
        searchFilterString = filterString;
        clientAdapter.setCurrentoffset(0);
        super.filter(filterString, joinTableString, mainConditionString, qrCode);

    }

    @Override
    public void filterandSortExecute() {
        super.filterandSortExecute();
    }


    @Override
    public void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
        if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_DOSAGE_STATUS && view.getTag() instanceof CommonPersonObjectClient) {
            CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
            String baseEntityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, true);
            if (StringUtils.isNotBlank(baseEntityId)) {
                CoreChildHomeVisitActivity.startMe(getActivity(), new MemberObject(client), false);
            }
        } else if (view.getId() == R.id.filter_text_view) {


            openFilterDialog(true);
        }
    }

    @Override
    public void countExecute() {
//        visitType = "and ec_visit_log.visit_type ='ELCO Registration'";
//        super.countExecute();
    }

    @Override
    protected String filterandSortQuery() {
        visitType = "and ec_visit_log.visit_type ='ELCO Registration'";
        return super.filterandSortQuery();
    }
    //    @Override
//    public void countExecute() {
//        StringBuilder customFilter = new StringBuilder();
//        String query = "Select count(*) FROM ec_family_member LEFT JOIN ec_family ON  ec_family_member.relational_id = ec_family.id COLLATE NOCASE inner join ec_visit_log on ec_family_member.base_entity_id = ec_visit_log.base_entity_id WHERE  ec_family_member.date_removed is null AND  ((( julianday('now') - julianday(dob))/365) >13) AND  ((( julianday('now') - julianday(dob))/365) <50)" +
//                " AND  marital_status = 'Married' and gender = 'F' AND ec_family_member.base_entity_id  NOT IN  (select ec_anc_register.base_entity_id from ec_anc_register " +
//                "where ec_anc_register.is_closed = '0' group by ec_anc_register.base_entity_id)  and ec_family_member.base_entity_id  " +
//                "NOT IN (select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0' group by ec_pregnancy_outcome.base_entity_id) "
//                ;
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
//
//            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
//            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
//            customFilter.append(" group by ec_family_member.base_entity_id");
//        }
//        if (StringUtils.isNotBlank(customFilter)) {
//            query = query + customFilter;
//        }
//        Cursor c = null;
//
//        try{
//            c = commonRepository().rawCustomQueryForAdapter(query);
//            c.moveToFirst();
//            clientAdapter.setTotalcount(c.getInt(0));
//            Timber.v("total count here %s", clientAdapter.getTotalcount());
//            Log.v("VIST_QUERY","count sql:"+query);
//        } catch (Exception e) {
//            Timber.e(e);
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//        }
//    }

    @Override
    protected boolean isValidFilterForFts(CommonRepository commonRepository) {
        return false;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        setTotalPatients();
    }

//    @Override
//    protected String filterandSortQuery() {
//        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
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
//        String query = "";
//        try {
//
//                sqb.addCondition(customFilter.toString());
//                query = sqb.orderbyCondition(Sortqueries);
//                query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));
//
//
//        } catch (Exception e) {
//            Log.v("TESTING",""+e);
//            Timber.e(e);
//        }
//
//        return query;
//    }
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
                    String query = filterandSortQuery();
                    Cursor cursor = commonRepository().rawCustomQueryForAdapter(query);
                    if(cursor !=null && clientAdapter !=null){
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
    @Override
    protected int getToolBarTitle() {
        return R.string.menu_elco_clients;
    }
}
