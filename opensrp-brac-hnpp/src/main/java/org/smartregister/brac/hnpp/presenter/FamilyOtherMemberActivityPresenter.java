package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.model.FamilyProfileModel;
import org.smartregister.chw.core.contract.FamilyOtherMemberProfileExtendedContract;
import org.smartregister.chw.core.interactor.CoreFamilyProfileInteractor;
import org.smartregister.chw.core.presenter.CoreFamilyOtherMemberActivityPresenter;
import org.smartregister.brac.hnpp.interactor.HnppFamilyInteractor;
import org.smartregister.brac.hnpp.interactor.HnppFamilyProfileInteractor;
import org.smartregister.family.contract.FamilyOtherMemberContract;
import org.smartregister.family.contract.FamilyProfileContract;

public class FamilyOtherMemberActivityPresenter extends CoreFamilyOtherMemberActivityPresenter {

    public FamilyOtherMemberActivityPresenter(FamilyOtherMemberProfileExtendedContract.View view,
                                              FamilyOtherMemberContract.Model model, String viewConfigurationIdentifier,
                                              String familyBaseEntityId, String baseEntityId, String familyHead,
                                              String primaryCaregiver, String villageTown, String familyName) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId, baseEntityId, familyHead, primaryCaregiver, villageTown, familyName);
    }

    @Override
    protected CoreFamilyProfileInteractor getFamilyProfileInteractor() {
        if (profileInteractor == null) {
            this.profileInteractor = new HnppFamilyProfileInteractor();
        }
        return (CoreFamilyProfileInteractor) profileInteractor;
    }

    @Override
    protected FamilyProfileContract.Model getFamilyProfileModel(String familyName) {
        if (profileModel == null) {
            this.profileModel = new FamilyProfileModel(familyName);
        }
        return profileModel;
    }

    @Override
    protected void setProfileInteractor() {
        if (familyInteractor == null) {
            familyInteractor = new HnppFamilyInteractor();
        }
    }
}
