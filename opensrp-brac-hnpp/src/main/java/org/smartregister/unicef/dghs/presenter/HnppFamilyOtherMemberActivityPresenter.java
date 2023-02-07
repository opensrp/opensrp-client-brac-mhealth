package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.model.HnppFamilyProfileModel;
import org.smartregister.chw.core.contract.FamilyOtherMemberProfileExtendedContract;
import org.smartregister.chw.core.interactor.CoreFamilyProfileInteractor;
import org.smartregister.chw.core.presenter.CoreFamilyOtherMemberActivityPresenter;
import org.smartregister.unicef.dghs.interactor.HnppFamilyInteractor;
import org.smartregister.unicef.dghs.interactor.HnppFamilyProfileInteractor;
import org.smartregister.family.contract.FamilyOtherMemberContract;
import org.smartregister.family.contract.FamilyProfileContract;

public class HnppFamilyOtherMemberActivityPresenter extends CoreFamilyOtherMemberActivityPresenter {


    public HnppFamilyOtherMemberActivityPresenter(FamilyOtherMemberProfileExtendedContract.View view,
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
            this.profileModel = new HnppFamilyProfileModel(familyName,null,null,null);
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
