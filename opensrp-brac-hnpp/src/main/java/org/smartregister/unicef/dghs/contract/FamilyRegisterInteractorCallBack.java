package org.smartregister.unicef.dghs.contract;

import org.smartregister.family.contract.FamilyRegisterContract;

public interface FamilyRegisterInteractorCallBack extends FamilyRegisterContract.InteractorCallBack {
    void onRegistrationSaved(boolean isEditMode,String baseEntityId);
}
