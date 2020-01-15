package org.smartregister.brac.hnpp.activity;

import org.smartregister.brac.hnpp.fragment.HnppAncJsonFormFragment;
import org.smartregister.brac.hnpp.fragment.HnppFormViewFragment;
import org.smartregister.family.activity.FamilyWizardFormActivity;

public class HnppAncJsonFormActivity extends FamilyWizardFormActivity {
    @Override
    public void initializeFormFragment() {
        HnppAncJsonFormFragment jsonWizardFormFragment = HnppAncJsonFormFragment.getFormFragment("step1");
        this.getSupportFragmentManager().beginTransaction().add(com.vijay.jsonwizard.R.id.container, jsonWizardFormFragment).commit();
    }


}
