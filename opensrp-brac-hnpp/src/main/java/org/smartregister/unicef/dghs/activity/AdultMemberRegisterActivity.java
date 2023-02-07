package org.smartregister.unicef.dghs.activity;


import org.smartregister.unicef.dghs.fragment.AdultMemberRegisterFragment;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class AdultMemberRegisterActivity extends HnppAllMemberRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AdultMemberRegisterFragment();
    }
}
