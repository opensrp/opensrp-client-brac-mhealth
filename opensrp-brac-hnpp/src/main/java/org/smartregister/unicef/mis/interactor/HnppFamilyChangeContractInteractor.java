package org.smartregister.unicef.mis.interactor;
import org.smartregister.chw.core.utils.Utils;

public class HnppFamilyChangeContractInteractor implements FamilyChangeContractInteractor.Flavor {
    @Override
    public String getFamilyMembersSql(String familyID) {
        return Utils.getFamilyMembersSql(familyID);
    }
}
