package org.smartregister.unicef.mis.presenter;

import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.family.contract.FamilyProfileDueContract;
import org.smartregister.family.presenter.BaseFamilyProfileDuePresenter;

public class HnppChildProfileDuePresenter extends BaseFamilyProfileDuePresenter {

    public HnppChildProfileDuePresenter(FamilyProfileDueContract.View view, FamilyProfileDueContract.Model model, String viewConfigurationIdentifier, String familyBaseEntityId) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId);
    }

    @Override
    public String getMainCondition() {
        return "";// String.format("AND %s AND %s ", super.getMainCondition(), ChildDBConstants.childDueFilter(), ChildDBConstants.childAgeLimitFilter());
    }

    @Override
    public String getDefaultSortQuery() {
        return ChildDBConstants.KEY.LAST_HOME_VISIT +  " ASC ";
    }
}