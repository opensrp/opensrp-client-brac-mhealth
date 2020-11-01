package org.smartregister.brac.hnpp.fragment;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.AncRegisterFragmentModel;
import org.smartregister.brac.hnpp.presenter.HnppRiskAncRegisterFragmentPresenter;
import org.smartregister.brac.hnpp.presenter.HnppRiskPncRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;

public class HnppRiskPncRegisterFragment extends HnppPncRegisterFragment {

    protected int getToolBarTitle() {
        return R.string.menu_anc_risk_clients;
    }

    @Override
    protected String getCondition() {
        return super.getCondition()+" AND "+ CoreConstants.TABLE_NAME.FAMILY_MEMBER + ".is_risk = 'true'";
    }
}
