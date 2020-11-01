package org.smartregister.brac.hnpp.fragment;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.HnppElcoMemberRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.HnppElcoMemberRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.presenter.HnppRiskElcoMemberRegisterFragmentPresenter;
import org.smartregister.view.activity.BaseRegisterActivity;

public class HnppRiskElcoMemberRegisterFragment extends HnppElcoMemberRegisterFragment {
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new HnppRiskElcoMemberRegisterFragmentPresenter(this, new HnppElcoMemberRegisterFragmentModel(), viewConfigurationIdentifier);

    }
    @Override
    protected int getToolBarTitle() {
        return R.string.menu_anc_risk_clients;
    }
}
