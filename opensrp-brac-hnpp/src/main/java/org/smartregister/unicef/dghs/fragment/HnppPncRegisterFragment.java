package org.smartregister.unicef.dghs.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.dghs.model.HnppPncRegisterFragmentModel;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
import org.smartregister.unicef.dghs.provider.HnppPncRegisterProvider;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.anc.util.DBConstants;

import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.pnc.presenter.BasePncRegisterFragmentPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.Constants;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Set;
import timber.log.Timber;

public class HnppPncRegisterFragment extends HnppBasePncRegisterFragment implements View.OnClickListener {

    private static final String DUE_FILTER_TAG = "PRESSED";
    private View view;
    private boolean dueFilterActive = false;

    @Override
    public void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
      if (view.getId() == R.id.filter_text_view) {

          openFilterDialog(true);
        }
      if(view.getId() == R.id.add_child_button){
          CommonPersonObjectClient pc = (CommonPersonObjectClient) view.getTag();
          Toast.makeText(getActivity(),"Open child register",Toast.LENGTH_LONG).show();
      }
      if(view.getId() == R.id.due_button){
          String mobileNo = (String) view.getTag();
          Intent intent = new Intent(Intent.ACTION_DIAL);
          intent.setData(Uri.parse("tel:" + mobileNo));
          startActivity(intent);
      }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onResumption() {
        if(view!=null)
        NavigationMenu.getInstance(getActivity(), null, view.findViewById(org.smartregister.R.id.register_toolbar));
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
            getSearchView().setHint(getString(R.string.search_name_or_id));
            getSearchView().setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
        }
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

    private void dueFilter(View dueOnlyLayout) {
        filter(searchText(), "", getDueCondition());
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

    private String getDueCondition() {
        return " ifnull(substr(delivery_date, 7, 4)||'-'||substr(delivery_date, 4, 2)||'-'||substr(delivery_date, 1, 2), '+2 day') < STRFTIME('%Y%m%d', datetime('now')) " +
                " and julianday() - julianday(substr(delivery_date, 7, 4)||'-'||substr(delivery_date, 4, 2)||'-'||substr(delivery_date, 1, 2)) >= 2   ";
    }

    private void switchViews(View dueOnlyLayout, boolean isPress) {
        TextView dueOnlyTextView = dueOnlyLayout.findViewById(R.id.due_only_text_view);
        if (isPress) {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_due_filter_on, 0);
        } else {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_due_filter_off, 0);
        }
    }

    protected String getCondition() {
        return "";
//        String q = " " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DATE_REMOVED + " is null " +
//                "AND " + HnppConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME + "." + DBConstants.KEY.IS_CLOSED + " is 0 " +
//                "AND " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.IS_CLOSED + " = '0' ";
//        return q;
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        HnppPncRegisterProvider provider = new HnppPncRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }


    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new BasePncRegisterFragmentPresenter(this, new HnppPncRegisterFragmentModel(), null);
    }
    @Override
    public void filter(String filterString, String joinTableString, String mainConditionString, boolean qrCode) {
        this.joinTables = new String[]{CoreConstants.TABLE_NAME.FAMILY};
        searchFilterString = filterString;

        clientAdapter.setCurrentoffset(0);
        super.filter(filterString, joinTableString, mainConditionString, qrCode);

    }
    @Override
    protected void openProfile(CommonPersonObjectClient client) {

        HashMap<String, String> detailsMap = HnppApplication.ancRegisterRepository().getFamilyNameAndPhone(org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.FAMILY_HEAD, false));

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
    protected void openHomeVisit(CommonPersonObjectClient client) {
//        PncHomeVisitActivity.startMe(getActivity(), new MemberObject(client), false);
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
        Log.v("VIST_QUERY","defaultFilterAndSortQuery>>"+mainSelect);
        String sql = "";
        if(month != -1){
            sql = mainSelect+" inner join ec_visit_log on ec_visit_log.base_entity_id = ec_family_member.base_entity_id ";
        }else{
            sql = mainSelect;
        }
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(sql);
        joinTables = new String[]{"ec_family"};
        StringBuilder customFilter = new StringBuilder();
        String query = sql;//+" where " + getCondition();
        Log.v("VIST_QUERY","query>>"+query);


        if (StringUtils.isNotBlank(searchFilterString)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.LAST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.MIDDLE_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.PHONE_NUMBER, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));

        }
        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));

        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
        }if(month!=-1){

            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
            customFilter.append(" and ec_visit_log.visit_type ='PNC Home Visit'");
            customFilter.append(" group by ec_family_member.base_entity_id");
        }

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

            //}
        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

    @Override
    public void countExecute() {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        setTotalPatients();
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
                    Log.v("PNCTEST","query>>"+query);

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
    protected int getToolBarTitle() {
        return R.string.menu_pnc_clients;
    }


}
