package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.anc.fragment.BaseAncRegisterFragment;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.pnc.activity.BasePncRegisterActivity;

import timber.log.Timber;

public class HnppPncRegisterActivity extends BasePncRegisterActivity {

    public static void startHnppPncRegisterActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name) {
        Intent intent = new Intent(activity, org.smartregister.brac.hnpp.activity.HnppPncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);

        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, getFormTable());
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }
    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {

    }
    public static String getFormTable() {

        return HnppConstants.TABLE_NAME.PNC_REGISTRATION;
    }
}
