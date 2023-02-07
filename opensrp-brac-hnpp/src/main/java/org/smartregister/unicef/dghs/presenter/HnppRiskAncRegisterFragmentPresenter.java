package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.utils.HnppDBConstants;
import org.smartregister.chw.anc.contract.BaseAncRegisterFragmentContract;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.presenter.AncRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;

public class HnppRiskAncRegisterFragmentPresenter extends AncRegisterFragmentPresenter {
    public HnppRiskAncRegisterFragmentPresenter(BaseAncRegisterFragmentContract.View view, BaseAncRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }
//
//    @Override
//    public String getMainCondition() {
//        return super.getMainCondition()+" AND "+ CoreConstants.TABLE_NAME.FAMILY_MEMBER + ".is_risk = 'true'";
//    }
}
