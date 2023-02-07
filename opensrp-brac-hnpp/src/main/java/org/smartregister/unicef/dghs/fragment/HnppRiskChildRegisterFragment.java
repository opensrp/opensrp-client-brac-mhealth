package org.smartregister.unicef.dghs.fragment;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.model.HnppChildRegisterFragmentModel;
import org.smartregister.unicef.dghs.presenter.HnppChildRegisterFragmentPresenter;
import org.smartregister.unicef.dghs.presenter.HnppRiskChildRegisterFragmentPresenter;
import org.smartregister.view.activity.BaseRegisterActivity;

public class HnppRiskChildRegisterFragment extends HnppChildRegisterFragment {
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new HnppRiskChildRegisterFragmentPresenter(this, new HnppChildRegisterFragmentModel(), viewConfigurationIdentifier);

    }
    @Override
    protected int getToolBarTitle() {
        return R.string.menu_anc_risk_clients;
    }
}
