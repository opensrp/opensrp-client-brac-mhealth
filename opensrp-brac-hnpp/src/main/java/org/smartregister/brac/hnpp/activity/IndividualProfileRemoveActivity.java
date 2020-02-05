package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.brac.hnpp.fragment.IndividualProfileRemoveFragment;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.activity.CoreIndividualProfileRemoveActivity;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public class IndividualProfileRemoveActivity extends CoreIndividualProfileRemoveActivity {

    public static void startIndividualProfileActivity(Activity activity, CommonPersonObjectClient commonPersonObjectClient, String familyBaseEntityId, String familyHead, String primaryCareGiver, String viewRegisterClass) {
        Intent intent = new Intent(activity, IndividualProfileRemoveActivity.class);
        intent.putExtra(HnppConstants.INTENT_KEY.CHILD_COMMON_PERSON, commonPersonObjectClient);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyBaseEntityId);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, "");
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, "");
        intent.putExtra(HnppConstants.INTENT_KEY.VIEW_REGISTER_CLASS, viewRegisterClass);
        activity.startActivityForResult(intent, HnppConstants.ProfileActivityResults.CHANGE_COMPLETED);
    }

    @Override
    protected void setRemoveMemberFragment() {
        this.individualProfileRemoveFragment = IndividualProfileRemoveFragment.newInstance(getIntent().getExtras());
    }
}
