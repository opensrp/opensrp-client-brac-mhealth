package org.smartregister.unicef.mis.activity;


import org.smartregister.unicef.mis.fragment.AdultMemberRegisterFragment;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class AdultMemberRegisterActivity extends HnppAllMemberRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AdultMemberRegisterFragment();
    }
}
