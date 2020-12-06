package org.smartregister.brac.hnpp.activity;

import org.smartregister.brac.hnpp.fragment.AdultRiskRegisterFragment;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class AdultRiskRegisterActivity extends AdultMemberRegisterActivity {
    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AdultRiskRegisterFragment();
    }
}
