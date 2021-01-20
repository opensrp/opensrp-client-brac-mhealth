package org.smartregister.brac.hnpp.fragment;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.HnppChildRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.HnppRiskChildRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.presenter.IYCFRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.provider.HnppChildRegisterProvider;
import org.smartregister.brac.hnpp.provider.IYCFRegisterProvider;
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

    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        IYCFRegisterProvider childRegisterProvider = new IYCFRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.menu_iycf_clients;
    }
}
