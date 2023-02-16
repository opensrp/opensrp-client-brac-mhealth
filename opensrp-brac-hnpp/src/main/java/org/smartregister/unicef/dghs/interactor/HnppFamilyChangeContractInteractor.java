package org.smartregister.unicef.dghs.interactor;
import org.smartregister.chw.core.utils.Utils;

public class HnppFamilyChangeContractInteractor implements FamilyChangeContractInteractor.Flavor {
    @Override
    public String getFamilyMembersSql(String familyID) {
        return Utils.getFamilyMembersSql(familyID);
    }
}
