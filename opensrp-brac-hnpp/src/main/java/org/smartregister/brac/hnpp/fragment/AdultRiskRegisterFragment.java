package org.smartregister.brac.hnpp.fragment;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.HnppAllMemberRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.AdultRiskMemberRegisterFragmentPresenter;
import org.smartregister.view.activity.BaseRegisterActivity;

public class AdultRiskRegisterFragment extends AdultMemberRegisterFragment {

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new AdultRiskMemberRegisterFragmentPresenter(this, new HnppAllMemberRegisterFragmentModel(), viewConfigurationIdentifier);

    }
    @Override
    protected int getToolBarTitle() {
        return R.string.menu_anc_risk_clients;
    }
}
