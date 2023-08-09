package org.smartregister.unicef.dghs.activity;

import org.smartregister.unicef.dghs.fragment.HNPPJsonFormFragment;
import org.smartregister.unicef.dghs.fragment.HnppFormViewFragment;
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