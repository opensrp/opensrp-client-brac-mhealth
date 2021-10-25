package org.smartregister.brac.hnpp.fragment;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.HnppAllMemberRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.AdoMemberRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.presenter.AdultMemberRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.provider.HnppAdoMemberRegisterProvider;
import org.smartregister.brac.hnpp.provider.HnppAllMemberRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.util.Set;

public class AdolescentMemberRegisterFragment extends HnppAllMemberRegisterFragment {
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new AdoMemberRegisterFragmentPresenter(this, new HnppAllMemberRegisterFragmentModel(), viewConfigurationIdentifier);
        isNeedToShowDateFilter = true;
    }
    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HnppAdoMemberRegisterProvider childRegisterProvider = new HnppAdoMemberRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
    @Override
    public void countExecute() {
//        visitType = "and ec_visit_log.visit_type ='Adolescent package'";
//        super.countExecute();
    }

    @Override
    protected String filterandSortQuery() {
        visitType = "and ec_visit_log.visit_type ='Adolescent package'";
        return super.filterandSortQuery();
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.menu_ado_clients;
    }
}
