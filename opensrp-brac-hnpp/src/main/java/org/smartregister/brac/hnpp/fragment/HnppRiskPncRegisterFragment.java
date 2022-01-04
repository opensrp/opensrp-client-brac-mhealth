package org.smartregister.brac.hnpp.fragment;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.HnppPncRiskRegisterFragmentModel;
import org.smartregister.chw.pnc.presenter.BasePncRegisterFragmentPresenter;

public class HnppRiskPncRegisterFragment extends HnppPncRegisterFragment {
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new BasePncRegisterFragmentPresenter(this, new HnppPncRiskRegisterFragmentModel(), null);
    }

    protected int getToolBarTitle() {
        return R.string.menu_anc_risk_clients;
    }

}
