package org.smartregister.brac.hnpp.presenter;

import org.smartregister.chw.anc.contract.BaseAncRegisterFragmentContract;
import org.smartregister.chw.core.presenter.AncRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;

public class HnppRiskPncRegisterFragmentPresenter extends AncRegisterFragmentPresenter {
    public HnppRiskPncRegisterFragmentPresenter(BaseAncRegisterFragmentContract.View view, BaseAncRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return super.getMainCondition()+" AND "+ CoreConstants.TABLE_NAME.FAMILY_MEMBER + ".is_risk = 'true'";
    }
}
