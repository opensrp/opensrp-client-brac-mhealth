package org.smartregister.unicef.dghs.fragment;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.model.HnppChildRegisterFragmentModel;
import org.smartregister.unicef.dghs.presenter.HnppRiskChildRegisterFragmentPresenter;
import org.smartregister.unicef.dghs.presenter.IYCFRegisterFragmentPresenter;
import org.smartregister.unicef.dghs.provider.HnppChildRegisterProvider;
import org.smartregister.unicef.dghs.provider.IYCFRegisterProvider;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.util.Set;

public class IYCFRegisterFragment extends HnppChildRegisterFragment {
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new IYCFRegisterFragmentPresenter(this, new HnppChildRegisterFragmentModel(), viewConfigurationIdentifier);
        isNeedToShowDateFilter = true;
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        IYCFRegisterProvider childRegisterProvider = new IYCFRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
    @Override
    public void countExecute() {
//        visitType = "and ec_visit_log.visit_type ='IYCF package'";
//        super.countExecute();
    }

    @Override
    protected String filterandSortQuery() {
        visitType = "and ec_visit_log.visit_type ='IYCF package'";
        return super.filterandSortQuery();
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.menu_iycf_clients;
    }
}
