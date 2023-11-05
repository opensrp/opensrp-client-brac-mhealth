package org.smartregister.brac.hnpp.activity;


import org.smartregister.brac.hnpp.fragment.AdultMemberRegisterFragment;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class AdultMemberRegisterActivity extends HnppAllMemberRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AdultMemberRegisterFragment();
    }
}
