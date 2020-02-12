package org.smartregister.brac.hnpp.activity;

import org.smartregister.brac.hnpp.fragment.HNPPJsonFormFragment;
import org.smartregister.brac.hnpp.fragment.HnppFormViewFragment;
import org.smartregister.family.activity.FamilyWizardFormActivity;

public class HnppFormViewActivity extends FamilyWizardFormActivity {

    @Override
    public void initializeFormFragment() {
        HnppFormViewFragment jsonWizardFormFragment = HnppFormViewFragment.getFormFragment("step1");
        this.getSupportFragmentManager().beginTransaction().add(com.vijay.jsonwizard.R.id.container, jsonWizardFormFragment).commit();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
