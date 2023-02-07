package org.smartregister.unicef.dghs.fragment;

import org.smartregister.unicef.dghs.model.AncRegisterFragmentModel;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.fragment.CoreAncRegisterFragment;
import org.smartregister.chw.core.presenter.AncRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.unicef.dghs.provider.HnppAncRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import java.util.HashMap;
import java.util.Set;

public class AncRegisterFragment extends CoreAncRegisterFragment {

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HnppAncRegisterProvider provider = new HnppAncRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new AncRegisterFragmentPresenter(this, new AncRegisterFragmentModel(), null);
    }

    @Override
    protected void openProfile(CommonPersonObjectClient client) {

    }

    @Override
    protected void openHomeVisit(CommonPersonObjectClient client) {
        //Not needed on HF
    }
}
