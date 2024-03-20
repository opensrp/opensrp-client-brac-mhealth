package org.smartregister.unicef.mis.presenter;

import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.family.contract.FamilyProfileDueContract;
import org.smartregister.family.presenter.BaseFamilyProfileDuePresenter;

public class FamilyProfileDuePresenter extends BaseFamilyProfileDuePresenter {

    public FamilyProfileDuePresenter(FamilyProfileDueContract.View view, FamilyProfileDueContract.Model model, String viewConfigurationIdentifier, String familyBaseEntityId) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId);
    }

    @Override
    public String getMainCondition() {
        return String.format(" %s AND %s AND %s ", condition(), ChildDBConstants.childDueFilter(), ChildDBConstants.childAgeLimitFilter());
    }
    public String condition() {
        return String.format(" %s = '%s' and %s is null ", "relational_id", this.familyBaseEntityId, "date_removed");
    }


    @Override
    public String getDefaultSortQuery() {
        return ChildDBConstants.KEY.LAST_HOME_VISIT +  " ASC ";
    }


}