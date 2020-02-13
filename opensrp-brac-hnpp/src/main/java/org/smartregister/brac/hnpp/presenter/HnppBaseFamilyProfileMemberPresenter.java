package org.smartregister.brac.hnpp.presenter;

import org.smartregister.family.contract.FamilyProfileMemberContract;
import org.smartregister.family.presenter.BaseFamilyProfileMemberPresenter;

public class HnppBaseFamilyProfileMemberPresenter extends BaseFamilyProfileMemberPresenter {

    public HnppBaseFamilyProfileMemberPresenter(FamilyProfileMemberContract.View view, FamilyProfileMemberContract.Model model, String viewConfigurationIdentifier, String familyBaseEntityId, String familyHead, String primaryCaregiver) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId, familyHead, primaryCaregiver);
    }

    @Override
    public String getMainCondition() {
        return String.format(" %s = '%s' ", "object_relational_id", this.familyBaseEntityId);
    }
}
