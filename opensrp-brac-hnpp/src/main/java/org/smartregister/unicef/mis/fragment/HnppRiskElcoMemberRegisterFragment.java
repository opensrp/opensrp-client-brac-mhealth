package org.smartregister.unicef.mis.fragment;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.model.HnppElcoMemberRegisterFragmentModel;
import org.smartregister.unicef.mis.presenter.HnppRiskElcoMemberRegisterFragmentPresenter;
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
