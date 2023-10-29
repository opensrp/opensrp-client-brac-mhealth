package org.smartregister.unicef.mis.presenter;

import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.chw.anc.contract.BaseAncRegisterFragmentContract;
import org.smartregister.chw.core.presenter.AncRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;

public class HnppRiskAncRegisterFragmentPresenter extends AncRegisterFragmentPresenter {
    public HnppRiskAncRegisterFragmentPresenter(BaseAncRegisterFragmentContract.View view, BaseAncRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return super.getMainCondition()+" AND "+ CoreConstants.TABLE_NAME.FAMILY_MEMBER + ".is_risk = 'true' and "+CoreConstants.TABLE_NAME.FAMILY_MEMBER +".risk_event_type ='"+ HnppConstants.EVENT_TYPE.ANC_HOME_VISIT +"'";
    }
}
