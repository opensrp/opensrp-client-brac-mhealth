package org.smartregister.unicef.dghs.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.json.JSONObject;
import org.smartregister.unicef.dghs.fragment.IndividualProfileRemoveFragment;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.activity.SecuredActivity;

import timber.log.Timber;

public class IndividualProfileRemoveActivity extends SecuredActivity {

    protected IndividualProfileRemoveFragment individualProfileRemoveFragment;
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
    protected void onCreation() {
        setContentView(org.smartregister.chw.core.R.layout.activity_family_remove_member);
        findViewById(org.smartregister.chw.core.R.id.detail_toolbar).setVisibility(View.GONE);
        findViewById(org.smartregister.chw.core.R.id.close).setVisibility(View.GONE);
        findViewById(org.smartregister.chw.core.R.id.tvDetails).setVisibility(View.GONE);
        setRemoveMemberFragment();
        startFragment();
    }
    @Override
    protected void onResumption() {
        Timber.v("onResumption");
    }
    private void startFragment() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(org.smartregister.chw.core.R.id.flFrame, individualProfileRemoveFragment)
                .commit();
    }

    public void onRemoveMember() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }
    protected void setRemoveMemberFragment() {
        this.individualProfileRemoveFragment = IndividualProfileRemoveFragment.newInstance(getIntent().getExtras());
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);

                JSONObject form = new JSONObject(jsonString);
                individualProfileRemoveFragment.confirmRemove(form);
            } catch (Exception e) {
                Timber.e(e);
            }
        } else {
            finish();
        }
    }
}
