package org.smartregister.brac.hnpp.presenter;

import org.smartregister.family.contract.FamilyProfileMemberContract;
import org.smartregister.family.presenter.BaseFamilyProfileMemberPresenter;
import org.smartregister.family.util.DBConstants;

public class HnppBaseFamilyProfileMemberPresenter extends BaseFamilyProfileMemberPresenter {

    public HnppBaseFamilyProfileMemberPresenter(FamilyProfileMemberContract.View view, FamilyProfileMemberContract.Model model, String viewConfigurationIdentifier, String familyBaseEntityId, String familyHead, String primaryCaregiver) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId, familyHead, primaryCaregiver);
    }

    @Override
    public String getMainCondition() {
        return String.format(" %s = '%s' AND (%s IS NULL OR %s IS NOT NULL)", "relational_id", this.familyBaseEntityId, DBConstants.KEY.DATE_REMOVED, DBConstants.KEY.DOD);
    }
}
