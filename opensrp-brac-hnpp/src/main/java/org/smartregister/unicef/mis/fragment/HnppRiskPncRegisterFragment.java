package org.smartregister.unicef.mis.fragment;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.model.HnppPncRiskRegisterFragmentModel;
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
