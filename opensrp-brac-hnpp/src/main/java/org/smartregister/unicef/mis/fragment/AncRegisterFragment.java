package org.smartregister.unicef.mis.fragment;

import android.database.Cursor;
import android.support.v7.widget.Toolbar;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.fragment.BaseAncRegisterFragment;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.mis.model.AncRegisterFragmentModel;
import org.smartregister.chw.core.presenter.AncRegisterFragmentPresenter;
import org.smartregister.unicef.mis.nativation.view.NavigationMenu;
import org.smartregister.unicef.mis.provider.HnppAncRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import java.util.Set;

import timber.log.Timber;

public class AncRegisterFragment extends BaseAncRegisterFragment {
    private android.view.View view;
    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HnppAncRegisterProvider provider = new HnppAncRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
        this.view = view;
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
    @Override
    protected void refreshSyncProgressSpinner() {
        if (syncProgressBar != null) {
            syncProgressBar.setVisibility(android.view.View.GONE);
        }
        if (syncButton != null) {
            syncButton.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public void countExecute() {

        Cursor c = null;
        try {

            String query = "select count(*) from " + presenter().getMainTable() + " inner join " + CoreConstants.TABLE_NAME.FAMILY_MEMBER +
                    " on " + presenter().getMainTable() + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " +
                    CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID +
                    " where " + presenter().getMainCondition();

            if (StringUtils.isNotBlank(filters)) {
                query = query + " and ( " + filters + " ) ";
            }

            c = commonRepository().rawCustomQueryForAdapter(query);
            c.moveToFirst();
            clientAdapter.setTotalcount(c.getInt(0));
            Timber.v("total count here %d", clientAdapter.getTotalcount());

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

//    @Override
//    public void setupViews(android.view.View view) {
//        super.setupViews(view);
//        this.view = view;
//
//        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
//        toolbar.setContentInsetsAbsolute(0, 0);
//        toolbar.setContentInsetsRelative(0, 0);
//        toolbar.setContentInsetStartWithNavigation(0);
//        toolbar.setContentInsetStartWithNavigation(0);
//
//        android.view.View navbarContainer = view.findViewById(org.smartregister.chw.core.R.id.register_nav_bar_container);
//        navbarContainer.setFocusable(false);
//
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        android.view.View searchBarLayout = view.findViewById(org.smartregister.chw.core.R.id.search_bar_layout);
//        searchBarLayout.setLayoutParams(params);
//        searchBarLayout.setBackgroundResource(org.smartregister.chw.core.R.color.chw_primary);
//        searchBarLayout.setPadding(searchBarLayout.getPaddingLeft(), searchBarLayout.getPaddingTop(), searchBarLayout.getPaddingRight(), (int) Utils.convertDpToPixel(10, getActivity()));
//
//        CustomFontTextView titleView = view.findViewById(org.smartregister.chw.core.R.id.txt_title_label);
//        if (titleView != null) {
//            titleView.setPadding(0, titleView.getTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
//        }
//
//        android.view.View topLeftLayout = view.findViewById(org.smartregister.chw.core.R.id.top_left_layout);
//        topLeftLayout.setVisibility(android.view.View.GONE);
//
//        android.view.View topRightLayout = view.findViewById(org.smartregister.chw.core.R.id.top_right_layout);
//        topRightLayout.setVisibility(android.view.View.VISIBLE);
//
//        android.view.View sortFilterBarLayout = view.findViewById(org.smartregister.chw.core.R.id.register_sort_filter_bar_layout);
//        sortFilterBarLayout.setVisibility(android.view.View.GONE);
//
//        android.view.View filterSortLayout = view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout);
//        filterSortLayout.setVisibility(android.view.View.GONE);
//
//        android.view.View dueOnlyLayout = view.findViewById(org.smartregister.chw.core.R.id.due_only_layout);
//        dueOnlyLayout.setVisibility(android.view.View.VISIBLE);
//        dueOnlyLayout.setOnClickListener(registerActionHandler);
//
//        if (getSearchView() != null) {
//            getSearchView().setBackgroundResource(org.smartregister.chw.core.R.color.white);
//            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.chw.core.R.drawable.ic_action_search, 0, 0, 0);
//            getSearchView().setTextColor(getResources().getColor(org.smartregister.chw.core.R.color.text_black));
//        }
//
//        NavigationMenu.getInstance(getActivity(), null, toolbar);
//    }
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new AncRegisterFragmentPresenter(this, new AncRegisterFragmentModel(), null);
    }

    @Override
    protected String getMainCondition() {
        return super.getMainCondition();
    }

    @Override
    protected void openProfile(CommonPersonObjectClient client) {

    }

    @Override
    protected void openHomeVisit(CommonPersonObjectClient client) {
        //Not needed on HF
    }
}
