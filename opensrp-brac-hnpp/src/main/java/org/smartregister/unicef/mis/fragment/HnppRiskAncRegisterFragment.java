package org.smartregister.unicef.mis.fragment;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.model.AncRegisterFragmentModel;
import org.smartregister.unicef.mis.presenter.HnppRiskAncRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;

public class HnppRiskAncRegisterFragment extends HnppAncRegisterFragment {
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new HnppRiskAncRegisterFragmentPresenter(this, new AncRegisterFragmentModel(), null);

    }
    protected int getToolBarTitle() {
        return R.string.menu_anc_risk_clients;
    }

    @Override
    protected String getCondition() {
        return super.getCondition()+" AND "+ CoreConstants.TABLE_NAME.FAMILY_MEMBER + ".is_risk = 'true'";
    }
}
