package org.smartregister.brac.hnpp.presenter;

import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

public class AdultRiskMemberRegisterFragmentPresenter extends AdultMemberRegisterFragmentPresenter {

    public AdultRiskMemberRegisterFragmentPresenter(CoreChildRegisterFragmentContract.View view, CoreChildRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }
    @Override
    public String getMainCondition() {
        return String.format(" %s is null AND %s", CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+ DBConstants.KEY.DATE_REMOVED, ChildDBConstants.riskAdultFilterWithTableName());
    }

    @Override
    public String getMainCondition(String tableName) {
        return String.format(" %s is null AND %s", tableName + "." + DBConstants.KEY.DATE_REMOVED, ChildDBConstants.riskAdultFilterWithTableName());
    }
}
